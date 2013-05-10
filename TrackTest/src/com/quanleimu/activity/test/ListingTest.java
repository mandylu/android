package com.quanleimu.activity.test;

import java.util.ArrayList;

import android.text.TextUtils;
import android.view.View;

import com.baixing.activity.MainActivity;
import com.baixing.tracking.LogData;
import com.quanleimu.activity.R;

public class ListingTest extends BaseTest<MainActivity>{
	public ListingTest() {
		super(MainActivity.class);
	}
	
	public void testPV(){
		View v1 = solo.getText("物品交易");
		View item1 = (View) v1.getParent().getParent();
		solo.clickOnView(item1);
		
		solo.clickInList(0);
		
		View lvView = solo.getView(R.id.lvGoodsList);
		solo.waitForView(lvView);
		
		ArrayList<LogData> logs = TrackerLogSaver.getInstance().getLog("pageview", "/listing");
		assertTrue(logs != null && logs.size() == 1);
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("secondCateName")));
		
		View tv = solo.getText("筛选");
		solo.clickOnView(tv);
		
		logs = TrackerLogSaver.getInstance().getLog("pageview", "/listingFilter");
		assertTrue(logs != null && logs.size() == 1);
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("secondCateName")));		
	}
}