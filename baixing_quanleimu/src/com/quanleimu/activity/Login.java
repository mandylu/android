package com.quanleimu.activity;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.quanleimu.entity.UserBean;
import com.quanleimu.util.Communication;
import com.quanleimu.util.Util;
import com.quanleimu.view.SetMain;

public class Login extends BaseActivity {

	private Button login, backBtn;
	private EditText accoutnEt, passwordEt;
	public String backPageName = "";
	public String categoryEnglishName = "";
	public String json = "";
	public TextView tvTitle;
	private LinearLayout forget_layout, register_layout;
	private int type = 0;
	

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
		setContentView(R.layout.login);

		super.onCreate(savedInstanceState);
		intent = getIntent();
		bundle = intent.getExtras();
		String txt = bundle.getString("back");
		type = bundle.getInt("type");

		// 解决自动弹出输入法
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		login = (Button) findViewById(R.id.login);
		accoutnEt = (EditText) findViewById(R.id.accountEt);
		passwordEt = (EditText) findViewById(R.id.passwordEt);
		backBtn = (Button) findViewById(R.id.backBtn);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText("登录"); 
		forget_layout = (LinearLayout) findViewById(R.id.forget_layout);
		register_layout = (LinearLayout) findViewById(R.id.register_layout);
		if (txt != null) {
			if (!txt.equals("")) {
				backBtn.setText(txt);
			} else {
				backBtn.setVisibility(View.GONE);
			}
		}else{
			backBtn.setVisibility(View.GONE);
		}
		forget_layout.setOnClickListener(this);
		register_layout.setOnClickListener(this);
		login.setOnClickListener(this);
		backBtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v == forget_layout) {
			// 忘记密码
			intent.setClass(Login.this, ForgetPassword.class);
			startActivity(intent);
		} else if (v == login) {
			if (check()) {
				pd = ProgressDialog.show(Login.this, "提示", "请稍候...");
				pd.setCancelable(true);
				new Thread(new LoginThread()).start();
			}
		} else if (v == register_layout) {
			// 注册
			intent.setClass(Login.this, Register.class);
			startActivity(intent);
		} else if (v == backBtn) {
			finish();
		}
		super.onClick(v);
	}

	private boolean check() {
		if (accoutnEt.getText().toString().trim().equals("")) {
			Toast.makeText(Login.this, "账号不能为空！", 0).show();
			return false;
		} else if (passwordEt.getText().toString().trim().equals("")) {
			Toast.makeText(Login.this, "密码不能为空！", 0).show();
			return false;
		}
		return true;
	}

	// {"id":"79703763","error":{"message":"用户登录成功","code":0}}
	class LoginThread implements Runnable {
		public void run() {
			String apiName = "user_login";
			ArrayList<String> list = new ArrayList<String>();
			list.add("mobile=" + accoutnEt.getText().toString().trim());
			list.add("nickname=" + accoutnEt.getText().toString().trim());
			list.add("password=" + passwordEt.getText().toString().trim());

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
				myHandler.sendEmptyMessage(10);
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
					Toast.makeText(Login.this, message, 0).show();
					if (!id.equals("")) {
						// 登陆成功
						UserBean user = new UserBean();
						JSONObject jb = jsonObject.getJSONObject("id");
						user.setId(jb.getString("userId"));
						user.setPhone(jb.getString("mobile"));
						//user.setPhone(accoutnEt.getText().toString());
						user.setPassword(passwordEt.getText().toString());
						myApp.setMobile(user.getPhone());
						Util.saveDataToLocate(Login.this, "user", user);
						// bundle.putSerializable("user", user);
						// intent.putExtras(bundle);
						if(type == 0){
							intent.setClass(Login.this, SetMain.class);
						}else if(type == 1){
							intent.setClass(Login.this, MyCenter.class);
						}else if(type ==2){
							intent.setClass(Login.this, PostGoods.class);
						}
						startActivity(intent);
						finish();
						// setResult(1, intent);
						
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			case 2:
				Toast.makeText(Login.this, "登陆未成功，请稍后重试！", 3).show();
				break;
			case 10:
				if (pd != null) {
					pd.dismiss();
				}
				Toast.makeText(Login.this, "网络连接失败，请检查设置！", 3).show();
				break;
			}
			super.handleMessage(msg);
		}
	};
}
