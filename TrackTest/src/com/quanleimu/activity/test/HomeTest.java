package com.quanleimu.activity.test;

import java.util.ArrayList;

import android.test.suitebuilder.annotation.Smoke;
import android.view.View;

import com.baixing.activity.MainActivity;
import com.baixing.tracking.LogData;

public class HomeTest extends BaseTest<MainActivity> {
	public HomeTest() {
		super(MainActivity.class);
	}
	
	@Smoke
	public void testHomePV(){		
		View tv = solo.getText("物品交易");
		solo.waitForView(tv);
		ArrayList<LogData> homePVlogs = TrackerLogSaver.getInstance().getLog("pageview", "/home");
		assertTrue(homePVlogs != null && homePVlogs.size() == 1);
	}
}