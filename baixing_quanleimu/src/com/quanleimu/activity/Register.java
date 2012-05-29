package com.quanleimu.activity;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

import com.quanleimu.entity.PostGoodsBean;
import com.quanleimu.jsonutil.JsonUtil;
import com.quanleimu.util.Communication;
import com.quanleimu.view.BaseView;
import com.quanleimu.view.BaseView.TabDef;
import com.quanleimu.view.BaseView.TitleDef;

public class Register extends BaseView implements View.OnClickListener{

	private EditText accoutnEt, passwordEt,repasswordEt;
	public String backPageName = "";
	public String categoryEnglishName = "";
	public String json = "";
	private LinearLayout forget_layout;

	protected void Init(){
		LayoutInflater inflater = LayoutInflater.from(getContext());
		this.addView(inflater.inflate(R.layout.register, null));
		
		accoutnEt = (EditText) findViewById(R.id.accountEt);
		passwordEt = (EditText) findViewById(R.id.passwordEt);
		repasswordEt = (EditText) findViewById(R.id.repasswordEt);
		
		forget_layout = (LinearLayout) findViewById(R.id.forget_layout);
		forget_layout.setOnClickListener(this);
	}
	
	public Register(Context context){
		super(context); 
		
		Init();
	}
	
	public Register(Context context, Bundle bundle){
		super(context, bundle);
		
		Init();
	}

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
		title.m_leftActionHint = "登陆";
		title.m_rightActionHint = "提交";
		return title;
	}
	
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
				m_viewInfoListener.onNewView(new ForgetPassword(getContext(), null));
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
			System.out.println("url ------ >" + url);
			try {
				json = Communication.getDataByUrl(url);
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
					System.out.println("jsonObject--->" + jsonObject);
					String id,nickname;
					try {
						id = jsonObject.getString("id");
					} catch (Exception e) {
						id = "";
						e.printStackTrace();
					}
					try {
						nickname = jsonObject.getString("nickname");
					} catch (Exception e) {
						nickname = "";
						e.printStackTrace();
					}
					JSONObject json = jsonObject.getJSONObject("error");
					String message = json.getString("message");
					Toast.makeText(getContext(), message, 0).show();
					if (!id.equals("")) {
						// 注册成功
						if(null != m_viewInfoListener){
							m_viewInfoListener.onExit(Register.this);
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
}
