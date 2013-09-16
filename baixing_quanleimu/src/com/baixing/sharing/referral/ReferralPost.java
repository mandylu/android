package com.baixing.sharing.referral;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.telephony.SmsManager;
import android.util.Log;

import com.baixing.anonymous.AccountService;
import com.baixing.anonymous.AnonymousExecuter;
import com.baixing.anonymous.AnonymousNetworkListener;
import com.baixing.anonymous.BaseAnonymousLogic;
import com.baixing.broadcast.CommonIntentAction;
import com.baixing.data.GlobalDataManager;
import com.baixing.entity.UserBean;
import com.baixing.network.NetworkUtil;
import com.baixing.network.api.ApiParams;
import com.baixing.util.Util;
import com.baixing.util.post.PostNetworkService;
import com.baixing.widget.VerifyFailDialog;

public class ReferralPost implements ReferralCallback {
	
	private static final String TAG = ReferralPost.class.getSimpleName();
	
	private static final String SMS_TEXT = "我是百姓网推广员，您的百姓网账号初始密码是：";
	private static final int PASS_LEN = 6;
	
	private static ReferralPost instance = null;
	private static FragmentManager fragmentManager = null;
	private static PostNetworkService postNetworkService = null;
	
	private static String password;
	private static String verifyCode;
	private static boolean IsShowDlg;
	
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
	
	public void postNewAd(String phoneNumber) {
		password = NetworkUtil.getMD5(phoneNumber).substring(0, PASS_LEN);
		IsShowDlg = false;
		
		Intent smsIntent = new Intent();
		smsIntent.setAction(CommonIntentAction.ACTION_SEND_MSG);
		smsIntent.putExtra("phoneNumber", phoneNumber);
		doAction(smsIntent);
		
		sendRegisterCmd(phoneNumber);
	}

	private void sendRegisterCmd(final String phoneNumber) {
		AccountService.getInstance().initStatus(phoneNumber);
		AccountService.getInstance().initPassword(password);
		AccountService.getInstance().setActionListener(new AnonymousNetworkListener() {

			@Override
			public void onActionDone(String action, ResponseData response) {
				// TODO Auto-generated method stub
				Log.d(TAG, "action: " + action);
				if (action.equals(AccountService.Action_Done)) {
					Log.d(TAG, "Action Done");
				} else {
					Log.d(TAG, "response: " + response);
					if (response.success) {
						if (action.equals(BaseAnonymousLogic.Action_Register)) {
							if (!IsShowDlg) {
								showVerifyDlg();
								IsShowDlg = true;
							}
						} else if (action.equals(BaseAnonymousLogic.Action_Verify)) {
							helpSendPost(phoneNumber);
						}
					} else {
						if (action.equals(BaseAnonymousLogic.Action_Verify)) {
							if (!IsShowDlg) {
								showVerifyDlg();
								IsShowDlg = true;
							}
						} else if (action.equals(BaseAnonymousLogic.Action_Register)) {
							if (AnonymousExecuter.retreiveAccountStatusSync(phoneNumber).equals(BaseAnonymousLogic.Status_Registered_Verified)) {
								helpSendPost(phoneNumber);
							} else {
								if (!IsShowDlg) {
									showVerifyDlg();
									IsShowDlg = true;
								}
							}
						}
					}
				}
			}

			@Override
			public void beforeActionDone(String action, ApiParams outParams) {
				// TODO Auto-generated method stub
				Log.d(TAG, "action: " + action);
				Log.d(TAG, "response: " + outParams);
				if (action.equals(BaseAnonymousLogic.Action_Register)) {
					outParams.addParam("password", password);
				} else if (action.equals(BaseAnonymousLogic.Action_Verify) && verifyCode != null) {
					outParams.addParam("verifyCode", verifyCode);
					verifyCode = null;
				}
			}
			
		});
		AccountService.getInstance().start(BaseAnonymousLogic.Status_UnRegistered);
	}
	
	private void helpSendPost(String phone) {
		UserBean curUserBean = GlobalDataManager.getInstance().getAccountManager().getCurrentUser();
		if (curUserBean == null) {
			Log.e(TAG, "promoter didn't login");
			return;
		}
		GlobalDataManager.getInstance().getAccountManager().logout();
		UserBean newUserBean = new UserBean();
		newUserBean.setId(curUserBean.getId());
		newUserBean.setPhone(phone);
		newUserBean.setPassword(password, true);
		Util.saveDataToLocate(GlobalDataManager.getInstance().getApplicationContext(), "user", newUserBean);
		postNetworkService.doRegisterAndVerify(phone);
		GlobalDataManager.getInstance().getAccountManager().logout();
		Util.saveDataToLocate(GlobalDataManager.getInstance().getApplicationContext(), "user", curUserBean);
	}
	
	private void showVerifyDlg(){

		if(fragmentManager != null){
			new VerifyFailDialog(new VerifyFailDialog.VerifyListener() {
				
				@Override
				public void onReVerify(String mobile) {
					IsShowDlg = false;
					AccountService.getInstance().start(BaseAnonymousLogic.Status_Registered_UnVerified);
				}

				@Override
				public void onSendVerifyCode(String code) {
					IsShowDlg = false;
					verifyCode = code;
					AccountService.getInstance().start(BaseAnonymousLogic.Status_Registered_UnVerified, BaseAnonymousLogic.Status_CodeReceived);						
				}
			}).show(fragmentManager, null);
		}
	}

	@Override
	public void doAction(Intent intent) {
		String action = intent.getAction();
		if (CommonIntentAction.ACTION_SEND_MSG.equals(action)) {
			sendMsgFromLocal(intent.getStringExtra("phoneNumber"));
		} else if (CommonIntentAction.ACTION_SENT_POST.equals(action)) {
			ReferralUtil.getInstance().updateReferral("post");
		}
	}
	
	private void sendMsgFromLocal(String phoneNumber) {
		SmsManager smsManager = SmsManager.getDefault();
		String smsText = SMS_TEXT + password;
		smsManager.sendTextMessage(phoneNumber, null, smsText, null, null);
	}
}
