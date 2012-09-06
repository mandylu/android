package com.quanleimu.activity.test;

import java.util.List;

import android.test.AndroidTestCase;

import com.quanleimu.util.BXStats;
import com.quanleimu.util.BXStatsHelper;
import com.quanleimu.util.Util;

public class BXHelperTest extends AndroidTestCase {

	public void setUp()
	{
		try {
			super.setUp();
			BXStatsHelper.getInstance().clearData();
			Util.clearData(getContext(), BXStatsHelper.SERIALIZABLE_PATH);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	public void testLoadAndStore()
	{
		
		BXStatsHelper.getInstance().clearData();
		BXStatsHelper.getInstance().load(getContext());
		List<BXStats> dataFromFile = (List<BXStats>) Util.loadDataFromLocate(getContext(), BXStatsHelper.SERIALIZABLE_PATH);
		assertNull(dataFromFile);
		
		BXStatsHelper.getInstance().increase(BXStatsHelper.TYPE_AD_VIEW, "123");
		BXStatsHelper.getInstance().increase(BXStatsHelper.TYPE_CALL, null);
		BXStatsHelper.getInstance().increase(BXStatsHelper.TYPE_ADD_CONTACT, null);
		BXStatsHelper.getInstance().increase(BXStatsHelper.TYPE_SIXIN_SEND, null);
		BXStatsHelper.getInstance().increase(BXStatsHelper.TYPE_SMS_SEND, null);
		BXStatsHelper.getInstance().increase(BXStatsHelper.TYPE_WEIBO_SEND, null);
		BXStatsHelper.getInstance().increase(BXStatsHelper.TYPE_WEIXIN_SEND, null);
		
		BXStatsHelper.getInstance().store(getContext());
		dataFromFile = (List<BXStats>) Util.loadDataFromLocate(getContext(), BXStatsHelper.SERIALIZABLE_PATH);
		assertNotNull(dataFromFile);
		assertEquals(BXStatsHelper.getInstance().findStatus(BXStatsHelper.TYPE_CALL).getCount(), 1);
		assertEquals(BXStatsHelper.getInstance().findStatus(BXStatsHelper.TYPE_AD_VIEW).getCount(), 1);
		assertEquals(BXStatsHelper.getInstance().findStatus(BXStatsHelper.TYPE_ADD_CONTACT).getCount(), 1);
		assertEquals(BXStatsHelper.getInstance().findStatus(BXStatsHelper.TYPE_SIXIN_SEND).getCount(), 1);
		assertEquals(BXStatsHelper.getInstance().findStatus(BXStatsHelper.TYPE_WEIBO_SEND).getCount(), 1);
		assertEquals(BXStatsHelper.getInstance().findStatus(BXStatsHelper.TYPE_WEIXIN_SEND).getCount(), 1);
		assertEquals(BXStatsHelper.getInstance().findStatus(BXStatsHelper.TYPE_SMS_SEND).getCount(), 1);
	}
}
