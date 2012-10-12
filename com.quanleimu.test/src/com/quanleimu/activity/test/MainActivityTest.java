package com.quanleimu.activity.test;

import org.athrun.android.framework.AthrunTestCase;
import org.athrun.android.framework.Test;

import java.util.concurrent.TimeUnit;

import org.athrun.android.framework.viewelement.AbsListViewElement;
import org.athrun.android.framework.viewelement.TextViewElement;
import org.athrun.android.framework.viewelement.ViewElement;
import org.athrun.android.framework.viewelement.ViewGroupElement;

public class MainActivityTest extends AthrunTestCase {
	private static final String LOG_TAG = "MainActivityTest";

	public MainActivityTest() throws Exception {
		super("com.quanleimu.activity", "com.quanleimu.activity.QuanleimuMainActivity");
		AthrunTestCase.setMaxTimeToFindView(10000);
	}
	
	@Test
	public void testWaitForActivity() throws Exception {
		// log("This is a test for log() method");
		assertEquals(true, getDevice().waitForActivity("QuanleimuMainActivity", 5000));
	}
	
	@Test
	public void testSearchClick() throws Exception {
		
		findElementById("etSearch").doClick();
		TimeUnit.SECONDS.sleep(1);
		TextViewElement etSearchText = findElementById("etSearch",
				TextViewElement.class);
		etSearchText.setText("iphone");

		findElementById("btnCancel").doClick();
		
		TimeUnit.SECONDS.sleep(10);
		assertEquals("iphone", etSearchText.getText());
		getDevice().pressMenu();
		TimeUnit.SECONDS.sleep(2);
	}
	
	@Test
	public void testCategoryClick() throws Exception {
		AbsListViewElement catListView = findElementById("cateSelection",
				AbsListViewElement.class);
		ViewGroupElement catView = catListView.getChildByIndex(3,
				ViewGroupElement.class);
		TextViewElement catTextView = catView.findElementById("tvName",
				TextViewElement.class);
		TimeUnit.MILLISECONDS.sleep(300);
		catView.doClick();
		TimeUnit.SECONDS.sleep(3);
		assertEquals("房屋租售", catTextView.getText());
		
		AbsListViewElement subCatListView = findElementById("cateSelection",
				AbsListViewElement.class);
		ViewGroupElement subCatView = subCatListView.getChildByIndex(1,
				ViewGroupElement.class);
		subCatView.doClick();
		TimeUnit.SECONDS.sleep(10);
		getDevice().pressBack();
		TimeUnit.SECONDS.sleep(1);
		getDevice().pressBack();
		TimeUnit.SECONDS.sleep(2);
	}
	
	@Test
	public void testScrollToNextScreen() throws Exception {
		AbsListViewElement catListView = findElementById("cateSelection",
				AbsListViewElement.class);
		assertEquals(3, catListView.getLastVisiblePosition());
		catListView.scrollToNextScreen();
		TimeUnit.SECONDS.sleep(3);
		assertEquals(4, catListView.getFirstVisiblePosition());
		catListView.scrollToNextScreen();
		assertEquals(11, catListView.getLastVisiblePosition());
		TimeUnit.SECONDS.sleep(3);
		assertEquals(7, catListView.getFirstVisiblePosition());
	}
	
	@Test
	public void testPost() throws Exception {
		findElementById("ivPostGoods").doClick();
		AbsListViewElement gridView = findElementById("gridcategory",
				AbsListViewElement.class);
		assertNotNull(gridView);
		ViewGroupElement item = gridView.getChildByIndex(0, ViewGroupElement.class);
		assertNotNull(item);
		TextViewElement view = item.getChildByIndex(1, TextViewElement.class);
		assertEquals("物品交易", view.getText());
		item.doClick();
		TimeUnit.SECONDS.sleep(5);
	}
}
