package com.quanleimu.activity.test;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;

import com.jayway.android.robotium.solo.Solo;

class BaseTest<activity extends Activity> extends ActivityInstrumentationTestCase2<activity> {
	public BaseTest(Class<activity> activityClass) {
		super(activityClass);
		// TODO Auto-generated constructor stub
	}

	protected Solo solo;
	
	@Override
	protected void setUp() throws Exception {
		// TODO Auto-generated method stub
		super.setUp();
		//
		solo = new Solo(getInstrumentation(), getActivity());
	}
	
	@Override
	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
		TrackerLogSaver.getInstance().clearLog();
	}
}