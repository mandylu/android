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

	public void enterAndBackForItem(String text) {
		View v1 = solo.getText(text);
		View item1 = (View) v1.getParent().getParent();
		solo.clickOnView(item1);
		solo.goBack();
	}
	
	@Smoke
	public void testHomePV(){
		ArrayList<LogData> logs = TrackerLogSaver.getInstance().getLog();
		boolean pvExist = false;
		for(int i = logs.size() - 1; i >= 0; -- i){
			LogData log = logs.get(i);
			HashMap<String, String> map = log.getMap();
			if(map == null) continue;
			String type = map.get("tracktype");
			if(type.equals("pageview")){
				String url = map.get("url");
				if(url.equals("/home")){
					pvExist = true;
					break;
				}
			}
		}
		assertTrue(pvExist);
	}

	@Smoke
	public void testClick() {
		enterAndBackForItem("物品交易");
		enterAndBackForItem("车辆买卖");
		enterAndBackForItem("房屋租售");
		enterAndBackForItem("全职招聘");
		enterAndBackForItem("兼职招聘");
		enterAndBackForItem("求职简历");
		enterAndBackForItem("交友活动");
		enterAndBackForItem("宠物");
		enterAndBackForItem("生活服务");
		enterAndBackForItem("教育培训");		
	}

	@Override
	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
		TrackerLogSaver.getInstance().clearLog();
	}

}