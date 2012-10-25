package com.quanleimu.activity.test;

import org.athrun.android.framework.AthrunTestCase;
import org.athrun.android.framework.Test;
import org.athrun.android.framework.viewelement.AbsListViewElement;
import org.athrun.android.framework.viewelement.ScrollViewElement;

import android.util.Log;
import android.widget.ScrollView;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class KeepLiveTest extends BaixingTestCase {
	private static final String LOG_TAG = "ListingKeepLiveTest";
	private boolean willFinished = false;
	
	public KeepLiveTest() throws Exception {
		super();
		AthrunTestCase.setMaxTimeToFindView(10000);
		final Timer timer = new Timer();
        TimerTask tt = new TimerTask() { 
            @Override
            public void run () {
            	willFinished = true;
                timer.cancel();
            }
        };
        timer.schedule(tt, 1 * 60 * 60 * 1000); // N秒 * 1000
	}
	
	/*
	 * Start run this Ad Listing Test
	 */
	@Test
	public void runAdListing() throws Exception {
        while(!willFinished) {
        	int index = (int)(Math.random() * 9);
        	Log.i(LOG_TAG, "Start do Category.index." + index);
        	doFirstCategory(index);
        }
	}
	
	/*
	 * Start run post Test
	 */
	@Test
	public void runPost() throws Exception {
        while(!willFinished) {
        	int index = (int)(Math.random() * 9);
        	Log.i(LOG_TAG, "Start do Post.index." + index);
        	doPost(index);
        }
	}
	
	private void doFirstCategory(int index) throws Exception {
		openHomeCategoryByIndex(index);
		int maxCategoryNum = doScrollView(CATEGORY_SECOND_GRIDVIEW_ID, 5);
    	Log.i(LOG_TAG, "Start do Category.maxCategoryNum" + maxCategoryNum);
		if (maxCategoryNum < 0) return;
		scrollTop((int)(maxCategoryNum / 8), CATEGORY_SECOND_GRIDVIEW_ID);
		doSecondCategory((int)(Math.random() * maxCategoryNum));
		goBack(true);
	}
	
	private void doSecondCategory(int index) throws Exception {
		openSecondCategoryByIndex(index);
		int lastAdNum = doScrollView(AD_VIEWLIST_ID, 4);
		scrollTop(4, AD_VIEWLIST_ID);
		Log.i(LOG_TAG, "Start do Rand Ad.index." + index + "/" + lastAdNum);
		for(int i = 0; i < (lastAdNum > 5 ? 5 : 2); i++) {
			int rndIndex = (int)(Math.random() * (lastAdNum > 4 ? lastAdNum - 4 : 0));
			Log.i(LOG_TAG, "Start do Rand Ad.index." + index + "/" + lastAdNum + "/" + rndIndex);
			assertNotNull(openAdByIndex(rndIndex));
			BXViewGroupElement detailView = findElementById(AD_DETAILVIEW_ID,
					BXViewGroupElement.class);
			assertNotNull(detailView);
			adViewPicTouch();
			showNextView(AD_DETAILVIEW_ID);
			showNextView(AD_DETAILVIEW_ID);
			showNextView(AD_DETAILVIEW_ID);
			showPrevView(AD_DETAILVIEW_ID);
			adViewPicTouch();
			showNextView(AD_DETAILVIEW_ID);
			goBack(true);
			scrollTop((int) (index / 6), AD_VIEWLIST_ID);
		}
		goBack(true);
	}
	
	private void doPost(int index) throws Exception {
		String[][] postData = postDataQiecheyongpin;
		if (index == 0) postData = postDataJiaju;
		if (index == 2) postData = postDataXiezilou;
		if (index == 3) postData = postDataQitazhaopin;
		if (index == 4) postData = postDataYanyuan;
		if (index == 5) postData = postDataJianzhiJianli;
		if (index == 6) postData = postDataXunren;
		if (index == 7) postData = postDataChongwuyongpin;
		if (index == 8) postData = postDataLipindingzhi;
		if (index == 9) postData = postDataWaiyupeixun;
		String title = doPostByData(postData);
		if (title.length() > 0) {
			deleteAdByText(title);
		} else {
			openTabbar(TAB_ID_POST);
		}
	}
	
}
