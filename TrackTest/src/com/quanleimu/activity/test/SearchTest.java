package com.quanleimu.activity.test;
import java.util.ArrayList;

import android.view.View;
import android.widget.EditText;

import com.baixing.activity.MainActivity;
import com.baixing.tracking.LogData;
import com.jayway.android.robotium.solo.Solo;
import com.quanleimu.activity.R;

public class SearchTest extends BaseTest<MainActivity> {

	public SearchTest() {
		super(MainActivity.class);
	}

	public void testPV(){
		View v = solo.getView(R.id.right_action_parent);
		solo.clickOnView(v);
		
		View historyV = solo.getView(R.id.lvSearchHistory);
		solo.waitForView(historyV);
		
		ArrayList<LogData> logs = TrackerLogSaver.getInstance().getLog("pageview", "/search");
		assertTrue(logs != null && logs.size() == 1);
		TrackerLogSaver.getInstance().clearLog();
	}
	
	public void testSearchCategoryPV(){
		View v = solo.getView(R.id.right_action_parent);
		solo.clickOnView(v);
		
		View historyV = solo.getView(R.id.lvSearchHistory);
		solo.waitForView(historyV);

		EditText et = solo.getEditText(0);
		assertTrue(et != null);
		
		solo.enterText(et, "三星");
		View tv = solo.getText("搜索");
		solo.clickOnView(tv);

		Util.Sleep(3000);
		
		ArrayList<LogData> logs = TrackerLogSaver.getInstance().getLog("pageview", "/searchResultCategory");
		assertTrue(logs != null && logs.size() == 1);
		assertTrue(logs.get(0).getMap().get("searchKeyword").equals("三星"));
		TrackerLogSaver.getInstance().clearLog();
	}
	
	public void testSearchResultPV(){
		View v = solo.getView(R.id.right_action_parent);
		solo.clickOnView(v);
		
		View historyV = solo.getView(R.id.lvSearchHistory);
		solo.waitForView(historyV);

		EditText et = solo.getEditText(0);
		assertTrue(et != null);
		
		solo.enterText(et, "三星");
		View tv = solo.getText("搜索");
		solo.clickOnView(tv);
		
		Util.Sleep(3000);		
		solo.clickInList(0);
		
		View glView = solo.getView(R.id.lvGoodsList);
		solo.waitForView(glView);
		
		ArrayList<LogData> logs = TrackerLogSaver.getInstance().getLog("pageview", "/searchResult");
		assertTrue(logs != null && logs.size() == 1);
		assertTrue(logs.get(0).getMap().get("searchKeyword").equals("三星"));
		TrackerLogSaver.getInstance().clearLog();
	}
	
}