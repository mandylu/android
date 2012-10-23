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
		//选择类目求职简历>兼职求职简历
		//输入求职意向>家教，会计，设计，充场/座谈会
		//输入发布人>公司
		//输入姓名 “张三”
		//输入简历标题“测试简历”
		//点击右上方按钮“立即发布”
		//等待10s
		//点击“我的”>已发布的信息
		//检查页面title文字为“已发布的信息”
		//点击第一个发布信息进入
		//检查该信息标题，确保标题＝简历标题
		
	}
	
	@Test
	public void testPostPhoto() throws Exception {
		
		//进入发布页面
		//检查页面title文字，应为“发布”
		//点击拍照按钮
		//检查弹出页，包含“相册”“拍照”“取消”
		//选择拍照button
		//检查拍照页面弹出
		//点击手机自带的返回键
		//检查页面title文字，应为“发布”
		
	}
}
