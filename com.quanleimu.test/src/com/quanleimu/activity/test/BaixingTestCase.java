package com.quanleimu.activity.test;

import java.util.concurrent.TimeUnit;

import org.athrun.android.framework.AthrunTestCase;
import org.athrun.android.framework.utils.AthrunConnectorThread;
import org.athrun.android.framework.viewelement.AbsListViewElement;
import org.athrun.android.framework.viewelement.ScrollViewElement;
import org.athrun.android.framework.viewelement.TextViewElement;
import org.athrun.android.framework.viewelement.ViewElement;
import org.athrun.android.framework.viewelement.ViewGroupElement;

import android.util.Log;

public class BaixingTestCase extends AthrunTestCase {
	private static final String LOG_TAG = "MainActivityTest";
	
	//Common ID
	public static final String BACK_BUTTON_TEXT = "返回";
	//Home ID
	public static final String HOME_FIRST_RUN_ID = "topguide";
	public static final String HOME_MARK_ID = "lvLogoAndChangeCity";
	public static final String HOME_CATEGORY_VIEWLIST_ID = "cateSelection";
	public static final String HOME_CATEGORY_VIEWLIST_ITEM_NAME_ID = "tvName";

	public static final String TAB_ID_HOME = "ivHomePage";
	public static final String TAB_ID_POST = "ivPostGoods";
	public static final String TAB_ID_MY = "ivMyCenter";
	
	public static final String SEARCH_TEXTVIEW_ID = "etSearch";
	public static final String SEARCH_BUTTON_ID = "btnCancel";
	public static final String SEARCH_BUTTON_TEXT = "搜索";
	public static final String SEARCH_DELETE_TEXT = "清除历史记录";
	
	public static final String CATEGORY_VIEWLIST_ID = "cateSelection";
	
	//AdList && AdView ID
	public static final String AD_VIEWLIST_MARK_ID = "rlListInfo";
	public static final String AD_VIEWLIST_ID = "lvGoodsList";
	public static final String AD_DETAILVIEW_ID = "svDetail";
	public static final String AD_DETAILVIEW_PREV_ID = "btn_prev";
	public static final String AD_DETAILVIEW_NEXT_ID = "btn_next";
	public static final String AD_DETAILVIEW_TITLE_ID = "goods_tittle";
	public static final String AD_IMAGES_VIEWLIST_ID = "glDetail";
	
	public static final String AD_BIG_IMAGE_VIEW_ID = "vfCoupon";
	
	//POST ID
	public static final String POST_FORM_MARK_ID = "layout_txt";
	public static final String POST_SCROLLVIEW_ID = "scrollView1";
	public static final String POST_CATEGORY_GRIDVIEW_ID = "gridcategory";
	public static final String POST_SECOND_CATEGORY_LISTVIEW_ID = "post_other_list";
	public static final int POST_CATEGORY_SELEC_INDEX = 0;
	public static final String POST_META_LISTVIEW_ID = "post_other_list";
	public static final String POST_META_ITEM_DISPLAY_ID = "postshow";
	public static final String POST_META_ITEM_ID = "post_select";
	public static final String POST_META_EDITTEXT_ID = "postinput";
	public static final String POST_META_EDIT_DISPLAY_ID = "postshow";
	public static final String POST_META_EDIT_ITEM_ID = "postinputlayout";
	public static final String POST_DONE = "完成";
	public static final String POST_SEND = "立即发布";
	
	//My ID
	public static final String MY_LISTITEM_MYAD_ID = "rl_wosent";
	public static final String MY_LISTING_MYAD_COUNTER_ID = "tv_sentcount";
	public static final String MY_MYAD_APPROVE_BUTTON_ID = "ivMyfav";
	public static final String MY_MYAD_DELETE_BUTTON_ID = "ivMyhistory";
	public static final String MY_LISTING_HISTORY_ID = "rl_wohistory";
	public static final String MY_LISTING_HISTORY_COUNTER_ID = "tv_historycount";
	public static final String MY_AD_FxH_VIEWLIST_ID = "plvlist";
	
	public static final String MY_LOGOUT_BUTTON_ID = "注销";
	public static final String MY_LOGIN_BUTTON_ID = "btn_login";
	public static final String MY_LOGIN_USER_TEXTVIEW_ID = "et_account";
	public static final String MY_LOGIN_PASSWORD_TEXTVIEW_ID = "et_password";
	public static final String MY_LOGON_SUCCESS_MESSAGE = "用户登录成功";
	public static final String DIALOG_OK_BUTTON_ID = "确定";
	public static final String MY_EDIT_BUTTON_ID = "编辑";
	public static final String MY_DELETE_ALL_BUTTON_ID = "清空";
	
	public static final String MY_DETAILVIEW_MANAGE_BUTTON_ID = "managebtn";
	public static final String MY_DETAILVIEW_DELETE_BUTTON_ID = "manager_delete";
	
	//POST META TYPE
	public enum METATYPE {
		CATEGORY("METATYPE_CATEGORY"),
		SELECT("METATYPE_SELECT"),
		MULTISELECT("METATYPE_MULTI_SELECT"),
		TEXT("METATYPE_TEXT"),
		TITLE("METATYPE_TITLE");
		private String name;
		METATYPE(String name) {
			this.name = name;
		}
		@Override
		public String toString() {
			return this.name;
		}
	}
	//Test DATA
	public static final String TEST_DATA_MOBILE = "13917067724";
	public static final String TEST_DATA_PASSWORD = "whonwyhw";
	public static final String TEST_DATA_CAT_WUPINJIAOYI = "物品交易";
	public static String postDataJiaju[][] = {
		{"CATEGORY", "0", "家具"}, //物品交易
		{"SELECT", "供求", "转让"},
		{"TITLE", "标题", "物品交易标题家具"},
		{"TEXT", "价格", "10020"}, //价格 （todo：价格不能超过N位）
		{"SELECT", "发布人", "个人"},
		{"TEXT", "联系方式", TEST_DATA_MOBILE}
	};
	public static String postDataQiecheyongpin[][] = {
		{"CATEGORY", "1", "汽车用品"}, //车辆买卖， 汽车用品
		{"SELECT", "供求", "出售"},
		{"TITLE", "标题", "测试汽车用品标题"},
		{"SELECT", "发布人", "个人"},
		{"TEXT", "联系方式", TEST_DATA_MOBILE}
	};
	public static String postDataXiezilou[][] = {
		{"CATEGORY", "2", "写字楼出租"}, //房屋租售
		{"SELECT", "供求", "出租"},
		{"TITLE", "标题", "测试写字楼出租"},
		{"TEXT", "联系方式", TEST_DATA_MOBILE}
	};
	public static String postDataQitazhaopin[][] = {
		{"CATEGORY", "3", "其它招聘"}, //全职招聘
		{"SELECT", "供求", "招聘"},
		{"TITLE", "招聘职位", "招聘职位测试标题"},
		{"TEXT", "工资", "500"},
		{"SELECT", "工作地点", "奉贤,全部"},
		{"SELECT", "发布人", "公司直招"},
		{"MULTISELECT", "入职前交费项", "体检费,押金,伙食费"},
		{"SELECT", "入职前需交费", "无需缴纳"},
		{"SELECT", "是否退款", "不予退还所交费用"},
		{"TEXT", "公司名称", "公司名测试"},
		{"TEXT", "联系方式", TEST_DATA_MOBILE}
	};
	public static String postDataYanyuan[][] = {
		{"CATEGORY", "4", "演员"}, //兼职招聘
		{"TITLE", "标题", "兼职测试演员"},
		{"TEXT", "工资", "50"},
		{"SELECT", "工作地点", "宝山,全部"},
		{"SELECT", "发布人", "职业介绍"},
		{"TEXT", "联系方式", TEST_DATA_MOBILE}
	};
	public static String postDataJianzhiJianli[][] = {
		{"CATEGORY", "5", "兼职求职简历"}, //求职简历
		{"MULTISELECT", "求职意向", "模特,网站,摄影"},
		{"TEXT", "姓名", "测试员R"},
		{"SELECT", "发布人", "个人"},
		{"TEXT", "联系方式", TEST_DATA_MOBILE},
		{"TITLE", "简历标题", "兼职求职测试"},
	};
	public static String postDataXunren[][] = {
		{"CATEGORY", "6", "寻人/寻物"}, //交友活动
		{"TITLE", "标题", "这时一个寻物的测试"},
		{"TEXT", "联系方式", TEST_DATA_MOBILE}
	};
	public static String postDataChongwuyongpin[][] = {
		{"CATEGORY", "7", "宠物用品/食品"}, //宠物
		{"SELECT", "供求", "出售"},
		{"TITLE", "标题", "食品用品测试标题宠物"},
		{"SELECT", "发布人", "个人"},
		{"TEXT", "联系方式", TEST_DATA_MOBILE}
	};
	public static String postDataLipindingzhi[][] = {
		{"CATEGORY", "8", "礼品定制"}, //生活服务
		{"TITLE", "标题", "礼品定制测试标题"},
		{"TEXT", "公司名称", "测试公司"},
		{"TEXT", "联系方式", TEST_DATA_MOBILE}
	};
	public static String postDataWaiyupeixun[][] = {
		{"CATEGORY", "9", "外语培训"}, //教育培训
		{"TITLE", "信息标题", "外语培训测试标题"},
		{"TEXT", "学校名称", "测试学校"},
		{"TEXT", "联系方式", TEST_DATA_MOBILE}
	};
	
	@SuppressWarnings("unchecked")
	public BaixingTestCase() throws Exception {
		super("com.quanleimu.activity", "com.quanleimu.activity.QuanleimuMainActivity");
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		// log("This is a test for log() method");
		assertEquals(true, getDevice().waitForActivity("QuanleimuMainActivity", 5000));
		ViewElement v = findElementById(HOME_FIRST_RUN_ID);
		if (v != null) {
			v.doClick();
			TimeUnit.SECONDS.sleep(1);
		}
	}
	
	public void logout() throws Exception {
		openTabbar(TAB_ID_MY);
		ViewElement el = findElementByText(MY_LOGOUT_BUTTON_ID);
		if (el == null) return;
		el.doClick();
		TimeUnit.SECONDS.sleep(1);
		findElementByText(DIALOG_OK_BUTTON_ID, 0, true).doClick();
		TimeUnit.SECONDS.sleep(3);
	}
	
	public void logon() throws Exception {
		ViewElement loginBtn = findElementById(MY_LOGIN_BUTTON_ID);
		if (loginBtn != null) {
			TextViewElement etAccount = findElementById(MY_LOGIN_USER_TEXTVIEW_ID, TextViewElement.class);
			etAccount.setText(TEST_DATA_MOBILE);
			TextViewElement etPwd = findElementById(MY_LOGIN_PASSWORD_TEXTVIEW_ID, TextViewElement.class);
			etPwd.setText(TEST_DATA_PASSWORD);
			
			loginBtn.doClick();
			assertEquals(true, waitForText(MY_LOGON_SUCCESS_MESSAGE, 2000));
		}
	}
	
	public void openTabbar(String vId) throws Exception {
		ViewElement v = findElementById(vId);
		if (v != null) {
			v.doClick();
			TimeUnit.SECONDS.sleep(1);
		}
	}
	
	public void openPostCategory(int firstCatIndex, int secondCatIndex) throws Exception {
		openPostFirstCategory(firstCatIndex);
		selectMetaByIndex(secondCatIndex, POST_SECOND_CATEGORY_LISTVIEW_ID);
	}
	
	public void openPostFirstCategory(int firstCatIndex) throws Exception {
		
		openTabbar(TAB_ID_POST);
		
		logon();
		
		ScrollViewElement postView = findElementById(POST_SCROLLVIEW_ID, ScrollViewElement.class);
		if (postView != null) {
			assertNotNull(postView);

			TimeUnit.SECONDS.sleep(3);
			openPostItemByIndex(POST_CATEGORY_SELEC_INDEX);
		}
		AbsListViewElement gridView = findElementById(POST_CATEGORY_GRIDVIEW_ID,
				AbsListViewElement.class);
		assertNotNull(gridView);
		ViewGroupElement item = gridView.getChildByIndex(firstCatIndex, ViewGroupElement.class);
		assertNotNull(item);
		if (firstCatIndex == 0) {
			TextViewElement view = item.getChildByIndex(1, TextViewElement.class);
			assertEquals(TEST_DATA_CAT_WUPINJIAOYI, view.getText());
		}
		item.doClick();
		TimeUnit.SECONDS.sleep(3);
	}
	
	public void openPostItemByIndex(int index) throws Exception {
		ViewGroupElement postLayout = findElementById(POST_FORM_MARK_ID, ViewGroupElement.class);
		assertNotNull(postLayout);
		ViewGroupElement listView = findElementById(POST_META_ITEM_ID, index, ViewGroupElement.class);
		assertNotNull(listView);
		listView.doClick();
		TimeUnit.SECONDS.sleep(2);
	}
	
	public void openPostItemByName(String displayName) throws Exception {
		ViewGroupElement postLayout = findElementById(POST_FORM_MARK_ID, ViewGroupElement.class);
		assertNotNull(postLayout);
		int index = 0;
		ViewGroupElement dv = null;
		TextViewElement dtv = findMetaByName(POST_SCROLLVIEW_ID, displayName);
		assertNotNull(dtv);
		while(index < 50) {
			try {
				dv = findElementById(POST_META_ITEM_ID, index++, ViewGroupElement.class);
				Log.i(LOG_TAG, "setOtherMetaByName:openPostItemByName" + index);
				if (dv != null) {
					Log.i(LOG_TAG, "setOtherMetaByName:openPostItemByName" + index + POST_META_ITEM_ID);
					TextViewElement nv = dv.findElementById(POST_META_ITEM_DISPLAY_ID, TextViewElement.class);
					if (nv != null) Log.i(LOG_TAG, "setOtherMetaByName:openPostItemByName" + index + POST_META_ITEM_DISPLAY_ID + displayName + ":" + nv.getText());
					if (nv != null && nv.getText().equals(displayName)) {
						dv.doClick();
						TimeUnit.SECONDS.sleep(2);
						return;
					}
				}
			} catch (IndexOutOfBoundsException ex) {
				break;
			}
		}
		assertNotNull(dv);
	}
	
	public void setMetaByIndex (int index, String value) throws Exception {
		ViewGroupElement postLayout = findElementById(POST_FORM_MARK_ID, ViewGroupElement.class);
		assertNotNull(postLayout);
		TimeUnit.SECONDS.sleep(1);
		ScrollViewElement lv = findElementById(POST_SCROLLVIEW_ID, ScrollViewElement.class);
		assertNotNull(lv);
		int loop = 0;
		ViewElement v = setOtherMetaByIndex(index, value);
		while(v == null) {
			lv.scrollToNextScreen();
			TimeUnit.SECONDS.sleep(1);
			v = setOtherMetaByIndex(index, value);
			if (loop++ > 10) break;
		}
		assertNotNull(v);
	}
	public ViewElement setOtherMetaByIndex(int index, String value) throws Exception {
		try {
			TextViewElement tv = findElementById(POST_META_EDITTEXT_ID, index, TextViewElement.class);
			//assertNotNull(tv);
			if (tv != null) {
				tv.setText(value);
				TimeUnit.SECONDS.sleep(1);
			}
			return tv;
		} catch (IndexOutOfBoundsException ex) {
			return null;
		}
	}
	
	public void selectMetaByName(String displayName) throws Exception {
		TextViewElement v3 = findMetaByName(POST_META_LISTVIEW_ID, displayName);
		assertNotNull(v3);
		Log.i(LOG_TAG, "postshow:v3" + v3.getText());
		v3.doClick();
		TimeUnit.SECONDS.sleep(1);
	}
	
	public TextViewElement findMetaByName(String listViewId, String displayName) throws Exception {
		ScrollViewElement scrollView = null;
		AbsListViewElement listView = null;
		try {
			scrollView = findElementById(listViewId,
				ScrollViewElement.class);
			assertNotNull(scrollView);
		} catch (IllegalArgumentException ex) {
			listView = findElementById(listViewId,
					AbsListViewElement.class);
			assertNotNull(listView);
		}
		int loop = 0;
		TextViewElement v3 = findElementByText(displayName, 0, true);
		while(v3 == null) {
			if (scrollView != null) scrollView.scrollToNextScreen();
			else listView.scrollToNextScreen();
			TimeUnit.SECONDS.sleep(1);
			v3 = findElementByText(displayName, 0, true);
			if (loop++ > 10) break;
		}
		return v3;
	}
	
	public void setMetaByName (String displayName, String value) throws Exception {
		ViewGroupElement postLayout = findElementById(POST_FORM_MARK_ID, ViewGroupElement.class);
		assertNotNull(postLayout);
		TimeUnit.SECONDS.sleep(1);
		ScrollViewElement lv = findElementById(POST_SCROLLVIEW_ID, ScrollViewElement.class);
		assertNotNull(lv);
		int loop = 0;
		ViewElement v = setMetaValueByName(displayName, value);
		while(v == null) {
			lv.scrollToNextScreen();
			TimeUnit.SECONDS.sleep(1);
			v = setMetaValueByName(displayName, value);
			if (loop++ > 10) break;
		}
		assertNotNull(v);
	}
	public ViewElement setMetaValueByName(String displayName, String value) throws Exception {
		TextViewElement dtv = findMetaByName(POST_SCROLLVIEW_ID, displayName);
		assertNotNull(dtv);
		int index = 0;
		while(index < 50) {
			try {
				ViewGroupElement dv = findElementById(POST_META_EDIT_ITEM_ID, index++, ViewGroupElement.class);
				Log.i(LOG_TAG, "setOtherMetaByName:" + index);
				if (dv != null) {
					Log.i(LOG_TAG, "setOtherMetaByName:" + index + POST_META_EDIT_ITEM_ID);
					TextViewElement nv = dv.findElementById(POST_META_EDIT_DISPLAY_ID, TextViewElement.class);
					if (nv != null) Log.i(LOG_TAG, "setOtherMetaByName:" + index + POST_META_EDIT_DISPLAY_ID);
					if (nv != null && nv.getText().equals(displayName)) {
						Log.i(LOG_TAG, "setOtherMetaByName:" + index + POST_META_EDIT_DISPLAY_ID + displayName);
						for(int i = 1; i < dv.getChildCount(); i++) {
							ViewGroupElement ddv = dv.getChildByIndex(i, ViewGroupElement.class);
							TextViewElement tv = ddv.findElementById(POST_META_EDITTEXT_ID, TextViewElement.class);
							if (tv != null) {
								tv.setText(value);
								TimeUnit.SECONDS.sleep(1);
								return tv;
							}
						}
					}
				}
			} catch (IndexOutOfBoundsException ex) {
				break;
			}
		}
		return null;
	}
	
	public void selectMetaByIndex(int index) throws Exception {
		selectMetaByIndex(index, POST_META_LISTVIEW_ID);
	}
	
	public void selectMetaByIndex(int index, String listViewId) throws Exception {
		AbsListViewElement listView = findElementById(listViewId,
				AbsListViewElement.class);
		assertNotNull(listView);
		ViewGroupElement v = listView.getChildByIndex(index,
				ViewGroupElement.class);
		assertNotNull(v);
		v.doClick();
		TimeUnit.SECONDS.sleep(3);
	}
	
	public void postOtherDone() throws Exception {
		ViewElement el = findElementByText(POST_DONE);
		assertNotNull(el);
		el.doClick();
		TimeUnit.SECONDS.sleep(1);
	}

	public String doPostByData(String[][] postData) throws Exception {
		String title = "";
		for (int i = 0; i < postData.length; i++) {
			METATYPE type = METATYPE.valueOf(postData[i][0]);
			switch(type) {
			case CATEGORY:
				openPostFirstCategory(Integer.parseInt(postData[i][1]));
				selectMetaByName(postData[i][2]);
				break;
			case MULTISELECT:
			case SELECT:
				openPostItemByName(postData[i][1]);
				String[] metaNames = postData[i][2].split(",");
				for (int j = 0; j < metaNames.length; j++) {
					selectMetaByName(metaNames[j]);
				}
				if (type == METATYPE.MULTISELECT) postOtherDone();
				break;
			case TEXT:
			case TITLE:
				String txtVal = postData[i][2];
				if (type == METATYPE.TITLE) title = txtVal;
				setMetaByName(postData[i][1], txtVal);
				break;
			}
		}
		
		postSend();
		
		return title;
	}
	
	public void postSend() throws Exception {
		ViewElement eld = findElementByText(POST_SEND);
		assertNotNull(eld);
		eld.doClick();
		TimeUnit.SECONDS.sleep(3);
	}
	
	public void openHomeCategoryByIndex(int index) throws Exception {
		openTabbar(TAB_ID_HOME);
		ViewElement hv = findElementById(HOME_MARK_ID);
		int i = 0;
		while(hv == null && i++ < 5) {
			goBack();
			openTabbar(TAB_ID_HOME);
			hv = findElementById(HOME_MARK_ID);
		}
		assertNotNull(hv);
		AbsListViewElement catListView = findElementById(HOME_CATEGORY_VIEWLIST_ID,
				AbsListViewElement.class);
		ViewGroupElement catView = catListView.getChildByIndex(index + 1,
				ViewGroupElement.class);
		TextViewElement catTextView = catView.findElementById(HOME_CATEGORY_VIEWLIST_ITEM_NAME_ID,
				TextViewElement.class);
		TimeUnit.MILLISECONDS.sleep(300);
		catView.doClick();
		TimeUnit.SECONDS.sleep(2);
		assertNotNull(catTextView.getText());
		assertTrue(catTextView.getText().length() > 0);
	}
	
	public void openSecondCategoryByIndex(int index) throws Exception {
		AbsListViewElement subCatListView = findElementById(CATEGORY_VIEWLIST_ID,
				AbsListViewElement.class);
		ViewGroupElement subCatView = subCatListView.getChildByIndex(index,
				ViewGroupElement.class);
		subCatView.doClick();
		TimeUnit.SECONDS.sleep(8);
	}
	
	public void openCategoryByIndex(int firstCatIndex, int secondCatIndex) throws Exception {
		openHomeCategoryByIndex(firstCatIndex);
		openSecondCategoryByIndex(secondCatIndex);
	}
	
	public ViewGroupElement openAdByIndex(int index) throws Exception {
		return openAdByIndex(index, AD_VIEWLIST_ID);
	}
	
	public ViewGroupElement openAdByIndex(int index, String viewListId) throws Exception {
		AbsListViewElement avl = findElementById(viewListId, AbsListViewElement.class);
		assertNotNull(avl);
		/*ViewGroupElement avi = avl.getChildByIndex(0, ViewGroupElement.class);
		int i = 0;
		int j = 0;
		while (avi != null) {
			ViewGroupElement _avi_ = avi.findElementById(AD_VIEWLIST_MARK_ID, ViewGroupElement.class);
			if (_avi_ == null) {
				avl.scrollToNextScreen();
				TimeUnit.SECONDS.sleep(1);
				avi = avl.getChildByIndex(j, ViewGroupElement.class);
				_avi_ = avi.findElementById(AD_VIEWLIST_MARK_ID, ViewGroupElement.class);
				if (_avi_ == null) {
					avi = avl.getChildByIndex(++j, ViewGroupElement.class);
					continue;
				}
			}
			if (i++ == index) {
				avi.doClick();
				TimeUnit.SECONDS.sleep(1);
				break;
			}
			avi = avl.getChildByIndex(++j, ViewGroupElement.class);
		}
		*/
		int indexSize = 6;
		int pageSize = (int) (index / indexSize); //每页6个
		int i = 1;
		while (i++ < pageSize) {
			avl.scrollToNextScreen();
			TimeUnit.SECONDS.sleep(1);
		}
		ViewGroupElement avi = null;
		while(indexSize > 0) {
			try {
				avi = findElementById(AD_VIEWLIST_MARK_ID, index % (indexSize--), ViewGroupElement.class);
				if (avi != null) {
					avi.doClick();
					TimeUnit.SECONDS.sleep(1);
					break;
				}
			} catch (IndexOutOfBoundsException ex) {}
		}
		return avi;
	}
	
	public BXViewGroupElement showAd(int firstCatIndex, int secondCatIndex, int index) throws Exception {
		openTabbar(TAB_ID_MY);
		logon();
		openCategoryByIndex(firstCatIndex, secondCatIndex);
		TimeUnit.SECONDS.sleep(1);
		assertNotNull(openAdByIndex(index));
		BXViewGroupElement detailView = findElementById(AD_DETAILVIEW_ID,
				BXViewGroupElement.class);
		return detailView;
	}
	
	protected void adViewPicTouch() throws Exception {
		//查看第一个图片
		if (showAdPic(0)) {
			//Log.i(LOG_TAG, "pic:0");
			goBack(false);
			//滚动图片
			BXViewGroupElement ilv = findElementById(AD_IMAGES_VIEWLIST_ID, BXViewGroupElement.class);
			ilv.doTouch(-200);
			TimeUnit.SECONDS.sleep(1);
			//查看第二个图片
			if (showAdPic(1)) {
				//Log.i(LOG_TAG, "pic:1");
				goBack(false);
				//滚回图片
				ilv = findElementById(AD_IMAGES_VIEWLIST_ID, BXViewGroupElement.class);
				ilv.doTouch(200);
				TimeUnit.SECONDS.sleep(1);
				//Log.i(LOG_TAG, "pic:touch0");
				
				//滚动大图
				showAdPic(0);
				TimeUnit.SECONDS.sleep(1);
				showNextView(AD_BIG_IMAGE_VIEW_ID);
				//Log.i(LOG_TAG, "pic:touch1");
				goBack();
			}
		}
	}
	
	public void showNextView(String viewId) throws Exception {
		BXViewGroupElement bv = findElementById(viewId,
				BXViewGroupElement.class);
		TimeUnit.SECONDS.sleep(1);
		bv.doTouch(-bv.getWidth() + 20);
		TimeUnit.SECONDS.sleep(3);
	}
	
	public void showPrevView(String viewId) throws Exception {
		BXViewGroupElement bv = findElementById(viewId,
				BXViewGroupElement.class);
		TimeUnit.SECONDS.sleep(1);
		bv.doTouch(bv.getWidth() - 20);
		TimeUnit.SECONDS.sleep(3);
	}
	
	public boolean showAdPic(int index) throws Exception {
		ViewGroupElement ilv = findElementById(AD_IMAGES_VIEWLIST_ID, ViewGroupElement.class);
		if (ilv == null) return false;
		ViewElement iv = ilv.getChildByIndex(index);
		if (iv == null) return false;
		iv.doClick();
		TimeUnit.SECONDS.sleep(1);
		return (findElementById(AD_BIG_IMAGE_VIEW_ID) != null);
	}
	
	public int showMyAdList(String myListId, String myListCountId) throws Exception {
		openTabbar(TAB_ID_MY);
		TextViewElement elc = findElementById(myListCountId, TextViewElement.class);
		myItemClick(myListId);
		if (elc != null && elc.getText().length() > 0) return Integer.parseInt(elc.getText().replaceAll("\\D+",  ""));
		return 0;
	}
	
	public void deleteAllHistoryAds() throws Exception {
		if (showMyAdList(MY_LISTING_HISTORY_ID, MY_LISTING_HISTORY_COUNTER_ID) <= 0) return;
		TimeUnit.SECONDS.sleep(1);
		ViewElement ele = findElementByText(MY_EDIT_BUTTON_ID);
		if (ele == null) return;
		ele.doClick();
		TimeUnit.SECONDS.sleep(1);
		ViewElement eld = findElementByText(MY_DELETE_ALL_BUTTON_ID);
		if (eld == null) return;
		eld.doClick();
		getDevice().pressBack();
		TimeUnit.SECONDS.sleep(1);
	}
	
	public void deleteAdByText(String keyword) throws Exception {
		if (showMyAdList(MY_LISTITEM_MYAD_ID, MY_LISTING_MYAD_COUNTER_ID) <= 0) return;
		TimeUnit.SECONDS.sleep(2);
		ViewElement v = findElementByText(keyword, 0, true);
		if (v == null) return;
		v.doClick();
		TimeUnit.SECONDS.sleep(1);
		ViewElement vm = findElementById(MY_DETAILVIEW_MANAGE_BUTTON_ID);
		if (vm == null) {
			goBack();
			return;
		}
		vm.doClick();
		TimeUnit.SECONDS.sleep(1);
		ViewElement vd = findElementById(MY_DETAILVIEW_DELETE_BUTTON_ID);
		if (vd == null) {
			goBack();
			return;
		}
		vd.doClick();
		TimeUnit.SECONDS.sleep(1);
		findElementByText(DIALOG_OK_BUTTON_ID, 0, true).doClick();
		TimeUnit.SECONDS.sleep(1);
		goBack();
	}
	
	public void doSearch(String keyword) throws Exception {
		if (findElementById(HOME_MARK_ID) != null) {
			findElementById(SEARCH_TEXTVIEW_ID).doClick();
			TimeUnit.SECONDS.sleep(1);
		} else if (findElementById(AD_VIEWLIST_MARK_ID) != null && findElementById(SEARCH_BUTTON_ID) == null) {
			ViewElement btnSearch = findElementByText(SEARCH_BUTTON_TEXT, 0, true);
			if (btnSearch != null) {
				btnSearch.doClick();
				TimeUnit.SECONDS.sleep(1);
			}
		}
		if (keyword.length() > 0) {
			TextViewElement etSearchText = findElementById(SEARCH_TEXTVIEW_ID,
					TextViewElement.class);
			etSearchText.setText(keyword);
			assertEquals(keyword, etSearchText.getText());

			findElementById(SEARCH_BUTTON_ID).doClick();
			TimeUnit.SECONDS.sleep(2);
		}
	}
	
	public void selectSearch(String keyword) throws Exception {
		doSearch("");
		ViewElement iv = findElementByText(keyword, 0, true);
		if (iv != null) {
			iv.doClick();
			TimeUnit.SECONDS.sleep(2);
		}
	}
	
	public void myItemClick(String itemId) throws Exception {
		ViewElement el = findElementById(itemId);
		assertNotNull(el);
		el.doClick();
		TimeUnit.SECONDS.sleep(1);
	}
	public void goBack() throws Exception {
		goBack(true);
	}
	public void goBack(boolean force) throws Exception {
		ViewElement iv = findElementByText(BACK_BUTTON_TEXT, 0, true);
		if (!force) assertNotNull(iv);
		if (iv != null) {
			iv.doClick();
		} else {
			getDevice().pressBack();
		}

		TimeUnit.SECONDS.sleep(1);
	}
	
	/*
	 * 滚动页面，输出最后一个index号
	 */
	public int doScrollView(String viewId, int page) throws Exception {
		AbsListViewElement lv = findElementById(viewId,
				AbsListViewElement.class);
		if (lv == null) return 0;
		for (int i = 0; i < (page > 0 ? page: 10); i++) {
			lv.scrollToNextScreen();
		}
		TimeUnit.SECONDS.sleep(3);
		int lastIndex = lv.getLastVisiblePosition();
		return lastIndex;
	}
	public void scrollTop(int pageSize, String viewId) throws Exception {
		for (int i = 0; i < pageSize; i++) {
			BXViewGroupElement bv = findElementById(viewId, BXViewGroupElement.class);
			int height = bv.getHeight();
			bv.scrollTop(height / 2 - height /3, height / 2 + height /3);
			TimeUnit.SECONDS.sleep(2);
		}
	}
}
