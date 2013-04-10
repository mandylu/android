package com.quanleimu.activity.test.powermock;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.baixing.network.NetworkUtil;
import com.baixing.network.api.BaseApiCommand;
import com.baixing.network.api.PlainRespHandler;
import com.baixing.network.impl.HttpNetworkConnector;

@RunWith(PowerMockRunner.class)
@PrepareForTest(
{
    BaseApiCommand.class, NetworkUtil.class, HttpNetworkConnector.class,PlainRespHandler.class
})
public class NetworkUtilTests extends TestCase {
	
	public void testNetworkNotActivity() {
		Context contextMock = PowerMock.createMock(Context.class);
		ConnectivityManager mgrMock = PowerMock.createMock(ConnectivityManager.class);
		
		EasyMock.expect(contextMock.getSystemService(Context.CONNECTIVITY_SERVICE)).andReturn(mgrMock);
		EasyMock.expect(mgrMock.getActiveNetworkInfo()).andReturn(null);
		
		PowerMock.replayAll();
		assertFalse(NetworkUtil.isNetworkActive(contextMock));
		PowerMock.verifyAll();
	}
	
	public void testNetworkActive() {
		Context contextMock = PowerMock.createMock(Context.class);
		ConnectivityManager mgrMock = PowerMock.createMock(ConnectivityManager.class);
		NetworkInfo infoMock = PowerMock.createMock(NetworkInfo.class);
		
		EasyMock.expect(contextMock.getSystemService(Context.CONNECTIVITY_SERVICE)).andReturn(mgrMock);
		EasyMock.expect(mgrMock.getActiveNetworkInfo()).andReturn(infoMock);
		
		PowerMock.replayAll();
		assertTrue(NetworkUtil.isNetworkActive(contextMock));
		PowerMock.verifyAll();
	}
	
}
