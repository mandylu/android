package com.baixing.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Message;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baixing.android.api.ApiError;
import com.baixing.android.api.ApiParams;
import com.baixing.android.api.cmd.BaseCommand;
import com.baixing.android.api.cmd.HttpPostCommand;
import com.baixing.android.api.cmd.BaseCommand.Callback;
import com.baixing.data.GlobalDataManager;
import com.baixing.entity.UserBean;
import com.baixing.message.BxMessageCenter;
import com.baixing.message.IBxNotificationNames;
import com.baixing.tracking.TrackConfig.TrackMobile.BxEvent;
import com.baixing.tracking.TrackConfig.TrackMobile.Key;
import com.baixing.tracking.Tracker;
import com.quanleimu.activity.R;

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
				sendLoginCmd(account, password);
			}
		}
        else if(v.getId() == R.id.loginForgetPwdBtn){
			if(listener != null){
                listener.onForgetClicked();
			}
		}
	}

	private boolean check(String account, String password) {
		String tip = "";
		if (account == null || account.trim().equals("")) {
			Tracker.getInstance()
			.event(BxEvent.LOGIN_SUBMIT)
			.append(Key.REGISTER_RESULT_STATUS, false)
			.append(Key.REGISTER_RESULT_FAIL_REASON, "account is empty!")
			.end();
			Toast.makeText(view.getContext(), "账号不能为空！", 0).show();
			return false;
		} else if (password == null || password.trim().equals("")) {
			Tracker.getInstance()
			.event(BxEvent.LOGIN_SUBMIT)
			.append(Key.REGISTER_RESULT_STATUS, false)
			.append(Key.REGISTER_RESULT_FAIL_REASON, "password is empty!")
			.end();
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
				GlobalDataManager.getInstance()
						.setMobile(user.getPhone());
				Util.saveDataToLocate(view.getContext(), "user", user);
				GlobalDataManager.getInstance().getAccountManager().reloadUser();
				GlobalDataManager.getInstance().setPhoneNumber(user.getPhone());
				BxMessageCenter.defaultMessageCenter().postNotification(IBxNotificationNames.NOTIFICATION_LOGIN, user);
				
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
	
	private void sendLoginCmd(String account, String password) {
		ApiParams params = new ApiParams();
		params.addParam("mobile", account);
		params.addParam("nickname", account);
		params.addParam("password", password.trim());
		
		HttpPostCommand.createCommand(0, "user_login", params).execute(new Callback() {
			
			@Override
			public void onNetworkFail(int requstCode, ApiError error) {
				if(pd != null){
					pd.dismiss();
				}
				
				if(listener != null){
					listener.onLoginFail("登录未成功，请稍后重试！");
				}	
			}
			
			@Override
			public void onNetworkDone(int requstCode, String responseData) {
				if (responseData != null) {
					parseLoginResponse(responseData);
					if(pd != null){
						pd.dismiss();
					}
					return;
				}
			}
		});
	}
}
