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

		ArrayList<LogData> logs = TrackerLogSaver.getInstance().getLog("event", "City_Select");
		assertTrue(logs != null && logs.size() == 1);
		HashMap<String, String> logMap = logs.get(0).getMap();
		assertTrue(logMap.containsKey("GPS_result"));
		assertTrue(((String)logMap.get("block")).equals("hotcity"));
		assertTrue(TextUtils.isEmpty(logMap.get("searchKeyword")));
		assertTrue( (((String)logMap.get("GPS_result")).equals("1") && !logMap.containsKey("GPS_failReason"))
				|| (((String)logMap.get("GPS_result")).equals("0") && logMap.containsKey("GPS_failReason")) );
	}
}