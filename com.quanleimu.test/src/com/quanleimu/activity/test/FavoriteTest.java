package com.quanleimu.activity.test;

import org.athrun.android.framework.AthrunTestCase;
import org.athrun.android.framework.Test;


public class FavoriteTest extends BaixingTestCase {
	private static final String LOG_TAG = "MainActivityTest";
	
	public FavoriteTest() throws Exception {
		super();
		AthrunTestCase.setMaxTimeToFindView(10000);
	}
	
	@Test
	
	public void testFavoriteAd() throws Exception {
		
	   //进入类目，房屋租售>租房
	   //检查列表的title view文字部分包含“租房”
	   //任选一个信息进入
	   //检查右上方button文字为“收藏”
	   //点击收藏
	   //检查右上方button文字为“取消收藏”
	   //记录当前信息的标题，如”标题1“
	   //当前翻页至下一页
	   //点击右上方收藏
	   //翻页至下一页
	   //点击右上方收藏
	   //检查当前页右上方button文字为“取消收藏”
	   //记录当前信息的标题，如”标题2“
	   //翻至前一页，点击右上方取消收藏
	   //检查当前页右上方button文字为“收藏”
	   //点击返回，返回
	   //进入我的>收藏信息页面
	   //检查收藏信息列表，顺序为“标题2”,“标题1”
		
	}
}
