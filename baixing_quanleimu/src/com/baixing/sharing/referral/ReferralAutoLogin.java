package com.baixing.sharing.referral;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Message;

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
import com.baixing.util.FavoriteNetworkUtil;
import com.baixing.util.Util;
import com.baixing.util.ViewUtil;
import com.umeng.common.Log;

public class ReferralAutoLogin extends Thread implements AnonymousNetworkListener {
	
	private static final String TAG = ReferralAutoLogin.class.getSimpleName();
	
	Context context;
	String account;
	String password;
	String verifyCode;
	
	public ReferralAutoLogin(Context context) {
		this.context = context;
	}
	
	public void execute() {
		
		account = getAccount();
		password = getPasswd();
		Log.d(TAG, account + " - " + password);
		if(check(account, password)) {
			sendLoginCmd(context, account, password);
		}
	}
	
	private String getAccount() {
		return Util.getDevicePhoneNumber();
	}
	
	private String getPasswd() {
		Uri smsInboxUri = Uri.parse("content://sms/inbox");
		Log.d(TAG, "Uri: " + smsInboxUri.toString());
		Cursor cursor = context.getContentResolver().query(smsInboxUri, new String[] { "_id", "thread_id", "address", "person", "date","body", "type" }, null, null, null);
		String[] columns = new String[] { "address", "person", "date", "body","type" };
		if (cursor.getCount() > 0) {
		   String count = Integer.toString(cursor.getCount());
		   Log.d(TAG, "count: " + count);
		   while (cursor.moveToNext()) {
		       String address = cursor.getString(cursor.getColumnIndex(columns[0]));
		       Log.d(TAG, "addr: " + address);
		       String name = cursor.getString(cursor.getColumnIndex(columns[1]));
		       Log.d(TAG, "name: " + name);
		       String date = cursor.getString(cursor.getColumnIndex(columns[2]));
		       Log.d(TAG, "date: " + date);
		       String msg = cursor.getString(cursor.getColumnIndex(columns[3]));
		       Log.d(TAG, "msg: " + msg);
		       String type = cursor.getString(cursor.getColumnIndex(columns[4]));
		       Log.d(TAG, "type: " + type);
		    }
		}
		return "11235813";
	}

	private boolean check(String account, String password) {
		String tip = "";
		if (account == null || account.trim().equals("")) {
			Tracker.getInstance()
			.event(BxEvent.LOGIN_SUBMIT)
			.append(Key.REGISTER_RESULT_STATUS, false)
			.append(Key.REGISTER_RESULT_FAIL_REASON, "account is empty!")
			.end();
			ViewUtil.showToast(context, "账号不能为空！", false);
			return false;
		} else if (password == null || password.trim().equals("")) {
			Tracker.getInstance()
			.event(BxEvent.LOGIN_SUBMIT)
			.append(Key.REGISTER_RESULT_STATUS, false)
			.append(Key.REGISTER_RESULT_FAIL_REASON, "password is empty!")
			.end();
			ViewUtil.showToast(context, "密码不能为空！", false);
			return false;
		}
		return true;
	}
	
	private void sendLoginCmd(Context cxt, String account, String password) {
		AccountService.getInstance().initStatus(account);
		AccountService.getInstance().setActionListener(this);
		String status = AnonymousExecuter.retreiveAccountStatusSync(account);
		if (status != null && (status.equals(BaseAnonymousLogic.Status_Registered_UnVerified)
								|| status.equals(BaseAnonymousLogic.Status_Registered_Verified))) {
			if (this.verifyCode != null && verifyCode.length() > 0){
				AccountService.getInstance().start(status, BaseAnonymousLogic.Status_CodeReceived);
			}else{
				AccountService.getInstance().start(status);
			}
		} else {
			ViewUtil.showToast(cxt, status.equals(BaseAnonymousLogic.Status_UnRegistered) ? "帐号未注册" : status, false);
		}
	}
	
	@Override
	public void onActionDone(String action, ResponseData response) {
		if (action.equals(AccountService.Action_Done)) {
			ApiParams params = new ApiParams();
			params.addParam("mobile", account);
			params.addParam("nickname", account);
			params.addParam("password", password.trim());
			
			BaseApiCommand.createCommand("user_login", false, params).execute(GlobalDataManager.getInstance().getApplicationContext(), new Callback() {
				
				@Override
				public void onNetworkFail(String apiName, ApiError error) {
					// to do
				}
				
				@Override
				public void onNetworkDone(String apiName, String responseData) {
					if (responseData != null) {
						parseLoginResponse(responseData);
						 
						FavoriteNetworkUtil.syncFavorites(GlobalDataManager.getInstance().getApplicationContext(), 
								GlobalDataManager.getInstance().getAccountManager().getCurrentUser());
						return;
					}
				}
			});
		} else {
			if (!response.success) {		
				if (!action.equals(BaseAnonymousLogic.Action_Verify)) {
					ViewUtil.showToast(context, response.message, false);
				}
			} else if (action.equals(BaseAnonymousLogic.Action_SendSMS)) {
				verifyCode = response.message;
			}
		}
	}
	
	@Override
	public void beforeActionDone(String action, ApiParams outParams) {
		if (action.equals(BaseAnonymousLogic.Action_Verify) && verifyCode != null) {
			outParams.addParam("verifyCode", verifyCode);
			verifyCode = null;
		} else if (action.equals(BaseAnonymousLogic.Action_Register)
				|| action.equals(BaseAnonymousLogic.Action_Login)){
			outParams.addParam("password", password);
		}
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
				user.setPassword(password, true);
				GlobalDataManager.getInstance()
						.setMobile(user.getPhone());
				Util.saveDataToLocate(context, "user", user);
				GlobalDataManager.getInstance().setPhoneNumber(user.getPhone());
				BxMessageCenter.defaultMessageCenter().postNotification(IBxNotificationNames.NOTIFICATION_LOGIN, user);
			}
		} catch (JSONException e) {
			e.printStackTrace();		
		}
	}
}
