package com.quanleimu.activity.test;

import java.util.ArrayList;

import android.view.View;

import com.baixing.activity.PersonalActivity;
import com.baixing.tracking.LogData;

public class AccountTest extends BaseTest<PersonalActivity> {
	public AccountTest() {
		super(PersonalActivity.class);
	}
	
	public void testA(){		
		View tv = solo.getText("上海");
		solo.waitForView(tv);
		solo.clickOnView(tv);
		boolean login = solo.searchText("登录百姓网", true);
		if(!login){
			solo.clickOnText("设置");
			solo.clickOnText("退出登录");
			solo.clickOnButton(0);
			solo.goBack();
		}
	}
	
	public void testPV(){
		solo.clickOnText("登录百姓网");
		solo.waitForText("注册");
		ArrayList<LogData> logs = TrackerLogSaver.getInstance().getLog("pageview", "/login");
		assertTrue(logs != null && logs.size() == 1);
		
		solo.clickOnText("注册");
		solo.waitForText("注册帐号");
		logs = TrackerLogSaver.getInstance().getLog("pageview", "/register");
		assertTrue(logs != null && logs.size() == 1);
		
		solo.goBack();
		solo.clickOnText("点此找回");
		solo.waitForText("找回密码");
		logs = TrackerLogSaver.getInstance().getLog("pageview", "/forgetPassword");
		assertTrue(logs != null && logs.size() == 1);
		
		solo.goBack();
		solo.goBack();
		Util.loginWithSpecifiedAccount(solo);
		solo.clickOnText("设置");
		solo.clickOnText("修改密码");
		logs = TrackerLogSaver.getInstance().getLog("pageview", "/restPassword");
		assertTrue(logs != null && logs.size() == 1);
		
		solo.goBack();
		solo.clickOnText("绑定转发");
		logs = TrackerLogSaver.getInstance().getLog("pageview", "/3rdAuth");
		assertTrue(logs != null && logs.size() == 1);		
	}
}