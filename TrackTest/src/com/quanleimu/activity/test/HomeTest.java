package com.quanleimu.activity.test;

import java.util.ArrayList;

import android.test.suitebuilder.annotation.Smoke;

import com.baixing.activity.MainActivity;
import com.baixing.tracking.LogData;
import com.jayway.android.robotium.solo.Solo;

public class HomeTest extends BaseTest<MainActivity> {
	public HomeTest() {
		super(MainActivity.class);
	}
	
	@Smoke
	public void testHomePV(){		
		ArrayList<LogData> homePVlogs = TrackerLogSaver.getInstance().getLog("pageview", "/home");
		assertTrue(homePVlogs != null && homePVlogs.size() > 0);
	}
}