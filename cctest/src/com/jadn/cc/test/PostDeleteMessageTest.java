package com.jadn.cc.test;

import android.test.ActivityInstrumentationTestCase2;

import com.jadn.cc.ui.CarCast;
import com.jayway.android.robotium.solo.Solo;

public class PostDeleteMessageTest extends
		ActivityInstrumentationTestCase2<CarCast> {

	private Solo solo;

	public PostDeleteMessageTest() {
		super("com.jadn.cc", CarCast.class);
	}

	public void setUp() throws Exception {
		solo = new Solo(getInstrumentation(), getActivity());
	}

	/**
	 * If you download podcasts, then delete them all, the player screen incorrectly
	 * had the last download results on it. This test verifies that it now
	 * correctly says "No Podcasts"
	 **/
	public void testSubscriptionReset() throws Exception {
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
		solo.clickOnButton("Test");
		solo.waitForDialogToClose(20000);
		assertEquals("NPR: Wait Wait... Don't Tell Me! Podcast", solo
				.getEditText(1).getText().toString());
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

		solo.sendKey(Solo.MENU);

		solo.clickOnText("Podcasts");
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Delete All Podcasts");
		solo.clickOnText("Confirm");
		assertTrue(solo.searchText("No podcasts loaded."));
	}
}
