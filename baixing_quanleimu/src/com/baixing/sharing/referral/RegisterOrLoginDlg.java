package com.baixing.sharing.referral;

import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.baixing.data.GlobalDataManager;
import com.baixing.entity.UserBean;
import com.baixing.entity.UserProfile;
import com.baixing.message.BxMessageCenter;
import com.baixing.message.IBxNotificationNames;
import com.baixing.network.NetworkUtil;
import com.baixing.network.api.ApiError;
import com.baixing.network.api.ApiParams;
import com.baixing.network.api.BaseApiCommand;
import com.baixing.util.Util;
import com.quanleimu.activity.R;

public class RegisterOrLoginDlg extends DialogFragment {

	private static final String TAG = RegisterOrLoginDlg.class.getSimpleName();

	Timer timer;
	String token;
	String password;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceBundle) {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		View v = inflater.inflate(R.layout.dialog_register_login, null);
		Button RL = (Button) v.findViewById(R.id.btn_register_login);
		
		// test
		final EditText mobileText = (EditText) v.findViewById(R.id.txt_mobile_for_test);
		
		builder.setView(v).setTitle("请登录");
		final AlertDialog Dlg = builder.create();

		RL.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				if(NetworkUtil.isNetworkActive(getActivity())){
					// production
					/*SmsManager smsManager = SmsManager.getDefault();
					token = NetworkUtil.getMD5(Util.getDeviceUdid(getActivity()))
							.substring(0, 6);
					String smsText = "easyRegister_" + token;
					smsManager.sendTextMessage("106901336000", null, smsText, null,
							null);*/
					
					// test - start
					String mobile = mobileText.getText().toString();
					if (!TextUtils.isEmpty(mobile) && (mobile.length() == 11 || mobile.length() == 12)) {
						boolean isPromoter = mobile.startsWith("0");
						mobile = isPromoter ? mobile.substring(1) : mobile;
						if (Util.isValidMobile(mobile)) {
							token = NetworkUtil.getMD5(Util.getDeviceUdid(getActivity()))
									.substring(mobile.length() - 6, 6);
							String smsText = "easyRegister_" + token;
						
							ApiParams params = new ApiParams();
							params.addParam("mobile", mobile);
							params.addParam("content", smsText);
							BaseApiCommand.createCommand("sms_send_test", true, params).executeSync(GlobalDataManager.getInstance().getApplicationContext());
							
							ApiParams param = new ApiParams();
							param.addParam("mobile", mobile);
							param.addParam("type", isPromoter ? ReferralUtil.ROLE_PROMOTER : ReferralUtil.ROLE_NORMAL);
							BaseApiCommand.createCommand("save_promo_user_test", true, param).executeSync(GlobalDataManager.getInstance().getApplicationContext());
						} else {
							mobileText.setText("手机号非法！");
							return;
						}
					} else {
						mobileText.setText("输入非法！");
						return;
					}
					// test - end
				
					new Asker(2);
				}else{
					//lumengdi@baixing.net
					new AlertDialog.Builder(getActivity()) 
		            	.setTitle("网络错误") 
		            	.setMessage("网络连接失败，请确认网络连接") 
		            	.setPositiveButton("确定", new DialogInterface.OnClickListener() { 
		            		@Override 
		            		public void onClick(DialogInterface arg0, int arg1) { 
		            			Intent intent = new Intent("/");  
		            			ComponentName cm = new ComponentName("com.android.settings","com.android.settings.WirelessSettings");  
		            			intent.setComponent(cm);  
		            			intent.setAction("android.intent.action.VIEW");  
		            			getActivity().startActivityForResult( intent , 0);  		            			 
		            		} 
		            	}).show(); 
				}				
			}
		});
		return Dlg;
	}
	
	private void sendEasyRegisterCmd(final TimerTask task) {
		Log.d(TAG, token);
		ApiParams params = new ApiParams();
		params.addParam("token", token);
		BaseApiCommand.createCommand("user_easy_register", false,params).execute(GlobalDataManager.getInstance().getApplicationContext(),new BaseApiCommand.Callback() {

			@Override
			public void onNetworkFail(String apiName,ApiError error) {
				Log.d(TAG, error.toString());
				return;
			}

			@Override
			public void onNetworkDone(String apiName,String responseData) {
				try {
					Log.d(TAG, responseData.toString());
					task.cancel();
					JSONObject obj = new JSONObject(responseData);
					if (obj != null) {
						JSONObject error = obj.getJSONObject("error");
						if (error != null) {
							String code = error.getString("code");
							if (code != null && code.equals("0")) {
								timer.cancel();
								doLoginAction(obj);
							}
						}
					}
				} catch (JSONException e) {
					Log.d(TAG, e.getMessage());
					e.printStackTrace();
				}
			}
		});
	}
	
	private void doLoginAction(JSONObject responseObj) throws JSONException {
		ApiParams params = new ApiParams();
		params.addParam("mobile",responseObj.getString("mobile"));
		String json = BaseApiCommand.createCommand("getUser", false, params).executeSync(GlobalDataManager.getInstance().getApplicationContext());
		try{
			JSONObject obj = new JSONObject(json);
			if(obj != null){
				JSONObject error = obj.getJSONObject("error");
				if(error != null){
					String code = error.getString("code");
					if(code != null && code.equals("0")){
						String mobile = obj.getString("mobile");
						String password = obj.getString("password");
						String nickname = obj.getString("nickname");
						String createdTime = obj.getString("createdTime");
						String id = obj.getString("id");
						UserBean loginBean = new UserBean();
						loginBean.setId(id);
						loginBean.setPhone(mobile);
						String decPwd = Util.getDecryptedPassword(password);
						loginBean.setPassword(decPwd, false);
						Util.saveDataToLocate(GlobalDataManager.getInstance().getApplicationContext(), "user", loginBean);
						UserProfile profile = new UserProfile();
						profile.mobile = mobile;
						profile.nickName = nickname;
						profile.userId = id;
						profile.createTime = createdTime;
						Util.saveDataToLocate(GlobalDataManager.getInstance().getApplicationContext(), "userProfile", profile);
						BxMessageCenter.defaultMessageCenter().postNotification(IBxNotificationNames.NOTIFICATION_LOGIN, loginBean);
						closeDlg();
						
						if (!TextUtils.isEmpty(ReferralPromoter.getInstance().ID())) {
							ReferralNetwork.getInstance().savePromoLog(ReferralPromoter.getInstance().ID(), ReferralUtil.TASK_APP, profile.mobile, null, null, Util.getDeviceUdid(GlobalDataManager.getInstance().getApplicationContext()), profile.userId, null);
						}
					}
				}
			}
		}catch(JSONException e){
			e.printStackTrace();
		}
	}

	public class Asker {
		public Asker(int second) {
			timer = new Timer();
			timer.schedule(new AskTask(), 0, second * 1000);
		}

		class AskTask extends TimerTask {
			public void run() {
				sendEasyRegisterCmd(this);
			}
		}
	}

	public void closeDlg() {
		this.dismiss();
	}
}
