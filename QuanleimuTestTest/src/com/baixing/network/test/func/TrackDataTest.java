package com.baixing.network.test.func;

import com.baixing.network.api.ApiParams;

/**
 * 
 * test for API : <code>trackdata</code>
 * @author liuchong
 * 
 */
public class TrackDataTest extends FunctionTest {

	public void testTrackData() {
		ApiParams params = new ApiParams();
		params.zipRequest = true;
		
		String result = invokeApiSync("trackdata", false, params);
		
		assertNotNull(result);
		assertTrue(result.contains("成功收到数据"));
	}
}
