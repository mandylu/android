package com.quanleimu.activity.test;

import org.athrun.android.framework.AthrunTestCase;
import org.athrun.android.framework.Test;


public class PostViewTest extends BaixingTestCase {
	
	public PostViewTest() throws Exception {
	}
	
	@Test
	public void testNewPostChecking() throws Exception {
		
		//android3.0
		//home页点击右上方“发布”按钮
		//选择类目“兼职招聘”>"学生兼职／实习"
		//输入“联系方式”“具体地点”“标题”“工资”“工作地点”“发布人”
		//点击右上方按钮“完成”
		//等待5s
		//检查是否自动到“已发布的信息页面”（检查页面title文字为“已发布的信息”）
		//点击第一个发布的信息进入
		//检查该信息标题，确保标题＝简历标题
		
	}
	
	@Test
	public void testNewPostPhoto() throws Exception {
		
		//android3.0
		//home页点击右上方“发布”按钮
		//点击类目“生活服务”>"摄影"
		//检查页面title为“发布”
		//点击拍照按钮
		//检查弹出页，包含“相册”“拍照”“取消”
		//选择拍照button
		//检查拍照页面弹出
		//点击手机自带的返回键
		//检查页面title文字，应为“发布”
		//再次点击拍照按钮
		//检查弹出页，包含“相册”“拍照”“取消”
		//选择相册button
		//检查相册选择页面弹出
		//点击手机自带的返回键
		//检查页面title文字，应为“发布”
		//再次点击拍照按钮
		//检查弹出页，包含“相册”“拍照”“取消”
		//选择取消button
		//检查页面title文字，应为“发布”
							
	}
}
