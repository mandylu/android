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
	public static final String HOME_MARK_ID = "lvLogoAndChangeCity";
	public static final String HOME_CATEGORY_VIEWLIST_ID = "cateSelection";
	public static final String HOME_CATEGORY_VIEWLIST_ITEM_NAME_ID = "tvName";

	public static final String TAB_ID_HOME = "ivHomePage";
	public static final String TAB_ID_POST = "ivPostGoods";
	public static final String TAB_ID_MY = "ivMyCenter";
	
	public static final String AD_VIEWLIST_ID = "lvGoodsList";
	public static final String AD_VIEWLIST_MARK_ID = "rlListInfo";
	public static final String AD_DETAILVIEW_ID = "svDetail";
	public static final String AD_DETAILVIEW_PREV_ID = "btn_prev";
	public static final String AD_DETAILVIEW_NEXT_ID = "btn_next";
	public static final String AD_DETAILVIEW_TITLE_ID = "goods_tittle";
	
	public static final String SEARCH_TEXTVIEW_ID = "etSearch";
	public static final String SEARCH_BUTTON_ID = "btnCancel";
	
	public static final String CATEGORY_VIEWLIST_ID = "cateSelection";
	
	public static final String POST_SCROLLVIEW_ID = "scrollView1";
	public static final String POST_FORM_MARK_ID = "layout_txt";
	public static final String POST_CATEGORY_GRIDVIEW_ID = "gridcategory";
	public static final String POST_SECOND_CATEGORY_LISTVIEW_ID = "post_other_list";
	public static final int POST_CATEGORY_SELEC_INDEX = 0;
	public static final String POST_META_LISTVIEW_ID = "post_other_list";
	public static final String POST_META_ITEM_DISPLAY_ID = "postshow";
	public static final String POST_META_ITEM_ID = "post_select";
	public static final String POST_META_EDITTEXT_ID = "postinput";
	public static final String POST_DONE = "完成";
	public static final String POST_SEND = "立即发布";
	
	public static final String MY_LISTITEM_MYAD_ID = "rl_wosent";
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
	
	public static final String TEST_DATA_MOBILE = "13917067724";
	public static final String TEST_DATA_PASSWORD = "whonwyhw";
	public static final String TEST_DATA_CAT_WUPINJIAOYI = "物品交易";
	
	@SuppressWarnings("unchecked")
	public BaixingTestCase() throws Exception {
		super("com.quanleimu.activity", "com.quanleimu.activity.QuanleimuMainActivity");
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		// log("This is a test for log() method");
		assertEquals(true, getDevice().waitForActivity("QuanleimuMainActivity", 5000));
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
		findElementById(vId).doClick();
		TimeUnit.SECONDS.sleep(1);
	}
	
	public void openPostCategory(int firstCatIndex, int secondCatIndex) throws Exception {
		
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
		
		selectMetaByIndex(secondCatIndex, POST_SECOND_CATEGORY_LISTVIEW_ID);

	}
	
	public void openPostItemByIndex(int index) throws Exception {
		ViewGroupElement postLayout = findElementById(POST_FORM_MARK_ID, ViewGroupElement.class);
		assertNotNull(postLayout);
		ViewGroupElement listView = findElementById(POST_META_ITEM_ID, index, ViewGroupElement.class);
		assertNotNull(listView);
		listView.doClick();
		TimeUnit.SECONDS.sleep(2);
	}
	
	public void setMetaByIndex (int index, String value) throws Exception {
		ViewGroupElement postLayout = findElementById(POST_FORM_MARK_ID, ViewGroupElement.class);
		assertNotNull(postLayout);
		setOtherMetaByIndex(index, value);
	}
	public void setOtherMetaByIndex(int index, String value) throws Exception {
		TextViewElement tv = findElementById(POST_META_EDITTEXT_ID, index, TextViewElement.class);
		assertNotNull(tv);
		tv.setText(value);
		TimeUnit.SECONDS.sleep(1);
	}
	
	public void selectMetaByName(String displayName) throws Exception {
		int index = -1;
		AbsListViewElement listView = findElementById(POST_META_LISTVIEW_ID,
				AbsListViewElement.class);
		assertNotNull(listView);
		
		TextViewElement v3 = findElementByText(displayName, 0, true);
		assertNotNull(v3);
		Log.i(LOG_TAG, "postshow:v3" + v3.getText());
		v3.doClick();
		TimeUnit.SECONDS.sleep(1);
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
	
	public void openCategoryByIndex(int firstCatIndex, int secondCatIndex) throws Exception {
		openTabbar(TAB_ID_HOME);
		ViewElement hv = findElementById(HOME_MARK_ID);
		int i = 0;
		while(hv == null && i++ < 5) {
			getDevice().pressBack();
		}
		if (hv == null) return;
		AbsListViewElement catListView = findElementById(HOME_CATEGORY_VIEWLIST_ID,
				AbsListViewElement.class);
		ViewGroupElement catView = catListView.getChildByIndex(firstCatIndex,
				ViewGroupElement.class);
		TextViewElement catTextView = catView.findElementById(HOME_CATEGORY_VIEWLIST_ITEM_NAME_ID,
				TextViewElement.class);
		TimeUnit.MILLISECONDS.sleep(300);
		catView.doClick();
		TimeUnit.SECONDS.sleep(2);
		assertNotNull(catTextView.getText());
		assertTrue(catTextView.getText().length() > 0);
		
		AbsListViewElement subCatListView = findElementById(CATEGORY_VIEWLIST_ID,
				AbsListViewElement.class);
		ViewGroupElement subCatView = subCatListView.getChildByIndex(secondCatIndex,
				ViewGroupElement.class);
		subCatView.doClick();
		TimeUnit.SECONDS.sleep(5);
	}
	
	public ViewGroupElement openAdByIndex(int index) throws Exception {
		return openAdByIndex(index, AD_VIEWLIST_ID);
	}
	
	public ViewGroupElement openAdByIndex(int index, String viewListId) throws Exception {
		AbsListViewElement avl = findElementById(viewListId, AbsListViewElement.class);
		assertNotNull(avl);
		ViewGroupElement avi = avl.getChildByIndex(0, ViewGroupElement.class);
		int i = 0;
		int j = 0;
		while (avi != null) {
			avi = avl.getChildByIndex(j++, ViewGroupElement.class);
			ViewGroupElement _avi_ = avi.findElementById(AD_VIEWLIST_MARK_ID, ViewGroupElement.class);
			if (_avi_ == null) continue;
			if (i++ == index) {
				avi.doClick();
				TimeUnit.SECONDS.sleep(1);
				break;
			}
		}
		return avi;
	}
	
	public BXViewGroupElement showAd(int firstCatIndex, int secondCatIndex, int index) throws Exception {
		openTabbar(TAB_ID_MY);
		logon();
		openCategoryByIndex(firstCatIndex + 1, secondCatIndex);
		assertNotNull(openAdByIndex(index));
		BXViewGroupElement detailView = findElementById(AD_DETAILVIEW_ID,
				BXViewGroupElement.class);
		return detailView;
	}
	
	public void showNextAd(BXViewGroupElement view) throws Exception {
		TimeUnit.SECONDS.sleep(1);
		view.doTouch(-view.getWidth() + 20);
		TimeUnit.SECONDS.sleep(3);
	}
	
	public void showPrevAd(BXViewGroupElement view) throws Exception {
		TimeUnit.SECONDS.sleep(1);
		view.doTouch(view.getWidth() - 20);
		TimeUnit.SECONDS.sleep(3);
	}
	
	public int showMyAdList(String myListId, String myListCountId) throws Exception {
		openTabbar(TAB_ID_MY);
		ViewElement el = findElementById(myListId);
		assertNotNull(el);
		TextViewElement elc = findElementById(myListCountId, TextViewElement.class);
		el.doClick();
		TimeUnit.SECONDS.sleep(1);
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
}
