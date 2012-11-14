package com.quanleimu.activity.test;

import android.test.suitebuilder.TestSuiteBuilder;
import junit.framework.Test;
import junit.framework.TestSuite;

public class BXTestSuite extends TestSuite {
	public BXTestSuite() {
		addTestSuite(AdListingViewTest.class);
		addTestSuite(AdViewTest.class);
		addTestSuite(CityViewTest.class);
		addTestSuite(FavoriteTest.class);
		addTestSuite(MyViewTest.class);
		addTestSuite(PostProcessTest.class);
		addTestSuite(PostViewTest.class);
		addTestSuite(MainActivityTest.class);
		//addTest(createTest(KeepLiveTest.class, "runPost"));
	}
	
	public static Test suite() {
        return new BXTestSuite();
    }
}
