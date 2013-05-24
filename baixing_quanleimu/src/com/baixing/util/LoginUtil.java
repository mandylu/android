package com.baixing.util;

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

import com.baixing.anonymous.AccountService;
import com.baixing.anonymous.AnonymousExecuter;
import com.baixing.anonymous.AnonymousNetworkListener;
import com.baixing.anonymous.BaseAnonymousLogic;
import com.baixing.data.GlobalDataManager;
import com.baixing.entity.UserBean;
import com.baixing.message.BxMessageCenter;
import com.baixing.message.IBxNotificationNames;
import com.baixing.network.api.ApiError;
import com.baixing.network.api.ApiParams;
import com.baixing.network.api.BaseApiCommand;
import com.baixing.network.api.BaseApiCommand.Callback;
import com.baixing.tracking.TrackConfig.TrackMobile.BxEvent;
import com.baixing.tracking.TrackConfig.TrackMobile.Key;
import com.baixing.tracking.Tracker;
import com.baixing.widget.VerifyFailDialog;
import com.quanleimu.activity.R;

public class LoginUtil implements View.OnClickListener, AnonymousNetworkListener{
	static public interface LoginListener{
		abstract public void onLoginFail(String message);
		abstract public void onLoginSucceed(String message);
		abstract public void onVerifyFailed(String message);
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
				sendLoginCmd(v.getContext(), account, password);
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
			ViewUtil.showToast(view.getContext(), "账号不能为空！", false);
			return false;
		} else if (password == null || password.trim().equals("")) {
			Tracker.getInstance()
			.event(BxEvent.LOGIN_SUBMIT)
			.append(Key.REGISTER_RESULT_STATUS, false)
			.append(Key.REGISTER_RESULT_FAIL_REASON, "password is empty!")
			.end();
			ViewUtil.showToast(view.getContext(), "密码不能为空！", false);
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
				user.setPassword(password, true);
				GlobalDataManager.getInstance()
						.setMobile(user.getPhone());
				Util.saveDataToLocate(view.getContext(), "user", user);
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
	
	private String password;
	private String account;
	
	private void sendLoginCmd(Context cxt, String account, String password) {
		this.account = account;
		this.password = password;
		AccountService.getInstance().initStatus(account);
		AccountService.getInstance().setActionListener(this);
		String status = AnonymousExecuter.retreiveAccountStatusSync(account);
		if(status != null && (status.equals(BaseAnonymousLogic.Status_Registered_UnVerified)
								|| status.equals(BaseAnonymousLogic.Status_Registered_Verified))){
			if(this.verifyCode != null && verifyCode.length() > 0){
				AccountService.getInstance().start(status, BaseAnonymousLogic.Status_CodeReceived);
			}else{
				AccountService.getInstance().start(status);
			}
		}else{
			if(pd != null){
				pd.dismiss();
			}
			ViewUtil.showToast(view.getContext(), status.equals(BaseAnonymousLogic.Status_UnRegistered) ? "帐号未注册" : status, false);
		}
	}

	@Override
	public void onActionDone(String action, ResponseData response) {
		// TODO Auto-generated method stub
		if(action.equals(AccountService.Action_Done)){
			ApiParams params = new ApiParams();
			params.addParam("mobile", account);
			params.addParam("nickname", account);
			params.addParam("password", password.trim());
			
			BaseApiCommand.createCommand("user_login", false, params).execute(GlobalDataManager.getInstance().getApplicationContext(), new Callback() {
				
				@Override
				public void onNetworkFail(String apiName, ApiError error) {
					if(pd != null){
						pd.dismiss();
					}
					
					if(listener != null){
						String message = error == null ? "登录未成功，请稍后重试！" : error.getMsg(); 
						listener.onLoginFail(message);
					}	
				}
				
				@Override
				public void onNetworkDone(String apiName, String responseData) {
					if (responseData != null) {
						parseLoginResponse(responseData);
						if(pd != null){
							pd.dismiss();
						}
						return;
					}
				}
			});
		}else{
			if(!response.success){
				if(pd != null){
					pd.dismiss();
				}				
				if(action.equals(BaseAnonymousLogic.Action_Verify)){					
					if(listener != null){
						listener.onVerifyFailed(response.message);
					}
				}else{
					ViewUtil.showToast(view.getContext(), response.message, false);
				}
			}else if(action.equals(BaseAnonymousLogic.Action_SendSMS)){
				verifyCode = response.message;
			}
		}
	}
	
	public void reVerify(String code){
		verifyCode = code;
		pd = ProgressDialog.show(LoginUtil.this.view.getContext(), "提示", "请稍候...");
		pd.setCancelable(true);
		pd.show();
		sendLoginCmd(GlobalDataManager.getInstance().getApplicationContext(), this.account, this.password);
	}

	private String verifyCode;
	@Override
	public void beforeActionDone(String action, ApiParams outParams) {
		// TODO Auto-generated method stub
		if(action.equals(BaseAnonymousLogic.Action_Verify) && verifyCode != null){
			outParams.addParam("verifyCode", verifyCode);
			verifyCode = null;
		}else if(action.equals(BaseAnonymousLogic.Action_Register)
				|| action.equals(BaseAnonymousLogic.Action_Login)){
			outParams.addParam("password", password);
		}
	}

}
