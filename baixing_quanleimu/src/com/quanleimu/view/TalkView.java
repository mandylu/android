package com.quanleimu.view;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.quanleimu.activity.R;
import com.quanleimu.broadcast.CommonIntentAction;
import com.quanleimu.database.ChatMessageDatabase;
import com.quanleimu.entity.ChatMessage;
import com.quanleimu.entity.UserBean;
import com.quanleimu.entity.UserProfile;
import com.quanleimu.entity.compare.MsgTimeComparator;
import com.quanleimu.imageCache.SimpleImageLoader;
import com.quanleimu.jsonutil.JsonUtil;
import com.quanleimu.util.Communication;
import com.quanleimu.util.Util;

public class TalkView extends BaseView 
{
	//This is only a temp solution for checking current IM session, will remove within next release. add on version 2.6
	public static String CURRENT_RECEIVER_RRICKY = null;
	
	public static final int MAX_REQ_COUNT = 100;
	private static final int MSG_GETPROFILE = 1;
	private static final int MSG_GETTARGETICON = 2;
	private static final int MSG_GETMYICON = 3;
	
	private String targetUserId;
	private String targetIcon = null;
	private String adId;
	private String adTitle = "对话";
//	private ChatMessageListener msgListener;
	private BroadcastReceiver msgListener;
	private String sessionId;
	private String myUserId;
	private String myIcon = null;
	private long lastupdateTime = 0;
	private boolean alwaysSync;
	private boolean targetIsBoy = true;
	private boolean iamBoy = true;
	
	public TalkView(Context context) {
		super(context);
		
		doInit(context, null);
	}
	
	public TalkView(Context context, Bundle bundle) {
		super(context, bundle);
		ChatMessage msg = null;
		if (bundle != null)
		{
			targetUserId = bundle.getString("receiverId");
			myUserId = getMyId(); //FIXME: this is load from file, may cost times to load it on main thread.
			adId = bundle.getString("adId");
			if(bundle.containsKey("receiverNick")){
				adTitle = bundle.getString("receiverNick");
			}
			(new Thread(new GetPersonalProfileThread(targetUserId))).start();
			(new Thread(new GetPersonalProfileThread(myUserId))).start();
			if (bundle.containsKey("message"))
			{
				msg = (ChatMessage) bundle.getSerializable("message");
				lastupdateTime = msg.getTimestamp();
			}
			
			
			
			if (bundle.containsKey("sessionId"))
			{
				this.sessionId = bundle.getString("sessionId");
			}
			else if (msg != null)
			{
				this.sessionId = msg.getSession();//bundle.getString("session");
			}
			else
			{
				ChatMessageDatabase.prepareDB(context);
				String cachedSession = ChatMessageDatabase.getSessionId(myUserId, targetUserId, adId);
				sessionId = cachedSession == null ? sessionId : cachedSession;
			}
			
			alwaysSync = bundle.getBoolean("forceSync", false);
			
//			if (bundle.containsKey("adTitle"))
//			{
//				adTitle = bundle.getString("adTitle");
//			}
			
		}
		
		doInit(context, msg);
	}
	
	
	
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		
		//Load history or load msg from server.
		if (sessionId == null)
		{
			Thread t = new Thread(new LoadSvrMsgCmd());
			t.start();
		}
		else
		{
			Thread t = new Thread(new LoadLocalMsgCmd());
			t.start();
		}
		
		
		registerMsgListener();
		
		CURRENT_RECEIVER_RRICKY = targetUserId;
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		
		unregisterReceiver();
		
		CURRENT_RECEIVER_RRICKY = null;
	}

	public void onResume()
	{
		super.onResume();
	}
	
	private void registerMsgListener()
	{
		if (msgListener == null)
		{
			msgListener = new BroadcastReceiver() {

				public void onReceive(Context outerContext, Intent outerIntent) {
					if (outerIntent != null && outerIntent.hasExtra(CommonIntentAction.EXTRA_MSG_MESSAGE))
					{
						ChatMessage msg = (ChatMessage) outerIntent.getSerializableExtra(CommonIntentAction.EXTRA_MSG_MESSAGE);
						if (msg.getTo().equals(myUserId))
						{
							receiveAndUpdateUI(msg);
						}
					}
				}
				
			};
		}
		
		getContext().registerReceiver(msgListener, new IntentFilter(CommonIntentAction.ACTION_BROADCAST_NEW_MSG));
	}
	
	protected void unregisterReceiver()
	{
		if (msgListener != null)
		{
			getContext().unregisterReceiver(msgListener);
		}
	}
	
	public void onPause()
	{
		super.onPause();
	}
	
	@Override
	public TitleDef getTitleDef(){
		TitleDef title = new TitleDef();
		title.m_visible = true;
		title.m_title = adTitle;//"对话";
		
		title.m_leftActionHint = "返回";
		
		return title;
	}
	
	@Override
	public TabDef getTabDef(){
		TabDef tabDef = new TabDef();
		tabDef.m_visible = false;
		
		return tabDef;
	}	
	
	private void doInit(Context context, ChatMessage msg)
	{
		this.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		
		LayoutInflater inflator = LayoutInflater.from(context);
		View root = inflator.inflate(R.layout.im_session, null);
		addView(root, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		
		UIControl ctrl = new UIControl();
		final View sendBtn = (View) findViewById(R.id.im_send_btn);
		sendBtn.setOnClickListener(ctrl);
		findViewById(R.id.im_input_box).setOnClickListener(ctrl);
		
		initInputBox();
		
		//Show the message right now.
		if (msg != null)
		{
			receiveAndUpdateUI(msg);
		}

	}
	
	private void initInputBox()
	{
		final EditText inputBox = (EditText) findViewById(R.id.im_input_box);
		inputBox.setPadding(inputBox.getPaddingLeft(), 2, inputBox.getPaddingRight(), 2);//For nine-patch.
		
		final View sendBtn = (View) findViewById(R.id.im_send_btn);
		sendBtn.setEnabled(false);//Disable send by default.
		inputBox.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
				if (arg2 != null && arg2.getAction() == KeyEvent.ACTION_DOWN
						&& arg2.getKeyCode() == KeyEvent.KEYCODE_ENTER
						&& arg0.getText().length() > 0)
				{
					sendAndUpdateUI(arg0.getText().toString());
					arg0.setText("");
					return true;
				}
				return false;
			}});
		
		
		inputBox.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable edit) {
				
				if (edit == null || edit.length() == 0)
				{
					sendBtn.setEnabled(false);
				}
				else
				{
					sendBtn.setEnabled(true);
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				
			}
			
		});
		
	}
	
	private void sendAndUpdateUI(final String message)
	{
		//First step to update UI.
//		LayoutInflater inflator = LayoutInflater.from(getContext());
		View msgItem = loadMessageItem(true);//inflator.inflate(R.layout.im_message_item, null);
		TextView textItem = (TextView) msgItem.findViewById(R.id.im_message_content);
		textItem.setText(message);
		
		ViewGroup vp = (ViewGroup) findViewById(R.id.im_content_parent);
		vp.addView(msgItem);
		//TODO: post delay to scroll to bottom of scroll view.
		postScrollDelay();
		
		//Send the text to server.
		Thread t = new Thread(new SendMsgCmd(message));
		t.start();
	}
	
	private void receiveAndUpdateUI(final ChatMessage msg)
	{
		if(msg == null || !targetUserId.equals(msg.getFrom()))
		{
			return;
		}

		if (sessionId == null)
		{
			sessionId = msg.getSession();
		}
		
//		this.messageList.add(msg);
		
		this.postDelayed(new Runnable() {
			public void run() {
//				LayoutInflater inflator = LayoutInflater.from(getContext());
				View msgItem = loadMessageItem(false);//inflator.inflate(R.layout.im_message_item_received, null);
				TextView textView = (TextView) msgItem.findViewById(R.id.im_message_content);
				textView.setText(msg.getMessage());
				
				ViewGroup vp = (ViewGroup) findViewById(R.id.im_content_parent);
				vp.addView(msgItem);
				
				postScrollDelay();
			}
			
		}, 10);
	}
	
	private void mergeAndUpdateUI(final List<ChatMessage> list, final boolean isLocal)
	{
		Collections.sort(list, new MsgTimeComparator());
		lastupdateTime = list.get(list.size()-1).getTimestamp();
		
		this.postDelayed(new Runnable() {
			public void run() {
				ViewGroup vp = (ViewGroup) findViewById(R.id.im_content_parent);
				if (isLocal)
				{
					vp.removeAllViews();
				}
				
//				LayoutInflater inflator = LayoutInflater.from(getContext());
				for (ChatMessage msg : list)
				{
					final boolean isMine = myUserId.equals(msg.getFrom());
					View msgItem = loadMessageItem(isMine);//inflator.inflate(isMine ? R.layout.im_message_item : R.layout.im_message_item_received, null);
					TextView textView = (TextView) msgItem.findViewById(R.id.im_message_content);
					textView.setText(msg.getMessage());
					
					vp.addView(msgItem);
				}
				
				postScrollDelay();
			}
			
		}, 200);
		
		ChatMessageDatabase.prepareDB(getContext());
		for (ChatMessage tmp : list)
		{
			ChatMessageDatabase.storeMessage(tmp); //FIXME: we should do batch  update to save time.
		}
		
	}
	
	private void postScrollDelay()
	{
		this.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				ScrollView scroll = (ScrollView) findViewById(R.id.char_history_p);
				scroll.fullScroll(ScrollView.FOCUS_DOWN);
			}
		}, 200);
	}
	
	private void updateSendStatus(boolean succed)
	{
		//TODO:
	}
	
	class LoadLocalMsgCmd implements Runnable {

		@Override
		public void run() {
			List<ChatMessage> msgList = null;
			ChatMessageDatabase.prepareDB(getContext());
			
			if (sessionId != null)
			{
				msgList = ChatMessageDatabase.queryMessageBySession(sessionId); 
			}
			
			if (msgList != null && msgList.size() > 0)
			{
				mergeAndUpdateUI(msgList, true);
			}
			
			if (msgList == null || msgList.size() == 0 || alwaysSync)
			{
				new LoadSvrMsgCmd().run();
			}
			
		}
	}
	
	class LoadSvrMsgCmd implements Runnable 
	{
		final String apiName = "read_message";
		
		public void run() 
		{
			ArrayList<String> cmdOpts = new ArrayList<String>();
			cmdOpts.add("u_id=" + URLEncoder.encode(myUserId));
			if (sessionId != null)
			{
				cmdOpts.add("session_id=" + URLEncoder.encode(sessionId));
			}
			else
			{
				cmdOpts.add("u_id_other=" + URLEncoder.encode(targetUserId));
				cmdOpts.add("ad_id=" + URLEncoder.encode(adId));
			}
			
			//FIXME: only load messages within several days. we use 10 at present.
			if (lastupdateTime != 0)
			{
				cmdOpts.add("last_update_timestamp=" + URLEncoder.encode(lastupdateTime + ""));
			}
			cmdOpts.add("limit=" + MAX_REQ_COUNT);
			
			String url = Communication.getApiUrl(apiName, cmdOpts);
			
			try {
				String result = Communication.getDataByUrlGet(url);
				JSONObject obj = new JSONObject(result);
				if (obj.getInt("count") > 0)
				{
					JSONArray tmp = obj.getJSONArray("data");
					mergeAndUpdateUI(JsonUtil.parseChatMessages(tmp), false);
				}
			}
			catch(Throwable t)
			{
				//TODO: show error.
			}
		}
	}
	
	
	class SendMsgCmd implements Runnable 
	{
		private final String apiName = "send_message";
		private String message;
		public SendMsgCmd(String messageToSend)
		{
			this.message = messageToSend;
		}

		@Override
		public void run() {
			ArrayList<String> cmdOpts = new ArrayList<String>();
			cmdOpts.add("u_id_from=" + URLEncoder.encode(myUserId));
			cmdOpts.add("u_id_to=" + URLEncoder.encode(targetUserId));
			if (adId != null)
			{
				cmdOpts.add("ad_id=" + URLEncoder.encode(adId));
			}
			cmdOpts.add("message=" + URLEncoder.encode(message));
			if (sessionId != null)
			{
				cmdOpts.add("session_id=" + URLEncoder.encode(sessionId));
			}
			
			String url = Communication.getApiUrl(apiName, cmdOpts);
			
			try {
				String result = Communication.getDataByUrl(url,true);
				try {
					JSONObject json = new JSONObject(result);
					if (sessionId == null)
					{
						sessionId = json.getString("session_id");
					}
					
					lastupdateTime = json.getLong("timestamp");
					
					json.remove("u_id");
					json.put("ad_id", adId);
					json.put("u_id_from", myUserId);
					json.put("u_id_to", targetUserId);
					json.put("id", json.get("msg_id"));
					json.remove("msg_id");
					json.put("u_nick_from", "");
					json.put("u_nick_to", "");
					json.put("ad_title", "");
					json.put("message", message);
					
					ChatMessage chatMsgInst = ChatMessage.fromJson(json);
					
					ChatMessageDatabase.prepareDB(getContext());
					ChatMessageDatabase.storeMessage(chatMsgInst);
//					messageList.add(chatMsgInst);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				updateSendStatus(true);
			} catch (Throwable e) {
				updateSendStatus(false);
				e.printStackTrace();
			}
		}
	}
	
	private String getMyId()
	{
		UserBean user = (UserBean) Util.loadDataFromLocate(getContext(), "user");
		return user != null ? user.getId() : "";
	}
	
	private View loadMessageItem(boolean isMine)
	{
		LayoutInflater inflator = LayoutInflater.from(getContext());
		View msgItem = inflator.inflate(isMine ? R.layout.im_message_item : R.layout.im_message_item_received, null);
		ImageView iv = isMine ? (ImageView)msgItem.findViewById(R.id.myIcon) : (ImageView)msgItem.findViewById(R.id.targetIcon);
		if(iv != null){
			if(isMine){
				if(this.myIcon != null && !this.myIcon.equals("") && !this.myIcon.equals("null")){
					iv.setTag(myIcon);
					SimpleImageLoader.showImg(iv, myIcon, this.getContext());
				}else if(!iamBoy){
					iv.setImageResource(R.drawable.pic_my_avator_girl);
				}
			}else if(!isMine){
				if(this.targetIcon != null && !targetIcon.equals("") && !targetIcon.equals("null")){
					iv.setTag(targetIcon);
					SimpleImageLoader.showImg(iv, targetIcon, this.getContext());
				}else if(!targetIsBoy){
					iv.setImageResource(R.drawable.pic_my_avator_girl);
				}
			}
		}
		View msgParent = msgItem.findViewById(R.id.im_message_content_parent);
		msgParent.setPadding(msgParent.getPaddingLeft(), msgParent.getPaddingTop()/10, msgParent.getPaddingRight(), msgParent.getPaddingBottom()/10);
		
		return msgItem;
	}
	
	class UIControl implements View.OnClickListener, View.OnTouchListener
	{

		public void onClick(View v) {
			switch (v.getId())
			{
			case R.id.im_input_box:
				postScrollDelay();
				break;
			case R.id.im_send_btn:
				EditText text = (EditText) findViewById(R.id.im_input_box);
				if (text.length() != 0)
				{
					sendAndUpdateUI(text.getText().toString());
					text.setText("");
				}
				break;
				
				default:
					break;
			}
		}

		public boolean onTouch(View v, MotionEvent event) {
			//TODO: check if we need hide scroll bar when scroll message list.
//			if (v.getId() == R.id.char_history_p && event.getAction() == MotionEvent.ACTION_MOVE)
//			{
//			}
			return false;
		}
	}
	
	private void setPreviousIcon(boolean isMine){
		if(isMine && (iamBoy && (myIcon == null || myIcon.equals("") || myIcon.equals("null"))))
			return;
			
		if(!isMine && (targetIsBoy && (targetIcon == null || targetIcon.equals("") || targetIcon.equals("null"))))
			return;
		ViewGroup vp = (ViewGroup) findViewById(R.id.im_content_parent);
		if(vp != null){
			for(int i = 0; i < vp.getChildCount(); ++ i){
				ImageView iv = 
						(ImageView)(isMine ? vp.getChildAt(i).findViewById(R.id.myIcon) : vp.getChildAt(i).findViewById(R.id.targetIcon));
				if(iv == null) continue;
				if(isMine && (myIcon == null || myIcon.equals("") || myIcon.equals("null"))){
					if(!iamBoy){
						iv.setImageResource(R.drawable.pic_my_avator_girl);
					}
				}else if(!isMine && (targetIcon == null || targetIcon.equals("") || targetIcon.equals("null"))){
					if(!targetIsBoy){
						iv.setImageResource(R.drawable.pic_my_avator_girl);
					}
				}else{
					iv.setTag(isMine ? myIcon : targetIcon);
					SimpleImageLoader.showImg(iv, isMine ? myIcon : targetIcon, this.getContext());
				}
			}
		}
	}
	
	Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_GETPROFILE:
				if(msg.obj != null){
					TitleDef title = new TitleDef();
					title.m_visible = true;
					title.m_title = msg.obj.toString();				
					title.m_leftActionHint = "返回";
					m_viewInfoListener.onTitleChanged(title);

				}
				break;
			case MSG_GETTARGETICON:
				setPreviousIcon(false);
				break;
			case MSG_GETMYICON:
				setPreviousIcon(true);
				break;
			}
		}
	};
				
	class GetPersonalProfileThread implements Runnable {
		private String usrId = null;
		public GetPersonalProfileThread(String usrId){
			this.usrId = usrId;
		}
		@Override
		public void run() {
			if (usrId == null || usrId.equals(""))
			{
				return;
			}
			String upJson = Util.requestUserProfile(usrId);
			if(upJson != null){
				UserProfile up = UserProfile.from(upJson);
				if(up != null){
					if(usrId.equals(targetUserId)){
						targetIcon = up.squareImage;
						if(up.gender != null && up.gender.equals("女")){
							targetIsBoy = false;
						}
						myHandler.sendEmptyMessage(MSG_GETTARGETICON);
						Message msg = Message.obtain();
						msg.what = MSG_GETPROFILE;
						msg.obj = up.nickName;
						myHandler.sendMessage(msg);
					}else{
						if(up.gender != null && up.gender.equals("女")){
							iamBoy = false;
						}						
						myIcon = up.squareImage;
						myHandler.sendEmptyMessage(MSG_GETMYICON);
					}
				}
			}
		}
	}
	
}
