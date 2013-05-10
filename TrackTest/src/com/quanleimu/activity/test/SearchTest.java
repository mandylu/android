package com.quanleimu.activity.test;
import java.util.ArrayList;

import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.EditText;

import com.baixing.activity.MainActivity;
import com.baixing.tracking.LogData;
import com.jayway.android.robotium.solo.Solo;
import com.quanleimu.activity.R;

public class SearchTest extends ActivityInstrumentationTestCase2<MainActivity> {

	private Solo solo;

	public SearchTest() {
		super(MainActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		// TODO Auto-generated method stub
		super.setUp();
		//
		solo = new Solo(getInstrumentation(), getActivity());
	}
	
	public void testPV(){
		View v = solo.getView(R.id.right_action_parent);
		solo.clickOnView(v);
		ArrayList<LogData> logs = TrackerLogSaver.getInstance().getLog("pageview", "/search");
		assertTrue(logs != null && logs.size() == 1);
		TrackerLogSaver.getInstance().clearLog();
	}
	
	public void testSearchCategoryPV(){
		View v = solo.getView(R.id.right_action_parent);
		solo.clickOnView(v);

		EditText et = solo.getEditText(0);
		assertTrue(et != null);
		
		solo.enterText(et, "三星");
		View tv = solo.getText("搜索");
		solo.clickOnView(tv);
		
		ArrayList<LogData> logs = TrackerLogSaver.getInstance().getLog("pageview", "/searchResultCategory");
		assertTrue(logs != null && logs.size() == 1);
		assertTrue(logs.get(0).getMap().get("searchKeyword").equals("三星"));
		TrackerLogSaver.getInstance().clearLog();
	}
	
	public void testSearchResultPV(){
		View v = solo.getView(R.id.right_action_parent);
		solo.clickOnView(v);

		EditText et = solo.getEditText(0);
		assertTrue(et != null);
		
		solo.enterText(et, "三星");
		View tv = solo.getText("搜索");
		solo.clickOnView(tv);
		
		solo.clickInList(0);
		
		ArrayList<LogData> logs = TrackerLogSaver.getInstance().getLog("pageview", "/searchResult");
		assertTrue(logs != null && logs.size() == 1);
		assertTrue(logs.get(0).getMap().get("searchKeyword").equals("三星"));
		TrackerLogSaver.getInstance().clearLog();
	}
	
	@Override
	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
		TrackerLogSaver.getInstance().clearLog();
	}
}