package com.baixing.network.test.func;

import com.baixing.network.api.ApiParams;
import com.baixing.network.test.func.ResultHolder.STATE;

/**
 * 
 * test for API : <code>user_login</code>
 * @author liuchong
 * 
 */
public class LoginTest extends FunctionTest {

	public void testLogin() {
		ApiParams params = new ApiParams();
		params.addParam("mobile", "13512135857");
		params.addParam("nickname", "13512135857");
		params.addParam("password", "123456");
		
		
		
		final ResultHolder<String> result = invokeApi("user_login", false, params);
		
		assertEquals(STATE.SUCCED, result.state);
		assertTrue(result.result.length() > 0);
		assertTrue(result.result.contains("用户登录成功")); //Assert user id of test account.
	}
	
	public void testLoginFail() {
		ApiParams params = new ApiParams();
		params.addParam("mobile", "13512135857");
		params.addParam("nickname", "13512135857");
		params.addParam("password", "badpass");
		
		final ResultHolder<String> result = invokeApi("user_login", false, params);
		assertEquals(STATE.FAIL, result.state);
		assertEquals("密码错误", result.result);
	}
	
	public void testSyncLoginFail() {
		ApiParams params = new ApiParams();
		params.addParam("mobile", "13512135857");
		params.addParam("nickname", "13512135857");
		params.addParam("password", "badpassword");
		
		String result = invokeApiSync("user_login", false, params);
		assertTrue(result.contains("密码错误"));
	}
}
