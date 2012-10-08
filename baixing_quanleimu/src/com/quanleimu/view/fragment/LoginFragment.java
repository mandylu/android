package com.quanleimu.view.fragment;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.quanleimu.activity.BaseFragment;
import com.quanleimu.activity.BaseFragment.TabDef;
import com.quanleimu.activity.BaseFragment.TitleDef;
import com.quanleimu.activity.QuanleimuApplication;
import com.quanleimu.activity.R;
import com.quanleimu.entity.UserBean;
import com.quanleimu.util.Communication;
import com.quanleimu.util.Util;
import com.quanleimu.util.LoginUtil;
import com.tencent.mm.sdk.platformtools.Log;

public class LoginFragment extends BaseFragment implements LoginUtil.LoginListener {
	
	private static final int REQ_CODE_RESET_PASS = 1;
	private static final int REQ_CODE_REGISTER = 2;
	
	public String backPageName = "back";
	public String categoryEnglishName = "";
	
	private boolean bingo;
	
	private LoginUtil.LoginListener listener;
	private LoginUtil loginHelper;
	private static final int MSG_LOGINFAIL = 1;
	private static final int MSG_LOGINSUCCESS = 2;
	private static final int MSG_NEWREGISTERVIEW = 3;
	private static final int MSG_FORGETPASSWORDVIEW = 4;

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
	public void initTitle(TitleDef title){
		if(null != backPageName)
			title.m_leftActionHint = this.backPageName;
		else
			title.m_leftActionHint = null;
		
		title.m_visible = true;
		title.m_leftActionStyle = EBUTT_STYLE.EBUTT_STYLE_BACK;
//		title.m_rightActionHint = "确定";
//		title.m_rightActionStyle = EBUTT_STYLE.EBUTT_STYLE_NORMAL;
		title.m_title = "登录";
	}
	
	@Override
	public void initTab(TabDef tab){
		tab.m_visible = false;
	}
	
	

	@Override
	public void onResume() {
		super.onResume();
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
		
		this.backPageName = this.getArguments().getString("backPageName");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		RelativeLayout llLoginRoot = (RelativeLayout)inflater.inflate(R.layout.login, null);
		
		loginHelper = new LoginUtil(llLoginRoot, this);
		
		return llLoginRoot;
	}
	

	@Override
	protected void onFragmentBackWithData(int resultCode, Object result) {
		super.onFragmentBackWithData(resultCode, result);

		if(resultCode == REQ_CODE_RESET_PASS){
			Toast.makeText(getActivity(), "重置密码成功，请重新登录", 3).show();
		}
		else if (resultCode == REQ_CODE_REGISTER)
		{
			UserBean user = (UserBean) Util.loadDataFromLocate(getContext(), "user");
			
			handleRightAction();
		}
	
	}

//	public void handleRightAction()
//	{
////		if (check()) 
//		{
//			pd = ProgressDialog.show(getActivity(), "提示", "正在登录，请稍候...");
//			pd.setCancelable(true);
//			new Thread(new LoginThread()).start();
//		}
//		
//	}
	
	// {"id":"79703763","error":{"message":"用户登录成功","code":0}}
//	class LoginThread implements Runnable {
//		public void run() {
//			String apiName = "user_login";
//			ArrayList<String> list = new ArrayList<String>();
////			list.add("mobile=" + URLEncoder.encode(accoutnEt.getText().toString().trim(), "UTF-8"));
//			String nickname = accoutnEt.getText().toString().trim();
//			try{
//				nickname = URLEncoder.encode(nickname, "UTF-8");
//			}
//			catch(Exception e){
//				e.printStackTrace();
//			}
//			list.add("mobile=" + nickname);
//			list.add("nickname=" + nickname);
//			list.add("password=" + passwordEt.getText().toString().trim());
//
//			String url = Communication.getApiUrl(apiName, list);
//			try {
//				String json = Communication.getDataByUrl(url, true);
//				if (json != null) {
//					parseLoginResponse(json);
//				} else {
////					myHandler.sendEmptyMessage(2);
//					sendMessage(2, null);
//				}
//			} catch (UnsupportedEncodingException e) {
//				e.printStackTrace();
//			} catch (Exception e) {
////				myHandler.sendEmptyMessage(10);
//				sendMessage(10, null);
//				e.printStackTrace();
//			}
//		}
//	}
	
	
	
	@Override
	protected void handleMessage(Message msg, Activity activity, View rootView) {

		if (pd != null && pd.isShowing()) {
			pd.dismiss();
		}
		switch (msg.what) {
		case MSG_LOGINSUCCESS:
			if(msg.obj != null && msg.obj instanceof String){
				Toast.makeText(getContext(), (String)msg.obj, 0).show();
			}else{
				Toast.makeText(getContext(), "登陆成功", 0).show();
			}
			finishFragment();
			break;
		case MSG_LOGINFAIL:
			if(msg.obj != null && msg.obj instanceof String){
				Toast.makeText(getContext(), (String)msg.obj, 0).show();
			}else{
				Toast.makeText(getContext(), "登录未成功，请稍后重试！", 0).show();
			}
			break;
		case MSG_NEWREGISTERVIEW:
//			m_viewInfoListener.onNewView(new RegisterView(LoginView.this.getContext()));
			pushFragment(new RegisterFragment(), createArguments(null, null));
			break;
		case MSG_FORGETPASSWORDVIEW:
//			m_viewInfoListener.onNewView(new ForgetPasswordView(getContext(), null));
			pushFragment(new ForgetPassFragment(), createArguments(null, null));
			break;
		case 10:
			if (pd != null) {
				pd.dismiss();
			}
			Toast.makeText(getActivity(), "网络连接失败，请检查设置！", 3).show();
			break;
		}
	}

}