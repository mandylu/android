package com.quanleimu.adapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.quanleimu.activity.R;
import com.quanleimu.entity.ChatMessage;
import com.quanleimu.entity.compare.MsgTimeComparator;
import com.quanleimu.imageCache.SimpleImageLoader;
import com.quanleimu.view.fragment.GoodDetailFragment;

public class ChatMessageAdapter extends BaseAdapter {

	private List<ChatMessage> msgList = new ArrayList<ChatMessage>();
	private String myId;
	private LayoutInflater inflater;
	private final long MSG_GROUP_TIME = 12* 60 * 60;
	
	public ChatMessageAdapter(String myId)
	{
		this.myId = myId;
	}
	
	public void appendData(ChatMessage msg) //FIXME: shall we do merge???
	{
		List<ChatMessage> newList = new ArrayList<ChatMessage>();
		newList.addAll(msgList);
		
		if (msgList.size() > 0)
		{
			ChatMessage lastMsg = msgList.get(msgList.size()-1);
			long lastTime = lastMsg.getTimestamp();
			if (lastTime > msg.getTimestamp())
			{
				msg.setTimestamp(lastTime + 1); //
			}
		}
		newList.add(msg);
		
		refreshData(newList);
	}
	
	public void appendData(List<ChatMessage> newData, boolean head)
	{
		Collections.sort(newData, new MsgTimeComparator());
		
		if (head)
		{
			msgList.addAll(0, newData);
		}
		else
		{
			msgList.addAll(newData);
		}
	}
	
	
	public void refreshData(List<ChatMessage> newList)
	{
		Collections.sort(newList, new MsgTimeComparator());
		msgList = newList;
		
		this.notifyDataSetChanged();
	}

	
	
	@Override
	public int getCount() {
		return msgList.size();
	}
	
	public int getMyMsgCount() {
		int myCount = 0;
		for (ChatMessage msg : msgList) {
			if (msg.getFrom().equals(myId)) {
				myCount++;
			}
		}
		return myCount;
	}

	@Override
	public Object getItem(int arg0) {
		return msgList.get(arg0);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		if (inflater == null)
		{
			inflater = LayoutInflater.from(parent.getContext());
		}
		
		View item = convertView;
		if (item == null)
		{
			item = inflater.inflate(R.layout.item_im_message, null);
		}
		item.findViewById(R.id.tvTime).setVisibility(View.GONE);
		ChatMessage msg = msgList.get(position);
		if(position >= 0){
			ChatMessage preMsg = position == 0 ? null : msgList.get(position - 1);
			if(position == 0 || (preMsg != null && (Long.valueOf(msg.getTimestamp()) - Long.valueOf(preMsg.getTimestamp()) >= MSG_GROUP_TIME ))){
				Date date = new Date(Long.valueOf(msg.getTimestamp()) * 1000);
				SimpleDateFormat sDateFormat = new SimpleDateFormat("MM-dd HH:mm");
				String dt = sDateFormat.format(date);
				((TextView)item.findViewById(R.id.tvTime)).setText(dt);
				item.findViewById(R.id.tvTime).setVisibility(View.VISIBLE);
			}
		}
		final boolean isMine = myId.equalsIgnoreCase(msg.getFrom());
		
		loadMessageItem(isMine, item, msg);
		
		
		return item;
	}
	
	
	private void loadMessageItem(boolean isMine, View parent, ChatMessage msg)
	{
		View send = parent.findViewById(R.id.my_item);
		View receive = parent.findViewById(R.id.received_item);
		
		send.setVisibility(isMine ? View.VISIBLE : View.GONE);
		receive.setVisibility(isMine ? View.GONE : View.VISIBLE);
		
		View msgItem = isMine ? send : receive;
		View msgParent = msgItem.findViewById(R.id.im_message_content_parent);
		TextView textView = (TextView) msgItem.findViewById(R.id.im_message_content);
		textView.setText(msg.getMessage());
		
		msgParent.setPadding(msgParent.getPaddingLeft()/3, msgParent.getPaddingTop()/6, msgParent.getPaddingRight()/3, msgParent.getPaddingBottom()/6);		
	}

}
