package com.quanleimu.activity.test;

import java.util.ArrayList;

import android.support.v4.view.ViewPager;
import android.test.suitebuilder.annotation.Smoke;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.baixing.activity.MainActivity;
import com.baixing.tracking.LogData;
import com.baixing.widget.HorizontalListView;
import com.jayway.android.robotium.solo.Condition;
import com.quanleimu.activity.R;

public class VADTest extends BaseTest<MainActivity> {
	public VADTest() {
		super(MainActivity.class);
	}
	
	private View getCurrentItemView(int index){
		ViewPager vp = (ViewPager)solo.getView(R.id.svDetail);
		View currentItem = null;
		for(int i = 0; i < vp.getChildCount(); ++ i){
			View child = vp.getChildAt(i);
			if(child.getTag() != null && (Integer)child.getTag() == index - 1){
				currentItem = child;
				break;
			}			
		}
		return currentItem;
	}
	
	private int jumpToVAD(){
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
		
		return index;
	}

	public void testPV() {
		int index = jumpToVAD();
		
		ArrayList<LogData> logs = TrackerLogSaver.getInstance().getLog("pageview", "/viewAd");
		assertTrue(logs != null && logs.size() == 1);
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("secondCateName")));
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("adId")));
		
		View currentItem = getCurrentItemView(index);
		LinearLayout meta = (LinearLayout)currentItem.findViewById(R.id.meta);
		solo.clickOnView(meta.getChildAt(meta.getChildCount() - 1));
		
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
		
		solo.goBack();
		
		
		final View cutItem = getCurrentItemView(index);
		solo.waitForView(cutItem);
		this.getActivity().runOnUiThread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				((ScrollView)cutItem).scrollBy(0, 5000);
//				((ScrollView)cutItem).scrollBy(0, -500);	
			}			
		});
		Util.sleep(1000);
		View userView = cutItem.findViewById(R.id.user_info);

		ScrollView sv = (ScrollView)solo.getView(ScrollView.class, 0);
		sv.scrollTo(0, 500);
		
		solo.clickOnView(userView);
		solo.waitForView(ListView.class);
		logs = TrackerLogSaver.getInstance().getLog("pageview", "/user");
		assertTrue(logs != null && logs.size() == 1);
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("secondCateName")));
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("adId")));
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("adSenderId")));
		
		TrackerLogSaver.getInstance().clearLog();
	}
	
	private void _testMobileCallClick(){
		final View call = solo.getView(R.id.vad_call_btn);
		solo.clickOnView(call);

		ArrayList<LogData> logs = TrackerLogSaver.getInstance().getLog("event", "Viewad_MobileCallClick");
		assertTrue(logs != null && logs.size() == 1);
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("secondCateName")));
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("adId")));		
	}
	
	private void _testFav(){		
		View fav = solo.getView(R.id.vad_title_fav_parent);
		solo.clickOnView(fav);
		solo.waitForText("收藏");
		solo.clickOnText("收藏");
		solo.waitForCondition(new Condition(){

			@Override
			public boolean isSatisfied() {
				// TODO Auto-generated method stub
				return !solo.searchText("收藏成功");
			}
			
		}, 3000);
		ArrayList<LogData> logs = TrackerLogSaver.getInstance().getLog("event", "Viewad_Fav");
		assertTrue(logs != null && logs.size() == 1);
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("secondCateName")));
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("adId")));		

		fav = solo.getView(R.id.vad_title_fav_parent);
		solo.clickOnView(fav);

		solo.clickOnView(solo.getText("取消收藏"));
		logs = TrackerLogSaver.getInstance().getLog("event", "Viewad_Unfav");
		assertTrue(logs != null && logs.size() == 1);
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("secondCateName")));
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("adId")));		
	}
	
	private void _testSMS(){
		solo.clickOnView(solo.getView(R.id.vad_buzz_btn));
		solo.goBack();
		ArrayList<LogData> logs = TrackerLogSaver.getInstance().getLog("event", "Viewad_SMS");
		assertTrue(logs != null && logs.size() == 1);
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("secondCateName")));
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("adId")));				
	}
	
	private void _testShowMap(View currentItem){
		LinearLayout meta = (LinearLayout)currentItem.findViewById(R.id.meta);
		solo.clickOnView(meta.getChildAt(meta.getChildCount() - 1));
		
		View mapV = solo.getView(R.id.bmapsView);
		solo.waitForView(mapV);

		ArrayList<LogData> logs = TrackerLogSaver.getInstance().getLog("event", "Viewad_ShowMap");
		assertTrue(logs != null && logs.size() == 1);
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("secondCateName")));
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("adId")));		
		
		solo.goBack();
	}
	
	public void testEvent(){
		int index = jumpToVAD();		
		_testFav();				
		_testShowMap(getCurrentItemView(index));
		_testSMS();
		
	}
	
	public void testMobileClick(){
		jumpToVAD();
		_testMobileCallClick();
	}
}