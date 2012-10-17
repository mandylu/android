package com.quanleimu.activity.test;

import org.athrun.android.framework.AthrunTestCase;
import org.athrun.android.framework.Test;

import java.util.concurrent.TimeUnit;
import java.util.ArrayList;

import android.test.TouchUtils;
import android.view.View;
import android.util.Log;
import android.support.v4.view.ViewPager;

import org.athrun.android.framework.viewelement.AbsListViewElement;
import org.athrun.android.framework.viewelement.TextViewElement;
import org.athrun.android.framework.viewelement.ViewCoordinate;
import org.athrun.android.framework.viewelement.ViewElement;
import org.athrun.android.framework.viewelement.ViewGroupElement;
import org.athrun.android.framework.viewelement.ScrollViewElement;
import org.athrun.android.framework.viewelement.ViewUtils;

public class MainActivityTest extends BaixingTestCase {
	private static final String LOG_TAG = "MainActivityTest";
	
	public MainActivityTest() throws Exception {
		super();
		AthrunTestCase.setMaxTimeToFindView(10000);
	}
	
	@Test
	public void testSearchClick() throws Exception {
		
		findElementById(SEARCH_TEXTVIEW_ID).doClick();
		TimeUnit.SECONDS.sleep(1);
		TextViewElement etSearchText = findElementById(SEARCH_TEXTVIEW_ID,
				TextViewElement.class);
		etSearchText.setText("iphone");

		findElementById(SEARCH_BUTTON_ID).doClick();
		
		TimeUnit.SECONDS.sleep(10);
		assertEquals("iphone", etSearchText.getText());
		getDevice().pressMenu();
		TimeUnit.SECONDS.sleep(2);
	}
	
	@Test
	public void testCategoryClick() throws Exception {
		openCategoryByIndex(3, 1);
		getDevice().pressBack();
		TimeUnit.SECONDS.sleep(1);
		getDevice().pressBack();
		TimeUnit.SECONDS.sleep(1);
	}
	
	@Test
	public void testScrollToNextScreen() throws Exception {
		AbsListViewElement catListView = findElementById(CATEGORY_VIEWLIST_ID,
				AbsListViewElement.class);
		assertEquals(3, catListView.getLastVisiblePosition());
		catListView.scrollToNextScreen();
		TimeUnit.SECONDS.sleep(3);
		assertEquals(4, catListView.getFirstVisiblePosition());
		catListView.scrollToNextScreen();
		assertEquals(11, catListView.getLastVisiblePosition());
		TimeUnit.SECONDS.sleep(3);
		assertEquals(7, catListView.getFirstVisiblePosition());
	}
	
	@Test
	public void testPost() throws Exception {
		openPostCategory(0, 0);
	}
	
	@Test 
	public void testPostData() throws Exception {
		openPostCategory(1, 6);
		openPostItemByIndex(1);
		selectMetaByName("出售");
		openPostItemByIndex(2);
		selectMetaByName("宝山");
		selectMetaByName("全部");
		setMetaByIndex(0, "test title");

		openPostItemByIndex(3);
		selectMetaByName("个人");

		setMetaByIndex(1, TEST_DATA_MOBILE);
		
		//更多
		openPostItemByIndex(4);
		setOtherMetaByIndex(0, "100");
		ViewElement el = findElementByText(POST_DONE);
		assertNotNull(el);
		el.doClick();
		TimeUnit.SECONDS.sleep(1);

		ViewElement eld = findElementByText(POST_SEND);
		assertNotNull(eld);
		eld.doClick();
		TimeUnit.SECONDS.sleep(3);
	}
	
	@Test
	public void testMy() throws Exception {
		logout();
		ViewElement el = findElementById(MY_LISTITEM_MYAD_ID);
		assertNotNull(el);
		el.doClick();
		TimeUnit.SECONDS.sleep(1);
		assertNotNull(findElementById(MY_LISTITEM_MYAD_ID));
		
		logon();
		
		ViewElement el2 = findElementById(MY_LISTITEM_MYAD_ID);
		assertNotNull(el2);
		el2.doClick();
		TimeUnit.SECONDS.sleep(1);
		assertNull(findElementById(MY_LISTITEM_MYAD_ID));
		TimeUnit.SECONDS.sleep(2);
		
		findElementById(MY_MYAD_APPROVE_BUTTON_ID).doClick();
		TimeUnit.SECONDS.sleep(2);
		findElementById(MY_MYAD_DELETE_BUTTON_ID).doClick();
		TimeUnit.SECONDS.sleep(2);
	}
	
	@Test
	public void testAdViewTouch() throws Exception {
		BXViewGroupElement detailView = showAd(0, 0, 0);
		assertNotNull(detailView);
		
		TextViewElement titleView = findElementById(AD_DETAILVIEW_TITLE_ID, TextViewElement.class);
		assertNotNull(titleView);
		//Ad1
		String title1 = titleView.getText();
		Log.i(LOG_TAG, "title1:" + title1);
		//ViewElement next = findElementById(AD_DETAILVIEW_NEXT_ID);
		//assertNotNull(next);
		//next.doClick();
		showNextAd(detailView);
		
		//Ad2
		String title2 = findElementById(AD_DETAILVIEW_TITLE_ID, TextViewElement.class).getText();
		Log.i(LOG_TAG, "title2:" + title2);
		assertFalse(title1.equals(title2));

		showNextAd(detailView);
		//Ad3
		String title3 = findElementById(AD_DETAILVIEW_TITLE_ID, TextViewElement.class).getText();
		Log.i(LOG_TAG, "title3:" + title3);
		assertFalse(title2.equals(title3));

		showPrevAd(detailView);
		//Ad2
		Log.i(LOG_TAG, "titleX:" + findElementById(AD_DETAILVIEW_TITLE_ID, TextViewElement.class).getText());
		assertEquals(title2, findElementById(AD_DETAILVIEW_TITLE_ID, TextViewElement.class).getText());
		
	}
	
	public void testHistory() throws Exception {
		deleteAllHistoryAds();
		BXViewGroupElement detailView = showAd(1, 0, 2);
		int showCount = 1;
		for (;showCount < 5; showCount++) {
			showNextAd(detailView);
		}
		showPrevAd(detailView);
		
		getDevice().pressBack();
		getDevice().pressBack();
		getDevice().pressBack();
		
		showCount--;//todo bug:showCount 少了一个计数
		assertEquals(showCount, showMyAdList(MY_LISTING_HISTORY_ID, MY_LISTING_HISTORY_COUNTER_ID)); 
		
		assertNotNull(openAdByIndex(1, MY_AD_FxH_VIEWLIST_ID));
		BXViewGroupElement detailMyView = findElementById(AD_DETAILVIEW_ID,
				BXViewGroupElement.class);
		String title1 = findElementById(AD_DETAILVIEW_TITLE_ID, TextViewElement.class).getText();
		Log.i(LOG_TAG, "title1my:" + title1);
		showNextAd(detailMyView);
		
		String title2 = findElementById(AD_DETAILVIEW_TITLE_ID, TextViewElement.class).getText();
		Log.i(LOG_TAG, "title2my:" + title2);
		assertFalse(title1.equals(title2));
	}
	
}
