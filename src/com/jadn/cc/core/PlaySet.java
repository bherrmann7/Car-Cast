package com.jadn.cc.core;

import java.io.File;


public enum PlaySet {
		NOTES_HOME, NOTES_WORK, PODCASTS;
		
		public String getName() {
			return toString();
		}

		public File getRoot() {
			// BOBH TODO TESTING REMOVE THIS 
			File easyPhoneTunes = new File(android.os.Environment.getExternalStorageDirectory(),"music/podcast");
			if(easyPhoneTunes.exists())
				return easyPhoneTunes;
			
			return new File(Config.CarCastRoot, this.toString().toLowerCase().replace('_',
					'-'));
		}

}
