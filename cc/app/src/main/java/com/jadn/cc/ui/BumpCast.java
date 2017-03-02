package com.jadn.cc.ui;

import android.view.View;
import android.view.View.OnClickListener;

public class BumpCast implements OnClickListener {
    CarCast carCast;
    boolean direction;

    public BumpCast(CarCast carCast, boolean direction) {
        this.carCast = carCast;
        this.direction = direction;
    }

    @Override public void onClick(View v) {
        if (direction) {
            carCast.getContentService().next();
        } else {
            carCast.getContentService().previous();
        }
        carCast.updateUI();
    }
}
