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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.quanleimu.entity.PostGoodsBean;
import com.quanleimu.jsonutil.JsonUtil;
import com.quanleimu.util.Communication;

public class Register extends BaseActivity {

	private Button rigster,backBtn;
	private EditText accoutnEt, passwordEt,repasswordEt;
	public String backPageName = "";
	public String categoryEnglishName = "";
	public String json = "";
	public TextView tvTitle;
	private LinearLayout forget_layout;

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
		setContentView(R.layout.register);

		super.onCreate(savedInstanceState);
		// 解决自动弹出输入法  
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		rigster = (Button) findViewById(R.id.registerBtn);
		backBtn = (Button) findViewById(R.id.backBtn);
		accoutnEt = (EditText) findViewById(R.id.accountEt);
		passwordEt = (EditText) findViewById(R.id.passwordEt);
		repasswordEt = (EditText) findViewById(R.id.repasswordEt);
		
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText("注册");
		forget_layout = (LinearLayout) findViewById(R.id.forget_layout);
		forget_layout.setOnClickListener(this);
		rigster.setOnClickListener(this);
		backBtn.setOnClickListener(this);
 
	}

	@Override
	public void onClick(View v) {
		if (v == forget_layout) {
			// 忘记密码
			intent.setClass(Register.this, ForgetPassword.class);
			startActivity(intent);
		} else if (v == rigster) {
			if (check()) {
				pd = ProgressDialog.show(Register.this, "提示", "请稍候...");
				pd.setCancelable(true);
				new Thread(new RegisterThread()).start();
			}
		} else if (v == backBtn) {
			finish();
		}
		super.onClick(v);
	}

	private boolean check() {
		if (accoutnEt.getText().toString().trim().equals("")) {
			Toast.makeText(Register.this, "账号不能为空！", 0).show();
			return false;
		} else if (passwordEt.getText().toString().trim().equals("")) {
			Toast.makeText(Register.this, "密码不能为空！", 0).show();
			return false;
		} else if (!repasswordEt.getText().toString().equals(passwordEt.getText().toString())) {
			Toast.makeText(Register.this, "密码不一致！", 0).show();
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
					Toast.makeText(Register.this, message, 0).show();
					if (!id.equals("")) {
						// 注册成功
						finish();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			case 2:
				Toast.makeText(Register.this, "注册未成功，请稍后重试！", 3).show();
				break;
			}
			super.handleMessage(msg);
		}
	};
}
