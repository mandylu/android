package com.baixing.activity.test;

import org.athrun.android.framework.AthrunTestCase;
import org.athrun.android.framework.Test;
import org.athrun.android.framework.viewelement.TextViewElement;
import org.athrun.android.framework.viewelement.ViewElement;
import android.content.res.Resources;
import android.util.Log;

public class FavoriteTest extends BaixingTestCase {
	public static final String TEST_DATA_CATEGORY_ZUFANG = "租房";
	
	public FavoriteTest() throws Exception {
	}
	
	@Test
	public void testFavoriteAd() throws Exception {
		
	   //android3.0
	   //进入类目，房屋租售>租房
		openTabbar(TAB_ID_HOME_TEXT);
		openCategoryByIndex(3, 0);
	   //检查列表的title view文字部分包含“租房”
		ViewElement v = findElementByText(TEST_DATA_CATEGORY_ZUFANG);
	   //任选一个信息进入
		assertNotNull(openAdByIndex(null, random(6)));//任一
	   //检查右上方按钮为收藏前图片（空心五角星）
		//BXImageViewElement iv = findElementById(AD_FAVORITE_BUTTON_ID, BXImageViewElement.class);
		//assertTrue(iv.checkImageByName(AD_FAVORITE_ADD_IMG));
		TextViewElement iv = findElementByText(AD_FAVORITE_BUTTON_TEXT);
		assertNotNull(iv);
	   //点击右上方icon收藏
		clickView(iv);
	   //检查右上方button文字为收藏后图片（实心五角星）
		//assertTrue(iv.checkImageByName(AD_FAVORITE_REMOVE_IMG));
		assertElementByText(AD_FAVORITE_CANCEL_TEXT);
	   //记录当前信息的标题，如”标题1“
		String title1 = getTextByElementId(AD_DETAILVIEW_TITLE_ID);
	   //当前翻页至下一页
		showNextView(AD_DETAILVIEW_ID);
	   //点击右上方收藏
		//clickById(AD_FAVORITE_BUTTON_ID, BXImageViewElement.class);
		clickByText(AD_FAVORITE_BUTTON_TEXT);
	   //翻页至下一页
		showNextView(AD_DETAILVIEW_ID);
	   //点击右上方收藏
		//clickById(AD_FAVORITE_BUTTON_ID, BXImageViewElement.class);
		clickByText(AD_FAVORITE_BUTTON_TEXT);
	   //检查右上方button文字为收藏后图片（实心五角星）
		//assertTrue(iv.checkImageByName(AD_FAVORITE_REMOVE_IMG));
		assertElementByText(AD_FAVORITE_CANCEL_TEXT);
	   //记录当前信息的标题，如”标题2“
		String title2 = getTextByElementId(AD_DETAILVIEW_TITLE_ID);
	   //翻至前一页，点击右上方icon取消收藏
		showPrevView(AD_DETAILVIEW_ID);
		//iv = clickById(AD_FAVORITE_BUTTON_ID, BXImageViewElement.class);
		clickByText(AD_FAVORITE_CANCEL_TEXT);
	   //检查右上方按钮为收藏前图片（空心五角星）
		//assertTrue(iv.checkImageByName(AD_FAVORITE_ADD_IMG));
		assertElementByText(AD_FAVORITE_BUTTON_TEXT);
	   //点击返回
		goBack();
	   //点击返回
		goBack(true);
	   //点击返回
		goBack();
	   //点击我的百姓网>收藏
		openTabbar(TAB_ID_MY_TEXT);
		openMyGridByText(MY_LISTING_FAVORITE_TEXT);
	   //检查前两个信息的收藏顺序，顺序应为“标题2”,“标题1”
		assertNotNull(openAdByItemIndex(0));
		assertEquals(title2, getTextByElementId(AD_DETAILVIEW_TITLE_ID));
		
		//恢复数据
		//clickById(AD_FAVORITE_BUTTON_ID, BXImageViewElement.class);
		clickByText(AD_FAVORITE_CANCEL_TEXT);
		goBack();
		assertNotNull(openAdByItemIndex(0));
		//clickById(AD_FAVORITE_BUTTON_ID, BXImageViewElement.class);
		clickByText(AD_FAVORITE_CANCEL_TEXT);
	}
}
