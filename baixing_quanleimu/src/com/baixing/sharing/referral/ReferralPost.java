package com.baixing.sharing.referral;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.baixing.anonymous.AccountService;
import com.baixing.anonymous.AnonymousNetworkListener;
import com.baixing.anonymous.BaseAnonymousLogic;
import com.baixing.broadcast.CommonIntentAction;
import com.baixing.data.GlobalDataManager;
import com.baixing.entity.UserBean;
import com.baixing.network.NetworkUtil;
import com.baixing.network.api.ApiError;
import com.baixing.network.api.ApiParams;
import com.baixing.network.api.BaseApiCommand;
import com.baixing.network.api.BaseApiCommand.Callback;
import com.baixing.util.Util;
import com.baixing.util.post.PostNetworkService;
import com.baixing.widget.VerifyFailDialog;

public class ReferralPost implements ReferralCallback, AnonymousNetworkListener {
	
	private static final String TAG = ReferralPost.class.getSimpleName();
	
	private static ReferralPost instance = null;
	private static FragmentManager fragmentManager = null;
	private static PostNetworkService postNetworkService = null;
	
	private static String password;
	private static String verifyCode;
	private static boolean IsShowDlg;
	private static String phoneNumber;
	
	private UserBean promoterBean = null;
	private UserBean businessBean = null;
	
	public static ReferralPost getInstance() {
		if (instance != null) {
			return instance;
		}
		instance = new ReferralPost();
		return instance;
	}
	
	public static void Config(FragmentManager fragmentManager, PostNetworkService postNetworkService) {
		ReferralPost.fragmentManager = fragmentManager;
		ReferralPost.postNetworkService = postNetworkService;
	}
	
	public void postNewAd(String phone) {
		phoneNumber = phone;
		password = getPasswd(phone);
		IsShowDlg = false;
		
		startVerify(phone);
	}
	
	private String getPasswd(String phone) {
		return NetworkUtil.getMD5(phone.substring(3, 10));
	}
	
	private void startVerify(String phoneNumber) {
		String phoneStatus = AccountService.getInstance().getAccountStatus(phoneNumber);
		AccountService.getInstance().initStatus(phoneNumber);
		AccountService.getInstance().setActionListener(this);
		Log.d(TAG, phoneStatus);
		if (BaseAnonymousLogic.Status_Registered_Verified.equals(phoneStatus)) {
			AccountService.getInstance().start(BaseAnonymousLogic.Status_Registered_Verified, BaseAnonymousLogic.Status_ForgetPwd);
		} else {
			AccountService.getInstance().start(phoneStatus);
		}
		
		if (!IsShowDlg) {
			showVerifyDlg(phoneNumber);
			IsShowDlg = true;
		}
	}
	
	private void helpSendPost(String phone) {
		ApiParams param = new ApiParams();
		param.addParam("mobile", phoneNumber);
		BaseApiCommand.createCommand("getUser", false, param).execute(GlobalDataManager.getInstance().getApplicationContext(), new Callback(){

			@Override
			public void onNetworkDone(String apiName, String responseData) {
				try {
					JSONObject obj = new JSONObject(responseData);
					if (obj != null) {
						JSONObject error = obj.getJSONObject("error");
						if (error != null) {
							String code = error.getString("code");
							if (code != null && code.equals("0")) {
								String password = obj.getString("password");
								String id = obj.getString("id");
								UserBean otherBean = new UserBean();
								otherBean.setId(id);
								otherBean.setPhone(phoneNumber);
								String decPwd = Util.getDecryptedPassword(password);
								otherBean.setPassword(decPwd, false);
								businessBean = otherBean;
								
								UserBean ownerBean = GlobalDataManager.getInstance().getAccountManager().getCurrentUser();
								if (ownerBean == null) {
									Log.e(TAG, "promoter didn't login");
									return;
								}
								promoterBean = ownerBean;
								
								GlobalDataManager.getInstance().getAccountManager().logout();
								Util.saveDataToLocate(GlobalDataManager.getInstance().getApplicationContext(), "user", otherBean);
								postNetworkService.doRegisterAndVerify(phoneNumber);
								GlobalDataManager.getInstance().getAccountManager().logout();
								Util.saveDataToLocate(GlobalDataManager.getInstance().getApplicationContext(), "user", ownerBean);
							}
						}
					}
				} catch(JSONException e) {
					e.printStackTrace();
				}				
			}

			@Override
			public void onNetworkFail(String apiName, ApiError error) {
				// TODO Auto-generated method stub
				Log.e(TAG, "Network Error");
			}
			
		});
	}
	
	private void showVerifyDlg(final String phone) {

		if(fragmentManager != null) {
			new VerifyFailDialog(new VerifyFailDialog.VerifyListener() {
				
				@Override
				public void onReVerify(String mobile) {
					IsShowDlg = false;
					startVerify(phone);
				}

				@Override
				public void onSendVerifyCode(String code) {
					IsShowDlg = false;
					verifyCode = code;
					sendVerifyCode(phone);						
				}
			}).show(fragmentManager, null);
		}
	}
	
	private void sendVerifyCode(String mobile) {
		String status = AccountService.getInstance().getAccountStatus(mobile);
		if (status != null && !status.equals(BaseAnonymousLogic.Status_UnRegistered)) {
			AccountService.getInstance().initStatus(mobile);
			AccountService.getInstance().setActionListener(this);
			AccountService.getInstance().start(BaseAnonymousLogic.Status_Registered_UnVerified, BaseAnonymousLogic.Status_CodeReceived);
		} else {
			Log.e(TAG, "failed to registered");
		}
	}

	@Override
	public void doAction(Intent intent) {
		String action = intent.getAction();
		if (CommonIntentAction.ACTION_SEND_MSG.equals(action)) {
			//sendMsgFromLocal(intent.getStringExtra("phoneNumber"));
		} else if (CommonIntentAction.ACTION_SENT_POST.equals(action)) {
			if (promoterBean != null && businessBean != null) {
				HashMap<String, String> attrs = new HashMap<String, String>();
				attrs.put("adId", intent.getStringExtra("adId"));
				if (ReferralNetwork.getInstance().savePromoLog(promoterBean.getPhone(), ReferralUtil.TASK_POST, businessBean.getPhone(), Util.getDeviceUdid(GlobalDataManager.getInstance().getApplicationContext()), promoterBean.getId(), null, businessBean.getId(), attrs)) {
					// log success
				}
			}
		}
	}
	
	@Override
	public void onActionDone(String action, ResponseData response) {
		Log.d(TAG, "action: " + action);
		if (action.equals(AccountService.Action_Done)) {
			Log.d(TAG, "Action Done");
		} else {
			Log.d(TAG, "response: " + response.message);
			if (action.equals(BaseAnonymousLogic.Action_Verify) && response.success) {
				helpSendPost(phoneNumber);
			} else if (action.equals(BaseAnonymousLogic.Action_Register) && !response.success) {
				startVerify(phoneNumber);
			} else if (!IsShowDlg) {
				showVerifyDlg(phoneNumber);
				IsShowDlg = true;
			}
		}
	}

	@Override
	public void beforeActionDone(String action, ApiParams outParams) {
		Log.d(TAG, "action: " + action);
		Log.d(TAG, "outParams: " + outParams.toString());
		if (action.equals(BaseAnonymousLogic.Action_Register)) {
			outParams.addParam("password", password);
		} else if (action.equals(BaseAnonymousLogic.Action_Verify)) {
			outParams.addParam("verifyCode", verifyCode);
			verifyCode = null;
		}
	}
}
