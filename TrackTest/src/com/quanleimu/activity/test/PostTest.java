package com.quanleimu.activity.test;

import java.util.ArrayList;

import android.view.View;

import com.baixing.activity.MainActivity;
import com.baixing.tracking.LogData;
import com.quanleimu.activity.R;

public class PostTest extends BaseTest<MainActivity> {
	public PostTest() {
		super(MainActivity.class);
	}
	
	public void testCameraPV(){		
		View v = solo.getText("免费发布");
		solo.waitForView(v);
		solo.clickOnView(v);

		View tv = solo.getText("跳过拍照");
		solo.waitForView(tv);
		ArrayList<LogData> logs = TrackerLogSaver.getInstance().getLog("pageview", "/post/camera");
		assertTrue(logs != null && logs.size() == 1);
		assertTrue(logs.get(0).getMap().get("from").equals("others"));
		assertTrue(logs.get(0).getMap().get("isEdit").equals("0"));
		solo.goBack();
	}
	
	public void testPostPV(){
		View postV = solo.getText("免费发布");
		solo.waitForView(postV);
		solo.clickOnView(postV);
		
		
		View tv = solo.getText("跳过拍照");
		solo.waitForView(tv);
		solo.clickOnView(tv);
		
		View v = solo.getView(R.id.postgoodslayout);
		solo.waitForView(v);
		
		ArrayList<LogData> homePVlogs = TrackerLogSaver.getInstance().getLog("pageview", "/post");
		assertTrue(homePVlogs != null && homePVlogs.size() == 1);
		
		solo.goBack();
		solo.goBack();
		solo.clickOnButton(0);
	}	
}