package com.quanleimu.activity.test;

import org.athrun.android.framework.AthrunTestCase;
import org.athrun.android.framework.Test;


public class PostViewTest extends BaixingTestCase {
	private static final String LOG_TAG = "MainActivityTest";
	
	public PostViewTest() throws Exception {
		super();
		AthrunTestCase.setMaxTimeToFindView(10000);
	}
	
	@Test
	public void testPostChecking() throws Exception {
		
		//进入发布页面
		//选择任意类目，输入完所有必填项
		//记录描述信息
	    //点击发布
		//等待10s
		//点击“我的”>已发布的信息
		//检查页面title文字为“已发布的信息”
		//点击第一个发布信息进入
		//检查该信息desctiption，确保它登录记录的描述信息
		
	}
	
	@Test
	public void testPostPhoto() throws Exception {
		
		//进入发布页面
		//检查页面title文字，应为“发布”
		//点击拍照按钮
		//检查弹出页，包含“相册”“拍照”“取消”
		//选择拍照button
		//检查拍照页面弹出
		//点击返回
		//检查页面title文字，应为“发布”
		
	}
}
