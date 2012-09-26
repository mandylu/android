package com.quanleimu.view;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.quanleimu.activity.R;
import com.quanleimu.database.ChatMessageDatabase;
import com.quanleimu.entity.ChatMessage;
import com.quanleimu.entity.ChatSession;
import com.quanleimu.util.Communication;
import com.quanleimu.util.ParameterHolder;
import com.quanleimu.util.Util;
import com.quanleimu.util.ViewUtil;
import com.quanleimu.view.PersonalCenterEntryView.GetPersonalSessionsThread;
import com.quanleimu.widget.PullToRefreshListView;
import com.quanleimu.adapter.SessionListAdapter;
import com.quanleimu.broadcast.CommonIntentAction;
import com.quanleimu.broadcast.NotificationIds;

public class SessionListView extends BaseView implements View.OnClickListener, PullToRefreshListView.OnRefreshListener, OnItemClickListener{
	private List<ChatSession> sessions = null;
	private BroadcastReceiver chatMessageReceiver;
	
	
	private static final int MSG_NEW_MESSAGE = 1;
	private static final int MSG_NEW_SESSION = 2;
	private static final int MSG_NEW_SESSION_FAIL = 3;
	
	public SessionListView(Context ctx, List<ChatSession> sessions){
		super(ctx);
		this.sessions = sessions;
		init();
	}
	
	private void init(){
		LayoutInflater inflater = LayoutInflater.from(getContext());
		this.addView(inflater.inflate(R.layout.sessionlist, null));

//		PullToRefreshListView plv = (PullToRefreshListView)this.findViewById(R.id.lv_sessionlist);
		ListView plv = (ListView)this.findViewById(R.id.lv_sessionlist);
		SessionListAdapter adapter = new SessionListAdapter(this.getContext(), sessions);
		plv.setAdapter(adapter);
		plv.setOnItemClickListener(this);
//		plv.setPullToRefreshEnabled(false);
		
		if (this.sessions == null || this.sessions.size() == 0)
		{
			findViewById(R.id.session_loading).setVisibility(View.VISIBLE);
		}
		
		ViewUtil.removeNotification(getContext(), NotificationIds.NOTIFICATION_ID_CHAT_MESSAGE);
	}
	
	private SessionListAdapter getAdapter()
	{
		ListView plv = (ListView)this.findViewById(R.id.lv_sessionlist);
		return (SessionListAdapter) plv.getAdapter();
	}
	
	
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		ListView plv = (ListView)this.findViewById(R.id.lv_sessionlist);
		plv.requestFocus();
		
		syncSessions(Util.getMyId(getContext()));
		
		registerReceiver();
		BaseAdapter adapter = this.getAdapter();
		if(adapter != null){
			adapter.notifyDataSetChanged();
		}
		
	}
	
	protected void onDetachedFromWindow()
	{
		super.onDetachedFromWindow();
		unregisterReceiver();
	}
	
	public void onDestroy()
	{
		super.onDestroy();
		chatMessageReceiver = null;
	}

	@Override
	public void onClick(View v) {
		switch(v.getId())
		{
			case R.id.btnCancel:
				break;
		}
	}
	
	@Override
	public TitleDef getTitleDef(){
		TitleDef title = new TitleDef();
		title.m_leftActionHint = "返回";
		title.m_leftActionStyle = EBUTT_STYLE.EBUTT_STYLE_BACK;
//		title.m_rightActionHint = "编辑";
		title.m_title = "私信";
		title.m_visible = true;
		return title;
	}
	
	@Override
	public TabDef getTabDef(){
		TabDef tab = new TabDef();
		tab.m_visible = true;
		tab.m_tabSelected = ETAB_TYPE.ETAB_TYPE_MINE;
		return tab;
	}

	@Override
	public void onRefresh() {
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View item, int pos, long id) 
	{
		ChatSession session = (ChatSession) arg0.getAdapter().getItem(pos);
		Bundle bundle = new Bundle();
		bundle.putString("sessionId", session.getSessionId());
		bundle.putString("receiverId", session.getOppositeId());
		bundle.putString("adId", session.getAdId());
		bundle.putString("adTitle", session.getAdTitle());
		bundle.putBoolean("forceSync", true);
		bundle.putString("receiverNick", session.getOppositeNick());
		
		updateSessionInfo(session, item);
		
		m_viewInfoListener.onNewView(new TalkView(getContext(), bundle));
	}
	
	private void updateSessionInfo(ChatSession session, View item)
	{
		ChatMessageDatabase.prepareDB(getContext());
		ChatMessage lastMessage = ChatMessageDatabase.getLastMessage(session.getSessionId());
		if (lastMessage != null )
		{
			item.findViewById(R.id.unreadicon).setVisibility(View.INVISIBLE);
			session.setTimeStamp(lastMessage.getTimestamp() + "");
			session.setLastMsg(lastMessage.getMessage());
		}
	}
	 
	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what)
			{
			case MSG_NEW_SESSION_FAIL:
				findViewById(R.id.session_loading).setVisibility(View.GONE);
				break;
			case MSG_NEW_SESSION:
				getAdapter().updateSessions((List<ChatSession>) msg.obj);
				findViewById(R.id.session_loading).setVisibility(View.GONE);
			case MSG_NEW_MESSAGE:
				getAdapter().notifyDataSetChanged();
				break;
			}
		}
		
	};
	
	private void registerReceiver()
	{
		if (chatMessageReceiver == null)
		{
			chatMessageReceiver = new BroadcastReceiver() {

				public void onReceive(Context outerContext, Intent outerIntent) {
					if (outerIntent != null && outerIntent.hasExtra(CommonIntentAction.EXTRA_MSG_MESSAGE))
					{
						ChatMessage msg = (ChatMessage) outerIntent.getSerializableExtra(CommonIntentAction.EXTRA_MSG_MESSAGE);
						onNewMessage(msg);
					}
				}
			};
		}
		
		getContext().registerReceiver(chatMessageReceiver, new IntentFilter(CommonIntentAction.ACTION_BROADCAST_NEW_MSG));
	}
	
	private void unregisterReceiver()
	{
		if (chatMessageReceiver != null)
		{
			getContext().unregisterReceiver(chatMessageReceiver);
		}
	}
	
	private void onNewMessage(ChatMessage msg)
	{
		for (ChatSession session : this.sessions)
		{
			if (msg.getSession().equals(session.getSessionId()))
			{
				handler.sendEmptyMessage(MSG_NEW_MESSAGE);
				return;
			}
		}
		
		syncSessions(msg.getTo());
	}
	
	private void syncSessions(String userId)
	{
		ParameterHolder params = new ParameterHolder();
		params.addParameter("u_id", userId);
		
		Communication.executeAsyncGetTask("read_session", params, new Communication.CommandListener() {
			
			@Override
			public void onServerResponse(String serverMessage) {
//				findViewById(R.id.session_loading).setVisibility(View.GONE);
				List<ChatSession> newSessions = ChatSession.fromJson(serverMessage);
				Message msg = handler.obtainMessage(MSG_NEW_SESSION, newSessions);
				handler.sendMessage(msg);
			}
			
			@Override
			public void onException(Exception ex) {
				//Ignor this exception.
				handler.sendEmptyMessage(MSG_NEW_SESSION_FAIL);
//				findViewById(R.id.session_loading).setVisibility(View.GONE);
			}
		});
	}
}