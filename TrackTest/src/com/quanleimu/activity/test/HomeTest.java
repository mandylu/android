package com.quanleimu.activity.test;

import java.util.ArrayList;
import java.util.HashMap;

import android.test.suitebuilder.annotation.Smoke;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import com.baixing.activity.MainActivity;
import com.baixing.tracking.LogData;
import com.quanleimu.activity.R;

public class HomeTest extends BaseTest<MainActivity> {
	public HomeTest() {
		super(MainActivity.class);
	}
	
	@Smoke
	public void testHomePV(){		
		View tv = solo.getText("物品交易");
		solo.waitForView(tv);
		ArrayList<LogData> homePVlogs = TrackerLogSaver.getInstance().getLog("pageview", "/home");
		assertTrue(homePVlogs != null && homePVlogs.size() == 1);
	}
	
	public void testCitySelectEvent(){
		View title = solo.getView(R.id.logo_root);
		title.performClick();
		
		LinearLayout hotParent = (LinearLayout)solo.getView(R.id.linearHotCities);
		for(int i = 0; i < hotParent.getChildCount(); ++ i){
			if(hotParent.getChildAt(i).findViewById(R.id.ivChoose).getVisibility() == View.VISIBLE){
				continue;
			}
			View child = hotParent.getChildAt(i);
			solo.clickOnView(child);
			break;
		}		

		solo.waitForView(solo.getView(R.id.logo_root));
		ArrayList<LogData> logs = TrackerLogSaver.getInstance().getLog("event", "City_Select");
		assertTrue(logs != null && logs.size() == 1);
		HashMap<String, String> logMap = logs.get(0).getMap();
		assertTrue(logMap.containsKey("GPS_result"));
		assertTrue(((String)logMap.get("block")).equals("hotcity"));
		assertTrue(TextUtils.isEmpty(logMap.get("searchKeyword")));
		assertTrue( (((String)logMap.get("GPS_result")).equals("1") && !logMap.containsKey("GPS_failReason"))
				|| (((String)logMap.get("GPS_result")).equals("0") && logMap.containsKey("GPS_failReason")) );
	}
	
	public void testRecentCategory(){
		View v1 = solo.getText("物品交易");
		solo.clickOnView(v1);
		
		solo.clickInList(0);
		
		View lvView = solo.getView(R.id.lvGoodsList);
		solo.waitForView(lvView);
		
		solo.goBack();
		solo.goBack();
		
		ArrayList<LogData> logs = TrackerLogSaver.getInstance().getLog("event", "RecentCategory_Chow");
		assertTrue(logs != null && logs.size() >= 1);
		int count =  Integer.valueOf(logs.get(logs.size() - 1).getMap().get("count"));
		String cateNames = logs.get(logs.size() - 1).getMap().get("secondCateNames");
		assertEquals(count, cateNames.split(" ").length);

		LinearLayout ll = (LinearLayout)solo.getView(R.id.ll_categories);
		solo.clickOnView(ll.getChildAt(0));
		
		lvView = solo.getView(R.id.lvGoodsList);
		solo.waitForView(lvView);

		logs = TrackerLogSaver.getInstance().getLog("event", "RecentCategory_Click");
		assertTrue(logs != null && logs.size() == 1);
		int count2 =  Integer.valueOf(logs.get(logs.size() - 1).getMap().get("count"));
		String cateNames2 = logs.get(0).getMap().get("secondCateNames");
		assertEquals(count, count2);
		assertEquals(cateNames, cateNames2);
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("secondCateName")));
	}
}