package com.quanleimu.activity.test;

import org.athrun.android.framework.AthrunTestCase;
import org.athrun.android.framework.Test;


public class AdViewTest extends BaixingTestCase {
	private static final String LOG_TAG = "MainActivityTest";
	
	public AdViewTest() throws Exception {
		super();
		AthrunTestCase.setMaxTimeToFindView(10000);
	}
	
	@Test
	
public void testPicSave() throws Exception {
		
		//android2.7.2
		//首页点击交友活动>女找男
		//检查列表的title view文字部分包含“女找男”
		//选择一个带图信息进入
		//点击图片
		//检查title右侧包含button“保存”
		//点击左上方button返回
		//点击图片再次进入
		//点击右上方按钮保存
		//检查弹出式提示信息，包含“成功”
		
	}
	
	@Test
	
	public void testMapView() throws Exception {
			
		//android2.7.2
	    //进入类目车辆买卖>二手轿车
		//检查列表的title view文字部分包含"二手轿车"
		//选择一个不带图信息进入
		//提取当前信息地区地点信息，如“浦东金桥”
		//点击地图查看
		//检查页面title包含当前地区地点文字“金桥”
	    //检查地图正文部分，文字包含文字“金桥”
		}
		
	@Test
	
public void testNewPicSave() throws Exception {
		
		//android3.0
		//首页点击交友活动>女找男
		//检查列表的title view文字部分包含“女找男”
		//选择一个带图信息进入
		//点击图片
		//检查title右侧包含button“保存”
		//点击左上方button返回
		//点击图片再次进入
		//点击右上方按钮保存
		//检查弹出式提示信息，包含“成功”
		
	}
	
	@Test
	
	public void testNewMapView() throws Exception {
			
		//android3.0
	    //进入类目车辆买卖>二手轿车
		//检查列表的title view文字部分包含"二手轿车"
		//选择一个不带图信息进入
		//提取当前信息的地区地点信息，如“浦东金桥”
		//点击地图查看
		//检查页面title包含当前地区地点文字“金桥”
	    //检查地图正文部分，文字包含文字“金桥”
		//点击返回
		//点击返回
		//选择一个带图的信息进入
		//提取当前信息的地区地点信息，如“徐汇交大”
		//点击地图查看
		//检查页面title包含当前地区地点文字“交大”
	    //检查地图正文部分，文字包含文字“交大”
		}
		
}
