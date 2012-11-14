package com.quanleimu.activity.test;

import java.util.concurrent.TimeUnit;

import org.athrun.android.framework.AthrunTestCase;
import org.athrun.android.framework.Test;
import org.athrun.android.framework.viewelement.AbsListViewElement;
import org.athrun.android.framework.viewelement.TextViewElement;
import org.athrun.android.framework.viewelement.ViewElement;
import org.athrun.android.framework.viewelement.ViewGroupElement;

import android.util.Log;


public class AdListingViewTest extends BaixingTestCase {
	
	public AdListingViewTest() throws Exception {
	}
	
	@Test
	public void testViewListing() throws Exception {
		// android3.0
		//点击物品交易>手机号码
		viewListing(0, 1);
		//进入车辆买卖>本地下线车
		viewListing(1, 2);
		
	}
	
	private void viewListing(int first, int second) throws Exception {
		BXViewGroupElement detailView = showAd(first, second, 0);
		//检查listing信息不为空
		assertNotNull(detailView);
		//向下浏览30个信息
		goBack();

		//验证是 Listing 页面
		BXViewGroupElement lv = findElementById(AD_VIEWLIST_ID, BXViewGroupElement.class);
		assertNotNull(lv);
		//向下滚动
		ViewElement footer = scrollAdListViewToFooter(lv);
		assertNotNull(footer);
		TimeUnit.SECONDS.sleep(2);
		//试图点击（有可能已经自动加载了）
		footer = findElementById(AD_VIEWLIST_MORE_ID);
		if (footer != null) footer.doClick();
		TimeUnit.SECONDS.sleep(1);
	    //向下拖动
		footer = scrollAdListViewToFooter(lv);
		assertNotNull(footer);
		TimeUnit.SECONDS.sleep(1);
		//页面展开后，点击最后一个信息进入
		ViewGroupElement av = null;
		for(int i = 8; i > 0; i--) {
			av = openAdByIndex(i);
			if (av != null) break;
		}
		assertNotNull(av);
		TimeUnit.SECONDS.sleep(1);
		//从viewad页面返回至listing页面
		goBack(true);
		//继续三次返回至一级类目
		goBack(true);
		goBack(true);
	    //检查当前页的tab bar上的文字是“浏览信息”
		TextViewElement t = findElementById(TAB_ID_HOME_V3, TextViewElement.class);
		assertNotNull(t);
		assertEquals(t.getText(), TAB_ID_HOME_TEXT);
	}
	
	@Test
	public void testViewPic() throws Exception {
		
		//android3.0
		//点击我的百姓网>设置>流量优化设置
		setAdListingViewType(MY_SETTING_VIETTYPE_PIC_TEXT);
		//点击返回
		goBack();
		//点击浏览信息
		openTabbar(TAB_ID_HOME_V3);
		//点击物品交易>台式电脑
		openCategoryByIndex(0, 4);
		//检查listing信息为带图片展示
		ViewElement v = findElementById(AD_VIEWLIST_ITEM_IMAGE_ID);
		assertNotNull(v);
		//点击返回
		goBack();
		//点击返回
		goBack();
		//点击我的百姓网>设置>流量优化设置
		setAdListingViewType(MY_SETTING_VIETTYPE_NO_PIC_TEXT);
		//点击返回
		goBack();
		//点击浏览信息
		openTabbar(TAB_ID_HOME_V3);
		//点击物品交易>台式电脑
		openCategoryByIndex(0, 4);
		//检查listing信息为不带图片展示
		v = findElementById(AD_VIEWLIST_ITEM_IMAGE_ID);
		assertNull(v);
		goBack();
		goBack();
		setAdListingViewType(MY_SETTING_VIETTYPE_PIC_TEXT);
		
	}
}