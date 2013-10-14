package com.baixing.view.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baixing.activity.BaseFragment;
import com.baixing.data.AccountManager;
import com.baixing.data.GlobalDataManager;
import com.baixing.entity.UserBean;
import com.baixing.sharing.referral.ReferralNetwork;
import com.baixing.sharing.referral.ReferralPromoter;
import com.baixing.sharing.referral.ReferralUtil;
import com.baixing.tracking.TrackConfig.TrackMobile.BxEvent;
import com.baixing.tracking.TrackConfig.TrackMobile.Key;
import com.baixing.tracking.TrackConfig.TrackMobile.PV;
import com.baixing.tracking.Tracker;
import com.baixing.util.LoginUtil;
import com.baixing.util.Util;
import com.baixing.util.ViewUtil;
import com.baixing.widget.VerifyFailDialog;
import com.quanleimu.activity.R;

public class LoginFragment extends BaseFragment implements LoginUtil.LoginListener {
	
	public static final int MSG_LOGIN_SUCCESS = 0x1234FFFF;
	
	public String backPageName = "back";
	public String categoryEnglishName = "";
	static public final String KEY_RETURN_CODE ="login_return_code";////the value should be int
	private LoginUtil loginHelper;
	private static final int MSG_LOGINFAIL = 1;
	private static final int MSG_LOGINSUCCESS = 2;
	private static final int MSG_NEWREGISTERVIEW = 3;
	private static final int MSG_FORGETPASSWORDVIEW = 4;

	public void onLoginFail(String message){
		sendMessage(MSG_LOGINFAIL, message);
		Tracker.getInstance()
		.event(BxEvent.LOGIN_SUBMIT)
		.append(Key.LOGIN_RESULT_STATUS, "0")
		.append(Key.LOGIN_RESULT_FAIL_REASON, message)
		.end();
		Log.d("loginfragment","onLoginFail");
	}
	public void onLoginSucceed(String message){
		sendMessage(MSG_LOGINSUCCESS, message);
		Log.d("loginfragment","onLoginSucceed");
		Tracker.getInstance()
		.event(BxEvent.LOGIN_SUBMIT)
		.append(Key.LOGIN_RESULT_STATUS, "1")
		.end();
	}
	public void onRegisterClicked(){
		sendMessage(MSG_NEWREGISTERVIEW, null);
		Tracker.getInstance().event(BxEvent.LOGIN_REGISTER).end();
	}
	public void onForgetClicked(){
		Tracker.getInstance().event(BxEvent.LOGIN_FORGETPASSWORD).end();
		sendMessage(MSG_FORGETPASSWORDVIEW, null);
	}
	
	@Override
	public boolean handleBack() {
		Tracker.getInstance().event(BxEvent.LOGIN_BACK).end();
		return super.handleBack();
	}
	@Override
	public void initTitle(TitleDef title){
		title.m_leftActionHint = "返回";		
		title.m_visible = true;
		title.m_rightActionHint = "注册";
		title.m_title = "登录";
	}

	@Override
	public int[] excludedOptionMenus() {
		return new int[]{OPTION_LOGIN};
	}
	
    @Override
    public void handleRightAction() {
        super.handleRightAction();
        onRegisterClicked();
    }
    
    private boolean paused = false;
    private boolean needShowDlg = false;
 
    @Override
    public void onPause(){
    	super.onPause();
    	paused = true;
    }

	@Override
	public void onResume() {
		super.onResume();
		this.pv = PV.LOGIN;
		Tracker.getInstance().pv(PV.LOGIN).end();
		paused = false;
		if(needShowDlg){
			this.showVerifyDlg();
			needShowDlg = false;
		}		
	}
	
	private void showVerifyDlg(){
    	if(this.paused){
    		needShowDlg = true;
    		return;
    	}

		VerifyFailDialog dlg = new VerifyFailDialog(new VerifyFailDialog.VerifyListener() {
			
			@Override
			public void onReVerify(String mobile) {
				// TODO Auto-generated method stub
//				showProgress(R.string.dialog_title_info, R.string.dialog_message_waiting, false);
				loginHelper.reVerify("");
			}

			@Override
			public void onSendVerifyCode(String code) {
				// TODO Auto-generated method stub				
//				showProgress(R.string.dialog_title_info, R.string.dialog_message_waiting, false);
				loginHelper.reVerify(code);
			}
		});
		dlg.show(getFragmentManager(), null);		
		needShowDlg = false;
	}
	
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
	}
	
	

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
//		if (bingo)
//		{
//			Log.w(TAG, "finish fragment on resume");
//			finishFragment();
//		}
//		
//		Log.w(TAG, "start fragment on resume");
//		bingo = true;
//		pushFragment(new ForgetPassFragment(), null);
	}

	@Override
	public void onStackTop(boolean isBack) {
		super.onStackTop(isBack);
		
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Util.logout(); //For bug : 40383, clear cached login data when login fragment is created.
		
		this.backPageName = this.getArguments().getString("backPageName");
		
	}

	@Override
	public View onInitializeView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		RelativeLayout llLoginRoot = (RelativeLayout)inflater.inflate(R.layout.login, null);
		
		Bundle bundle = this.getArguments();
		if(bundle != null && bundle.containsKey("defaultNumber")){
			String number = bundle.getString("defaultNumber");
			if(Util.isValidMobile(number)){
				((TextView)llLoginRoot.findViewById(R.id.et_account)).setText(number);
			}
		}
		loginHelper = new LoginUtil(llLoginRoot, this);
		
		return llLoginRoot;
	}
	

	@Override
	protected void onFragmentBackWithData(int resultCode, Object result) {
		super.onFragmentBackWithData(resultCode, result);

		if (resultCode == RegisterFragment.MSG_REGISTER_SUCCESS || resultCode == ForgetPassFragment.MSG_FORGET_PWD_SUCCEED) {
            finishFragment(resultCode, result);
        }	
	}

	@Override
	protected void handleMessage(Message msg, Activity activity, View rootView) {

		hideProgress();
		
		switch (msg.what) {
		case MSG_LOGINSUCCESS:
			
			// zengjin@baixing.net
			if (!TextUtils.isEmpty(ReferralPromoter.getInstance().ID())) {
				AccountManager am = GlobalDataManager.getInstance().getAccountManager();
				UserBean ub = am.getCurrentUser();
				ReferralNetwork.getInstance().savePromoLog(ReferralPromoter.getInstance().ID(), ReferralUtil.TASK_APP, ub.getPhone(), null, null, Util.getDeviceUdid(GlobalDataManager.getInstance().getApplicationContext()), ub.getId(), null);
			}
			
			if(msg.obj != null && msg.obj instanceof String){
				ViewUtil.showToast(activity, (String)msg.obj, false);
			}else{
				ViewUtil.showToast(activity, "登陆成功", false);
			}
			if(this.getArguments() != null && getArguments().containsKey(KEY_RETURN_CODE)){
				this.finishFragment(getArguments().getInt(KEY_RETURN_CODE), null);
			}else{
//				Bundle bundle = createArguments(null, null);
//				bundle.putInt("defaultPageIndex", 1);
//				((BaseActivity)this.getActivity()).pushFragment(new PersonalInfoFragment(), bundle, true);
				this.finishFragment(MSG_LOGIN_SUCCESS, null);
			}
			break;
		case MSG_LOGINFAIL:
			String msgToShow = "登录未成功，请稍后重试！";
			if(msg.obj != null && msg.obj instanceof String){
				msgToShow =  (String)msg.obj;
			}
			ViewUtil.showToast(activity, msgToShow, false);
			break;
		case MSG_NEWREGISTERVIEW:
//			m_viewInfoListener.onNewView(new RegisterView(LoginView.this.getContext()));
			pushFragment(new RegisterFragment(), createArguments(null, null));
			break;
		case MSG_FORGETPASSWORDVIEW:
//			m_viewInfoListener.onNewView(new ForgetPasswordView(getContext(), null));
			Bundle bundle = createArguments("找回密码", null);
			bundle.putString(ForgetPassFragment.Forget_Type, "forget");
			String phone = ((TextView)getView().findViewById(R.id.et_account)).getText().toString();
			if(phone != null && phone.length() > 0 && Util.isValidMobile(phone)){
				bundle.putString("defaultNumber", phone);
			}
			pushFragment(new ForgetPassFragment(), bundle);
			break;
		case 10:
			hideProgress();
			ViewUtil.showToast(getActivity(), "网络连接失败，请检查设置！", true);
			break;
		}
	}
	
	public boolean hasGlobalTab()
	{
		return false;
	}
	
	
	
	@Override
	public void onVerifyFailed(String message) {
		hideProgress();
		// TODO Auto-generated method stub
		showVerifyDlg();
	}

}
