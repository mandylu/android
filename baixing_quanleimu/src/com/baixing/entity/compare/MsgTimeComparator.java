//liuchong@baixing.com
package com.baixing.entity.compare;

import java.util.Comparator;

import com.baixing.entity.ChatMessage;

public class MsgTimeComparator implements Comparator<ChatMessage> {

	@Override
	public int compare(ChatMessage lhs, ChatMessage rhs) 
	{
		if (lhs.getTimestamp() < rhs.getTimestamp())
		{
			return -1;
		}
		else if (lhs.getTimestamp() > rhs.getTimestamp())
		{
			return 1;
		}
		
		return 0;
	}

}
