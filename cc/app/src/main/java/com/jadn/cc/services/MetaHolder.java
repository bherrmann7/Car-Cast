package com.jadn.cc.services;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;

import android.content.Context;
import android.util.Log;

import com.jadn.cc.core.Config;

/** Meta information about podcasts. **/
public class MetaHolder {
    private Context context;
    private Config config;

	private List<MetaFile> metas = new ArrayList<MetaFile>();

	public MetaHolder(Context context) {
        	this(context, null);
	}

	public MetaHolder(Context context, File current) {
        	this.context = context;
        	this.config = new Config(context);
		loadMeta(current);
	}

	public void delete(int i) {
		metas.get(i).delete();
		metas.remove(i);
	}

	public MetaFile extract(int i) {
		return metas.remove(i);
	}

	public MetaFile get(int current) {
		return metas.get(current);
	}

	public int getSize() {
		return metas.size();
	}

	/* Really a part of the constructor -- assumes "metas" is empty */
	private void loadMeta(File current) {
	String currentName = current == null ? null : current.getName();
        int currentIndex = -1;
        boolean priorityFileAddedToOrder = false;
	File[] files = config.getPodcastsRoot().listFiles();
        File order = config.getPodcastRootPath("podcast-order.txt");

        if (files == null)
			return;

		// Load files in proper order
		if (order.exists()) {
			try {
				DataInputStream dis = new DataInputStream(new FileInputStream(order));
				String line = null;
				while ((line = dis.readLine()) != null) {
					File file = config.getPodcastRootPath(line);
					if (file.exists()) {
						metas.add(new MetaFile(file));
                                                if ( currentName != null && currentName.equals(file.getName()) )
                                                {
                                                   currentIndex = metas.size() - 1;
	                                           Log.d("CarCast", "currentIndex: " + currentIndex);
                                                }
					}
				}
			} catch (IOException e) {
				Log.e("CarCast", "reading order file", e);
			}
		}

                if ( 0 <= currentIndex && currentIndex < metas.size() )
                {
                   // We've already encountered the currently-playing file;
                   // currentIndex is its position.
                   // assert 0 <= currentIndex;
                   // assert currentIndex < metas.size();
                   String prev, curr;
                   do
                   {
                      currentIndex += 1;
                      prev = metas.get(currentIndex - 1).getBaseFilename();
                      curr = metas.get(currentIndex).getBaseFilename();
                   } while ( currentIndex < metas.size() && curr.equals(prev) );
                   // currentIndex is now the index *after* the
                   // currently-playing podcast file and any following priority
                   // podcasts.
                   // assert 1 <= currentIndex;
                   // assert currentIndex <= metas.size();
                }

                // Fail safe.
                // The code above looks good to me.  I've tested it in various ways.
                // However, if there are any errors, it could result in incorrect indexing
                // into the `metas` array, which would crash the app.
                // So, just to be safe for the time being, let's leave some code here that
                // catches any problems.
                if ( currentIndex < 1 || metas.size() < currentIndex )
                   // SHOULD NOT HAPPEN!
                   currentIndex = -1;

		// Look for "Found Files" -- not in ordered list... but sitting in the directory
		ArrayList<File> foundFiles = new ArrayList<File>();
		for (File file : files) {
			if (file.length() == 0) {
				file.delete();
				continue;
			}
			if (file.getName().endsWith(".mp3") || file.getName().endsWith(".3gp") || file.getName().endsWith(".ogg")) {
				if (!alreadyHas(file)) {
                                        if ( 0 <= currentIndex && isPriority(file) )
                                        {
                                           // The currently-playing podcast is in "podcast-order.txt", so insert the new file
                                           // immediately after it.
	                                   Log.d("CarCast", "adding: " + currentIndex + " " + file.getName());
                                           metas.add(currentIndex++, new MetaFile(file));
                                           priorityFileAddedToOrder = true;
                                        }
                                        else
                                           // Just append the new file.  If this is from a priority podcast, it'll sort
                                           // into the right place anyway.
                                           foundFiles.add(file);
				}
			}
		}
		// Order the found files by file name.
		Collections.sort(foundFiles, new Comparator<File>() {
			@Override
			public int compare(File object1, File object2) {
				return object1.getName().compareTo(object2.getName());
			}
		});
		Log.i("carcast", "loadMeta found:"+foundFiles.size()+" meta:"+metas.size());
		for (File file : foundFiles) {
                     metas.add(new MetaFile(file));
		}

                // We need to save the order if any priority files have been inserted into the ordered
                // part of the playlist.  If we don't, then those priority files may appear to jump around
                // in the playlist. For example, if we select a new file for playback and then stop and restart
                // the app, then the priority files will jump to after the newly-playing file.
                if ( priorityFileAddedToOrder )
                   saveOrder();
	}

	boolean alreadyHas(File file) {
		for (MetaFile metaFile : metas) {
			if (metaFile.getFilename().equals(file.getName())) {
				return true;
			}
		}
		return false;
	}

	public SortedSet<Integer> moveTop(SortedSet<Integer> checkedItems) {
		List<MetaFile> tops = new ArrayList<MetaFile>();
		Integer[] ciArry = checkedItems.toArray(new Integer[0]);
		for (int i = checkedItems.size() - 1; i >= 0; i--) {
			tops.add(0, metas.get(ciArry[i]));
			metas.remove(metas.get(ciArry[i]));
		}
		for (MetaFile metaFile : tops) {
			metas.add(0, metaFile);
		}
		checkedItems.clear();
		for (MetaFile atop : tops) {
			checkedItems.add(metas.indexOf(atop));
		}
		saveOrder();
		return checkedItems;
	}

	public SortedSet<Integer> moveUp(SortedSet<Integer> checkedItems) {
		for (int i = 0; i < metas.size(); i++) {
			if (checkedItems.contains(i)) {
				if (!checkedItems.contains(i - 1)) {
					swapBack(checkedItems, i);
				}
			}
		}
		saveOrder();
		return checkedItems;
	}

	private void swapBack(SortedSet<Integer> checkedItems, int i) {
		if (i == 0)
			return;
		checkedItems.remove(i);
		checkedItems.add(i - 1);
		MetaFile o = metas.remove(i);
		metas.add(i - 1, o);
	}

	public SortedSet<Integer> moveBottom(SortedSet<Integer> checkedItems) {
		List<MetaFile> bottoms = new ArrayList<MetaFile>();
		Integer[] ciArry = checkedItems.toArray(new Integer[0]);
		for (int i = checkedItems.size() - 1; i >= 0; i--) {
			bottoms.add(0, metas.get(ciArry[i]));
			metas.remove(metas.get(ciArry[i]));
		}
		for (MetaFile metaFile : bottoms) {
			metas.add(metaFile);
		}
		checkedItems.clear();
		for (MetaFile atop : bottoms) {
			checkedItems.add(metas.indexOf(atop));
		}
		saveOrder();
		return checkedItems;
	}

	public SortedSet<Integer> moveDown(SortedSet<Integer> checkedItems) {
		for (int i = metas.size() - 2; i >= 0; i--) {
			if (checkedItems.contains(i)) {
				if (!checkedItems.contains(i + 1)) {
					swapForward(checkedItems, i);
				}
			}
		}
		saveOrder();
		return checkedItems;
	}

	private void swapForward(SortedSet<Integer> checkedItems, int i) {
		checkedItems.remove(i);
		checkedItems.add(i + 1);
		MetaFile o = metas.remove(i);
		metas.add(i + 1, o);
	}

	public void saveOrder() {
        File order = config.getPodcastRootPath("podcast-order.txt");
        StringBuilder sb = new StringBuilder();
		for (MetaFile metaFile : metas) {
			sb.append(metaFile.getFilename());
			sb.append('\n');
		}
		try {
			FileOutputStream fos = new FileOutputStream(order);
			fos.write(sb.toString().getBytes());
			fos.close();
		} catch (Exception e) {
			Log.e("carcast", "saving order", e);
		}
	}

        // IMPORTANT:
        // The regular expression used here *must* match the file naming scheme used in
        // DownloadHelper.downloadNewPodCasts().
        private boolean isPriority(File file)
        {
           String pattern = "^\\d+:\\d\\d:\\d+\\..*"; // E.g. "YYYY:00:XXXX.mp3"
           boolean priority = file.getName().matches(pattern);
	   Log.d("CarCast", "priority: " + priority + " " + file.getName());
           return priority;
        }

}

// vim: set noet ci pi sts=0 sw=4 ts=4
