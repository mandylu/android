package com.quanleimu.activity.test;

import java.util.List;

import android.test.AndroidTestCase;

import com.quanleimu.database.ChatMessageDatabase;
import com.quanleimu.entity.ChatMessage;

public class ChatMessageDbTest extends AndroidTestCase {
	
	public void setUp()
	{
		try {
			super.setUp();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void testStoreAndQuery()
	{
		final long timestamp = System.currentTimeMillis()/1000 - 5 * 3600 * 24; //Five days ago
		ChatMessage originalMessage = new ChatMessage();
		originalMessage.setFrom("123456");
		originalMessage.setTo("456789");
		originalMessage.setId("999999");
		originalMessage.setAdId("111");
		originalMessage.setMessage("message");
		originalMessage.setSession("999999");
		originalMessage.setTimestamp(timestamp);

		ChatMessageDatabase.prepareDB(getContext());
		ChatMessageDatabase.storeMessage(originalMessage);
		
		List<ChatMessage> msgList = ChatMessageDatabase.queryMessageBySession("999999");
		assertTrue("should have message after store", msgList.size() > 0);
		assertEquals(originalMessage.getAdId(), msgList.get(0).getAdId());
		assertEquals(originalMessage.getFrom(), msgList.get(0).getFrom());
		assertEquals(originalMessage.getId(), msgList.get(0).getId());
		assertEquals(originalMessage.getMessage(), msgList.get(0).getMessage());
		assertEquals(originalMessage.getSession(), msgList.get(0).getSession());
		assertEquals(originalMessage.getTimestamp(), msgList.get(0).getTimestamp());
		assertEquals(originalMessage.getTo(), msgList.get(0).getTo());
		
		ChatMessageDatabase.deleteMsgOlderthan(timestamp + 10); 
		msgList = ChatMessageDatabase.queryMessageBySession("999999");
		assertTrue("should have message after store", msgList.size() == 0);
	}
	

}
