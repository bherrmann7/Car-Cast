package com.jadn.cc.test;

import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.jadn.cc.ui.CarCast;
import com.jayway.android.robotium.solo.Solo;

public class DeleteListenedToTest extends
		ActivityInstrumentationTestCase2<CarCast> {

	private Solo solo;

	public DeleteListenedToTest() {
		super("com.jadn.cc", CarCast.class);
	}

	public void setUp() throws Exception {
		solo = new Solo(getInstrumentation(), getActivity());
	}

	public void testDeleteListenedTo() throws Exception {
						
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Settings");
		solo.clickOnText("Max downloads");
		solo.clickOnText("2");
		solo.goBack();
		
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Subscriptions");
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Delete All");
		assertEquals(0, solo.getCurrentListViews().get(0).getAdapter()
				.getCount());
		// add in fakefeed cast
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Add");
		solo.enterText(0, "jadn.com/cctest/testsub.xml");
		solo.enterText(1, "testing feed");
		solo.clickOnButton("Save");

		solo.goBack();
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Podcasts");
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Erase");
		solo.clickOnButton("Erase");
		
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Delete All Podcasts");
		solo.clickOnText("Confirm");

		assertTrue(solo.searchText("No podcasts loaded."));

		solo.sendKey(Solo.MENU);
		solo.clickOnText("Download Podcasts");
		solo.clickOnText("Start Downloads");
		solo.waitForText(" COMPLETED ", 1, 10 * 1000);

		solo.goBack();
		assertTrue(solo.searchText("1/2"));
		
		solo.clickOnImageButton(1);
		// let both mp3 files play.
		Thread.sleep(10*1000);
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Podcasts");
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Delete Listened To");
		solo.goBack();
		assertTrue(solo.searchText("1/1"));

	}
}
