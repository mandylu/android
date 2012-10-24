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
		// android3.0
		//点击物品交易>手机号码
		//检查listing信息不为空
		//向下浏览30个信息
		//检查底部提示：点击载入下30条
		//向下拖动
		//向下浏览30个信息
		//检查底部提示：点击载入下30条
	    //向下拖动
		//检查底部提示：点击载入下30条
	    //向下拖动
		//页面展开后，点击最后一个信息进入
		//从viewad页面返回至listing页面
		//继续三次返回至一级类目
	    //检查当前页的tab bar上的文字是“浏览信息”
		//进入车辆买卖>本地下线车
		//同上手机号码listing页面操作方式
		
	}
	
	@Test
	public void testViewPic() throws Exception {
	
		//android2.7.2
		//进入设置>列表模式
		//记录模式信息（图片 or 文字）
		//点击返回
		//切换到home
		//进入任意二级类目listing页面
		//模式信息＝图片
		//检查每条信息带图片展示
		//模式信息＝文字
		//检查每条信息不带图片展示
		
	}
	
	@Test
	public void testNewViewPic() throws Exception {
		
		//android3.0
		//点击我的百姓网>设置>流量优化设置
		//点击图片模式
		//点击返回
		//点击浏览信息
		//点击物品交易>台式电脑
		//检查listing信息为带图片展示
		//点击返回
		//点击返回
		//点击我的百姓网>设置>流量优化设置
		//点击省流量模式
		//点击返回
		//点击浏览信息
		//点击物品交易>台式电脑
		//检查listing信息为不带图片展示
		
}
}