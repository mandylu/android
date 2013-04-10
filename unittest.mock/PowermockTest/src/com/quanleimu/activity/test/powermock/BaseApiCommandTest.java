package com.quanleimu.activity.test.powermock;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Pair;

import com.baixing.network.NetworkUtil;
import com.baixing.network.api.ApiParams;
import com.baixing.network.api.BaseApiCommand;
import com.baixing.network.api.PlainRespHandler;
import com.baixing.network.impl.GetRequest;
import com.baixing.network.impl.HttpNetworkConnector;

@RunWith(PowerMockRunner.class)
@PrepareForTest(
{
    BaseApiCommand.class, NetworkUtil.class, HttpNetworkConnector.class,PlainRespHandler.class, TargetClass.class, Pair.class
})
public class BaseApiCommandTest extends TestCase {
	
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
	
	public void testExecuteGet() throws Exception {
		String apiName = "testApi";
		ApiParams params = new ApiParams();
		
		BaseApiCommand cmd = BaseApiCommand.createCommand(apiName, true, params);
		
		PowerMock.mockStatic(NetworkUtil.class);
		EasyMock.expect(NetworkUtil.getTimeStamp()).andReturn(new Long(1234567));
		EasyMock.expect(NetworkUtil.getMD5("timestamp=" + 1234567)).andReturn("md5_of_timestamp");
		
		GetRequest getMock = PowerMock.createMock(GetRequest.class);
		HttpNetworkConnector connectorMock = PowerMock.createMock(HttpNetworkConnector.class);
		PlainRespHandler handlerMock = PowerMock.createMock(PlainRespHandler.class);
		
		Map<String, String> map = new HashMap<String, String>();
		map.put(ApiParams.KEY_TIMESTAMP, "1234567");
		map.put(ApiParams.KEY_ACCESSTOKEN, "md5_of_timestamp");
		PowerMock.expectNiceNew(GetRequest.class, EasyMock.eq("http://www.baixing.com/api/mobile.testApi/?"), EasyMock.anyObject(), EasyMock.eq(false)).andReturn(getMock);
		
		PowerMock.mockStatic(HttpNetworkConnector.class);
		EasyMock.expect(HttpNetworkConnector.connect()).andReturn(connectorMock);
		
		PowerMock.expectNew(PlainRespHandler.class).andReturn(handlerMock);
		
//		final String expectedResult = "{simple result.}";
//		Pair<Boolean, String> p = new Pair<Boolean, String>(true, expectedResult);
		EasyMock.expect(connectorMock.sendHttpRequestSync(null, getMock, handlerMock)).andReturn(null);

		PowerMock.replayAll();
		String result = cmd.executeSync(null);
		PowerMock.verifyAll();
		
	}
	
    public void testCreateDirectoryStructure_ok() throws Exception {
		final String path = "directoryPath";
		File fileMock = EasyMock.createMock(File.class);

		TargetClass tested = new TargetClass();

		PowerMock.expectNew(File.class, path).andReturn(fileMock);

		EasyMock.expect(fileMock.exists()).andReturn(false);
		EasyMock.expect(fileMock.mkdirs()).andReturn(true);

		PowerMock.replay(fileMock, File.class);
		assertTrue(tested.createDirectoryStructure(path));
		PowerMock.verify(fileMock, File.class);
    }
}
