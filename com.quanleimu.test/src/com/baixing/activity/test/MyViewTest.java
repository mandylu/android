package com.baixing.activity.test;

import java.util.concurrent.TimeUnit;

import org.athrun.android.framework.AthrunTestCase;
import org.athrun.android.framework.Test;
import org.athrun.android.framework.viewelement.TextViewElement;


public class MyViewTest extends BaixingTestCase {
	
	public MyViewTest() throws Exception {
	}
	
	@Test
	public void testMyProfile() throws Exception {
		
		//android3.0
		//进入“我的百姓网”页面
		openTabbar(TAB_ID_HOME_TEXT);
		logout();
		//检查是否包含登录按钮
		assertNoElementById(MY_PROFILE_EDIT_BUTTON_ID);
		//若包含登录按钮,则输入正确用户名，密码
		//点击登录
		//等待3s
		logon();
		sleep(2);
		//检查是否包含登录按钮，确保登录成功
		assertElementById(MY_PROFILE_EDIT_BUTTON_ID);
		//点击编辑按钮进入个人资料
		clickById(MY_PROFILE_EDIT_BUTTON_ID);
	    //点击用户名，清空
		TextViewElement tv = findElementById(MY_PROFILE_EDIT_USERNAME_ID, TextViewElement.class);
		String oldName = tv.getText();
		tv.clearText();
		//检查，确保用户名＝Null
		assertEquals(tv.getText(), "");
		//点击用户名，输入“tester”
		String tmpName = "tester" + String.valueOf(random(100000));
		tv.inputText(tmpName);
		//点击完成
		clickByText(MY_PROFILE_EDIT_UPDATE_TEXT, true);
		sleep(1);
		//检查结果：用户名＝“tester”
		assertEquals(findElementById(MY_PROFILE_USERNAME_ID, TextViewElement.class).getText(), tmpName);
		
		//恢复数据
		clickById(MY_PROFILE_EDIT_BUTTON_ID);
		tv = findElementById(MY_PROFILE_EDIT_USERNAME_ID, TextViewElement.class);
		tv.setText(oldName);
		clickByText(MY_PROFILE_EDIT_UPDATE_TEXT, true);
	}
}
