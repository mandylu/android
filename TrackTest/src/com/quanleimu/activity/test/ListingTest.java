package com.quanleimu.activity.test;

import java.util.ArrayList;

import android.text.TextUtils;
import android.view.View;

import com.baixing.activity.MainActivity;
import com.baixing.tracking.LogData;
import com.jayway.android.robotium.solo.Condition;
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
		
		solo.goBack();
		solo.goBack();
	}
	
	public void testCategoryListingEvent(){
		TrackerAspect.setWifiConnected(false);
		View v1 = solo.getText("物品交易");
		View item1 = (View) v1.getParent().getParent();
		solo.clickOnView(item1);
		solo.clickInList(0);
		
		if(solo.waitForDialogToOpen(500)){
			solo.clickOnButton(1);
		}		

		solo.waitForCondition(new Condition(){

			@Override
			public boolean isSatisfied() {
				// TODO Auto-generated method stub
				return solo.getView(R.id.pull_to_refresh_progress).getVisibility() == View.GONE;
			}
			
		}, 15000);
		Util.sleep(1000);
		
		ArrayList<LogData> logs = TrackerLogSaver.getInstance().getLog("event", "Listing");
		assertTrue(logs != null && logs.size() == 1);
		assertTrue(Integer.valueOf(logs.get(0).getMap().get("total_adsCount")) > 0);
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("categoryEnglishName")));
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("cityEnglishName")));
		
		solo.clickInList(1);
		View detailV = solo.getView(R.id.llDetail);
		solo.waitForView(detailV);
		
		logs = TrackerLogSaver.getInstance().getLog("event", "Listing_SelectedRowIndex");
		assertTrue(logs != null && logs.size() == 1);
		assertTrue(Integer.valueOf(logs.get(0).getMap().get("selectedRowIndex")) == 0);
		solo.goBack();
		
		solo.scrollToBottom();		
		solo.clickOnText("点击加载");
		
		TrackerLogSaver.getInstance().clearLog();
		
		solo.waitForCondition(new Condition(){

			@Override
			public boolean isSatisfied() {
				// TODO Auto-generated method stub
				return !solo.waitForText("正在加载", 1, 500);
			}
			
		}, 5000);
		Util.sleep(1000);
		
		logs = TrackerLogSaver.getInstance().getLog("event", "Listing_More");
		assertTrue(logs != null && logs.size() == 1);
		assertTrue(Integer.valueOf(logs.get(0).getMap().get("total_adsCount")) > 0);
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("categoryEnglishName")));
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("cityEnglishName")));
		
		TrackerAspect.setWifiConnected(true);
	}
	
	public void testSearchEvent(){
		View v = solo.getView(R.id.right_action_parent);
		solo.clickOnView(v);
		
		View historyV = solo.getView(R.id.lvSearchHistory);
		solo.waitForView(historyV);
		
		solo.enterText(0, "三星");
		
		View tv = solo.getText("搜索");
		solo.clickOnView(tv);
		
		solo.waitForCondition(new Condition(){

			@Override
			public boolean isSatisfied() {
				// TODO Auto-generated method stub
				return !solo.waitForText("搜索中", 1, 500);
			}
			
		}, 3000);
		
		solo.clickInList(0);
		
		solo.waitForCondition(new Condition(){

			@Override
			public boolean isSatisfied() {
				// TODO Auto-generated method stub
				return !solo.waitForText("搜索中", 1, 500);
			}
			
		}, 3000);
		
		View glView = solo.getView(R.id.lvGoodsList);
		solo.waitForView(glView);
		
		ArrayList<LogData> logs = TrackerLogSaver.getInstance().getLog("event", "Listing");
		assertTrue(logs != null && logs.size() == 1);
		assertTrue(Integer.valueOf(logs.get(0).getMap().get("total_adsCount")) > 0);
		assertEquals(logs.get(0).getMap().get("searchKeyword"), "三星");
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("categoryEnglishName")));
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("cityEnglishName")));
	}
	
	public void testFilterEvent(){
		TrackerAspect.setWifiConnected(false);
		View v1 = solo.getText("物品交易");
		View item1 = (View) v1.getParent().getParent();
		solo.clickOnView(item1);
		solo.clickInList(0);
		
		solo.waitForCondition(new Condition(){

			@Override
			public boolean isSatisfied() {
				// TODO Auto-generated method stub
				return solo.getView(R.id.pull_to_refresh_progress).getVisibility() == View.GONE;
			}
			
		}, 15000);
		Util.sleep(1000);
		
		solo.clickOnView(solo.getView(R.id.filter_item_1));
		solo.waitForDialogToOpen(1000);		
		ArrayList<LogData> logs = TrackerLogSaver.getInstance().getLog("event", "Listing_TopFilterOpen");
		assertTrue(logs != null && logs.size() == 1);
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("secondCateName")));
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("filterName")));
		
		solo.clickInList(2);
		solo.clickInList(2);
		
		solo.waitForCondition(new Condition(){

			@Override
			public boolean isSatisfied() {
				// TODO Auto-generated method stub
				return solo.getView(R.id.pull_to_refresh_progress).getVisibility() == View.GONE;
			}
			
		}, 15000);
		Util.sleep(1000);
		
		logs = TrackerLogSaver.getInstance().getLog("event", "Listing_TopFilterSubmit");
		assertTrue(logs != null && logs.size() == 1);
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("secondCateName")));
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("filterName")));
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("filterValue")));
		
		solo.clickOnText("筛选");
		solo.waitForText("更多筛选");
		solo.enterText(0, "三星");
		solo.clickOnView(solo.getText("请选择"));
		solo.waitForDialogToOpen(1000);
		solo.clickInList(2);
		solo.clickOnButton("确定");
		solo.waitForCondition(new Condition(){

			@Override
			public boolean isSatisfied() {
				// TODO Auto-generated method stub
				return solo.getView(R.id.pull_to_refresh_progress).getVisibility() == View.GONE;
			}
			
		}, 15000);
		Util.sleep(1000);
		logs = TrackerLogSaver.getInstance().getLog("event", "Listing_FilterSubmit");
		assertTrue(logs != null && logs.size() == 1);
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("secondCateName")));
	}
}