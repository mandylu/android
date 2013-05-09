package com.quanleimu.activity.test;

import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.baixing.activity.MainActivity;

public class SecondCategoryTest extends
		ActivityInstrumentationTestCase2<MainActivity> {

	public SecondCategoryTest() {
		super(MainActivity.class);
	}
	
	public void testPV(){
		Log.d("category", "second category");
	}
}
