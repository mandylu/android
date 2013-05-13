package com.quanleimu.activity.test;

import java.util.ArrayList;

import android.support.v4.view.ViewPager;
import android.test.suitebuilder.annotation.Smoke;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.baixing.activity.MainActivity;
import com.baixing.tracking.LogData;
import com.baixing.widget.HorizontalListView;
import com.jayway.android.robotium.solo.Condition;
import com.quanleimu.activity.R;

public class VADTest extends BaseTest<MainActivity> {
	public VADTest() {
		super(MainActivity.class);
	}

	@Smoke
	public void testPV() {
		View v1 = solo.getText("物品交易");
		solo.waitForView(v1);
		View item1 = (View) v1.getParent().getParent();
		solo.clickOnView(item1);

		solo.clickInList(0);

		solo.waitForCondition(new Condition(){

			@Override
			public boolean isSatisfied() {
				// TODO Auto-generated method stub
				return solo.getView(R.id.pull_to_refresh_progress).getVisibility() == View.GONE;
			}
			
		}, 15000);
		Util.sleep(3000);
		
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
		
		View detailV = solo.getView(R.id.llDetail);
		solo.waitForView(detailV);
		
		ArrayList<LogData> logs = TrackerLogSaver.getInstance().getLog("pageview", "/viewAd");
		assertTrue(logs != null && logs.size() == 1);
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("secondCateName")));
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("adId")));
		
		ViewPager vp = (ViewPager)solo.getView(R.id.svDetail);
		View currentItem = null;
		for(int i = 0; i < vp.getChildCount(); ++ i){
			View child = vp.getChildAt(i);
			if(child.getTag() != null && (Integer)child.getTag() == index - 1){
				currentItem = child;
				LinearLayout meta = (LinearLayout)child.findViewById(R.id.meta);
				solo.clickOnView(meta.getChildAt(meta.getChildCount() - 1));
				break;
			}
			
		}		
		
		View mapV = solo.getView(R.id.bmapsView);
		solo.waitForView(mapV);
		
		logs = TrackerLogSaver.getInstance().getLog("pageview", "/viewAdMap");
		assertTrue(logs != null && logs.size() == 1);
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("secondCateName")));
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("adId")));
		solo.goBack();
		
		assertTrue(currentItem != null);
		HorizontalListView hlv = (HorizontalListView)currentItem.findViewById(R.id.glDetail);
		solo.clickOnView(hlv.getChildAt(0));
		
		View bigGallery = solo.getView(R.id.vfCoupon);
		solo.waitForView(bigGallery);

		logs = TrackerLogSaver.getInstance().getLog("pageview", "/viewAdPic");
		assertTrue(logs != null && logs.size() == 1);
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("secondCateName")));
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("adId")));
		
		TrackerLogSaver.getInstance().clearLog();
	}
}