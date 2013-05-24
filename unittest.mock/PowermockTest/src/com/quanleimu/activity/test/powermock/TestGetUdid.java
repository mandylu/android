package com.quanleimu.activity.test.powermock;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.modules.junit4.PowerMockRunner;

import android.content.Context;
import android.content.SharedPreferences;

@RunWith(PowerMockRunner.class)
public class TestGetUdid{

//	@Test
//	public void testGetDeviceUidFromPref()
//	{
//		Context cxtMock = PowerMock.createMock(Context.class);
//		
////		SharedPreferences prefMock = PowerMock.createMock(SharedPreferences.class);
////		Editor editMock = PowerMock.createMock(Editor.class);
////		cxtMock.getSharedPreferences(Util.PREF_DEVICE_ID, Context.MODE_PRIVATE);
////		EasyMock.expect(cxtMock.getSharedPreferences(Util.PREF_DEVICE_ID, Context.MODE_PRIVATE)).andReturn(prefMock);
//////		EasyMock.expect(prefMock.edit()).andReturn(editMock);
////		EasyMock.expect(prefMock.contains(Util.PREF_KEY_DEVICE_ID)).andReturn(true);
////		EasyMock.expect(prefMock.getString(Util.PREF_KEY_DEVICE_ID, null)).andReturn("123456");
//		
//		PowerMock.replayAll();
//		TestCase.assertEquals("123456", Util.getDeviceUdid(cxtMock));
//		PowerMock.verifyAll();
//		
//	}
	
	@Test 
	public void testIt() {
		
	}
	
	
}
