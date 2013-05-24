package com.quanleimu.activity.test;

import java.util.ArrayList;

import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.view.View;

import com.baixing.activity.MainActivity;
import com.baixing.tracking.LogData;
import com.jayway.android.robotium.solo.Solo;

public class SecondCategoryTest extends
		ActivityInstrumentationTestCase2<MainActivity> {
	private Solo solo;
	public SecondCategoryTest() {
		super(MainActivity.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		// TODO Auto-generated method stub
		super.setUp();
		solo = new Solo(getInstrumentation(), getActivity());
	}
	
	public void enterAndBackForItem(String text) {
		View v1 = solo.getText(text);
		View item1 = (View) v1.getParent().getParent();
		solo.clickOnView(item1);
		solo.goBack();
	}
	
	private void assertCategoryName(String categoryName){
		ArrayList<LogData> categoryPVlogs = TrackerLogSaver.getInstance().getLog("pageview", "/categories");
		assertTrue(categoryPVlogs != null && categoryPVlogs.size() == 1);
		assertTrue(categoryPVlogs.get(0).getMap().get("firstCateName").equals(categoryName));		
	}
	
	public void testPV(){
		enterAndBackForItem("物品交易");		
		assertCategoryName("ershou");
		TrackerLogSaver.getInstance().clearLog();
		
		enterAndBackForItem("车辆买卖");		
		assertCategoryName("cheliang");
		TrackerLogSaver.getInstance().clearLog();
		
		enterAndBackForItem("房屋租售");		
		assertCategoryName("fang");
		TrackerLogSaver.getInstance().clearLog();

		enterAndBackForItem("全职招聘");		
		assertCategoryName("gongzuo");
		TrackerLogSaver.getInstance().clearLog();

		enterAndBackForItem("兼职招聘");		
		assertCategoryName("jianzhi");
		TrackerLogSaver.getInstance().clearLog();

		enterAndBackForItem("求职简历");		
		assertCategoryName("qiuzhi");
		TrackerLogSaver.getInstance().clearLog();

		enterAndBackForItem("交友活动");		
		assertCategoryName("huodong");
		TrackerLogSaver.getInstance().clearLog();

		enterAndBackForItem("宠物");		
		assertCategoryName("chongwuleimu");
		TrackerLogSaver.getInstance().clearLog();

		enterAndBackForItem("生活服务");		
		assertCategoryName("fuwu");
		TrackerLogSaver.getInstance().clearLog();

		enterAndBackForItem("教育培训");		
		assertCategoryName("jiaoyupeixun");
		TrackerLogSaver.getInstance().clearLog();
	}
}
