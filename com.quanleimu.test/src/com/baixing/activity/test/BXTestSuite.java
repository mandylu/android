package com.baixing.activity.test;

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
		//addTestSuite(PostViewTest.class);
		addTest(createTest(PostViewTest.class, "testPostChecking"));
		//addTestSuite(SampleTest.class);
		//addTest(createTest(KeepLiveTest.class, "runPost"));
	}
	
	public static Test suite() {
        return new BXTestSuite();
    }
}
