package com.quanleimu.view;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.Toast;

import com.quanleimu.activity.QuanleimuApplication;
import com.quanleimu.activity.R;
import com.quanleimu.util.Communication;
import com.quanleimu.util.Util;
import com.quanleimu.entity.UserBean;

public class RegisterView extends BaseView{

	private EditText accoutnEt, passwordEt,repasswordEt;
	public String backPageName = "";
	public String categoryEnglishName = "";
	public String json = "";

	protected void Init(){
        
		LayoutInflater inflater = LayoutInflater.from(getContext());
		addView(inflater.inflate(R.layout.register, null));
		
		accoutnEt = (EditText) findViewById(R.id.accountEt);
		passwordEt = (EditText) findViewById(R.id.passwordEt);
		repasswordEt = (EditText) findViewById(R.id.repasswordEt);
	}
	
	public RegisterView(Context context){
		super(context); 
		
		Init();
	}
	
	public RegisterView(Context context, Bundle bundle){
		super(context, bundle);
		
		Init();
	}
	
//	@Override
//	public void onAttachedToWindow(){
//		super.onAttachedToWindow();
//		
//		EditText activeText = 	accoutnEt.isFocused() ? accoutnEt :
//								passwordEt.isFocused() ? passwordEt :
//								repasswordEt.isFocused() ? repasswordEt : null;
//
//		if(null != activeText){
//	        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE); 
//	        imm.showSoftInput(activeText, 0);
//	        imm.toggleSoftInput(0,InputMethodManager.HIDE_NOT_ALWAYS);
//		}
//	}
	
	

	public boolean onRightActionPressed(){
		if (check()) {
			pd = ProgressDialog.show(getContext(), "提示", "请稍候...");
			pd.setCancelable(true);
			new Thread(new RegisterThread()).start();
		}
		
		return true;
	}//called when right button on title bar pressed, return true if handled already, false otherwise
	
	public TitleDef getTitleDef(){
		TitleDef title = new TitleDef();
		title.m_title = "注册账号";
		title.m_visible = true;
		title.m_leftActionHint = "登录";
		title.m_rightActionHint = "提交";
		return title;
	}
	
	public TabDef getTabDef(){
		TabDef tab = new TabDef();
		tab.m_visible = false;
		return tab;
	}


	private boolean check() {
		if (accoutnEt.getText().toString().trim().equals("")) {
			Toast.makeText(getContext(), "账号不能为空！", 0).show();
			return false;
		} else if (passwordEt.getText().toString().trim().equals("")) {
			Toast.makeText(getContext(), "密码不能为空！", 0).show();
			return false;
		} else if (!repasswordEt.getText().toString().equals(passwordEt.getText().toString())) {
			Toast.makeText(getContext(), "密码不一致！", 0).show();
			return false;
		}
		return true;
	}

	// 13564852987//{"id":{"nickname":"API_2129712564","userId":"79703682"},"error":{"message":"用户注册成功","code":0}}
	// 13564852977//{"id":{"nickname":"API_2130603956","userId":"79703763"},"error":{"message":"用户注册成功","code":0}}

	class RegisterThread implements Runnable {
		public void run() {

			String apiName = "user_register";
			ArrayList<String> list = new ArrayList<String>();

			list.add("mobile=" + accoutnEt.getText().toString());
			list.add("password=" + passwordEt.getText().toString());
			list.add("isRegister=1");
			String url = Communication.getApiUrl(apiName, list);
			try {
				json = Communication.getDataByUrl(url, true);
				if (json != null) {
					myHandler.sendEmptyMessage(1);
				} else {
					myHandler.sendEmptyMessage(2);
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
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
			case 1:
				try {
					JSONObject jsonObject = new JSONObject(json);
					String id;
					try {
						id = jsonObject.getString("id");
					} catch (Exception e) {
						id = "";
						e.printStackTrace();
					}
					JSONObject json = jsonObject.getJSONObject("error");
					String message = json.getString("message");
					Toast.makeText(getContext(), message, 0).show();
					if (!id.equals("")) {
						// 注册成功
						
						UserBean user = new UserBean();
						user.setId(accoutnEt.getText().toString());
						user.setPhone(accoutnEt.getText().toString());
						user.setPassword(passwordEt.getText().toString());
						QuanleimuApplication.getApplication().setMobile(user.getPhone());
						Util.saveDataToLocate(getContext(), "user", user);
						
						if(null != m_viewInfoListener){
							m_viewInfoListener.onBack();
							m_viewInfoListener.onSetResult(1212, 1212, null);
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			case 2:
				Toast.makeText(getContext(), "注册未成功，请稍后重试！", 3).show();
				break;
			}
			super.handleMessage(msg);
		}
	};

//	@Override
//	public void onFocusChange(View v, boolean hasFocus) {
//	
//		if(hasFocus){
//			EditText activeText = 	accoutnEt.isFocused() ? accoutnEt :
//									passwordEt.isFocused() ? passwordEt :
//									repasswordEt.isFocused() ? repasswordEt : null;
//	        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE); 
//	        imm.showSoftInput(activeText, 0);
//	        imm.toggleSoftInput(0,InputMethodManager.HIDE_NOT_ALWAYS);
//		}
//	}
}
