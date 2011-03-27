package com.jadn.cc.test;

import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.jadn.cc.ui.CarCast;
import com.jayway.android.robotium.solo.Solo;

public class PodcastTest extends ActivityInstrumentationTestCase2<CarCast> {

	private Solo solo;

	public PodcastTest() {
		super("com.jadn.cc", CarCast.class);
	}

	public void setUp() throws Exception {
		solo = new Solo(getInstrumentation(), getActivity());
	}

	public void testSubscriptionReset() throws Exception {
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Subscriptions");
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Delete All");
		assertEquals(0, solo.getCurrentListViews().get(0).getAdapter()
				.getCount());
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Reset to Demos");
		assertEquals(7, solo.getCurrentListViews().get(0).getAdapter()
				.getCount());
	}

	// www.hbo.com/podcasts/billmaher/podcast.xml
	public void testSubscriptionToBill() throws Exception {
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Subscriptions");
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Add");
		solo.enterText(0, "www.hbo.com/podcasts/billmaher/podcast.xml");
		// solo.enterText(0, "jadn.com/podcast.xml");
		solo.clickOnButton("Test");
		solo.waitForDialogToClose(20000);
		// assertTrue(solo.searchText("Feed is OK"));

		// assertTrue(solo.getEditText(1).getText().toString().trim().length()!=0);
		assertEquals("Real Time with Bill Maher", solo.getEditText(1).getText()
				.toString());
	}

	// Spanish Podcast
	// 
	public void testSpanishPodcast() throws Exception {
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Subscriptions");
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Add");
		solo.enterText(0,
				"www.ondacero.es/OndaCero/rss/La-rosa-de-los-vientos/2166771");
		// solo.enterText(0, "jadn.com/podcast.xml");
		solo.clickOnButton("Test");
		solo.waitForDialogToClose(20000);
		// assertTrue(solo.searchText("Feed is OK"));

		// assertTrue(solo.getEditText(1).getText().toString().trim().length()!=0);
		assertEquals("La rosa de los vientos", solo.getEditText(1).getText()
				.toString());
	}

	// http://rss.sciam.com/sciam/60secsciencepodcast
	public void testStockPodcasts() throws Exception {

		solo.sendKey(Solo.MENU);
		solo.clickOnText("Subscriptions");

		for (String podcast : mySetPodcasts) {
			Log.i("PodcastTest", "Testing "+podcast);
			solo.sendKey(Solo.MENU);
			solo.clickOnText("Add");
			solo.enterText(0, podcast.substring("http://".length()));
			// solo.enterText(0, "jadn.com/podcast.xml");
			solo.clickOnButton("Test");
			solo.waitForDialogToClose(50000);
			// assertTrue(solo.searchText("Feed is OK"));

			assertTrue("" != solo.getEditText(1).getText().toString());
			solo.goBack();
			solo.goBack();
		}
		// assertTrue(solo.getEditText(1).getText().toString().trim().length()!=0);
	}

	String[] mySetPodcasts = {
			"http://feeds.feedburner.com/lincolnbereanchurchpodcast",
			"http://rss.sciam.com/sciam/60-second-psych",
			"http://www.cringely.com/feed/podcast/",
			//"http://audio.commonwealthclub.org/audio/podcast/weekly.xml",
			"http://nytimes.com/services/xml/rss/nyt/podcasts/techtalk.xml",
			"http://www.leoville.tv/podcasts/ww.xml",
			"http://feeds.feedburner.com/tedtalks_audio",
			"http://www.cbc.ca/podcasting/includes/quirks.xml",
			"http://hansamann.podspot.de/rss",
			"http://jbosscommunityasylum.libsyn.com/rss",
			"http://feeds.feedburner.com/cnet/androidatlasmp3?tag=contentBody%3bpodcastMain",
			"http://www.theregister.co.uk/software/microbite/headlines.rss",
			"http://twit.tv/node/7952/feed",
			"http://rss.sciam.com/sciam/60-second-earth",
			"http://michaelkatz.libsyn.com/rss",
			"http://www.stanford.edu/group/edcorner/uploads/podcast/EducatorsCorner.xml",
			"http://tempoposse.herod.net/feed.rss",
			"http://www.thenakedscientists.com/naked_scientists_enhanced_podcast.xml",
			"http://revision3.com/rofl/feed/mp3",
			"Http://Thisweekin.com/thisweekin-android",
			"http://www.discovery.com/radio/xml/sciencechannel.xml",
			"Http://Steelecreek.libsyn.com/rss",
			"http://feeds.feedburner.com/Ruby5",
			"http://rss.sciam.com/sciam/60secsciencepodcast",
			"http://www.marketwatch.com/feeds/podcast/podcast.asp?count=10&doctype=116&column=The+Wall+Street+Journal+Tech+Talk",
			"http://feeds.feedburner.com/androidcentralpodcast",
			"http://rss.cnn.com/services/podcasting/piersmorganaudio/rss?format=rss",
			"http://buzzoutloudpodcast.cnet.com",
			"http://feeds.feedburner.com/Radioandroid?format=xml" };

}
