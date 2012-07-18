package com.quanleimu.view;

import java.io.UnsupportedEncodingException;
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
import com.quanleimu.util.Communication;
import com.quanleimu.util.Util;

import android.widget.RelativeLayout;
public class LoginView extends BaseView implements OnClickListener{

	private EditText accoutnEt, passwordEt;
	public String backPageName = "";
	public String categoryEnglishName = "";
	private LinearLayout forget_layout, register_layout;
	
	protected void Init(){
		LayoutInflater inflater = LayoutInflater.from(getContext());
		
		RelativeLayout llLoginRoot = (RelativeLayout)inflater.inflate(R.layout.login, null);
		this.addView(llLoginRoot);
		
		accoutnEt = (EditText) findViewById(R.id.accountEt);
		passwordEt = (EditText) findViewById(R.id.passwordEt);
		forget_layout = (LinearLayout) findViewById(R.id.forget_layout);
		register_layout = (LinearLayout) findViewById(R.id.register_layout);
		
		
		forget_layout.setOnClickListener(this);
		register_layout.setOnClickListener(this);
	}
	
	public LoginView(Context context, String backPageName_){
		super(context);
		this.backPageName = backPageName_;
		
		Init();
		}
	public LoginView(Context context, Bundle bundle){
		super(context);
		
		this.backPageName = bundle.getString("backPageName");
		
		Init();		
		}
	
	public Bundle extracBundle(){
		Bundle bundle = new Bundle();
		bundle.putString("backPageName", this.backPageName);
		
		return bundle;
		}//return a bundle that could be used to re-build the very BaseView
	
	@Override
	public boolean onRightActionPressed(){
		if (check()) {
			pd = ProgressDialog.show(getContext(), "提示", "正在登录，请稍候...");
			pd.setCancelable(true);
			new Thread(new LoginThread()).start();
		}
		
		return true;
	}//called when right button on title bar pressed, return true if handled already, false otherwise
	
	@Override
	public TitleDef getTitleDef(){
		TitleDef title = new TitleDef();
		
		if(null != backPageName)
			title.m_leftActionHint = this.backPageName;
		else
			title.m_leftActionHint = null;
		
		title.m_visible = true;
		title.m_leftActionStyle = EBUTT_STYLE.EBUTT_STYLE_BACK;
		title.m_rightActionHint = "确定";
		title.m_rightActionStyle = EBUTT_STYLE.EBUTT_STYLE_NORMAL;
		title.m_title = "登录";
		
		return title;
	}
	
	@Override
	public TabDef getTabDef(){
		TabDef tab = new TabDef();
		tab.m_visible = false;
		return tab;
		}

	@Override
	public void onClick(View v) {
		if (v == forget_layout) {
			// 忘记密码
			if(null != m_viewInfoListener){
				m_viewInfoListener.onNewView(new ForgetPasswordView(getContext(), null));
			}
		}else if (v == register_layout) {
			// 注册
			if(null != m_viewInfoListener){
				m_viewInfoListener.onNewView(new RegisterView(getContext()));
			}
		} 
	}

	private boolean check() {
		if (accoutnEt.getText().toString().trim().equals("")) {
			Toast.makeText(getContext(), "账号不能为空！", 0).show();
			return false;
		} else if (passwordEt.getText().toString().trim().equals("")) {
			Toast.makeText(getContext(), "密码不能为空！", 0).show();
			return false;
		}
		return true;
	}
	
	@Override
	public void onPreviousViewBack(int message, Object obj){
		if(message == 1){
			Toast.makeText(getContext(), "重置密码成功，请重新登录", 3).show();
		}
	}

	// {"id":"79703763","error":{"message":"用户登录成功","code":0}}
	class LoginThread implements Runnable {
		public void run() {
			String apiName = "user_login";
			ArrayList<String> list = new ArrayList<String>();
//			list.add("mobile=" + URLEncoder.encode(accoutnEt.getText().toString().trim(), "UTF-8"));
			String nickname = accoutnEt.getText().toString().trim();
			try{
				nickname = URLEncoder.encode(nickname, "UTF-8");
			}
			catch(Exception e){
				e.printStackTrace();
			}
			list.add("mobile=" + nickname);
			list.add("nickname=" + nickname);
			list.add("password=" + passwordEt.getText().toString().trim());

			String url = Communication.getApiUrl(apiName, list);
			try {
				String json = Communication.getDataByUrl(url);
				if (json != null) {
					parseLoginResponse(json);
				} else {
					myHandler.sendEmptyMessage(2);
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (Exception e) {
				myHandler.sendEmptyMessage(10);
				e.printStackTrace();
			}
		}
	}
	
//	@Override
//	public void onAttachedToWindow(){
//		super.onAttachedToWindow();
//		
//		if(accoutnEt passwordEt)
//        UserBean user = (UserBean) Util.loadDataFromLocate(getContext(), "user");
//        
//	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(1212 == resultCode){
			
			UserBean user = (UserBean) Util.loadDataFromLocate(getContext(), "user");
			accoutnEt.setText(user.getId()); 
			passwordEt.setText(user.getPassword());
			
			onRightActionPressed();
		}
	}
	
	private void parseLoginResponse(String json_response){
		try {
			JSONObject jsonObject = new JSONObject(json_response);

			String id;
			try {
				id = jsonObject.getString("id");
			} catch (Exception e) {
				id = "";
				e.printStackTrace();
			}
			JSONObject json = jsonObject.getJSONObject("error");
			String message = json.getString("message");
			
			Message msg = Message.obtain();
			
			if (!id.equals("")) {
				// 登录成功
				UserBean user = new UserBean();
				JSONObject jb = jsonObject.getJSONObject("id");
				user.setId(jb.getString("userId"));
				user.setPhone(jb.getString("mobile"));
				//user.setPhone(accoutnEt.getText().toString());
				user.setPassword(passwordEt.getText().toString());
				QuanleimuApplication.getApplication().setMobile(user.getPhone());
				Util.saveDataToLocate(getContext(), "user", user);
				
				msg.what = 0;				
			}else{
				msg.what = 1;
			}
			
			msg.obj = message;		

			myHandler.sendMessage(msg);
		} catch (JSONException e) {
			e.printStackTrace();
			myHandler.sendEmptyMessage(2);
		}		
	}

	// 管理线程的Handler
	Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (pd.isShowing()) {
				pd.dismiss();
			}
			switch (msg.what) {
			case 0:
				Toast.makeText(getContext(), (String)msg.obj, 0).show();
				if(null != m_viewInfoListener){
					m_viewInfoListener.onBack();
				}
				break;
			case 1:
				Toast.makeText(getContext(), (String)msg.obj, 0).show();
				break;				
			case 2:
				Toast.makeText(getContext(), "登录未成功，请稍后重试！", 3).show();
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
