package com.quanleimu.activity.test;

import java.util.ArrayList;

import android.widget.TextView;

import com.baixing.activity.PersonalActivity;
import com.baixing.tracking.LogData;

public class SettingTest extends BaseTest<PersonalActivity> {
	public SettingTest() {
		super(PersonalActivity.class);
	}

	private void chooseCity(){
		if(solo.searchText("上海")){
			TextView tv = solo.getText("上海");
			solo.clickOnView(tv);
		}
	}
	
	public void testEvent(){
		chooseCity();
		
		solo.clickOnView(solo.getText("设置"));
		
		solo.clickOnText("检查更新");
		ArrayList<LogData> logs = TrackerLogSaver.getInstance().getLog("event", "Settings_CheckUpdate");
		assertTrue(logs != null && logs.size() == 1);
		
		solo.clickOnText("关于");
		solo.goBack();
		logs = TrackerLogSaver.getInstance().getLog("event", "Settings_About");
		assertTrue(logs != null && logs.size() == 1);

		solo.clickOnText("反馈");
		solo.goBack();
		solo.goBack();
		logs = TrackerLogSaver.getInstance().getLog("event", "Settings_Feedback");
		assertTrue(logs != null && logs.size() == 1);
		
		solo.clickOnText("评价我们");
		solo.goBack();
		logs = TrackerLogSaver.getInstance().getLog("event", "Settings_Rate");
		assertTrue(logs != null && logs.size() == 1);
		
		solo.clickOnText("流量优化设置");
		solo.goBack();
		logs = TrackerLogSaver.getInstance().getLog("event", "settingsPicMode");
		assertTrue(logs != null && logs.size() == 1);
		
		solo.clickOnText("退出登录");
		logs = TrackerLogSaver.getInstance().getLog("event", "Settings_Logout");
		assertTrue(logs != null && logs.size() == 1);		
		
		solo.clickOnButton(1);
		logs = TrackerLogSaver.getInstance().getLog("event", "Settings_Logout_Cancel");
		assertTrue(logs != null && logs.size() == 1);

		solo.clickOnText("退出登录");
		solo.clickOnButton(0);
		logs = TrackerLogSaver.getInstance().getLog("event", "Settings_Logout_Confirm");
		assertTrue(logs != null && logs.size() == 1);

	}
}