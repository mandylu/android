package com.quanleimu.activity.test;

import java.util.ArrayList;
import java.util.HashMap;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.Smoke;
import android.view.View;

import com.baixing.activity.MainActivity;
import com.baixing.tracking.LogData;
import com.jayway.android.robotium.solo.Solo;

public class HomeTest extends ActivityInstrumentationTestCase2<MainActivity> {

	private Solo solo;

	public HomeTest() {
		super(MainActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		// TODO Auto-generated method stub
		super.setUp();
		//
		solo = new Solo(getInstrumentation(), getActivity());
	}
	
	@Smoke
	public void testHomePV(){		
		ArrayList<LogData> homePVlogs = TrackerLogSaver.getInstance().getLog("pageview", "/home");
		assertTrue(homePVlogs != null && homePVlogs.size() > 0);
	}

	@Override
	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
		TrackerLogSaver.getInstance().clearLog();
	}

}