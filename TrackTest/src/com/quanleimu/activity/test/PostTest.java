package com.quanleimu.activity.test;

import java.util.ArrayList;

import android.view.View;

import com.baixing.activity.PostActivity;
import com.baixing.tracking.LogData;
import com.quanleimu.activity.R;

public class PostTest extends BaseTest<PostActivity> {
	public PostTest() {
		super(PostActivity.class);
	}
	
	public void testCameraPV(){		
		View tv = solo.getText("跳过拍照");
		solo.waitForView(tv);
		ArrayList<LogData> homePVlogs = TrackerLogSaver.getInstance().getLog("pageview", "/post/camera");
		assertTrue(homePVlogs != null && homePVlogs.size() == 1);
	}
	
	public void testPostPV(){		
		View tv = solo.getText("跳过拍照");
		solo.waitForView(tv);
		solo.clickOnView(tv);
		
		View v = solo.getView(R.id.postgoodslayout);
		solo.waitForView(v);
		
		ArrayList<LogData> homePVlogs = TrackerLogSaver.getInstance().getLog("pageview", "/post");
		assertTrue(homePVlogs != null && homePVlogs.size() == 1);		
	}
	
	public void testEditPV(){
		solo.goBack();
		Util.loginWithSpecifiedAccount(solo);
		View tv = solo.getText("已发布信息");
		solo.clickOnView(tv);
		
		View item = solo.getView(R.id.goods_item_view_root);
		
		
	}
	
}