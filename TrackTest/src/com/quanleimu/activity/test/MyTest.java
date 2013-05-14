package com.quanleimu.activity.test;

import java.util.ArrayList;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.baixing.activity.PersonalActivity;
import com.baixing.tracking.LogData;
import com.jayway.android.robotium.solo.Condition;
import com.quanleimu.activity.R;

public class MyTest extends BaseTest<PersonalActivity> {
	public MyTest() {
		super(PersonalActivity.class);
	}
	
	private void chooseCity(){
		TextView tv = solo.getText("上海");
		solo.clickOnView(tv);
	}
	
	public void testMyAPV(){
		chooseCity();
		TrackerLogSaver.getInstance().clearLog();
		Util.loginWithSpecifiedAccount(solo);
		ArrayList<LogData> logs = TrackerLogSaver.getInstance().getLog("pageview", "/my");
		
		assertTrue(logs != null && logs.size() >= 1);
		assertTrue(logs.get(0).getMap().get("isLogin").equals("1"));
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("userId")));
	}
	
	public void testMyPostPV(){
		
		solo.clickOnView(solo.getText("已发布信息"));
		
		solo.waitForCondition(new Condition(){

			@Override
			public boolean isSatisfied() {
				// TODO Auto-generated method stub
				return solo.getView(R.id.pull_to_refresh_progress).getVisibility() == View.GONE;
			}
			
		}, 15000);
		Util.sleep(3000);
		
		ArrayList<LogData> logs = TrackerLogSaver.getInstance().getLog("pageview", "/myAds_sent");
		assertTrue(logs != null && logs.size() == 1);
		assertTrue(Integer.valueOf(logs.get(0).getMap().get("adsCount")) >= 0);
		
		solo.clickInList(1);
		
		View edit = solo.getText("修改");
		logs = TrackerLogSaver.getInstance().getLog("pageview", "/myViewad");
		assertTrue(logs != null && logs.size() == 1);
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("adSenderId")));
		assertTrue(Integer.valueOf(logs.get(0).getMap().get("adStatus")) >= 0);
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("secondCateName")));
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("adId")));
		
		solo.clickOnView(edit);
		solo.getView(R.id.goodscontent);
		logs = TrackerLogSaver.getInstance().getLog("pageview", "/editPost");
		assertTrue(logs != null && logs.size() == 1);
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("secondCateName")));
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("adId")));
	}
	
	public void testMyFavPV(){
		solo.clickOnView(solo.getText("收藏"));
		ArrayList<LogData> logs = TrackerLogSaver.getInstance().getLog("pageview", "/favAds");
		assertTrue(logs != null && logs.size() == 1);
		assertTrue(Integer.valueOf(logs.get(0).getMap().get("adsCount")) >= 0);
	}
	
	public void testMySettings(){
		solo.clickOnView(solo.getText("设置"));
		ArrayList<LogData> logs = TrackerLogSaver.getInstance().getLog("pageview", "/settings");
		assertTrue(logs != null && logs.size() == 1);		
	}
}