package com.jadn.cc.test;

import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.jadn.cc.core.Util;
import com.jadn.cc.services.DownloadHistory;
import com.jadn.cc.services.EnclosureHandler;
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
		solo.clickOnButton("Delete");
		assertEquals(0, solo.getCurrentListViews().get(0).getAdapter().getCount());
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Reset to Demos");
		solo.clickOnButton("Reset to Demos");
		assertEquals(7, solo.getCurrentListViews().get(0).getAdapter().getCount());
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
		assertEquals("Real Time with Bill Maher", solo.getEditText(1).getText().toString());

		solo.clickOnText("Save");
	}

	// http://feeds.feedburner.com/itpc/wwwwaylandws/Wayland_Productions/Were_Alive_-_Podcast/rssxml
	public void testZombie() throws Exception {
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Subscriptions");
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Add");
		solo.enterText(0, "feeds.feedburner.com/itpc/wwwwaylandws/Wayland_Productions/Were_Alive_-_Podcast/rssxml");
		// solo.enterText(0, "jadn.com/podcast.xml");
		solo.clickOnButton("Test");
		solo.waitForDialogToClose(20000);
		// assertTrue(solo.searchText("Feed is OK"));

		// assertTrue(solo.getEditText(1).getText().toString().trim().length()!=0);
		assertEquals("We're Alive - A \"Zombie\" Story of survival", solo.getEditText(1).getText().toString());

		solo.clickOnText("Save");
	}

	// Windows encoding
	public void testSubscriptionEncodedWindows() throws Exception {
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Subscriptions");
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Add");
		solo.enterText(0, "www.gomaespuma.com/podcast.asp");
		solo.clickOnButton("Test");
		solo.waitForDialogToClose(100 * 20000);
		assertEquals("Podcast con orejas", solo.getEditText(1).getText().toString());
	}

	public void testUTFFast() throws Exception {
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Subscriptions");
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Add");
		solo.enterText(0, "www.cbc.ca/podcasting/includes/quirks.xml");
		solo.clickOnButton("Test");
		solo.waitForDialogToClose(20000);
		assertEquals("Quirks and Quarks Segmented Show from CBC Radio", solo.getEditText(1).getText().toString());
	}

	public void testChurchPodcast1() throws Exception {
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Subscriptions");
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Add");
		solo.enterText(0, "cstonechurch.sermon.net/rss/client/cstonechurch/type/audio");
		solo.clickOnButton("Test");
		solo.waitForDialogToClose(20000);
		// assertTrue(solo.searchText("Feed is OK"));

		assertTrue("" != solo.getEditText(1).getText().toString());

		solo.clickOnText("Save");

	}

	public void testChurchPodcast2() throws Exception {
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Subscriptions");
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Add");
		solo.enterText(0, "www.sermon.net/rss/cstonechurch/main_channel");
		// solo.enterText(0, "jadn.com/podcast.xml");
		solo.clickOnButton("Test");
		solo.waitForDialogToClose(20000);
		// assertTrue(solo.searchText("Feed is OK"));

		assertTrue("" != solo.getEditText(1).getText().toString());

		solo.clickOnText("Save");

	}

	public void testChurchPodcast3() throws Exception {
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Subscriptions");
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Add");
		solo.enterText(0, "feeds.feedburner.com/lincolnbereanchurchpodcast");
		// solo.enterText(0, "jadn.com/podcast.xml");
		solo.clickOnButton("Test");
		solo.waitForDialogToClose(20000);
		// assertTrue(solo.searchText("Feed is OK"));

		assertTrue("" != solo.getEditText(1).getText().toString());

		solo.clickOnText("Save");
	}

	public void testNPRPodcast2() throws Exception {
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Subscriptions");
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Add");
		solo.enterText(0, "www.npr.org/rss/podcast.php?id=35");
		// solo.enterText(0, "jadn.com/podcast.xml");
		solo.clickOnButton("Test");
		solo.waitForDialogToClose(20000);
		// assertTrue(solo.searchText("Feed is OK"));

		assertTrue(!"".equals(solo.getEditText(1).getText().toString()));

		solo.clickOnText("Save");

	}

	public void testMarkLevin() throws Exception {
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Subscriptions");
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Add");
		solo.enterText(0, "www.marklevinshow.com/rss/ilevin.xml");
		// solo.enterText(0, "jadn.com/podcast.xml");
		solo.clickOnButton("Test");
		solo.waitForDialogToClose(20000);
		// assertTrue(solo.searchText("Feed is OK"));

		assertTrue(!"".equals(solo.getEditText(1).getText().toString()));

		solo.clickOnText("Save");

	}

	public void testNPR() throws Exception {
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Subscriptions");
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Add");
		solo.enterText(0, "leoville.tv/podcasts/kfi.xml");
		// solo.enterText(0, "jadn.com/podcast.xml");
		solo.clickOnButton("Test");
		solo.waitForDialogToClose(20000);
		// assertTrue(solo.searchText("Feed is OK"));

		assertTrue(!"".equals(solo.getEditText(1).getText().toString()));

		solo.clickOnText("Save");

	}

	public void testFlatironschurch() throws Exception {
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Subscriptions");
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Add");
		solo.enterText(0, "www.flatironschurch.com/podcasts/fcc_audio_podcast.php");
		// solo.enterText(0, "jadn.com/podcast.xml");
		solo.clickOnButton("Test");
		solo.waitForDialogToClose(20000);
		// assertTrue(solo.searchText("Feed is OK"));

		assertTrue(!"".equals(solo.getEditText(1).getText().toString()));

		solo.clickOnText("Save");

	}

	// http://www.podiobooks.com/title/8810/feed

	// This test takes too long to run... need a fake audio book to test with
	public void xtestBook() throws Exception {
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Subscriptions");
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Delete All");
		solo.clickOnButton("Delete");
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Add");
		solo.enterText(0, "www.podiobooks.com/bookfeed/23795/627/book.xml");
		solo.clickOnButton("Test");
		solo.waitForDialogToClose(20000);
		assertTrue(!"".equals(solo.getEditText(1).getText().toString()));

		// assertTrue(solo.searchText("Feed is OK"));
		solo.scrollDown();
		solo.clickOnCheckBox(1);
		solo.pressSpinnerItem(0, 5);
		solo.clickOnText("Save");

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
		solo.waitForText(" COMPLETED ", 1, 20 * 60 * 1000);

	}

	// test Umlet character in this feed
	public void testUmlet() throws Exception {
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Subscriptions");
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Add");
		solo.enterText(0, "www.cczwei.de/rss_issues.php");
		solo.clickOnButton("Test");
		solo.waitForDialogToClose(20000);

		assertTrue(!"".equals(solo.getEditText(1).getText().toString()));

		solo.clickOnText("Save");
	}

	public void testDarFM() throws Exception {
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Subscriptions");
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Add");
		solo.enterText(0, "www.dar.fm/rss/12345.xml");
		solo.clickOnButton("Test");
		solo.waitForDialogToClose(20000);

		assertTrue(!"".equals(solo.getEditText(1).getText().toString()));

		solo.clickOnText("Save");
	}

	// test Umlet character in this feed
	public void testTwit() throws Exception {
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Subscriptions");
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Delete All");
		solo.clickOnButton("Delete");
		assertEquals(0, solo.getCurrentListViews().get(0).getAdapter().getCount());
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Add");
		solo.enterText(0, "twit.tv/node/feed");
		solo.clickOnButton("Test");
		solo.waitForDialogToClose(20000);
		assertTrue(!"".equals(solo.getEditText(1).getText().toString()));
		solo.clickOnText("Save");
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
		solo.waitForText(" COMPLETED ", 1, 20 * 60 * 1000);

	}

	// http://rss.sciam.com/sciam/60secsciencepodcast
	public void testStockPodcasts() throws Exception {

		solo.sendKey(Solo.MENU);
		solo.clickOnText("Subscriptions");
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Add");

		for (String podcast : mySetPodcasts) {
			Log.i("PodcastTest", "Testing " + podcast);
			solo.clearEditText(0);
			solo.enterText(0, podcast);
			// solo.enterText(0, "jadn.com/podcast.xml");
			solo.clickOnButton("Test");
			solo.waitForDialogToClose(50000);
			// assertTrue(solo.searchText("Feed is OK"));

			assertFalse("Unable to read feed title: " + podcast, solo.getEditText(1).getText().toString().length() == 0);
			solo.clearEditText(1);
		}
		// assertTrue(solo.getEditText(1).getText().toString().trim().length()!=0);
	}

	String[] mySetPodcasts = {
			"http://www.marklevinshow.com/rss/ilevin.xml",
			"http://www.rzim.org/rss/rss-lmpt.aspx",
			"http://www.rzim.org/rss/rss-jt.aspx",

			// no new line on first line
			"http://www.cringely.com/feed/podcast/",
			"http://www.cbc.ca/podcasting/includes/quirks.xml",

			// User reported issues
			"http://cstonechurch.sermon.net/rss/client/cstonechurch/type/audio",
			"http://www.sermon.net/rss/cstonechurch/main_channel",
			"http://feeds.feedburner.com/lincolnbereanchurchpodcast",

			// App "Stock" examples.
			"http://rss.sciam.com/sciam/60-second-psych",
			// "http://audio.commonwealthclub.org/audio/podcast/weekly.xml",
			"http://nytimes.com/services/xml/rss/nyt/podcasts/techtalk.xml", "http://www.leoville.tv/podcasts/ww.xml",
			"http://feeds.feedburner.com/tedtalks_audio", "http://hansamann.podspot.de/rss", "http://jbosscommunityasylum.libsyn.com/rss",
			"http://feeds.feedburner.com/cnet/androidatlasmp3?tag=contentBody%3bpodcastMain",
			"http://www.theregister.co.uk/software/microbite/headlines.rss", "http://twit.tv/node/7952/feed",
			"http://rss.sciam.com/sciam/60-second-earth",
			"http://michaelkatz.libsyn.com/rss",
			"http://www.stanford.edu/group/edcorner/uploads/podcast/EducatorsCorner.xml",
			"http://tempoposse.herod.net/feed.rss",
			"http://www.thenakedscientists.com/naked_scientists_enhanced_podcast.xml",
			"http://revision3.com/rofl/feed/mp3",
			// "Http://Thisweekin.com/thisweekin-android",
			"http://feeds.feedburner.com/ThisWeekInAndroidaudioOnly", "http://www.discovery.com/radio/xml/sciencechannel.xml",
			"Http://Steelecreek.libsyn.com/rss", "http://feeds.feedburner.com/Ruby5", "http://rss.sciam.com/sciam/60secsciencepodcast",
			"http://www.marketwatch.com/feeds/podcast/podcast.asp?count=10&doctype=116&column=The+Wall+Street+Journal+Tech+Talk",
			"http://feeds.feedburner.com/androidcentralpodcast", "http://rss.cnn.com/services/podcasting/piersmorganaudio/rss?format=rss",
			"http://buzzoutloudpodcast.cnet.com", "http://feeds.feedburner.com/Radioandroid?format=xml" };

	public void testJustUtilMethod() throws Exception {
		for (String podcast : mySetPodcasts) {
			EnclosureHandler enclosureHandler = new EnclosureHandler(DownloadHistory.getInstance());
			enclosureHandler.setMax(2);
			try {
				Util.findAvailablePodcasts(podcast, enclosureHandler);
			} catch (Throwable t) {
				fail("on " + podcast + " msg: " + t.getMessage());
			}
			assertFalse("No title found, probably cant parse", enclosureHandler.getTitle().equals(""));
		}
	}

	// "http://rss.sciam.com/sciam/60-second-earth",
	public void test60SecEarth() throws Exception {
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Subscriptions");
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Delete All");
		solo.clickOnButton("Delete");
		assertEquals(0, solo.getCurrentListViews().get(0).getAdapter().getCount());
		solo.sendKey(Solo.MENU);
		solo.clickOnText("Add");
		solo.enterText(0, "rss.sciam.com/sciam/60-second-earth");
		solo.clickOnButton("Test");
		solo.waitForDialogToClose(20000);
		assertTrue(!"".equals(solo.getEditText(1).getText().toString()));
		solo.clickOnText("Save");
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
	}

}
