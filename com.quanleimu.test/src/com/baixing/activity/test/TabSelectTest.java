package com.baixing.activity.test;

import java.util.concurrent.TimeUnit;

import org.athrun.android.framework.viewelement.TextViewElement;
import org.athrun.android.framework.viewelement.ViewElement;

public class TabSelectTest extends BaixingTestCase {

	public TabSelectTest() throws Exception {
		super();
	}
	
	public void testHomePersonalSwitch() throws Exception
	{
		doSearch("ipad");
		TimeUnit.SECONDS.sleep(2);
		
		TextViewElement v = findElementById(VIEW_TITLE_ID, TextViewElement.class);
		assertNotNull("title should not be null after serach", v);
		assertEquals("平板电脑/iPad", v.getText());
		
		openTabbar(TAB_ID_MY_TEXT);
		v = findElementById(VIEW_TITLE_ID, TextViewElement.class);
		assertEquals("个人中心", v.getText());
		
		openTabbar(TAB_ID_HOME_TEXT);
		v = findElementById(VIEW_TITLE_ID, TextViewElement.class);
		assertEquals("平板电脑/iPad", v.getText());
		
	}
	
	

}
