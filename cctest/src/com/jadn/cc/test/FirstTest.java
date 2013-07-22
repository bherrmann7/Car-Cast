package com.jadn.cc.test;

import android.test.ActivityInstrumentationTestCase2;

import com.jadn.cc.ui.CarCast;
import com.jayway.android.robotium.solo.Solo;

public class FirstTest extends ActivityInstrumentationTestCase2<CarCast> {

	private Solo solo;

	public FirstTest() {
		super("com.jadn.cc", CarCast.class);
	}

	public void setUp() throws Exception {
		solo = new Solo(getInstrumentation(), getActivity());
	}

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }


    public void testPreferenceIsSaved() throws Exception {
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Settings");
		solo.isCheckBoxChecked(1);// wifi checkbox
	}


}
