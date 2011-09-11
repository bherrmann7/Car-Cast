package com.jadn.cc.test;

import android.test.ActivityInstrumentationTestCase2;

import com.jadn.cc.ui.CarCast;
import com.jayway.android.robotium.solo.Solo;

public class WSJListenedToTest extends
		ActivityInstrumentationTestCase2<CarCast> {

	private Solo solo;

	public WSJListenedToTest() {
		super("com.jadn.cc", CarCast.class);
	}

	public void setUp() throws Exception {
		solo = new Solo(getInstrumentation(), getActivity());
	}
	
		
	public void testWSJ() throws Exception {
						
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
		solo.enterText(0, "feeds.wsjonline.com/wsj/podcast_wall_street_journal_tech_news_briefing");
		solo.enterText(1, "WSJ");
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
		solo.clickOnText("Download Podcasts");
		solo.clickOnText("Start Downloads");
		solo.waitForText(" COMPLETED ", 1, 10 * 1000);
				
		solo.goBack();
		assertTrue(solo.searchText("1/2"));

	}
}
