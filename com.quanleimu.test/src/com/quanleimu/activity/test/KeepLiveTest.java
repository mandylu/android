package com.quanleimu.activity.test;

import org.athrun.android.framework.AthrunTestCase;
import org.athrun.android.framework.Test;
import org.athrun.android.framework.viewelement.AbsListViewElement;
import org.athrun.android.framework.viewelement.ScrollViewElement;
import org.athrun.android.framework.viewelement.TextViewElement;

import android.util.Log;
import android.widget.ScrollView;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class KeepLiveTest extends BaixingTestCase {
	private boolean willFinished = false;

	private static String postDataJiaju[][] = {
		{"CATEGORY", "0", "家具"}, //物品交易
		//{"SELECT", "供求", "转让"},
		{"TEXT", "价格", "10020"}, //价格 （todo：价格不能超过N位）
		{"TEXT", "联系方式", TEST_DATA_MOBILE},
		{"TEXT", "具体地点", "测试具体地点测试"},
		{"TITLE", "描述", "物品交易标题家具"},
		//{"SELECT", "发布人", "个人"},
		//{"SELECT", "地点", "宝山,全部"}
	};
	private static String postDataQiecheyongpin[][] = {
		{"CATEGORY", "1", "汽车用品"}, //车辆买卖， 汽车用品
		//{"SELECT", "供求", "出售"},
		{"TEXT", "联系方式", TEST_DATA_MOBILE},
		{"TEXT", "具体地点", "测试具体地点测试"},
		{"TITLE", "标题", "测试汽车用品标题"},
		//{"SELECT", "发布人", "个人"},
		{"SELECT", "地区", "宝山,全部"}
	};
	private static String postDataXiezilou[][] = {
		{"CATEGORY", "2", "写字楼出租"}, //房屋租售
		{"TEXT", "联系方式", TEST_DATA_MOBILE},
		//{"SELECT", "供求", "出租"},
		{"TEXT", "具体地点", "测试具体地点测试"},
		{"TITLE", "描述", "测试写字楼出租"},
		//{"SELECT", "地区", "宝山,全部"}
	};
	private static String postDataQitazhaopin[][] = {
		{"CATEGORY", "3", "其它招聘"}, //全职招聘
		{"TEXT", "联系方式", TEST_DATA_MOBILE},
		//{"SELECT", "供求", "招聘"},
		{"TEXT", "具体地点", "测试具体地点测试"},
		{"TITLE", "描述", "招聘职位测试标题"},
		{"TEXT", "工资", "500"},
		//{"SELECT", "工作地点", "奉贤,全部"},
		//{"SELECT", "发布人", "公司直招"},
		//{"MULTISELECT", "入职前交费项", "体检费,押金,伙食费"},
		//{"SELECT", "入职前需交费", "无需缴纳"},
		//{"SELECT", "是否退款", "不予退还所交费用"},
		{"TEXT", "公司名称", "公司名测试"}
	};
	private static String postDataYanyuan[][] = {
		{"CATEGORY", "4", "演员"}, //兼职招聘
		{"TEXT", "联系方式", TEST_DATA_MOBILE},
		{"TEXT", "具体地点", "测试具体地点测试"},
		{"TITLE", "补充说明", "兼职测试演员"},
		{"TEXT", "工资", "50"},
		//{"SELECT", "工作地点", "宝山,全部"}
		//{"SELECT", "发布人", "职业介绍"}
	};
	private static String postDataJianzhiJianli[][] = {
		{"CATEGORY", "5", "兼职求职简历"}, //求职简历
		{"TEXT", "联系方式", TEST_DATA_MOBILE},
		{"TEXT", "具体地点", "测试具体地点测试"},
		//{"SELECT", "现居住地", "宝山,全部"},
		{"MULTISELECT", "求职意向", "模特,网站,摄影"},
		{"TEXT", "姓名", "测试员R"},
		//{"SELECT", "发布人", "个人"},
		{"TITLE", "工作经历", "兼职求职测试"}
	};
	private static String postDataXunren[][] = {
		{"CATEGORY", "6", "寻人/寻物"}, //交友活动
		{"TEXT", "联系方式", TEST_DATA_MOBILE},
		{"TEXT", "具体地点", "测试具体地点测试"},
		{"TITLE", "描述", "这时一个寻物的测试"},
		//{"SELECT", "地点", "宝山,全部"}
	};
	private static String postDataChongwuyongpin[][] = {
		{"CATEGORY", "7", "宠物用品/食品"}, //宠物
		//{"SELECT", "供求", "出售"},
		{"TEXT", "联系方式", TEST_DATA_MOBILE},
		{"TEXT", "具体地点", "测试具体地点测试"},
		{"TITLE", "补充说明", "食品用品测试标题宠物"},
		//{"SELECT", "发布人", "个人"},
		//{"SELECT", "地点", "宝山,全部"}
	};
	private static String postDataLipindingzhi[][] = {
		{"CATEGORY", "8", "礼品定制"}, //生活服务
		{"TEXT", "联系方式", TEST_DATA_MOBILE},
		{"TEXT", "具体地点", "测试具体地点测试"},
		{"TITLE", "标题", "礼品定制测试标题"},
		{"SELECT", "地区", "宝山,全部"},
		{"TEXT", "公司名称", "测试公司"}
	};
	private static String postDataWaiyupeixun[][] = {
		{"CATEGORY", "9", "外语培训"}, //教育培训
		//{"DESC", "详细说明", "详细说明测试教育培训"},
		{"TEXT", "联系方式", TEST_DATA_MOBILE},
		{"TEXT", "具体地点", "测试具体地点测试"},
		{"TITLE", "信息标题", "外语培训测试标题"},
		{"SELECT", "上课地点", "宝山,全部"},
		{"TEXT", "学校名称", "测试学校"}
	};
	
	public KeepLiveTest() throws Exception {
		super();
		final Timer timer = new Timer();
        TimerTask tt = new TimerTask() { 
            @Override
            public void run () {
            	willFinished = true;
                timer.cancel();
            }
        };
        timer.schedule(tt, 1 * 60 * 60 * 1000); // N秒 * 1000
	}
	
	/*
	 * Start run this Ad Listing Test
	 */
	@Test
	public void runAdListing() throws Exception {
        while(!willFinished) {
        	int index = (int)(Math.random() * 9);
        	Log.i(LOG_TAG, "Start do Category.index." + index);
        	doFirstCategory(index);
        	Runtime.getRuntime().gc();
        }
	}
	
	/*
	 * Start run post Test
	 */
	@Test
	public void runPost() throws Exception {
		logon();
        while(!willFinished) {
        	int index = (int)(Math.random() * 9);
        	Log.i(LOG_TAG, "Start do Post.index." + index);
        	doPost(index);
        	Runtime.getRuntime().gc();
        }
	}

	/*
	 * Start run post all category Test
	 */
	@Test
	public void runPostAll() throws Exception {
		BXLog.xr();
		runPostAllByIndex(0, 0);
	}
	
	/*
	 * Start run post all category Test from crash/break
	 */
	@Test
	public void runPostAllFB() throws Exception {
		String xr = BXLog.xr();
		if (xr.length() == 0) return;
		String[] lines = xr.split("\n");
		int l = lines.length;
		int firstIndex = -1;
		int secondIndex = -1;
		for(int i = l - 1; i > 0; i--) {
			String line = lines[i];
			String[] ps = line.split(",");
			if (ps.length >= 4) {
				if (ps[0].equals("Category") && ps[1].equals("Post") && ps[2].length() > 0 && ps[3].length() > 0) {
					try {
						firstIndex = Integer.valueOf(ps[2]);
						secondIndex = Integer.valueOf(ps[3]);
						BXLog.x("FB,Category,Post," + firstIndex + "," + secondIndex);
						break;
					} catch (Exception e) {}
				}
			}
		}
		if (firstIndex > -1 && secondIndex > -1)
		runPostAllByIndex(firstIndex, secondIndex);
	}

	private void runPostAllByIndex(int firstIndex, int secondIndex) throws Exception {
		String postErrors = "";
		logon();
		openTabbar(TAB_ID_MY_V3);
		deleteAllAds(MY_LISTING_MYAD_TEXT);
		deleteAllAds(MY_LISTING_MYAD_APPROVE_TEXT);
		for (int i = firstIndex; i < 10; i++) {
			openTabbar(TAB_ID_POST);
			openPostFirstCategory(i);
			AbsListViewElement subCatListView = findElementById(CATEGORY_SECOND_GRIDVIEW_ID,
					AbsListViewElement.class);
			if (subCatListView == null) {
				goBack();
				openTabbar(TAB_ID_POST);
				openPostFirstCategory(i);
				subCatListView = findElementById(CATEGORY_SECOND_GRIDVIEW_ID,
						AbsListViewElement.class);
			}
			int count = (subCatListView != null) ? subCatListView.getChildCount() : 50;
			Log.i(LOG_TAG, "runPostAll:" + count);
			String oldCateName = "";
			int retry = 0;
			for(int j = (i == firstIndex ? secondIndex : 0); j < count ; j++) {
				BXLog.x("Category,Post," + i + "," + j);
				openTabbar(TAB_ID_POST);
				openPostFirstCategory(i);
				openSecondCategoryByIndex(j);
				Log.i(LOG_TAG, "runPostAll:" + j);
				TextViewElement v = findElementById(VIEW_TITLE_ID, TextViewElement.class);
				if (v == null) {
					Log.i(LOG_TAG, "runPostAll:Category v==null prev" + oldCateName);
					BXLog.x("ERRORRETRY,Category,Post," + i + "," + j + "," + oldCateName);
					if (retry++ == 0) {
						j--;
						Log.i(LOG_TAG, "runPostAll:Category v==null retry" + oldCateName);
						continue;
					}
				}
				if (v != null) {
					retry = 0;
					if(!oldCateName.equals(v.getText())) {
						oldCateName = v.getText();
						BXLog.x("Category,Post," + i + "," + j + "," + oldCateName);
						Log.i(LOG_TAG, "runPostAll:Category " + oldCateName);
						postAutoEnterData();
						TimeUnit.SECONDS.sleep(1);
						if (!postSend(false)) {
							lockStatus(SCREEN_SAVE_LOCK_FILE, "");
							Log.i(LOG_TAG, "POST Category1:" + oldCateName + " ERROR");
						}
						afterPostSend();
						if (!checkPostSuccess(true)) {
							lockStatus(SCREEN_SAVE_LOCK_FILE, "");
							postErrors += "POST Category:" + oldCateName + " ERROR\n";
							Log.i(LOG_TAG, "POST Category2:" + oldCateName + " ERROR");
							BXLog.x("ERROR,Category,Post," + i + "," + j + "," + oldCateName);
						}
					} else {
						afterPostSend();
						break;
					}
				} else {
					afterPostSend();
				}
			}
		}
		assertTrue(postErrors, postErrors.length() == 0);

		deleteAllAds(MY_LISTING_MYAD_TEXT);
		deleteAllAds(MY_LISTING_MYAD_APPROVE_TEXT);
	}
	
	public void runOnePost() throws Exception {
		logon();
		runOnePost(3, "司机");
	}
	
	private void runOnePost(int firstIndex, String cateName) throws Exception {
		openTabbar(TAB_ID_POST);
		openPostFirstCategory(firstIndex);
		openSecondCategoryByName(cateName);
		Log.i(LOG_TAG, "runOnePost:" + cateName);
		TextViewElement v = findElementById(VIEW_TITLE_ID, TextViewElement.class);
		assertNotNull("runOnePost:Category v==null prev", v);
		assertEquals(cateName, v.getText());
		BXLog.x("Category,OnePost," + firstIndex + "," + cateName);
		Log.i(LOG_TAG, "runOnePost:Category " + cateName);
		postAutoEnterData();
		TimeUnit.SECONDS.sleep(1);
		if (!postSend(false)) {
			lockStatus(SCREEN_SAVE_LOCK_FILE, "");
			Log.i(LOG_TAG, "OnePost Category1:" + cateName + " ERROR");
		}
		afterPostSend();
		if (!checkPostSuccess(true)) {
			lockStatus(SCREEN_SAVE_LOCK_FILE, "");
			Log.i(LOG_TAG, "OnePost Category2:" + cateName + " ERROR");
			BXLog.x("ERROR,Category,OnePost," + firstIndex + "," + cateName);
		}
	}
	
	private void doFirstCategory(int index) throws Exception {
		openHomeCategoryByIndex(index);
		int maxCategoryNum = doScrollView(CATEGORY_SECOND_GRIDVIEW_ID, 5);
    	Log.i(LOG_TAG, "Start do Category.maxCategoryNum" + maxCategoryNum);
		if (maxCategoryNum < 0) return;
		scrollTop((int)(maxCategoryNum / 8), CATEGORY_SECOND_GRIDVIEW_ID);
		doSecondCategory((int)(Math.random() * maxCategoryNum));
		goBack(true);
	}
	
	private void doSecondCategory(int index) throws Exception {
		openSecondCategoryByIndex(index);
		int lastAdNum = doScrollView(AD_VIEWLIST_ID, 4);
		
		scrollTop(4, AD_VIEWLIST_ID);
		Log.i(LOG_TAG, "Start do Rand Ad.index." + index + "/" + lastAdNum);
		for(int i = 0; i < (lastAdNum > 5 ? 5 : 2); i++) {
			int rndIndex = (int)(Math.random() * (lastAdNum > 4 ? lastAdNum - 4 : 0)) + 1;
			Log.i(LOG_TAG, "Start do Rand Ad.index." + index + "/" + lastAdNum + "/" + rndIndex);
			assertNotNull(openAdByIndex(rndIndex));
			BXViewGroupElement detailView = findElementById(AD_DETAILVIEW_ID,
					BXViewGroupElement.class);
			assertNotNull(detailView);
			adViewPicTouch();
			showNextView(AD_DETAILVIEW_ID);
			showNextView(AD_DETAILVIEW_ID);
			showNextView(AD_DETAILVIEW_ID);
			showPrevView(AD_DETAILVIEW_ID);
			adViewPicTouch();
			showNextView(AD_DETAILVIEW_ID);
			goBack(true);
			scrollTop((int) (lastAdNum / 6), AD_VIEWLIST_ID);
		}
		goBack(true);
	}
	
	private void doPost(int index) throws Exception {
		openTabbar(TAB_ID_POST);
		String[][] postData = postDataQiecheyongpin;
		if (index == 0) postData = postDataJiaju;
		if (index == 2) postData = postDataXiezilou;
		if (index == 3) postData = postDataQitazhaopin;
		if (index == 4) postData = postDataYanyuan;
		if (index == 5) postData = postDataJianzhiJianli;
		if (index == 6) postData = postDataXunren;
		if (index == 7) postData = postDataChongwuyongpin;
		if (index == 8) postData = postDataLipindingzhi;
		if (index == 9) postData = postDataWaiyupeixun;
		String title = doPostByData(postData);
		if (title.length() > 0) {
			deleteAdByText(title);
		}
	}
	
}
