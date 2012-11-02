package com.quanleimu.activity.test;

import java.util.concurrent.TimeUnit;

import org.athrun.android.framework.AthrunTestCase;
import org.athrun.android.framework.Test;
import org.athrun.android.framework.viewelement.ViewElement;


public class CityViewTest extends BaixingTestCase {
	public static final String TEST_DATA_APP_NAME_TEXT = "百姓网";
	public static final String TEST_DATA_CITY_SHENZHEN = "深圳";
	
	public CityViewTest() throws Exception {
		super();
		AthrunTestCase.setMaxTimeToFindView(10000);
	}
	
	@Test
	public void testCityView() throws Exception {
		
		//android3.0
		//home页点击左上方按钮“**百姓网”
		ViewElement v = findElementById(HOME_APP_NAME_ID);
		assertNotNull(v);
		v.doClick();
		TimeUnit.SECONDS.sleep(1);
		//检查title文字为“选择城市”
		v = findElementByText(CITY_SELECT_TEXT);
		assertNotNull(v);
		//点击深圳
		v = findElementByText(TEST_DATA_CITY_SHENZHEN);
		assertNotNull(v);
		v.doClick();
		TimeUnit.SECONDS.sleep(1);
		//检查home title为“深圳百姓网”
		v = findElementByText(TEST_DATA_CITY_SHENZHEN);
		assertNotNull(v);
		v = findElementByText(TEST_DATA_APP_NAME_TEXT);
		assertNotNull(v);
		//再次在home页点击左上方按钮“深圳百姓网”
		//滑动当前页至下方，点击“选择其他城市”
		//检查title为“选择省份”
		//点击返回
		//检查title为“选择城市”
		//再次滑动当前页至下方，点击“选择其他城市”
		//点击“浙江”
		//检查title为“选择城市”
		//点击“杭州”
		//检查home title为“杭州百姓网”
		
	}
}