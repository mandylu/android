package com.quanleimu.activity.test;

import java.util.ArrayList;

import android.view.View;
import android.widget.TextView;

import com.baixing.activity.PersonalActivity;
import com.baixing.tracking.LogData;
import com.jayway.android.robotium.solo.Condition;
import com.quanleimu.activity.R;

public class FavoriteTest extends BaseTest<PersonalActivity> {
	public FavoriteTest() {
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
		solo.clickOnView(solo.getText("收藏"));
		
		solo.waitForCondition(new Condition(){

			@Override
			public boolean isSatisfied() {
				// TODO Auto-generated method stub
				return solo.getView(R.id.pull_to_refresh_progress).getVisibility() == View.GONE;
			}
			
		}, 15000);
		
		solo.clickOnView(solo.getView(R.id.rlListOperate));
		ArrayList<LogData> logs = TrackerLogSaver.getInstance().getLog("event", "Fav_Manage");
		assertTrue(logs != null && logs.size() == 1);
		
		solo.clickOnText("取消收藏");
		
		assertTrue(solo.waitForText("取消收藏成功", 1, 3000));
		logs = TrackerLogSaver.getInstance().getLog("event", "Fav_Delete");
		assertTrue(logs != null && logs.size() == 1);

	}
}