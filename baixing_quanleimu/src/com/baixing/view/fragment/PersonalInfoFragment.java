package com.baixing.view.fragment;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient.CustomViewCallback;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baixing.broadcast.CommonIntentAction;
import com.baixing.database.ChatMessageDatabase;
import com.baixing.entity.ChatMessage;
import com.baixing.entity.ChatSession;
import com.baixing.entity.GoodsDetail;
import com.baixing.entity.GoodsList;
import com.baixing.entity.UserBean;
import com.baixing.entity.UserProfile;
import com.baixing.imageCache.SimpleImageLoader;
import com.baixing.jsonutil.JsonUtil;
import com.baixing.util.Communication;
import com.baixing.util.ErrorHandler;
import com.baixing.util.LoginUtil;
import com.baixing.util.Tracker;
import com.baixing.util.Util;
import com.baixing.util.TrackConfig.TrackMobile.Key;
import com.baixing.util.TrackConfig.TrackMobile.PV;
import com.baixing.view.fragment.HomeFragment.GetPersonalProfileThread;
import com.baixing.widget.CustomizeGridView;
import com.baixing.widget.EditUsernameDialogFragment;
import com.baixing.widget.CustomizeGridView.GridInfo;
import com.baixing.widget.CustomizeGridView.ItemClickListener;
import com.quanleimu.activity.BaseFragment;
import com.quanleimu.activity.BaseFragment.TabDef;
import com.quanleimu.activity.BaseFragment.TitleDef;
import com.quanleimu.activity.QuanleimuApplication;
import com.quanleimu.activity.R;

public class PersonalInfoFragment extends BaseFragment implements View.OnClickListener, LoginUtil.LoginListener, ItemClickListener {

	public static final int REQ_EDIT_PROFILE = 1;
	public static final int REQ_REGISTER = 2;
	
	public static final int INDEX_POSTED = 0;
	public static final int INDEX_LIMITED = 1;
	public static final int INDEX_DELETED = 2;
	public static final int INDEX_FAVORITE = 3;
	public static final int INDEX_MESSAGE = 4;
	public static final int INDEX_HISTORY = 5;
	public static final int INDEX_SETTING = 6;	
	
	public int postNum = 0;
	public int limitedNum = 0;
	public int deletedNum = 0;
	public int favoriteNum = 0;
	public int unreadMessageNum = 0;
	public int historyNum = 0;
	
    private UserProfile userProfile;
    
    private EditUsernameDialogFragment editUserDlg;
	
	private Bundle bundle = null;
	private UserBean user = null;
	private String json = null;
	private String upJson = null;
//	private String locationJson = null;
	private String sessionsJson = null;
	static final int MSG_GETPERSONALADS = 1;
	public static final int MSG_GETPERSONALPROFILE = 2;
	static final int MSG_GETPERSONALLOCATION = 3;
	static final int MSG_GETPERSONALSESSIONS = 4;
	static final int MSG_LOGINSUCCESS = 5;
	static final int MSG_LOGINFAIL = 6;
	static final int MSG_NEWREGISTERVIEW = 7;
	static final int MSG_FORGETPASSWORDVIEW = 8;
    public static final int MSG_EDIT_USERNAME_SUCCESS = 100;
    public static final int MSG_SHOW_TOAST = 101;
    public static final int MSG_SHOW_PROGRESS = 102;

	private UserProfile up = null;
	
	@Override
	public void onLoginFail(String message){
		sendMessage(MSG_LOGINFAIL, message);
	}
	public void onLoginSucceed(String message){
		sendMessage(MSG_LOGINSUCCESS, message);		
	}
	public void onRegisterClicked(){
		sendMessage(MSG_NEWREGISTERVIEW, null);
	}
	public void onForgetClicked(){
		sendMessage(MSG_FORGETPASSWORDVIEW, null);
	}
	@Override
	public void initTitle(TitleDef title) {
		title.m_visible = true;
		title.m_title = "个人中心";
	}
	
	public boolean hasGlobalTab()
	{
		return true;
	}

	@Override
	public void initTab(TabDef tab) {
		tab.m_visible = true;
		tab.m_tabSelected = ETAB_TYPE.ETAB_TYPE_MINE;
	}
	
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.bundle = this.getArguments();
		if (savedInstanceState != null)
		{
			Log.e(TAG, "check if arguments is auto saved ? restore:" + this.getArguments());
		}
	}
	
	

	@Override
	public void onStackTop(boolean isBack) {
		String cityName = QuanleimuApplication.getApplication().getCityName();
		if (null == cityName || "".equals(cityName)) {
			this.pushFragment(new CityChangeFragment(), createArguments("切换城市", "首页"));
		}else
		{
			TextView titleLabel = (TextView) getTitleDef().m_titleControls.findViewById(R.id.title_label_city);
			titleLabel.setText(QuanleimuApplication.getApplication().getCityName());			
		}
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (savedInstanceState != null)
		{
			Log.d(TAG, "recreate view from saved data." + this.getClass().getName());
		}

		View v = inflater.inflate(R.layout.personalentryview, null);
		v.findViewById(R.id.rl_wosent).setOnClickListener(this);
		v.findViewById(R.id.rl_wofav).setOnClickListener(this);
		v.findViewById(R.id.rl_setting).setOnClickListener(this);

		reloadUser(v);
		
		return v;
	}
	
    private void reloadUser(View v) {
        user = Util.getCurrentUser();
        if (user != null && user.getPhone() != null && !user.getPhone().equals("")) {
            userProfile = (UserProfile) Util.loadDataFromLocate(getActivity(), "userProfile", UserProfile.class);
            if (userProfile != null) {
                fillProfile(userProfile, v);
            } else {
                new Thread(new GetPersonalProfileThread()).start();
            }
        }
    }
	
	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		this.pv = PV.MY;
		Tracker.getInstance().pv(PV.MY).append(Key.ISLOGIN, Util.isUserLogin()).append(Key.USERID, user!=null ? user.getId() : null).end();
	}
	
	


	@Override
	protected void onFragmentBackWithData(int requestCode, Object result) {
		if (requestCode == REQ_EDIT_PROFILE && result != null)
		{
			forceUpdate();
		}else if(REQ_REGISTER == requestCode && result != null){
//			forceUpdate();
		}
	}
	
	private void forceUpdate()
	{
		showProgress(R.string.dialog_title_info, R.string.dialog_message_data_loading, true);
		
		new Thread(new GetPersonalAdsThread()).start();
		new Thread(new GetPersonalProfileThread()).start();
		new Thread(new GetPersonalSessionsThread()).start();
	}




	class GetPersonalAdsThread implements Runnable {
		@Override
		public void run() {
			String apiName = "ad_list";
			ArrayList<String> list = new ArrayList<String>();
			 
			list.add("query=userId:" + user.getId() + " AND status:0");
			list.add("activeOnly=0");
			list.add("start=0");
			list.add("rt=1");
			list.add("rows=1000");
			
			if(bundle != null && bundle.getString("lastPost") != null){
				list.add("newAdIds=" + bundle.getString("lastPost"));
			}
			String url = Communication.getApiUrl(apiName, list);
			try {
				json = Communication.getDataByUrl(url, false);
				sendMessage(MSG_GETPERSONALADS, null);
				return;
			} catch (UnsupportedEncodingException e) {
				Message msg2 = Message.obtain();
				msg2.what = ErrorHandler.ERROR_SERVICE_UNAVAILABLE;
				QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg2);
			} catch (IOException e) {
				Message msg2 = Message.obtain();
				msg2.what = ErrorHandler.ERROR_NETWORK_UNAVAILABLE;
				QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg2);
			} catch (Communication.BXHttpException e) {
				Message msg2 = Message.obtain();
				msg2.what = ErrorHandler.ERROR_NETWORK_UNAVAILABLE;
				QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg2);
			}
			
			hideProgress();
		}
	}
	

	@Override
	protected void handleMessage(Message msg, Activity activity, View rootView) {
		switch (msg.what) {
		case MSG_FORGETPASSWORDVIEW:
			pushFragment(new ForgetPassFragment(), createArguments(null, null));
			break;
		case MSG_NEWREGISTERVIEW:
			Bundle bundle = createArguments(null, null);
			bundle.putInt(ARG_COMMON_REQ_CODE, REQ_REGISTER);
			pushFragment(new RegisterFragment(), bundle); //FIXME:
//			m_viewInfoListener.onNewView(new RegisterView(PersonalCenterEntryView.this.getContext()));
			break;
        case MSG_EDIT_USERNAME_SUCCESS:
            hideProgress();
            editUserDlg.dismiss();
            reloadUser(getView());
			break;			
		case MSG_LOGINFAIL:
			if(msg.obj != null && msg.obj instanceof String){
				Toast.makeText(activity, (String)msg.obj, 0).show();
			}else{
				Toast.makeText(activity, "登录未成功，请稍后重试！", 0).show();
			}
			break;
        case MSG_SHOW_PROGRESS:
            showSimpleProgress();
            break;
        case MSG_SHOW_TOAST:
            hideProgress();
            Toast.makeText(activity, msg.obj.toString(), 1).show();
            break;
		case MSG_GETPERSONALADS:
//			hideProgress();
//			
//			if (json != null) {
//				GoodsList gl = JsonUtil.getGoodsListFromJson(json);
//				
//				List<GoodsDetail> listMyPost = gl.getData();
//				if(listMyPost != null){
//					for(int i = listMyPost.size() - 1; i >= 0; -- i){
//						if(!listMyPost.get(i).getValueByKey("status").equals("0")){
//							listMyPost.remove(i);
//						}
//					}
//				}
//				if(getActivity() != null)
//				{
//					TextView tvPersonalAds = (TextView) getActivity().findViewById(R.id.tv_sentcount);
//					tvPersonalAds.setText(String.valueOf((listMyPost == null) ? 0 : listMyPost.size()));
//				}
//				QuanleimuApplication.getApplication().setListMyPost(listMyPost);
//			}
			break;
		case MSG_GETPERSONALPROFILE:
			if(upJson != null){
				up = UserProfile.from(upJson);
				if (getActivity() != null)
				{
					Util.saveDataToLocate(getActivity(), "userProfile", up);
					if(up != null){
						fillProfile(up,rootView);
					}
				}
			}
			break;
		case MSG_GETPERSONALLOCATION:
//			if(locationJson != null){
//				try{
//					JSONArray metaAry = new JSONArray(locationJson);
//					if(metaAry != null && metaAry.length() > 0){
//						JSONObject meta = metaAry.getJSONObject(0);
//						if(meta != null){
//							if(meta.has("displayName")){
//								String location = meta.getString("displayName");
//								if(location != null){
//									((TextView)getActivity().findViewById(R.id.personalLocation)).setText(location);
//								}
//							}								
//						}
//					}
//				}catch(JSONException e){
//					e.printStackTrace();
//				}
//			}
			break;
		case MSG_GETPERSONALSESSIONS:
//			if(this.sessions != null){
//				updateMessageCountInfo(rootView);
//			}
			break;
        case MSG_USER_LOGOUT:
        	getView().findViewById(R.id.userInfoLayout).setVisibility(View.GONE);
			break;			
		}
		
	
	}

	private void updateMessageCountInfo(View rootView)
	{
//		if (this.sessions != null)
//		{
//			ChatMessageDatabase.prepareDB(getActivity());
//			String count = String.valueOf(ChatMessageDatabase.getUnreadCount(null, Util.getMyId(getActivity())));
//			((TextView)rootView.findViewById(R.id.tv_buzzcount)).setText(count + "未读");
//		}else{
//			((TextView)rootView.findViewById(R.id.tv_buzzcount)).setText("0未读");
//		}
	}
	
	class GetLocationThread implements Runnable{
		public GetLocationThread(String objId){
			this.objId = objId;
		}
		private String objId = "";
		@Override
		public void run() {
//			String apiName = "metaobject";
//			ArrayList<String> list = new ArrayList<String>();
//			 
//			list.add("objIds=" + objId);
//			
//			String url = Communication.getApiUrl(apiName, list);
//			try {
//				locationJson = Communication.getDataByUrl(url, false);
//				sendMessage(MSG_GETPERSONALLOCATION, null);
//				return;
//			} catch (UnsupportedEncodingException e) {
//				Message msg2 = Message.obtain();
//				msg2.what = ErrorHandler.ERROR_SERVICE_UNAVAILABLE;
//				QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg2);
//			} catch (IOException e) {
//				Message msg2 = Message.obtain();
//				msg2.what = ErrorHandler.ERROR_NETWORK_UNAVAILABLE;
//				QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg2);
//			} catch (Communication.BXHttpException e) {
//				Message msg2 = Message.obtain();
//				msg2.what = ErrorHandler.ERROR_NETWORK_UNAVAILABLE;
//				QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg2);
//			}
//
//			hideProgress();
		}		
	}
	
	class GetPersonalProfileThread implements Runnable {
		@Override
		public void run() {
			if (user == null)
			{
				return;
			}
			upJson = Util.requestUserProfile(user.getId());
			sendMessage(MSG_GETPERSONALPROFILE, null);

			hideProgress();
		}
	}	

	class GetPersonalSessionsThread implements Runnable {
		@Override
		public void run() {
//			if (user == null)
//			{
//				return;
//			}
//			
//			String apiName = "read_session";
//			ArrayList<String> list = new ArrayList<String>();
//			 
//			list.add("u_id=" + user.getId());
//			
//			String url = Communication.getApiUrl(apiName, list);
//			try {
//				sessionsJson = Communication.getDataByUrl(url, true); //Only load cached data here.
//				if(sessionsJson != null){
//					sessions = ChatSession.fromJson(sessionsJson);
//				}
//				sendMessage(MSG_GETPERSONALSESSIONS, null);
//				return;
//			} catch (UnsupportedEncodingException e) {
//				Message msg2 = Message.obtain();
//				msg2.what = ErrorHandler.ERROR_SERVICE_UNAVAILABLE;
//				QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg2);
//			} catch (IOException e) {
//				Message msg2 = Message.obtain();
//				msg2.what = ErrorHandler.ERROR_NETWORK_UNAVAILABLE;
//				QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg2);
//			} catch (Communication.BXHttpException e) {
//				Message msg2 = Message.obtain();
//				msg2.what = ErrorHandler.ERROR_NETWORK_UNAVAILABLE;
//				QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg2);
//			}
//
//			hideProgress();
		}
	}	
	
	private void fillProfile(UserProfile up, View userInfoView){
        View activity = userInfoView;

        if(up.nickName != null){
            ((TextView)activity.findViewById(R.id.userInfoNickname)).setText(up.nickName);
        }else{
            ((TextView)activity.findViewById(R.id.userInfoNickname)).setText("");
        }
        // 新版本只保留 nickname
        if(up.createTime != null && !up.equals("")){
            try{
                Date date = new Date(Long.parseLong(up.createTime) * 1000);
                SimpleDateFormat df = new SimpleDateFormat("yyyy年MM月", Locale.SIMPLIFIED_CHINESE);
                ((TextView)activity.findViewById(R.id.userInfoJoinDays)).setText(df.format(date) + "");
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        
        View userInfoLayout = activity.findViewById(R.id.userInfoLayout);
        userInfoLayout.setVisibility(View.VISIBLE);
        View editBtn = activity.findViewById(R.id.userInfo_editUsername_btn);
        editBtn.setOnClickListener(this);
    }
	
	@Override
	public void onItemClick(GridInfo info, int index) {	
		switch (index)
		{
		case INDEX_POSTED:
            {
            	pushPersonalPostFragment(PersonalPostFragment.TYPE_MYPOST);				
            }
			break;
		case INDEX_LIMITED:
			{
				pushPersonalPostFragment(PersonalPostFragment.TYPE_INVERIFY);
			}
			break;
		case INDEX_DELETED:
            {
            	pushPersonalPostFragment(PersonalPostFragment.TYPE_DELETED);
            }
            break;
		case INDEX_FAVORITE:
			{
				Bundle bundle = createArguments(null, null);
				bundle.putBoolean("isFav", true);
				pushFragment(new FavoriteAndHistoryFragment(), bundle);					
			}
			break;
		case INDEX_MESSAGE:
			{
				Bundle bundle = createArguments(null, null);
				ArrayList<ChatSession> tmpList = new ArrayList<ChatSession>();
//				tmpList.addAll(this.sessions); 需要获取 sessions 数据
				bundle.putSerializable("sessions", tmpList);
				pushFragment(new SessionListFragment(), bundle);
			}
			break;
		case INDEX_HISTORY:
			{
				Bundle bundle = createArguments(null, null);
				bundle.putBoolean("isFav", false);
				pushFragment(new FavoriteAndHistoryFragment(), bundle);
			}
			break;
		case INDEX_SETTING:
			{
				pushFragment(new SetMainFragment(), null);
			}
			break;
		}
	}
	
	
	private void pushPersonalPostFragment(int type) {
		Bundle bundle = createArguments(null, null);
		bundle.putInt(PersonalPostFragment.TYPE_KEY, type);
		pushFragment(new PersonalPostFragment(), bundle);
	}
    
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.userInfo_editUsername_btn:
                editUserDlg = new EditUsernameDialogFragment();
                editUserDlg.handler = this.handler;
                editUserDlg.show(getFragmentManager(), null);
                break;
            case R.id.rl_wosent:
            	pushPersonalPostFragment(PersonalPostFragment.TYPE_MYPOST);	
            	break;
            case R.id.rl_wofav:
            	Bundle bundle = createArguments(null, null);
				bundle.putBoolean("isFav", true);
				pushFragment(new FavoriteAndHistoryFragment(), bundle);		
            	break;
            case R.id.rl_setting:
            	pushFragment(new SetMainFragment(), null);
            	break;

            default:
                break;
        }
    }
    
	@Override
	public void handleSearch() {
		this.pushFragment(new SearchFragment(), this.getArguments());
	};
	
	public int getEnterAnimation()
	{
		return 0;
	}
	
	public int getExitAnimation()
	{
		return 0;
	}

}
