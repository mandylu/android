package com.quanleimu.activity.test;

import org.athrun.android.framework.AthrunDevice;
import org.athrun.android.framework.AthrunTestCase;
import org.athrun.android.framework.Test;

//all post test data
public class PostProcessTest extends BaixingTestCase {

	private static String postPosDataShouji[][] = {
		{"CATEGORY", "0", "二手手机"}, //物品交易->二手手机
		{"TITLE", "描述", "二手手机测试positive"},
		{"TITLE", "价格", "999"},
		{"TEXT", "联系方式", TEST_DATA_MOBILE},
		{"TEXT", "具体地点", "浦东金桥博兴路1676弄"},
	
	};
	
	private static String postNagDataShouji[][] = {
		{"CATEGORY", "0", "二手手机"}, //物品交易->二手手机
		{"TITLE", "描述", "二手手机测试nagetive"},
		{"TITLE", "价格", ""}, //留空
		{"TEXT", "联系方式", TEST_DATA_MOBILE},
		{"TEXT", "具体地点", "浦东金桥博兴路1676弄"},
	
	};
	
	private static String postPosDataHaoma[][] = {
		{"CATEGORY", "1", "手机号码"}, //物品交易->手机号码
		{"TITLE", "描述", "手机号码测试positive"},
		{"TITLE", "价格", "800"},
		{"TEXT", "联系方式", TEST_DATA_MOBILE},
		{"TEXT", "具体地点", "浦东耀华路西营路"},
	
	};

	private static String postNagDataHaoma[][] = {
		{"CATEGORY", "1", "手机号码"}, //物品交易->手机号码
		{"TITLE", "描述", ""}, //留空
		{"TITLE", "价格", "800"},
		{"TEXT", "联系方式", TEST_DATA_MOBILE},
		{"TEXT", "具体地点", "浦东耀华路西营路"},
	
	};

	private static String postPosDataBijiben[][] = {
		{"CATEGORY", "2", "笔记本"}, //物品交易->笔记本
		{"TITLE", "描述", "笔记本测试positive"},
		{"TITLE", "价格", "2888"},
		{"TEXT", "联系方式", TEST_DATA_MOBILE},
		{"TEXT", "具体地点", "徐家汇广元西路"},
	
	};

	private static String postNagDataBijiben[][] = {
		{"CATEGORY", "2", "笔记本"}, //物品交易->笔记本
		{"TITLE", "描述", "笔记本测试nagetive"},
		{"TITLE", "价格", "2888"},
		{"TEXT", "联系方式", TEST_DATA_MOBILE},
		{"TEXT", "具体地点", ""}, //留空
	
	};
	
	private static String postPosDataIpad[][] = {
		{"CATEGORY", "3", "平板电脑/ipad"}, //物品交易->平板电脑/ipad
		{"TITLE", "描述", "平板电脑测试positive"},
		{"TITLE", "价格", "1888"},
		{"TEXT", "联系方式", TEST_DATA_MOBILE},
		{"TEXT", "具体地点", "徐家汇广元西路55号"},
	
	};

	private static String postNagDataIpad[][] = {
		{"CATEGORY", "3", "平板电脑/ipad"}, //物品交易->平板电脑/ipad
		{"TITLE", "描述", "平板电脑测试nagetive"},
		{"TITLE", "价格", "1888"},
		{"TEXT", "联系方式", TEST_DATA_MOBILE}, //联系方式留空
		{"TEXT", "具体地点", "徐家汇广元西路55号"},
	
	};
	
	public PostProcessTest() throws Exception {
	}
	
	@Test
	public void testPostProcess() throws Exception {
		
		//android3.0
		//选择制定二级类目发布
		//清空具体地点信息
		//依次填入对应类目下的positive test data
		//点击发布
		//检查是否发布成功
		//若提示“发布成功”则在已发布列表确认生成
		//若提示版规错误，则在未审核通过列表确认生成
	}
}
