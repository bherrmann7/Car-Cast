package com.jadn.cc.ui; import android.os.Handler;

public class Updater extends Thread {

	Handler handler;
	private boolean keepGoing = true;

	Runnable runnable;

	public Updater(Handler handler, Runnable runnable) {
		this.handler = handler;
		this.runnable = runnable;
		start();
	}

	public void allDone() {
		keepGoing = false;
	}

	@Override
    public void run() {
		while (keepGoing) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(keepGoing)
				handler.post(runnable);
		}
	}
}
