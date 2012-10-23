package com.quanleimu.activity.test;

import org.athrun.android.framework.AthrunTestCase;
import org.athrun.android.framework.Test;


public class AdListingViewTest extends BaixingTestCase {
	private static final String LOG_TAG = "MainActivityTest";
	
	public AdListingViewTest() throws Exception {
		super();
		AthrunTestCase.setMaxTimeToFindView(10000);
	}
	
	@Test
	public void testViewListing() throws Exception {
		
		// 进入物品交易>手机号码listing页面
		//检查listing信息不为空
		//向下浏览30个信息
		//检查底部提示：正在加载更多，请稍候
		//向下浏览到第100个信息，进入
		//检查viewad个数显示100／120
		//从viewad页面返回至listing页面
		//继续两次返回至一级类目
	    //检查home title包含百姓网字样
		//进入车辆买卖>本地下线车
		//同上手机号码listing页面操作方式
		
	}
	
	@Test
	public void testViewPic() throws Exception {
		
		// 进入设置>列表模式
		//记录模式信息（图片 or 文字）
		//点击返回
		//切换到home
		//进入任意二级类目listing页面
		//模式信息＝图片
		//检查每条信息带图片展示
		//模式信息＝文字
		//检查每条心西不带图片展示
		
	}
}
