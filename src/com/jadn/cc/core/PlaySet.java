package com.jadn.cc.core;

import java.io.File;


public enum PlaySet {
		PODCASTS, NOTES_HOME, NOTES_WORK;
		
		public File getRoot() {
			return new File(Config.CarCastRoot, this.toString().toLowerCase().replace('_',
					'-'));
		}

		public String getName() {
			return toString();
		}

}
