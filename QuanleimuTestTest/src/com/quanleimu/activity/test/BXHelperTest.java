package com.quanleimu.activity.test;

import java.util.List;

import android.test.AndroidTestCase;

import com.quanleimu.util.BXStatus;
import com.quanleimu.util.BXStatusHelper;
import com.quanleimu.util.Util;

public class BXHelperTest extends AndroidTestCase {

	public void setUp()
	{
		try {
			super.setUp();
			BXStatusHelper.getInstance().clearData();
			Util.clearData(getContext(), BXStatusHelper.SERIALIZABLE_PATH);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	public void testLoadAndStore()
	{
		
		BXStatusHelper.getInstance().clearData();
		BXStatusHelper.getInstance().load(getContext());
		List<BXStatus> dataFromFile = (List<BXStatus>) Util.loadDataFromLocate(getContext(), BXStatusHelper.SERIALIZABLE_PATH);
		assertNull(dataFromFile);
		
		BXStatusHelper.getInstance().increase(BXStatusHelper.TYPE_AD_VIEW, "123");
		BXStatusHelper.getInstance().increase(BXStatusHelper.TYPE_CALL, null);
		BXStatusHelper.getInstance().increase(BXStatusHelper.TYPE_ADD_CONTACT, null);
		BXStatusHelper.getInstance().increase(BXStatusHelper.TYPE_SIXIN_SEND, null);
		BXStatusHelper.getInstance().increase(BXStatusHelper.TYPE_SMS_SEND, null);
		BXStatusHelper.getInstance().increase(BXStatusHelper.TYPE_WEIBO_SEND, null);
		BXStatusHelper.getInstance().increase(BXStatusHelper.TYPE_WEIXIN_SEND, null);
		
		BXStatusHelper.getInstance().store(getContext());
		dataFromFile = (List<BXStatus>) Util.loadDataFromLocate(getContext(), BXStatusHelper.SERIALIZABLE_PATH);
		assertNotNull(dataFromFile);
		assertEquals(BXStatusHelper.getInstance().findStatus(BXStatusHelper.TYPE_CALL).getCount(), 1);
		assertEquals(BXStatusHelper.getInstance().findStatus(BXStatusHelper.TYPE_AD_VIEW).getCount(), 1);
		assertEquals(BXStatusHelper.getInstance().findStatus(BXStatusHelper.TYPE_AD_VIEW).description(), "123,");
		assertEquals(BXStatusHelper.getInstance().findStatus(BXStatusHelper.TYPE_ADD_CONTACT).getCount(), 1);
		assertEquals(BXStatusHelper.getInstance().findStatus(BXStatusHelper.TYPE_SIXIN_SEND).getCount(), 1);
		assertEquals(BXStatusHelper.getInstance().findStatus(BXStatusHelper.TYPE_WEIBO_SEND).getCount(), 1);
		assertEquals(BXStatusHelper.getInstance().findStatus(BXStatusHelper.TYPE_WEIXIN_SEND).getCount(), 1);
		assertEquals(BXStatusHelper.getInstance().findStatus(BXStatusHelper.TYPE_SMS_SEND).getCount(), 1);
	}
}
