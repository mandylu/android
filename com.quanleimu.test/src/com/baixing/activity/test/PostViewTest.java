package com.baixing.activity.test;

import java.util.concurrent.TimeUnit;

import org.athrun.android.framework.AthrunDevice;
import org.athrun.android.framework.AthrunTestCase;
import org.athrun.android.framework.Test;

import android.os.Build;
import android.view.KeyEvent;


public class PostViewTest extends BaixingTestCase {

	public static String postDataXuesheng[][] = {
		{"CATEGORY", "4", "学生兼职/实习"}, //兼职招聘
		{"TEXT", "联系电话", TEST_DATA_MOBILE},
		{"TEXT", "具体地点", "测试具体地点测试"},
		{"TITLE", "描述", "兼职测试演员"},
		{"TEXT", "工资", "50"},
	};
	private static final String TEST_DATA_SHEYING = "摄影";
	public static String postDataSheying[][] = {
		{"CATEGORY", "8", TEST_DATA_SHEYING}, //生活服务
		{"TEXT", "联系电话", TEST_DATA_MOBILE},
		{"TEXT", "具体地点", "测试具体地点测试"},
		{"TITLE", "描述", "礼品定制测试标题"},
		{"TEXT", "公司名称", "测试公司"}
	};
	
	public PostViewTest() throws Exception {
	}
	
	@Test
	public void testPostChecking() throws Exception {
		openTabbar(TAB_ID_POST_TEXT);
		//android3.0
		//home页点击右上方“发布”按钮
		//选择类目“兼职招聘”>"学生兼职／实习"
		//输入“联系方式”“具体地点”“标题”“工资”“工作地点”“发布人”
		//点击右上方按钮“完成”
		String title = doPostByData(postDataXuesheng);
		//等待5s
		//检查是否自动到“已发布的信息页面”（检查页面title文字为“已发布的信息”）
		assertElementByText(MY_LISTING_TITLE_TEXT);
		//点击第一个发布的信息进入
		if (title.length() > 0) {
			//检查该信息标题，确保标题＝简历标题
			//TODO 版规问题，如果版规挡住了，没法验证
			//assertNotNull(findElementByText(title, 0, true));
			
			//恢复数据
			deleteAdByText(title);
		}
	}
	
	@Test
	public void testPostPhoto() throws Exception {
		
		//android3.0
		//home页点击右上方“发布”按钮
		openTabbar(TAB_ID_POST_TEXT);
		//点击类目“生活服务”>"摄影"
		String title = postEnterData(postDataSheying);
		//检查页面title为“发布”
		assertElementByText(TEST_DATA_SHEYING);
		assertElementByText(POST_SEND);
		//点击拍照按钮
		doClickPostPhoto();
		//检查弹出页，包含“相册”“拍照”“取消”
		//选择拍照button
		clickByText(POST_CAMERA_PHOTO_TEXT);//TODO 手机没有自带返回键或无效
		//检查拍照页面弹出
		//点击手机自带的返回键
		sleep(1);
		waitClickCamera();
		//goBack(); //TODO AthrunDevice.pressBackAcrossApp();
		//检查页面title文字，应为“发布”
		sleep(1);
		assertElementByText(TEST_DATA_SHEYING);
		assertElementByText(POST_SEND);
		//再次点击拍照按钮
		doClickPostPhoto();
		//检查弹出页，包含“相册”“拍照”“取消”
		//选择相册button
		clickByText(POST_GALLERY_PHOTO_TEXT);
		//检查相册选择页面弹出
		//点击手机自带的返回键
		sleep(1);
		waitSendKey(KeyEvent.KEYCODE_BACK);
		//goBack(); //TODO AthrunDevice.pressBackAcrossApp();
		//检查页面title文字，应为“发布”
		assertElementByText(TEST_DATA_SHEYING);
		assertElementByText(POST_SEND);
		//再次点击拍照按钮
		doClickPostPhoto();
		//检查弹出页，包含“相册”“拍照”“取消”
		//选择取消button
		clickByText(MSGBOX_CANCEL_TEXT);
		//检查页面title文字，应为“发布”
		assertElementByText(TEST_DATA_SHEYING);
		assertElementByText(POST_SEND);
							
	}
}