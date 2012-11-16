package com.quanleimu.activity.test;

import org.athrun.android.framework.AthrunDevice;
import org.athrun.android.framework.AthrunTestCase;
import org.athrun.android.framework.Test;

//all post test data
public class PostProcessTest extends BaixingTestCase {

	private static String postPosOneShouji[][] = {
		{"CATEGORY", "0", "二手手机"}, //物品交易->二手手机
		{"TITLE", "描述", "二手手机测试positive1"},
		{"TITLE", "价格", "999"},
		{"TEXT", "联系方式", TEST_DATA_MOBILE},//取默认
		{"TEXT", "具体地点", "浦东金桥博兴路1676弄"},
	
	};
	
	private static String postPosTwoShouji[][] = {
		{"CATEGORY", "0", "二手手机"}, //物品交易->二手手机
		{"TITLE", "描述", "二手手机测试positive2"},
		{"TITLE", "价格", "0"},
		{"TEXT", "联系方式", TEST_DATA_MOBILE},//取默认
		{"TEXT", "具体地点", "浦东金桥博兴路1676弄"},
	
	};
	
	private static String postPosThreeShouji[][] = {
		{"CATEGORY", "0", "二手手机"}, //物品交易->二手手机
		{"TITLE", "描述", "二手手机测试positive3"},
		{"TITLE", "价格", "1000"},
		{"TEXT", "联系方式", TEST_DATA_MOBILE},//取默认
		{"TEXT", "具体地点", "上海市徐汇区广元西路55号"},//取默认
	
	};
	
	private static String postPosFourShouji[][] = {
		{"CATEGORY", "0", "二手手机"}, //物品交易->二手手机
		{"TITLE", "描述", "二手手机测试positive3"},
		{"TITLE", "价格", "1000"},
		{"TEXT", "联系方式", "123456789"},//qq
		{"TEXT", "具体地点", "浦东金桥博兴路1676弄"},
	
	};
	private static String postNagOneShouji[][] = {
		{"CATEGORY", "0", "二手手机"}, //物品交易->二手手机
		{"TITLE", "描述", "二手手机测试nagetive1"},
		{"TITLE", "价格", ""}, //留空
		{"TEXT", "联系方式", TEST_DATA_MOBILE},
		{"TEXT", "具体地点", "浦东金桥博兴路1676弄"},
	
	};

	private static String postNagTwoShouji[][] = {
		{"CATEGORY", "0", "二手手机"}, //物品交易->二手手机
		{"TITLE", "描述", ""}, //留空
		{"TITLE", "价格", "999"}, 
		{"TEXT", "联系方式", TEST_DATA_MOBILE},
		{"TEXT", "具体地点", "浦东金桥博兴路1676弄"},
	
	};
	
	private static String postPosOneQuanzhi[][] = {
		{"CATEGORY", "0", "全职求职简历"}, //求职简历->全职求职简历
		{"TITLE", "工作经历", "曾经做过：电焊，服务员，宣传员，售货员，保洁员，幼教"}, //留空
		{"TITLE", "联系方式", TEST_DATA_MOBILE}, 
		{"TEXT", "现居住地", "上海浦东金桥博兴路"},
		{"SELECT", "求职意向", }, //全选
		{"TEXT", "姓名", "李四四" }, //全选
	
	};
	
	
	public PostProcessTest() throws Exception {
	}
	
	@Test
	public void testErshouProcess() throws Exception {
		
		//android3.0
		//选择类目“二手手机”
		//清空所有已填信息
		//填入对应test data
		//输入框输入时检查：键盘弹起时发布页面可上下拖动
		//全部填完后检查已填内容，确保正确，未被清空
		//点击发布按钮
		//检查是否发布成功
		//若提示“发布成功”则在已发布列表确认生成
		//若提示版规错误，则在未审核通过列表确认生成
		//恢复初始（删除发布信息）
	}

   @Test
   public void testQuanzhiProcess() throws Exception {
	
	    //android3.0
	    //选择类目“全职求职简历”
	    //清空所有已填信息
	    //填入对应test data
	    //全部填完后检查已填内容，确保正确，未被清空
	    //点击发布按钮
	    //检查是否发布成功
	    //若提示“发布成功”则在已发布列表确认生成
	    //若提示版规错误，则在未审核通过列表确认生成
	    //删除发布信息
}
}
