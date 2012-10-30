package com.quanleimu.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import android.widget.Button;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Message;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.quanleimu.activity.QuanleimuApplication;
import com.quanleimu.activity.R;
import com.quanleimu.entity.UserBean;

public class LoginUtil implements View.OnClickListener{
	static public interface LoginListener{
		abstract public void onLoginFail(String message);
		abstract public void onLoginSucceed(String message);
		abstract public void onRegisterClicked();
		abstract public void onForgetClicked();
	}
	private View view;
	private LoginListener listener;
	private ProgressDialog pd;
	public LoginUtil(View v, LoginListener listener){
		view = v;
		this.listener = listener;
		if(view != null){
//			View register = view.findViewById(R.id.btn_register);
//			if(register != null){
//				register.setOnClickListener(this);
//			}
			View login = view.findViewById(R.id.btn_login);
			if(login != null){
				login.setOnClickListener(this);
			}


			Button forgetBtn = (Button) view.findViewById(R.id.loginForgetPwdBtn);
			if(forgetBtn != null){
                forgetBtn.setOnClickListener(this);
			}
		}
	}
	
	@Override
	public void onClick(View v){
//		if(v.getId() == R.id.btn_register){
//			if(listener != null){
//				listener.onRegisterClicked();
//			}
//		}else

        if(v.getId() == R.id.btn_login){
			String account = ((TextView)view.findViewById(R.id.et_account)).getText().toString();
			String password = ((TextView)view.findViewById(R.id.et_password)).getText().toString();
			
			if(check(account, password)){
		        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE); 
		        imm.hideSoftInputFromWindow(view.getWindowToken(), 0); 

				pd = ProgressDialog.show(LoginUtil.this.view.getContext(), "提示", "请稍候...");
				pd.setCancelable(true);
				pd.show();
				(new Thread(new LoginThread(account, password))).start();
			}
		}
        else if(v.getId() == R.id.loginForgetPwdBtn){
			if(listener != null){
                listener.onForgetClicked();
			}
		}
	}

	private boolean check(String account, String password) {
		if (account == null || account.trim().equals("")) {
			Toast.makeText(view.getContext(), "账号不能为空！", 0).show();
			return false;
		} else if (password == null || password.trim().equals("")) {
			Toast.makeText(view.getContext(), "密码不能为空！", 0).show();
			return false;
		}
		return true;
	}	
	
	private void parseLoginResponse(String json_response) {
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
				// user.setPhone(accoutnEt.getText().toString());
				String password = ((TextView) view.findViewById(R.id.et_password))
						.getText().toString();
				user.setPassword(password);
				QuanleimuApplication.getApplication()
						.setMobile(user.getPhone());
				Util.saveDataToLocate(view.getContext(), "user", user);

				if(listener != null){
					listener.onLoginSucceed(message == null ? "登陆成功" : message);
				}
			} else {
				if(listener != null){
					listener.onLoginFail(message == null ? "登陆失败" : message);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
			if(listener != null){
				listener.onLoginFail("登陆失败");
			}			
		}
	}

	class LoginThread implements Runnable {
		private String account = "";
		private String password = "";

		public LoginThread(String account, String password) {
			this.account = account;
			this.password = password;
		}

		public void run() {
			String apiName = "user_login";
			ArrayList<String> list = new ArrayList<String>();
			try {
				account = URLEncoder.encode(account, "UTF-8");
			} catch (Exception e) {
				e.printStackTrace();
			}
			list.add("mobile=" + account);
			list.add("nickname=" + account);
			list.add("password=" + password.trim());

			String url = Communication.getApiUrl(apiName, list);
			try {
				String json = Communication.getDataByUrl(url, true);
				if (json != null) {
					parseLoginResponse(json);
					if(pd != null){
						pd.dismiss();
					}
					return;
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(pd != null){
				pd.dismiss();
			}
			
			if(listener != null){
				listener.onLoginFail("登录未成功，请稍后重试！");
			}	
		}
	}
}
