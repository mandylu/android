package com.baixing.network.test.func;

import java.util.List;

import com.baixing.entity.Ad;
import com.baixing.entity.Ad.EDATAKEYS;
import com.baixing.entity.AdList;
import com.baixing.jsonutil.JsonUtil;
import com.baixing.network.api.ApiConfiguration;
import com.baixing.network.api.ApiParams;
import com.baixing.network.api.BaseApiCommand;

public class AdRefreshTest extends FunctionTest {
	
	
	public void testRefresh() {
		ApiConfiguration.config("shanghai.liuchong.baixing.com", cacheProxy, "api_mobile_android", "c6dd9d408c0bcbeda381d42955e08a3f");
		BaseApiCommand.init("fakeudid", "87104556", "3.1", "baixingtest", "shanghai");
		
		ApiParams params = new ApiParams();
//		params.appendAuthInfo("13512135857", "123456");
		
		BaseApiCommand cmd = BaseApiCommand.createCommand("ad_user_list", true, params);
		String result = cmd.executeSync(getContext());
		
		AdList list = JsonUtil.getGoodsListFromJsonByJackson(result);
		List<Ad> ads = list.getData();
		assertTrue(ads.size() > 0);
		final String adId = ads.get(0).getValueByKey(EDATAKEYS.EDATAKEYS_ID);
		
		ApiParams refreshParams = new ApiParams();
		refreshParams.addParam("adId", adId);
		refreshParams.addParam("pay", 1);
		BaseApiCommand refreshCmd = BaseApiCommand.createCommand("ad_refresh", true, refreshParams);
		String refreshResult = refreshCmd.executeSync(getContext());
		assertFalse("refresh should not succed as user is not authenticated. But server message is " + refreshResult,  refreshResult.contains("\"code\":0"));
	}
	
	public void testRefreshWithAdId() {
		ApiConfiguration.config("shanghai.liuchong.baixing.com", cacheProxy, "api_mobile_android", "c6dd9d408c0bcbeda381d42955e08a3f");
		BaseApiCommand.init("fakeudid", "87104556", "3.1", "baixingtest", "shanghai");
		
		ApiParams params = new ApiParams();
//		params.appendAuthInfo("13512135857", "123456");
		
		BaseApiCommand cmd = BaseApiCommand.createCommand("ad_user_list", true, params);
		String result = cmd.executeSync(getContext());
		
		AdList list = JsonUtil.getGoodsListFromJsonByJackson(result);
		List<Ad> ads = list.getData();
		assertTrue(ads.size() > 0);
		final String adId = ads.get(0).getValueByKey(EDATAKEYS.EDATAKEYS_ID);
		
		//First check, do not provide adId.
		ApiParams refreshParams = new ApiParams();
		refreshParams.addParam("adId", adId);
		BaseApiCommand refreshCmd = BaseApiCommand.createCommand("ad_refresh", true, refreshParams);
		String refreshResult = refreshCmd.executeSync(getContext());
		assertTrue(refreshResult.contains("刷新可以让该信息重回顶部，您本月还能刷新该信息"));
	}
	
	public void testRefreshNoAdId() {
		ApiConfiguration.config("shanghai.liuchong.baixing.com", cacheProxy, "api_mobile_android", "c6dd9d408c0bcbeda381d42955e08a3f");
		BaseApiCommand.init("fakeudid", "87104556", "3.1", "baixingtest", "shanghai");
		
		//First check, do not provide adId.
		ApiParams refreshParams = new ApiParams();
//		refreshParams.addParam("adId", adId);
		BaseApiCommand refreshCmd = BaseApiCommand.createCommand("ad_refresh", true, refreshParams);
		String refreshResult = refreshCmd.executeSync(getContext());
		assertTrue(refreshResult.contains("\"code\":111"));
	}
	
}
