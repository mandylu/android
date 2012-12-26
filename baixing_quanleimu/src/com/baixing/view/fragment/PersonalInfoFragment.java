package com.baixing.view.fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.baixing.activity.BaseFragment;
import com.baixing.data.GlobalDataManager;
import com.baixing.entity.UserBean;
import com.baixing.entity.UserProfile;
import com.baixing.message.BxMessageCenter;
import com.baixing.message.BxMessageCenter.IBxNotification;
import com.baixing.message.IBxNotificationNames;
import com.baixing.tracking.Tracker;
import com.baixing.tracking.TrackConfig.TrackMobile.Key;
import com.baixing.tracking.TrackConfig.TrackMobile.PV;
import com.baixing.util.LoginUtil;
import com.baixing.util.Util;
import com.baixing.widget.EditUsernameDialogFragment;
import com.quanleimu.activity.R;

public class PersonalInfoFragment extends BaseFragment implements View.OnClickListener, LoginUtil.LoginListener, Observer {

	private static final int REQ_EDIT_PROFILE = 1;
	private static final int REQ_REGISTER = 2;
  
    private EditUsernameDialogFragment editUserDlg;
	
	private UserBean user = null;
	private static final int MSG_GETPERSONALADS = 1;
	private static final int MSG_GETPERSONALPROFILE = 2;
	private static final int MSG_GETPERSONALLOCATION = 3;
	private static final int MSG_GETPERSONALSESSIONS = 4;
	private static final int MSG_LOGINSUCCESS = 5;
	private static final int MSG_LOGINFAIL = 6;
	private static final int MSG_NEWREGISTERVIEW = 7;
	private static final int MSG_FORGETPASSWORDVIEW = 8;
	private static final int MSG_ANONYMOUS_USER = 9;
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
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getArguments();
		user = GlobalDataManager.getInstance().getAccountManager().getCurrentUser();
		BxMessageCenter.defaultMessageCenter().registerObserver(this, IBxNotificationNames.NOTIFICATION_LOGIN);
		BxMessageCenter.defaultMessageCenter().registerObserver(this, IBxNotificationNames.NOTIFICATION_LOGOUT);
		if (savedInstanceState != null)
		{
			Log.e(TAG, "check if arguments is auto saved ? restore:" + this.getArguments());
		}
	}
	
	

	@Override
	public void onStackTop(boolean isBack) {
		String cityName = GlobalDataManager.getInstance().getCityName();
		if (null == cityName || "".equals(cityName)) {
			this.pushFragment(new CityChangeFragment(), createArguments("切换城市", "首页"));
		}else
		{
			TextView titleLabel = (TextView) getTitleDef().m_titleControls.findViewById(R.id.title_label_city);
			titleLabel.setText(GlobalDataManager.getInstance().getCityName());			
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

		if (up != null)
		{
			this.fillProfile(up, v);
		}
		else
		{
			reloadUser(v);
		}
		
		return v;
	}
	
    private void reloadUser(View v) {
        if (user != null && user.getPhone() != null && !user.getPhone().equals("")) {
        	new Thread(new Runnable() {
				
				@Override
				public void run() {
					UserProfile profile = (UserProfile) Util.loadDataFromLocate(getActivity(), "userProfile", UserProfile.class);
					if (profile != null)
					{
						sendMessage(MSG_GETPERSONALPROFILE, profile);
					}
					else
					{
						new Thread(new GetPersonalProfileThread()).start();
					}
				}
			}).start();
        }
        else
        {
        	sendMessage(MSG_ANONYMOUS_USER, null);
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
		Tracker.getInstance().pv(PV.MY).append(Key.ISLOGIN, GlobalDataManager.getInstance().getAccountManager().isUserLogin()).append(Key.USERID, user!=null ? user.getId() : null).end();
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
		
//		new Thread(new GetPersonalAdsThread()).start();
		new Thread(new GetPersonalProfileThread()).start();
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
			break;
		case MSG_GETPERSONALPROFILE:
			up = (UserProfile) msg.obj;
			if(up != null){
				fillProfile(up,rootView);
			}
			break;
		case MSG_ANONYMOUS_USER:
			fillProfile(null, rootView);
			break;
		case MSG_GETPERSONALLOCATION:
			break;
		case MSG_GETPERSONALSESSIONS:
			break;
        case MSG_USER_LOGOUT:
        	getView().findViewById(R.id.userInfoLayout).setVisibility(View.GONE);
			break;			
		}
		
	
	}

	private class GetPersonalProfileThread implements Runnable {
		@Override
		public void run() {
			if (user == null)
			{
				return;
			}
			String upJson = Util.requestUserProfile(user.getId());
			if(upJson != null){
				UserProfile profile = UserProfile.from(upJson);
				if (getActivity() != null)
				{
					Util.saveDataToLocate(getActivity(), "userProfile", profile);
					sendMessage(MSG_GETPERSONALPROFILE, profile);
				}
			}

			hideProgress();
		}
	}	

	private void fillProfile(UserProfile up, View userInfoView){
        View activity = userInfoView;
        
        if (up == null)
        {
        	View userInfoLayout = activity.findViewById(R.id.userInfoLayout);
        	userInfoLayout.setVisibility(View.GONE);
        	return;
        }

        if(up.nickName != null){
            ((TextView)activity.findViewById(R.id.userInfoNickname)).setText(up.nickName);
        }else{
            ((TextView)activity.findViewById(R.id.userInfoNickname)).setText("");
        }
        
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
            	pushFragment(new SettingFragment(), null);
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
	
	public void onDestroy()
	{
		super.onDestroy();
		BxMessageCenter.defaultMessageCenter().removeObserver(this);
	}
	
	public int getExitAnimation()
	{
		return 0;
	}
	@Override
	public void update(Observable observable, Object data) {
		if (data instanceof IBxNotification)
		{
			IBxNotification note = (IBxNotification) data;
			if (IBxNotificationNames.NOTIFICATION_LOGIN.equals(note.getName())
					|| IBxNotificationNames.NOTIFICATION_LOGOUT.equals(note.getName())) {
				user = (UserBean) note.getObject();
				up = null;
				reloadUser(getView());
			}
		}
	}

}
