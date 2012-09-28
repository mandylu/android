package com.quanleimu.view;

import java.io.UnsupportedEncodingException;
import com.quanleimu.util.LoginUtil;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.quanleimu.activity.QuanleimuApplication;
import com.quanleimu.activity.R;
import com.quanleimu.entity.UserBean;
import com.quanleimu.util.Util;

import android.widget.RelativeLayout;

public class LoginView extends BaseView implements LoginUtil.LoginListener {

	private String backPageName = "";
	private LoginUtil.LoginListener listener;
	private LoginUtil loginHelper;
	private static final int MSG_LOGINFAIL = 1;
	private static final int MSG_LOGINSUCCESS = 2;
	private static final int MSG_NEWREGISTERVIEW = 3;
	private static final int MSG_FORGETPASSWORDVIEW = 4;

	public void onLoginFail(String message){
		Message msg = Message.obtain();
		msg.what = MSG_LOGINFAIL;
		msg.obj = message;
		myHandler.sendMessage(msg);
	}
	public void onLoginSucceed(String message){
		Message msg = Message.obtain();
		msg.what = MSG_LOGINSUCCESS;
		msg.obj = message;
		myHandler.sendMessage(msg);	
	}
	public void onRegisterClicked(){
		myHandler.sendEmptyMessage(MSG_NEWREGISTERVIEW);
	}
	public void onForgetClicked(){
		myHandler.sendEmptyMessage(MSG_FORGETPASSWORDVIEW);
	}
	
	protected void Init() {
		LayoutInflater inflater = LayoutInflater.from(getContext());

		View llLoginRoot = inflater.inflate(R.layout.login, null);
		loginHelper = new LoginUtil(llLoginRoot, this);
		this.addView(llLoginRoot);

	}

	public LoginView(Context context, String backPageName_) {
		super(context);
		this.backPageName = backPageName_;

		Init();
	}

	public LoginView(Context context, Bundle bundle) {
		super(context);

		this.backPageName = bundle.getString("backPageName");

		Init();
	}

	public Bundle extracBundle() {
		Bundle bundle = new Bundle();
		bundle.putString("backPageName", this.backPageName);

		return bundle;
	}// return a bundle that could be used to re-build the very BaseView


	@Override
	public TitleDef getTitleDef() {
		TitleDef title = new TitleDef();

		if (null != backPageName)
			title.m_leftActionHint = this.backPageName;
		else
			title.m_leftActionHint = null;

		title.m_visible = true;
		title.m_leftActionStyle = EBUTT_STYLE.EBUTT_STYLE_BACK;
		title.m_title = "登录";

		return title;
	}

	@Override
	public TabDef getTabDef() {
		TabDef tab = new TabDef();
		tab.m_visible = false;
		return tab;
	}

	@Override
	public void onPreviousViewBack(int message, Object obj) {
		if (message == 1) {
			Toast.makeText(getContext(), "重置密码成功，请重新登录", 3).show();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (1212 == resultCode) {

			UserBean user = (UserBean) Util.loadDataFromLocate(getContext(),
					"user");
//			accoutnEt.setText(user.getId());
//			passwordEt.setText(user.getPassword());

			onRightActionPressed();
		}
	}

	Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
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
				if (null != m_viewInfoListener) {
					m_viewInfoListener.onBack();
				}
				break;
			case MSG_LOGINFAIL:
				if(msg.obj != null && msg.obj instanceof String){
					Toast.makeText(getContext(), (String)msg.obj, 0).show();
				}else{
					Toast.makeText(getContext(), "登录未成功，请稍后重试！", 0).show();
				}
				break;
			case MSG_NEWREGISTERVIEW:
				m_viewInfoListener.onNewView(new RegisterView(LoginView.this.getContext()));
				break;
			case MSG_FORGETPASSWORDVIEW:
				m_viewInfoListener.onNewView(new ForgetPasswordView(getContext(), null));
				break;
			case 10:
				if (pd != null) {
					pd.dismiss();
				}
				Toast.makeText(getContext(), "网络连接失败，请检查设置！", 3).show();
				break;
			}
			super.handleMessage(msg);
		}
	};
}
