package com.baixing.network.test.func;

import com.baixing.network.api.ApiConfiguration;
import com.baixing.network.api.ApiError;
import com.baixing.network.api.ApiParams;
import com.baixing.network.api.BaseApiCommand;
import com.baixing.network.api.BaseApiCommand.Callback;
import com.baixing.network.test.func.ResultHolder.STATE;
import com.baixing.network.test.util.MemoCacheStub;

import android.test.AndroidTestCase;

public class FunctionTest extends AndroidTestCase {
	
	protected MemoCacheStub cacheProxy = new MemoCacheStub();
	
	public void setUp() throws Exception {
		super.setUp();
		
		ApiConfiguration.config("www.baixing.com", cacheProxy, "api_mobile_android", "c6dd9d408c0bcbeda381d42955e08a3f");
		BaseApiCommand.init("fakeudid", "87104556", "3.1", "baixingtest", "shanghai");
	}
	
	public void tearDown() throws Exception {
		super.tearDown();
		
		cacheProxy.clear();
	}
	
	protected ResultHolder<String> invokeApi(String apiName, boolean isGet, ApiParams params) {
		final ResultHolder<String> holder = new ResultHolder<String>();
		BaseApiCommand cmd = BaseApiCommand.createCommand(apiName, isGet, params);
		cmd.execute(getContext(), new Callback() {

			@Override
			public void onNetworkDone(String apiName, String responseData) {
				holder.state = STATE.SUCCED;
				holder.result = responseData;
			}

			@Override
			public void onNetworkFail(String apiName, ApiError error) {
				holder.state = STATE.FAIL;
				holder.result = error.getMsg();
			}
		});
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			fail("thread sleep issue " + e.getMessage());
		}
		
		return holder;
	}
	
	protected String invokeApiSync(String apiName, boolean isGet, ApiParams params) {
		BaseApiCommand cmd = BaseApiCommand.createCommand(apiName, isGet, params);
		return cmd.executeSync(getContext());
	}
	
}
