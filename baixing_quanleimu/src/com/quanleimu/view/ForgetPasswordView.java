package com.quanleimu.view;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.webkit.WebView;
import android.widget.Toast;

import com.quanleimu.activity.R;
import com.quanleimu.util.Communication;

public class ForgetPasswordView extends BaseView {

	public String backPageName = "";
	public String categoryEnglishName = "";
	public String json = "";
	private WebView web;

	public ForgetPasswordView(Context context, Bundle bundle){
		super(context);
		
		Init();
	}
	
	protected void Init(){
		LayoutInflater inflater = LayoutInflater.from(getContext());
		this.addView(inflater.inflate(R.layout.forget_password, null));
		
		web = (WebView) findViewById(R.id.web);
		web.loadUrl("http://www.baixing.com/auth/findPassword/");
	}

	public TitleDef getTitleDef(){
		TitleDef title = new TitleDef();
		title.m_visible = true;
		title.m_title = "忘记密码";
		title.m_leftActionHint = "返回";
		return title;
	}
	public TabDef getTabDef(){
		TabDef tab = new TabDef();
		tab.m_visible = false;
		return tab;
	}

	// {"id":"79703763","error":{"message":"用户登录成功","code":0}}
	class LoginThread implements Runnable {
		public void run() {
			String apiName = "user_login";
			ArrayList<String> list = new ArrayList<String>();
//			list.add("mobile=" + accoutnEt.getText().toString().trim());
//			list.add("password=" + passwordEt.getText().toString().trim());

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
				myHandler.sendEmptyMessage(3);
				e.printStackTrace();
			} catch (Exception e) {
				myHandler.sendEmptyMessage(3);
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
						// 登陆成功
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			case 2:
				Toast.makeText(getContext(), "登陆未成功，请稍后重试！", 3).show();
				break;
			case 3:
				Toast.makeText(getContext(), "网络连接失败，请检查设置！", 3).show();
				break;				
			}
			super.handleMessage(msg);
		}
	};
}
