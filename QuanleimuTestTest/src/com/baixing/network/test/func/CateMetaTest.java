package com.baixing.network.test.func;

import com.baixing.network.api.ApiError;
import com.baixing.network.api.ApiParams;
import com.baixing.network.api.BaseApiCommand;
import com.baixing.network.api.BaseApiCommand.Callback;
import com.baixing.network.test.func.ResultHolder.STATE;

/**
 * 
 * test for API : <code>category_meta_filter</code>
 * @author liuchong
 * 
 */
public class CateMetaTest extends FunctionTest {
	public void testListing() {
		ApiParams params = new ApiParams();
		params.addParam("categoryEnglishName", "ershou");
		params.addParam("cityEnglishName", "shanghai");

		final ResultHolder<String> holder = invokeApi("category_meta_filter", true, params);
		assertEquals(STATE.SUCCED, holder.state);
		assertTrue(holder.result.contains("闵行"));
	}
	
	public void testListingFromCache() {

		assertEquals(0, cacheProxy.size());
		ApiParams params = new ApiParams();
		params.addParam("categoryEnglishName", "ershou");
		params.addParam("cityEnglishName", "shanghai");

		ResultHolder<String> first = invokeApi("category_meta_filter", true, params);
		assertEquals(STATE.SUCCED, first.state);
		assertTrue(first.result.contains("闵行"));
		
//		assertTrue(cacheProxy.hasValue(first.result));
		assertEquals(1, cacheProxy.size());
		
		//TODO: following code should be execute on offline state.
		params.useCache = true;
		ResultHolder<String> second = invokeApi("category_meta_filter", true, params);
		assertEquals(STATE.SUCCED, second.state);
		assertTrue(second.result.contains("闵行"));
		assertEquals(first.result,second.result);
	}
}


