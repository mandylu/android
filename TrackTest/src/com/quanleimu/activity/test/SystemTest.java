package com.quanleimu.activity.test;

import java.util.ArrayList;

import android.text.TextUtils;
import android.view.KeyEvent;

import com.baixing.activity.MainActivity;
import com.baixing.tracking.LogData;

public class SystemTest extends BaseTest<MainActivity> {
	public SystemTest() {
		super(MainActivity.class);
	}

	
	public void testSystemEvents(){
		solo.waitForText("百姓网");
		ArrayList<LogData> logs = TrackerLogSaver.getInstance().getLog("event", "App_Start");
		assertTrue(logs != null && logs.size() == 1);
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("city")));
		
		solo.sendKey(KeyEvent.KEYCODE_MENU);
		assertTrue(solo.waitForText("切换城市", 1, 1000));
		logs = TrackerLogSaver.getInstance().getLog("event", "Menu_Show");
		assertTrue(logs != null && logs.size() == 1);
		
		solo.clickOnText("设置");
		logs = TrackerLogSaver.getInstance().getLog("event", "Menu_Action");
		assertTrue(logs != null && logs.size() == 1);
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("fragment")));
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("menuActionType")));
	}
}