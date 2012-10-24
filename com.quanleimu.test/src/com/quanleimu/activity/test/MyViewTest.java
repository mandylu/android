package com.quanleimu.activity.test;

import org.athrun.android.framework.AthrunTestCase;
import org.athrun.android.framework.Test;


public class MyViewTest extends BaixingTestCase {
	private static final String LOG_TAG = "MainActivityTest";
	
	public MyViewTest() throws Exception {
		super();
		AthrunTestCase.setMaxTimeToFindView(10000);
	}
	
	@Test
	
	public void testMyProfile() throws Exception {
		
		//android2.7.2
		//进入“我的”
		//检查是否包含登录按钮
		//1.包含 2.不包含
		//若为1,输入正确用户名，密码
		//点击登录
		//等待3s
		//点击编辑按钮进入个人资料
	    //点击用户名，清空
		//检查，确保用户名＝Null
		//点击用户名，输入“tester”
		//点击性别
		//检查弹出框，包含两个可选项“男”“女”
		//点击“女”
		//点击城市
		//检查页面title＝“选择常居地”
		//点击“北京”
		//检查页面title＝“北京”
		//点击“朝阳”
		//检查页面title＝"朝阳"
		//点击“西坝河”
		//点击完成
		//检查结果：用户名＝“tester”，性别＝“女”，城市＝“北京”
		
	}
	
	@Test
	
	public void testNewMyProfile() throws Exception {
		
		//android3.0
		//进入“我的百姓网”页面
		//检查是否包含登录按钮
		//若包含登录按钮,则输入正确用户名，密码
		//点击登录
		//等待3s
		//检查是否包含登录按钮，确保登录成功
		//点击编辑按钮进入个人资料
	    //点击用户名，清空
		//检查，确保用户名＝Null
		//点击用户名，输入“tester”
		//点击完成
		//检查结果：用户名＝“tester”
		
	}
}
