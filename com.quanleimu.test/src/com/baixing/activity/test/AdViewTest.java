package com.baixing.activity.test;

import java.util.concurrent.TimeUnit;

import org.athrun.android.framework.AthrunTestCase;
import org.athrun.android.framework.Test;
import org.athrun.android.framework.viewelement.TextViewElement;
import org.athrun.android.framework.viewelement.ViewElement;

import android.util.Log;


public class AdViewTest extends BaixingTestCase {
	public static final String TEST_DATA_CATEGORY_NZN = "女找男";
	public static final String TEST_DATA_CATEGORY_QICHE = "二手轿车";
	
	public AdViewTest() throws Exception {
		
	}
		
	@Test
	public void testPicSave() throws Exception {
		
		//android3.0
		//首页点击交友活动>女找男
		openTabbar(TAB_ID_HOME_V3);
		openCategoryByIndex(6, 1);
		//检查列表的title view文字部分包含“女找男”
		ViewElement v = findElementByText(TEST_DATA_CATEGORY_NZN);
		//assertNotNull(v);
		v = savePhoto(6, 1);
		assertNotNull(v); // clickByText(AD_BIG_IMAGE_SAVE_TEXT);
		//检查弹出式提示信息，包含“成功”
		assertEquals(true, waitForSubText(AD_BIG_IMAGE_SAVED_TEXT, 1000));
		goBack();
	}
	
	@Test
	public void testMapView() throws Exception {
			
		//android3.0
	    //进入类目车辆买卖>二手轿车
		openTabbar(TAB_ID_HOME_V3);
		openCategoryByIndex(1, 0);
		//检查列表的title view文字部分包含"二手轿车"
		ViewElement v = findElementByText(TEST_DATA_CATEGORY_QICHE);
		assertNotNull(v);
		//选择一个不带图信息进入
		openAdWithPic(false);
		//提取当前信息的地区地点信息，如“浦东金桥”
		TextViewElement tv = findDetailViewMetaByName(AD_DETAIL_META_AREA_TEXT);
		assertNotNull(tv);
		checkMap();
		//选择一个带图的信息进入
		openAdWithPic(true);
		//提取当前信息的地区地点信息，如“徐汇交大”
		tv = findDetailViewMetaByName(AD_DETAIL_META_AREA_TEXT);
		checkMap();
	}
	
	private void checkMap() throws Exception {
		//提取当前信息的地区地点信息，如“浦东金桥”
		TextViewElement tv = findDetailViewMetaByName(AD_DETAIL_META_AREA_TEXT);
		assertNotNull(tv);
		if (tv != null) {
			//点击地图查看
			tv.doClick();
			TimeUnit.SECONDS.sleep(1);
			assertNull(findElementByText(TEST_DATA_CATEGORY_QICHE));
			//检查页面title包含当前地区地点文字“金桥”
			String area = tv.getText();
			tv = findElementById(VIEW_TITLE_ID, TextViewElement.class);
			assertNotNull(tv);
			boolean found = false;
			assertTrue("not found area:" + area, area.indexOf(tv.getText()) != -1);
		}
		//点击返回
		goBack();
		//点击返回
		goBack();
	}
		
}
