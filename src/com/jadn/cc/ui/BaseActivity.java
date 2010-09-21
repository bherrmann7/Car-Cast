package com.jadn.cc.ui; import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.RemoteException;

import com.jadn.cc.core.Subscription;
import com.jadn.cc.services.ContentService;
import com.jadn.cc.services.IContentService;
import com.jadn.cc.trace.TraceUtil;

public abstract class BaseActivity extends Activity implements ServiceConnection {
	public final static String[] releaseData = new String[] {
            "21-Sep-2010", "Added 'details' button to 'Download Podcasts' screen.",
            "10-Sep-2010", "Added a setting for detailed download information (with email option for problem troubleshooting)",
            "2-Sep-2010", "Re-enable screen rotation of player (for people with car docks.)",
		    "23-Aug-2010", "Patrick Forhan: Sorted subscription list, No rotation in player mode",
		    "7-Aug-2010", "Updated text size on podcast search page. Added privacy settings. (Also updated podcast search database!) NOTE: includes Patrick's internal changes to podcasts.txt",
      		"27-Jul-2010", "Testing Patricks changes",
          	"18-Jul-2010", "Bug Fixes: delete empty files, tweak download progress bar",
         	"24-Jun-2010", "Make Audio Recorder more obvious.\n\nThanks Patrick Forhan!!\n\n'Car Cast Pro' will get this update in about a week.",
			"22-Jun-2010b", "Fix allowing phone display to turn off (Thanks to Ofer Webman!!), increase font on download screen.",
		    "22-Jun-2010", "Fix allowing phone display to turn off.\n\nThanks to Ofer Webman!!",
			"10-Jun-2010", "Fix font sizes on rotated screen.",
			"12-Jan", "Fix 'podcast' list font sizes on Droid (for Clyde.)",
			"2-Jan", "Car Cast is now open source.\n\nsee http://jadn.com/cc/",
			"27-Dec", "Change Droid layout to use g1 layout (for larger buttons.)",
			"17-Dec", "Handle feeds with bad lengths, for Matt L",
			"11-Dec", "Allow Landscape mode since new layout is usable in landscape.",
		    "09-Dec", "Fix layout on Droid, thanks Pete Smith.\nAdd setting so you can disable 'Auto Play Next' Podcast for Jim H.\nChanged layout of main screen to be relative for HTC Tattoo users.",
			"08-Dec", "Add setting so you can disable 'Auto Play Next' Podcast for Jim H.   Changed layout of main screen to be relative for HTC Tattoo users.",
			"21-Nov", "On Podcasts, touching a podcast will cause it to play.  Thanks to Tom Howes.",
	 		"09-Nov", "Fix botch to yesterday's max 'ulimited' downloads fix.",
		 	"08-Nov", "Shackman found setting max downloads to unlimited was broken.  Fixed.  Thanks.",
		    "04-Nov", "Change to work with more phones (1.5 compatible, not 1.6)",
			"23-Oct", "Thanks for using Car Cast.  Please support Car Cast by providing feedback. (Use Menu/Email Feedback)\n\n- Fix to ignore bad search results.\n- Extra debug code for an unsual download problem.\n", "18-Oct-2009",//
			"22-Oct", "added extra debugging code for playback problems", "18-Oct-2009",//
			"18-Oct", "added Email Feedback button", "18-Oct-2009",//
			"15-Oct", "bug fix in showing podcasts.", "15-Oct-2009",//
	    	"19-Sept", "bug fixes in searching.", "19-Sep-2009",//
 	    	"18-Sept", "bug fixes in title processing.  Added remote stacktrace reporting.", "18-Sep-2009",//
     	    "13-Sept", "Simple Video Support (seems odd for commuting, but people have asked.) "+
     	    "If a video (.mp4) is in the feed, it will be saved to the camera directory for playback with camera application.", "13-Sep-2009", //
		    "rc5", "Fix crash when choosing 'Search Again' on search results - sheesh.", "12-Sep-2009", //
			"rc4", "Clicking on podcast title switches to Audio Recorder", "07-Sep-2009", //
			"rc3", "Trippled size of podcast database for searches", "05-Sep-2009", //
			"rc2", "Treat .m4a like .mp3\nThanks to Daniel Browne!!", "03-Sep-2009", //
    		"beta rc1", "fix subscription longpress", "01-Sep-2009", //
    		"adc2", "package rename", "31-Aug-2009", //
		    "beta 08.31", "added splash checkbox to settings", "31-Aug-2009", //
			"beta 08.30", "Added subscription search", "30-Aug-2009", //
			"beta 08.28b", "Email Podcast for first podcast fixed", "28-Aug-2009", //
			"beta 08.28", "SplashScreen added/fix no podcasts bug", "28-Aug-2009", //
			"beta 08.26", "Changed download default to 2 podcasts per subscription (instead of 5)", "25-Aug-2009", //
			"beta 08.25", "on first run, use sample subscriptions", "25-Aug-2009", //
			"beta 08.24", "focus on bug fixes", "24-Aug-2009", //
			"oswald5", "ready for market?", "22-Aug-2009", //
			"nanobots2", "added delete page", "21-Aug-2009", //
	        "monkey2", "add podcast list", "18-Aug-2009", //
		    "llama, llama Red Pajama", "Bow to Ed", "17-Aug-2009", //
     		"klondike", "remove extranous features", "14-Aug-2009", //
			"Jumping Jackrabbit", "rework package structure", "11-Aug-2009", //
			"Himalayas",
			"Uses actual service class for handing media content.",
			"06-Aug-2009", //
			"ice cream 4",
			"pings home every 15.  Immediately on wifi connect.", //
			"29-July-2009", //
			"hummus", "After phone call resume.",
			"26-July-2009", // 
			"grapes", "Fix deletion and remembering location", "09-July-2009",
			"french fries", "order podcasts by date", "22-Jun-2009", // 
			"easter egg", "add/delete sites", "21-Jun-2009" };

	public static String getVersion() {
		return releaseData[0];
	}
	
	@SuppressWarnings("unchecked")
	public  static String getVersionName(Context context, Class cls) 
	{
	  try {
	    ComponentName comp = new ComponentName(context, cls);
	    PackageInfo pinfo = context.getPackageManager().getPackageInfo(comp.getPackageName(), 0);
	    return pinfo.versionName;
	  } catch (android.content.pm.PackageManager.NameNotFoundException e) {
	    return null;
	  }
	}	
 
	public static void report(Throwable e1) {
		TraceUtil.report(e1);
	}
	
	IContentService contentService;
	
	public void esay(Throwable re) {
		TraceUtil.report(re);		
	}

	public IContentService getContentService() {
		return contentService;
	}

	protected List<Subscription> getSubscriptions() {
		try {
			List<Subscription> subs = contentService.getSubscriptions();

			return subs;
		} catch (RemoteException re) {
			esay(re);
			return Collections.emptyList();
		}
	}

	abstract void onContentService() throws RemoteException ;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		this.bindService(new Intent(getApplicationContext(),
				ContentService.class), this, Context.BIND_AUTO_CREATE);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();		
		this.unbindService(this);
	}

	public void onServiceConnected(android.content.ComponentName name,
			android.os.IBinder iservice) {		
			if (name.getClassName().equals(ContentService.class.getName())){
				contentService = IContentService.Stub.asInterface(iservice);
			try {
				onContentService();
			} catch (RemoteException re) {
				esay(re);
			}
		}
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		//eventService = null;
	}

}
