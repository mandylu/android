package com.baixing.sharing.referral;

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
import com.baixing.network.api.ApiParams;
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
		password = getPasswd(phoneNumber);
		IsShowDlg = false;
		
		startVerify(phoneNumber);
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
		
		Intent smsIntent = new Intent();
		smsIntent.setAction(CommonIntentAction.ACTION_SEND_MSG);
		smsIntent.putExtra("phoneNumber", phone);
		doAction(smsIntent);
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
			ReferralNetwork.getInstance().updateReferral("post", intent.getStringExtra("phoneNumber"));
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
