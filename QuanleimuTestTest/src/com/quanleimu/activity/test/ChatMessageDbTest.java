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
		
		ChatMessageDatabase.prepareDB(getContext());
		ChatMessageDatabase.clearDatabase();
	}
	
	public void testStoreAndQuery()
	{
		ChatMessageDatabase.prepareDB(getContext());
		ChatMessageDatabase.clearDatabase();
		
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
		
		ChatMessage msg = ChatMessageDatabase.queryMessageByMsgId("999999");
		assertNotNull(msg);
		assertEquals(originalMessage.getAdId(), msg.getAdId());
		assertEquals(originalMessage.getFrom(), msg.getFrom());
		assertEquals(originalMessage.getId(), msg.getId());
		assertEquals(originalMessage.getMessage(), msg.getMessage());
		assertEquals(originalMessage.getSession(), msg.getSession());
		assertEquals(originalMessage.getTimestamp(), msg.getTimestamp());
		assertEquals(originalMessage.getTo(), msg.getTo());
		
//		ChatMessageDatabase.deleteMsgOlderthan(timestamp + 10); 
//		msgList = ChatMessageDatabase.queryMessageBySession("999999");
//		assertTrue("should have message after store", msgList.size() == 0);
	}
	
	
	public void testRemoveOlderThan()
	{
		ChatMessage msg1 = mock("111", 111);
		ChatMessage msg2 = mock("222", 222);
		ChatMessage msg3 = mock("333", 333);
		
		ChatMessageDatabase.storeMessage(msg1);
		ChatMessageDatabase.storeMessage(msg2);
		ChatMessageDatabase.storeMessage(msg3);
		
		ChatMessage cha1 = ChatMessageDatabase.queryMessageByMsgId("111");
		ChatMessage cha2 = ChatMessageDatabase.queryMessageByMsgId("222");
		assertNotNull(cha1);
		assertNotNull(cha2);
		
		ChatMessageDatabase.clearOldMessage(2);
		cha1 = ChatMessageDatabase.queryMessageByMsgId("111");
		cha2 = ChatMessageDatabase.queryMessageByMsgId("222");
		assertNull(cha1);
		assertNotNull(cha2);
		
	}
	
	static ChatMessage mock(String msgId, long timestamp)
	{
		ChatMessage originalMessage = new ChatMessage();
		originalMessage.setFrom("123456");
		originalMessage.setTo("456789");
		originalMessage.setId(msgId);
		originalMessage.setAdId("111");
		originalMessage.setMessage("message");
		originalMessage.setSession("999999");
		originalMessage.setTimestamp(timestamp);
		
		return originalMessage;
	}
	

}
