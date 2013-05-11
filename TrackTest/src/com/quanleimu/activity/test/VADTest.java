package com.quanleimu.activity.test;

import java.util.ArrayList;

import android.support.v4.view.ViewPager;
import android.test.suitebuilder.annotation.Smoke;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

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

		Util.Sleep(15000);
		
		ArrayList<ListView> listingView = solo.getCurrentViews(ListView.class);
		int index = 0;
		for(int i = 0; i < listingView.get(0).getChildCount(); ++ i){
			ImageView iv = (ImageView)listingView.get(0).getChildAt(i).findViewById(R.id.ivInfo);
			if(iv != null){
				if(!TextUtils.isEmpty((String)iv.getTag())){
					index = i;
					break;
				}
			}
		}
		
		View item = listingView.get(0).getChildAt(index);
		solo.clickOnView(item);
		
		String address = ((TextView)item.findViewById(R.id.tvDateAndAddress)).getText().toString();
		Util.Sleep(3000);
		
		View detailV = solo.getView(R.id.llDetail);
		solo.waitForView(detailV);
		
		ArrayList<LogData> logs = TrackerLogSaver.getInstance().getLog("pageview", "/viewAd");
		assertTrue(logs != null && logs.size() == 1);
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("secondCateName")));
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("adId")));
		
		View addressView = solo.getText(address);
//		ViewPager vp = (ViewPager)solo.getView(R.id.svDetail);
//		int currentIndex = vp.getCurrentItem();
//		View currentParent = vp.getChildAt(index == 1 ? 1 : currentIndex - 1);
//		LinearLayout parent = (LinearLayout)currentParent.findViewById(R.id.meta);
//		int childCount = parent.getChildCount();
//		View mapView = parent.getChildAt(childCount > 1 ? childCount - 1 : 0);
		solo.clickOnView(addressView);
		
		
		View mapV = solo.getView(R.id.bmapsView);
		solo.waitForView(mapV);
		
		logs = TrackerLogSaver.getInstance().getLog("pageview", "/viewAdMap");
		assertTrue(logs != null && logs.size() == 1);
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("secondCateName")));
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("adId")));
		solo.goBack();
		
		

	}
}