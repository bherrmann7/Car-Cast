package com.jadn.cc.ui;

import android.view.View;
import android.view.View.OnClickListener;

public class Bumper implements OnClickListener {
    int bump;
    CarCast carCast;

    public Bumper(CarCast carCast, int bump) {
        this.bump = bump;
        this.carCast = carCast;
    }

    @Override public void onClick(View v) {
        carCast.getContentService().bumpForwardSeconds(bump);
        carCast.updateUI();
    }
}
