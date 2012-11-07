package com.quanleimu.activity.test;

import org.athrun.android.framework.AthrunTestCase;
import org.athrun.android.framework.Test;

import java.util.concurrent.TimeUnit;
import java.util.ArrayList;

import android.test.TouchUtils;
import android.view.View;
import android.util.Log;

import org.athrun.android.framework.viewelement.AbsListViewElement;
import org.athrun.android.framework.viewelement.TextViewElement;
import org.athrun.android.framework.viewelement.ViewCoordinate;
import org.athrun.android.framework.viewelement.ViewElement;
import org.athrun.android.framework.viewelement.ViewGroupElement;
import org.athrun.android.framework.viewelement.ScrollViewElement;
import org.athrun.android.framework.viewelement.ViewUtils;


public class MainActivityTest extends BaixingTestCase {
	private static final String SCREEN_LOG_TAG = "TestKeyGuardTest";
	
	public MainActivityTest() throws Exception {
	}
	
	@Test
	public void simpleTest() throws Exception {
		System.out.println("test start");
		assertTrue("test", false);

		System.out.println("test end");
	}
	
	@Test
	public void testSearchClick() throws Exception {
		doSearch("iphone");
		goBack();
		doSearch("ipad");
		goBack();
		goBack();
		doSearch("mac book");
		//getDevice().pressMenu();
		TimeUnit.SECONDS.sleep(2);
		goBack();
		goBack();
		selectSearch("ipad");
		
		assertNull(findElementById(SEARCH_TEXTVIEW_ID));//todo
		goBack();
		goBack();
		doSearch("");
		selectSearch(SEARCH_DELETE_TEXT);
		assertNotNull(findElementById(SEARCH_TEXTVIEW_ID));
		//assertNotNull(findElementById(SEARCH_BUTTON_ID));
		try {
			findElementById("ipad");//稍微复杂了点，因为上面那种会出错
			assertTrue(false);
		} catch (NoSuchFieldException ex) {
		}
		
		doSearch("android");
	}
	
	@Test
	public void testCategoryClick() throws Exception {
		openCategoryByIndex(2, 1);
		goBack();
		TimeUnit.SECONDS.sleep(1);
		goBack();
		TimeUnit.SECONDS.sleep(1);
	}
	
	@Test
	public void testPost() throws Exception {
		openPostCategory(0, 0);
	}
	
	@Test 
	public void testPostData() throws Exception {
		openPostCategory(1, 6);//车辆买卖， 汽车用品
		//openPostItemByIndex(0);
		//selectMetaByName("宝山");
		//selectMetaByName("全部");
		//openPostItemByIndex(1);
		//selectMetaByName("个人");
		//setMetaByName("价格", "500");
		setMetaByName("联系方式", TEST_DATA_MOBILE);
		setMetaByName("具体地点", "测试具体地点测试");
		setMetaByName("补充说明", "时代单位的得到搜索俄文存储得到力量存储的");

		assertNotNull(findElementByText("时代单位的得到搜索俄文存储得到力量存储的"));
		assertNotNull(findElementByText(TEST_DATA_MOBILE));
		
		//更多
		//openPostItemByIndex(4);
		//assertNotNull(setOtherMetaByIndex(0, "100"));
		//postOtherDone();
		
		postSend();
		
		deleteAdByText("时代单位的得到搜索俄文存储得到力量存储的");
	}
	
	public void testPostPhoto() throws Exception {
		openTabbar(TAB_ID_POST);
		openPostCategory(3, 2);
		TimeUnit.SECONDS.sleep(1);
		doClickPostPhoto();
		TimeUnit.SECONDS.sleep(5);
		ViewElement v = findElementByText(MSGBOX_CANCEL_TEXT, 0, true);
		assertNotNull(v);
		v.doClick();
	}
	
	@Test
	public void testMy() throws Exception {
		logout();
		//TODO
		logon();
		//TODO
		openMyGridByText(MY_LISTING_MYAD_TEXT);
		logout();
	}
	
	@Test
	public void testAdViewTouch() throws Exception {
		BXViewGroupElement detailView = showAd(0, 0, 0);
		assertNotNull(detailView);
	
		adViewPicTouch();
		TextViewElement titleView = findElementById(AD_DETAILVIEW_TITLE_ID, TextViewElement.class);
		assertNotNull(titleView);
		//Ad1
		String title1 = titleView.getText();
		//Log.i(LOG_TAG, "title1:" + title1);
		//ViewElement next = findElementById(AD_DETAILVIEW_NEXT_ID);
		//assertNotNull(next);
		//next.doClick();
		
		showNextView(AD_DETAILVIEW_ID);
		adViewPicTouch();
		//Ad2
		String title2 = findElementById(AD_DETAILVIEW_TITLE_ID, TextViewElement.class).getText();
		//Log.i(LOG_TAG, "title2:" + title2);
		assertFalse(title1.equals(title2));

		showNextView(AD_DETAILVIEW_ID);
		adViewPicTouch();
		//Ad3
		String title3 = findElementById(AD_DETAILVIEW_TITLE_ID, TextViewElement.class).getText();
		//Log.i(LOG_TAG, "title3:" + title3);
		assertFalse(title2.equals(title3));

		showPrevView(AD_DETAILVIEW_ID);
		//Ad2
		//Log.i(LOG_TAG, "titleX:" + findElementById(AD_DETAILVIEW_TITLE_ID, TextViewElement.class).getText());
		assertEquals(title2, findElementById(AD_DETAILVIEW_TITLE_ID, TextViewElement.class).getText());
		adViewPicTouch();
		
	}

	public void testHistory() throws Exception {
		deleteAllHistoryAds();
		BXViewGroupElement detailView = showAd(1, 0, 2);
		int showCount = 1;
		for (;showCount < 5; showCount++) {
			showNextView(AD_DETAILVIEW_ID);
		}
		showPrevView(AD_DETAILVIEW_ID);
		
		goBack();
		goBack();
		goBack();
		
		openMyGridByText(MY_LISTING_HISTORY_TEXT);
		
		assertNotNull(openAdByItemIndex(1));
		
		String title1 = findElementById(AD_DETAILVIEW_TITLE_ID, TextViewElement.class).getText();
		Log.i(LOG_TAG, "title1my:" + title1);
		showNextView(AD_DETAILVIEW_ID);
		
		String title2 = findElementById(AD_DETAILVIEW_TITLE_ID, TextViewElement.class).getText();
		Log.i(LOG_TAG, "title2my:" + title2);
		assertFalse(title1.equals(title2));
	}
	
	@Test
	public void testScrollToNextScreen() throws Exception {
		logon();
		openTabbar(TAB_ID_POST);
		openPostFirstCategory(0);
		AbsListViewElement catListView = findElementById(CATEGORY_SECOND_GRIDVIEW_ID,
				AbsListViewElement.class);
		assertEquals(30,  catListView.getLastVisiblePosition());
		catListView.scrollToNextScreen();
		TimeUnit.SECONDS.sleep(3);
		assertEquals(0, catListView.getFirstVisiblePosition());
		catListView.scrollToNextScreen();
		assertEquals(30, catListView.getLastVisiblePosition());
		TimeUnit.SECONDS.sleep(3);
		assertEquals(0, catListView.getFirstVisiblePosition());
	}
	
}
