package com.quanleimu.activity.test;

import java.util.ArrayList;

import android.test.suitebuilder.annotation.Smoke;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.baixing.activity.MainActivity;
import com.baixing.tracking.LogData;
import com.quanleimu.activity.R;

public class VADTest extends BaseTest<MainActivity> {
	public VADTest() {
		super(MainActivity.class);
	}

	@Smoke
	public void testPV() {
		View v1 = solo.getText("物品交易");
		View item1 = (View) v1.getParent().getParent();
		solo.clickOnView(item1);

		solo.clickInList(0);

//		View lvView = solo.getView(R.id.lvGoodsList);
//		solo.waitForView(lvView);
		Util.Sleep(15000);
		
		ArrayList<ListView> listingView = solo.getCurrentViews(ListView.class);
		int index = 0;
		for(int i = 0; i < listingView.get(0).getChildCount(); ++ i){
			ImageView iv = (ImageView)listingView.get(0).getChildAt(i).findViewById(R.id.ivInfo);
			if(iv != null){
				if(iv.getDrawable() != null){
					index = i;
					break;
				}
			}
		}
		
		solo.clickInList(index);
		Util.Sleep(3000);
		
		View detailV = solo.getView(R.id.llDetail);
		solo.waitForView(detailV);
		
		ArrayList<LogData> logs = TrackerLogSaver.getInstance().getLog("pageview", "/viewAd");
		assertTrue(logs != null && logs.size() == 1);
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("secondCateName")));
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("adId")));
		
//		View iv = solo.getView(R.id.action_indicator_img);
//		solo.getv
//		View mapV = (View)solo.getImage(R.drawable.vad_icon_location).getParent();		
//		solo.clickOnView(mapV);
		Util.Sleep(10000);
		logs = TrackerLogSaver.getInstance().getLog("pageview", "/viewAdMap");
		assertTrue(logs != null && logs.size() == 1);
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("secondCateName")));
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("adId")));
		solo.goBack();
		
		

	}
}