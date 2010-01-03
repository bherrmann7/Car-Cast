package com.jadn.cc.ui; import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;

public class BumpCast implements OnClickListener {
	boolean direction;
	CarCast carCast;

	public BumpCast(CarCast carCast, boolean direction) {
		this.carCast = carCast;
		this.direction = direction;
	}

	@Override
	public void onClick(View v) {
		try {
			if (direction) {
				carCast.getContentService().next();
			} else {
				carCast.getContentService().previous();
			}
			carCast.updateUI();
		} catch (RemoteException e) {
			carCast.esay(e);
			e.printStackTrace();
		}
	}
}
