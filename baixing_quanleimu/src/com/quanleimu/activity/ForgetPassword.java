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
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.quanleimu.entity.PostGoodsBean;
import com.quanleimu.jsonutil.JsonUtil;
import com.quanleimu.util.Communication;

public class ForgetPassword extends BaseActivity {

	public String backPageName = "";
	public String categoryEnglishName = "";
	public String json = "";
	public TextView tvTitle;
	private Button backBtn; 
	private WebView web;

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.forget_password);

		super.onCreate(savedInstanceState);
		// 解决自动弹出输入法
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText("忘记密码");
		
		web = (WebView) findViewById(R.id.web);
		web.loadUrl("http://www.baixing.com/auth/findPassword/");
		
		backBtn = (Button)findViewById(R.id.backBtn);
		backBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ForgetPassword.this.finish();
			}
		});
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
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
					Toast.makeText(ForgetPassword.this, message, 0).show();
					if (!id.equals("")) {
						// 登陆成功
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			case 2:
				Toast.makeText(ForgetPassword.this, "登陆未成功，请稍后重试！", 3).show();
				break;
			case 3:
				Toast.makeText(ForgetPassword.this, "网络连接失败，请检查设置！", 3).show();
				break;				
			}
			super.handleMessage(msg);
		}
	};
}
