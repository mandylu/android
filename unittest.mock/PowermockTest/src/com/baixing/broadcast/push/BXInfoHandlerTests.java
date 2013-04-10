package com.baixing.broadcast.push;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

import org.easymock.EasyMock;
import org.easymock.LogicalOperator;
import org.json.JSONObject;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import android.content.Context;
import android.os.Bundle;

import com.baixing.broadcast.CommonIntentAction;
import com.baixing.broadcast.NotificationIds;
import com.baixing.broadcast.push.BXInfoHandler;

import junit.framework.TestCase;

@RunWith(PowerMockRunner.class)
@PrepareForTest ({com.baixing.util.ViewUtil.class, Bundle.class})

public class BXInfoHandlerTests extends TestCase {

	public void testHandleMessage() throws Exception {
		//Invoke parameter.
		final String message = "{a:\"home\",t:\"title\", d:{content:\"abc\"}}";
		
		Context contextMock = PowerMock.createMock(Context.class);
		
		BXInfoHandler handler = new BXInfoHandler(contextMock);

//		JSONObject json = PowerMock.createMockAndExpectNew(JSONObject.class, new Object[] {message});
//		EasyMock.expect(json.getJSONObject("d")).andReturn(null);
//		EasyMock.expect(json.getString("contet")).andReturn("");
//		EasyMock.expect(json.getString("d")).andReturn("");
//		EasyMock.expect(json.get("a")).andReturn("home");
		
		//create bundle mock.
		Bundle bundleMock = new Bundle(); //android.jar Stub exception.
		bundleMock.putString("data", "{\"content\":\"abc\"}");
		bundleMock.putString("page", "home");
		
		PowerMock.mockStatic(com.baixing.util.ViewUtil.class);
		com.baixing.util.ViewUtil.putOrUpdateNotification(EasyMock.eq(contextMock), EasyMock.eq(NotificationIds.NOTIFICATION_ID_BXINFO), 
				EasyMock.eq(CommonIntentAction.ACTION_NOTIFICATION_BXINFO), EasyMock.eq("title"), EasyMock.eq("abc"), EasyMock.cmp(bundleMock, new BundleComp(), LogicalOperator.EQUAL), EasyMock.eq(false));
		PowerMock.replayAll();
		handler.processMessage(message);
		PowerMock.verifyAll();
	}
	
	class BundleComp implements Comparator<Bundle> {

		@Override
		public int compare(Bundle o1, Bundle o2) {
			if (o1 == null && o2 == null) {
				return 0;
			} else if (o1 == null || o2 == null) {
				return 1;
			}
			
			Set keys = o1.keySet();
			Iterator keyIt = keys.iterator();
			while (keyIt.hasNext()) {
				String key = (String) keyIt.next();
				if (!o1.get(key).equals(o2.get(key))) {
					return 1;
				}
			}
			
			return 0;
		}
		
	}
}
