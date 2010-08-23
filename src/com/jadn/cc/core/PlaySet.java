package com.jadn.cc.core;

import java.io.File;


public enum PlaySet {
		PODCASTS, NOTES_HOME, NOTES_WORK;
		
		public File getRoot() {
			// BOBH TODO TESTING REMOVE THIS 
			File easyPhoneTunes = new File(android.os.Environment.getExternalStorageDirectory(),"music/podcast");
			if(easyPhoneTunes.exists())
				return easyPhoneTunes;
			
			return new File(Config.CarCastRoot, this.toString().toLowerCase().replace('_',
					'-'));
		}

		public String getName() {
			return toString();
		}

}
