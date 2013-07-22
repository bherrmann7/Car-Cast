package com.jadn.cc.test;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;
import android.view.MenuItem;

import com.jadn.cc.ui.CarCast;
import com.jayway.android.robotium.solo.Solo;

public class SettingsTest extends ActivityInstrumentationTestCase2<CarCast> {
    public SettingsTest() {
        super("com.jadn.cc", CarCast.class);
    }
    private Solo solo;

    public void setUp() throws Exception {
        solo = new Solo(getInstrumentation(), getActivity());
    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }

    public void testPreferenceIsSaved() throws Exception {

        solo.clickOnMenuItem("Settings");
    }

}
