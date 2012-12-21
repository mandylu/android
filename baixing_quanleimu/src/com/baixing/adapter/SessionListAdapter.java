//liuchong@baixing.com
package com.baixing.adapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.baixing.database.ChatMessageDatabase;
import com.baixing.entity.ChatSession;
import com.baixing.util.Communication;
import com.baixing.util.Tracker;
import com.baixing.util.Util;
import com.baixing.util.TrackConfig.TrackMobile.BxEvent;
import com.quanleimu.activity.R;

public class SessionListAdapter extends BaseAdapter {
	private List<ChatSession> list = new ArrayList<ChatSession>();
	private Context context;
	private LayoutInflater mInflater;
	private Handler handler;
	private int messageWhat;

	public List<ChatSession> getList() {
		return list;
	}

	public void setList(List<ChatSession> list) {
		this.list = list == null ? this.list : list;
	}

	public SessionListAdapter(Context context, List<ChatSession> list) {
		super();
		this.context = context;
		this.list = list == null ? this.list : list;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	}
	
	public void setMessageOutOnDelete(Handler h, int messageWhat){
		this.handler = h;
		this.messageWhat = messageWhat;
	}
	
	public void updateSessions(List<ChatSession> newList)
	{
		if (newList != null)
		{
			list.clear();
			list.addAll(newList);
			this.notifyDataSetChanged();
		}
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list == null ? 0 : list.size();
	}

	@Override
	public Object getItem(int arg0) {
		return list.get(arg0);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	class SessionHolder {
		public TextView userAndAd;
		public TextView lastChat;
		public TextView lastTime;
		public ImageButton rightArrow;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		SessionHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.item_sessionlist, null);
			holder = new SessionHolder();
			holder.userAndAd = (TextView) convertView.findViewById(R.id.tvUserAndAd);
			holder.lastChat = (TextView) convertView.findViewById(R.id.tvLastMsg);
			holder.lastTime = (TextView) convertView.findViewById(R.id.tvTimeAndDate);
			holder.rightArrow = (ImageButton) convertView.findViewById(R.id.sessionArrow);
			convertView.setTag(holder);

		} else {
			holder = (SessionHolder) convertView.getTag();

		}
		
		ChatSession info = list.get(position);
		if (info != null) {
			
			final long sessionTime = Long.parseLong(info.getTimeStamp());
			ChatMessageDatabase.prepareDB(context);
//			ChatMessage lastMessage = ChatMessageDatabase.getLastMessage(info.getSessionId());
			
			holder.userAndAd.setText(info.getOppositeNick());
//			holder.lastChat.setText(lastMessage != null && lastMessage.getTimestamp() > sessionTime ? lastMessage.getMessage() : info.getLastMsg());
			holder.lastChat.setText(info.getLastMsg());

			try
			{
				int unreadCount = ChatMessageDatabase.getUnreadCount(info.getSessionId(), Util.getMyId(context));
//				holder.readStatus.setVisibility(/*lastMessage == null || */unreadCount > 0 ? View.VISIBLE : View.INVISIBLE);
			}
			catch(Throwable t)
			{
				
			}
			SimpleDateFormat sf = new SimpleDateFormat("MM-dd HH:mm", Locale.SIMPLIFIED_CHINESE);
			Date date = new Date(Long.parseLong(info.getTimeStamp()) * 1000);
			String time = sf.format(date);
			holder.lastTime.setText(time);
			
			holder.rightArrow.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
//					Tracker.getInstance().event(BxEvent.BUZZLIST_MANAGE).end();
					
					AlertDialog.Builder builder = new AlertDialog.Builder(context);
	                builder.setTitle("操作")
	                        .setItems(R.array.item_operate_session,
	                                new DialogInterface.OnClickListener() {
	                                    public void onClick(DialogInterface dialog, int which) {
//	                    					Tracker.getInstance().event(BxEvent.BUZZLIST_DELETE).end();	                                    	
	                                        if (which == 0) {
	                                        	new Thread(new DeleteSessionThread(list.get(position))).start();
	                                        }
	                                    }
	                                })
	                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
	                            @Override
	                            public void onClick(DialogInterface dialog, int which) {
	                                dialog.dismiss();
	                            }
	                        });
	                AlertDialog alert = builder.create();
	                alert.show();					
				}
			});
		}
		return convertView;
	}
	
	class DeleteSessionThread implements Runnable
	{
		private ChatSession session;
		public DeleteSessionThread(ChatSession session) {
			this.session = session;
		}
		
		@Override
		public void run() {
			String apiName = "del_session";
			List<String> parameters = new ArrayList<String>();
			parameters.add("session_id=" + session.getSessionId());
			String apiUrl = Communication.getApiUrl(apiName, parameters);
			try {
				Communication.getDataByUrl(apiUrl, true);
				SessionListAdapter.this.list.remove(this.session);
				ChatMessageDatabase.deleteMsgBySession(this.session.getSessionId());
				Message msg = SessionListAdapter.this.handler.obtainMessage();				
				msg.what = SessionListAdapter.this.messageWhat;
				SessionListAdapter.this.handler.sendMessage(msg);
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		
	}
}