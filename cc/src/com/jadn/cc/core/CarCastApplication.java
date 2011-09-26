package com.jadn.cc.core;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.os.IBinder;
import android.util.Log;
import com.jadn.cc.services.ContentService;
import com.jadn.cc.services.ContentService.LocalBinder;
import com.jadn.cc.trace.TraceUtil;

public class CarCastApplication extends Application {
	public final static String[] releaseData = new String[] { 
		"25-Sep-2011", "For some headphone pause buttons and bluetooth pause buttons, CarCast now pauses.",
		"11-Sep-2011", "Fix: A change to keep the Archos tablet from sleeping during downloads.\n\nThanks Stephen Blott!",
		"22-Aug-2011", "Fix: Dont leave notification icon up after end of podcast(s) reached.",
		"17-Aug-2011", "Fix: Click on podcast link from a web browser, will show/add subscription to CarCast.",
		"14-Aug-2011a", "Hopefully a fix for Foreground/Background issues.\n\nThanks to Baruch Even!!!\nhttp://baruch.ev-en.org/",
		"11-Aug-2011",
		"Fix for www.flatironschurch.com podcast for Darrin Graham and friends",
			"29-July-2011",
				"Whew! I've been crazy busy with three other projects.  Finally quieting down.\n\nAdded confirmations to menu items Delete/Reset on Subscriptions screen\n\nEnjoy, Bob",
			"23-June-2011",
			"New subscriptions arent Unlimited by default.  If you suddenly start downloading hundreds of podcasts, check each subscriptions setting.",
			"29-May-2011",
			"Sorry the UI looks klunky (Subscription screen), but you can now specify max and order per subscription.  This make audiobook listening possible.  Yippie!! Thanks to Pat for laying the groundwork.",
			"20-May-2011",
			"On podcast screen, long press offers to 'delete all before'",
			"10-May-2011",
			"Restore 'press to play' from Podcasts screen",
			"08-May-2011",
			"Podcast list now has checkboxes for deleting",
			"12-Apr-2011",
			"In podcast list, show listened to podcasts with grey background",
			"07-Apr-2011",
			"Fixed loading of German (ISO-8859-1 encoded) podcasts",
			"02-Apr-2011",
			"Merged in Patrick's fixes for pausing on headset unplugged.  Yet another fix to podcast character encodings.\n\nCarCast is opensource!",
			"01-Apr-2011",
			"Another fix to podcast character encodings",
			"30-Mar-2011",
			"Fix podcasts encodeded in Windows-1252 (I hate non UTF8 character encoding.)",
			"27-Mar-2011",
			"Fixed Spanish/Latin Subscriptions.  Made subscription 'Test' show working message.",
			"28-Feb-2011",
			"Added code to keep wifi on, if it is on when downloading starts. (Round II)",
			"26-Feb-2011",
			"Try and make landscape mode more usable.",
			"19-Feb-2011",
			"Updated icon, Thanks to Peter Herrmann.  More space between buttons on player.",
			"28-Jan-2011",
			"Fix layout for: Audio Recorder, Subscription Add",
			"15-Jan-2011",
			"Added menu item to podcasts screen - delete listened to.",
			"27-Dec-2010",
			"Head phone unplug pauses player, wifi alarm fix, feedback goes to group.",
			"17-Dec-2010",
			"Podcast search and stat collection are back on.",
			"24-Nov-2010",
			"Turned off Podcast search - I'm moving and the server will be in a box.\n  Hopefully all back on by 15-DEC-2011",
			"15-Nov-2010",
			"enable/disable on subscriptions, warning when downloading without wifi, new icons on menus. ALL BY -> steffanfay@gmail.com <- send him thanks!!!",
			"27-Oct-2010",
			"Added automatic nightly download feature (beta).",
			"18-Oct-2010",
			"Move version info into settings title.",
			"13-Oct-2010",
			"Move Ads to top (based on user feedback!)\n\nNever Ads in Car Cast Pro.",
			"10-Oct-2010",
			"Experiement with Ads (I want to continue to improve Car Cast, Ads can help make that possible.  Note: No Ads in Car Cast Pro.)",
			"9-Oct-2010",
			"Minor adjustments to main screen layout.",
			"6-Oct-2010",
			"Fix feedback email (headsmack), fix Podcast and Subscription screens so delete doesnt loose place.  Also delete last works right.  Thanks Yoav Weiss!",
			"1-Oct-2010",
			"Added confirm to 'Delete All' menu item on Podcasts screen for Jim Fulner.",
			"21-Sep-2010",
			"Added 'details' button to 'Download Podcasts' screen.",
			"10-Sep-2010",
			"Added a setting for detailed download information (with email option for problem troubleshooting)",
			"2-Sep-2010",
			"Re-enable screen rotation of player (for people with car docks.)",
			"23-Aug-2010",
			"Patrick Forhan: Sorted subscription list, No rotation in player mode",
			"7-Aug-2010",
			"Updated text size on podcast search page. Added privacy settings. (Also updated podcast search database!) NOTE: includes Patrick's internal changes to podcasts.txt",
			"27-Jul-2010",
			"Testing Patricks changes",
			"18-Jul-2010",
			"Bug Fixes: delete empty files, tweak download progress bar",
			"24-Jun-2010",
			"Make Audio Recorder more obvious.\n\nThanks Patrick Forhan!!\n\n'Car Cast Pro' will get this update in about a week.",
			"22-Jun-2010b",
			"Fix allowing phone display to turn off (Thanks to Ofer Webman!!), increase font on download screen.",
			"22-Jun-2010",
			"Fix allowing phone display to turn off.\n\nThanks to Ofer Webman!!",
			"10-Jun-2010",
			"Fix font sizes on rotated screen.",
			"12-Jan",
			"Fix 'podcast' list font sizes on Droid (for Clyde.)",
			"2-Jan",
			getAppTitle() + " is now open source.\n\nsee http://jadn.com/cc/",
			"27-Dec",
			"Change Droid layout to use g1 layout (for larger buttons.)",
			"17-Dec",
			"Handle feeds with bad lengths, for Matt L",
			"11-Dec",
			"Allow Landscape mode since new layout is usable in landscape.",
			"09-Dec",
			"Fix layout on Droid, thanks Pete Smith.\nAdd setting so you can disable 'Auto Play Next' Podcast for Jim H.\nChanged layout of main screen to be relative for HTC Tattoo users.",
			"08-Dec",
			"Add setting so you can disable 'Auto Play Next' Podcast for Jim H.   Changed layout of main screen to be relative for HTC Tattoo users.",
			"21-Nov",
			"On Podcasts, touching a podcast will cause it to play.  Thanks to Tom Howes.",
			"09-Nov",
			"Fix botch to yesterday's max 'ulimited' downloads fix.",
			"08-Nov",
			"Shackman found setting max downloads to unlimited was broken.  Fixed.  Thanks.",
			"04-Nov",
			"Change to work with more phones (1.5 compatible, not 1.6)",
			"23-Oct",
			"Thanks for using Car Cast.  Please support Car Cast by providing feedback. (Use Menu/Email Feedback)\n\n- Fix to ignore bad search results.\n- Extra debug code for an unsual download problem.\n",
			"18-Oct-2009",//
			"22-Oct",
			"added extra debugging code for playback problems",
			"18-Oct-2009",//
			"18-Oct",
			"added Email Feedback button",
			"18-Oct-2009",//
			"15-Oct",
			"bug fix in showing podcasts.",
			"15-Oct-2009",//
			"19-Sept",
			"bug fixes in searching.",
			"19-Sep-2009",//
			"18-Sept",
			"bug fixes in title processing.  Added remote stacktrace reporting.",
			"18-Sep-2009",//
			"13-Sept",
			"Simple Video Support (seems odd for commuting, but people have asked.) "
					+ "If a video (.mp4) is in the feed, it will be saved to the camera directory for playback with camera application.",
			"13-Sep-2009", //
			"rc5", "Fix crash when choosing 'Search Again' on search results - sheesh.", "12-Sep-2009", //
			"rc4", "Clicking on podcast title switches to Audio Recorder", "07-Sep-2009", //
			"rc3", "Trippled size of podcast database for searches", "05-Sep-2009", //
			"rc2", "Treat .m4a like .mp3\nThanks to Daniel Browne!!", "03-Sep-2009", //
			"beta rc1", "fix subscription longpress", "01-Sep-2009", //
			"adc2", "package rename", "31-Aug-2009", //
			"beta 08.31", "added splash checkbox to settings", "31-Aug-2009", //
			"beta 08.30", "Added subscription search", "30-Aug-2009", //
			"beta 08.28b", "Email Podcast for first podcast fixed", "28-Aug-2009", //
			"beta 08.28", "SplashScreen added/fix no podcasts bug", "28-Aug-2009", //
			"beta 08.26", "Changed download default to 2 podcasts per subscription (instead of 5)", "25-Aug-2009", //
			"beta 08.25", "on first run, use sample subscriptions", "25-Aug-2009", //
			"beta 08.24", "focus on bug fixes", "24-Aug-2009", //
			"oswald5", "ready for market?", "22-Aug-2009", //
			"nanobots2", "added delete page", "21-Aug-2009", //
			"monkey2", "add podcast list", "18-Aug-2009", //
			"llama, llama Red Pajama", "Bow to Ed", "17-Aug-2009", //
			"klondike", "remove extranous features", "14-Aug-2009", //
			"Jumping Jackrabbit", "rework package structure", "11-Aug-2009", //
			"Himalayas", "Uses actual service class for handing media content.", "06-Aug-2009", //
			"ice cream 4", "pings home every 15.  Immediately on wifi connect.", //
			"29-July-2009", //
			"hummus", "After phone call resume.", "26-July-2009", //
			"grapes", "Fix deletion and remembering location", "09-July-2009", "french fries", "order podcasts by date", "22-Jun-2009", //
			"easter egg", "add/delete sites", "21-Jun-2009" };

	private Intent serviceIntent;
	private ContentService contentService;
	private ContentServiceListener contentServiceListener;

	@Override
	public void onCreate() {
		super.onCreate();
		serviceIntent = new Intent(this, ContentService.class);
	}

	private ServiceConnection contentServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder iservice) {
			Log.i("CarCast", "onServiceConnected; CN is " + name + "; binder is " + iservice);
			if (name.getClassName().equals(ContentService.class.getName())) {
				contentService = ((LocalBinder) iservice).getService();
				contentServiceListener.onContentServiceChanged(contentService);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.i("CarCast", "onServiceDisconnected; CN is " + name);
			if (name.getClassName().equals(ContentService.class.getName())) {
				contentService = null;
				contentServiceListener.onContentServiceChanged(contentService);
			}
		}
	};

	public void setContentServiceListener(ContentServiceListener listener) {
		this.contentServiceListener = listener;
		// make sure the service is running (may have been shut down by stopping
		// CarCast previously). Note that after the service has been stopped, we
		// need to bind to it again.
		// BIND_AUTO_CREATE forces the service to start running and continue
		// running until unbound.
		bindService(serviceIntent, contentServiceConnection, Context.BIND_AUTO_CREATE);

		// notify immediately if we have a contentService:
		listener.onContentServiceChanged(contentService);
	}

	public static String getVersion() {
		return releaseData[0];
	}

	public static String getVersionName(Context context, Class<?> cls) {
		try {
			ComponentName comp = new ComponentName(context, cls);
			PackageInfo pinfo = context.getPackageManager().getPackageInfo(comp.getPackageName(), 0);
			return pinfo.versionName;
		} catch (android.content.pm.PackageManager.NameNotFoundException e) {
			return null;
		}
	}

	public static void report(Throwable e1) {
		TraceUtil.report(e1);
	}

	public static void esay(Throwable re) {
		TraceUtil.report(re);
	}

	public static String getAppTitle() {
		return "Car Cast";
	}

	public void stopContentService() {
		Log.i("CarCast", "requesting stop; contentService is " + contentService);
		stopService(serviceIntent);
	}

}
