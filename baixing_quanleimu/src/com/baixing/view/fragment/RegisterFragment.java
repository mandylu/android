package com.baixing.view.fragment;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baixing.activity.BaseFragment;
import com.baixing.anonymous.AccountService;
import com.baixing.anonymous.AnonymousAccountLogic;
import com.baixing.anonymous.AnonymousNetworkListener;
import com.baixing.anonymous.BaseAnonymousLogic;
import com.baixing.data.GlobalDataManager;
import com.baixing.entity.UserBean;
import com.baixing.entity.UserProfile;
import com.baixing.message.BxMessageCenter;
import com.baixing.message.IBxNotificationNames;
import com.baixing.network.api.ApiError;
import com.baixing.network.api.ApiParams;
import com.baixing.network.api.BaseApiCommand;
import com.baixing.network.api.BaseApiCommand.Callback;
import com.baixing.tracking.TrackConfig.TrackMobile.BxEvent;
import com.baixing.tracking.TrackConfig.TrackMobile.Key;
import com.baixing.tracking.TrackConfig.TrackMobile.PV;
import com.baixing.tracking.Tracker;
import com.baixing.util.Util;
import com.baixing.util.ViewUtil;
import com.baixing.util.post.PostCommonValues;
import com.baixing.widget.VerifyFailDialog;
import com.quanleimu.activity.R;
import com.tencent.mm.algorithm.Base64;

public class RegisterFragment extends BaseFragment implements AnonymousNetworkListener {
	private EditText accoutnEt, passwordEt;
    private Button registerBtn;
    final public static int MSG_REGISTER_SUCCESS = 101;
	public String backPageName = "";
	public String json = "";
	private boolean registered = false;
	@Override
	public View onInitializeView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
        
		View v = inflater.inflate(R.layout.register, null);
		
		accoutnEt = (EditText) v.findViewById(R.id.accountEt);
		Bundle bundle = this.getArguments();
		boolean defaultNum = false;
		if(bundle != null && bundle.containsKey("defaultNumber")){
			String number = bundle.getString("defaultNumber");
			if(Util.isValidMobile(number)){
				accoutnEt.setText(number);
				defaultNum = true;
			}
		}
		if(!defaultNum){
			String deviceNum = Util.getDevicePhoneNumber();
			if(deviceNum != null && Util.isValidMobile(deviceNum)){
				accoutnEt.setText(deviceNum);
			}
		}
		passwordEt = (EditText) v.findViewById(R.id.passwordEt);
        registerBtn = (Button) v.findViewById(R.id.registerBtn);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (check()) {
                    showSimpleProgress();
//                    new Thread(new RegisterThread()).start();
                    sendRegisterCmd();
                }
            }
        });
		
		return v;
	}
	
	@Override
	public boolean handleBack() {
		Tracker.getInstance().event(BxEvent.REGISTER_BACK).end();
		return super.handleBack();
	}

	public void initTitle(TitleDef title){
		title.m_title = "注册账号";
		title.m_visible = true;
		title.m_leftActionHint = "登录";
	}
	
	private boolean check() {
		String msgToShow = null;
		try {
			if (accoutnEt.getText().toString().trim().equals("")) {
				Tracker.getInstance()
				.event(BxEvent.REGISTER_SUBMIT)
				.append(Key.REGISTER_RESULT_STATUS, false)
				.append(Key.REGISTER_RESULT_FAIL_REASON, "account is empty!")
				.end();
				msgToShow = "账号不能为空！";
				return false;
			} else if (passwordEt.getText().toString().trim().equals("")) {
				Tracker.getInstance()
				.event(BxEvent.REGISTER_SUBMIT)
				.append(Key.REGISTER_RESULT_STATUS, false)
				.append(Key.REGISTER_RESULT_FAIL_REASON, "password is empty!")
				.end();
				msgToShow = "密码不能为空！";
				return false;
			}
		} finally {
			if (msgToShow != null) {
				ViewUtil.showToast(getActivity(), msgToShow, false);
			}
		}
		return true;
	}

	// 13564852987//{"id":{"nickname":"API_2129712564","userId":"79703682"},"error":{"message":"用户注册成功","code":0}}
	// 13564852977//{"id":{"nickname":"API_2130603956","userId":"79703763"},"error":{"message":"用户注册成功","code":0}}
	
	private void sendRegisterCmd() {
		AccountService.getInstance().initStatus(accoutnEt.getText().toString());
		AccountService.getInstance().initPassword(passwordEt.getText().toString());
		AccountService.getInstance().setActionListener(this);
		AccountService.getInstance().start(BaseAnonymousLogic.Status_UnRegistered);
//		ApiParams params = new ApiParams();
//		params.addParam("mobile", accoutnEt.getText().toString());
//		params.addParam("password", passwordEt.getText().toString());
//		params.addParam("isRegister", 1);
//		
//		BaseApiCommand.createCommand("user_register", false, params).execute(getActivity(), new Callback() {
//			
//			@Override
//			public void onNetworkFail(String apiName, ApiError error) {
//				sendMessage(2, error == null ? "注册失败"  : error.getMsg());
//			}
//			
//			@Override
//			public void onNetworkDone(String apiName, String responseData) {
//				json = responseData;
//				if (json != null) {
//					sendMessage(1, null);
//				} else {
//					sendMessage(2, "response json is null!");
//				}
//			}
//		});
	}
	
	@Override
	protected void handleMessage(Message msg, Activity activity, View rootView) {

		hideProgress();
		
		switch (msg.what) {
		case 1:
			try {
				JSONObject jsonObject = new JSONObject(json);
				String usrId = "";
				String usrNick = "";
				String id;
				try {
					id = jsonObject.getString("id");
					JSONObject idObj = jsonObject.getJSONObject("id");
					if(idObj != null){
						usrId = idObj.getString("userId");
						usrNick = idObj.getString("nickname");
					}
				} catch (Exception e) {
					id = "";
					e.printStackTrace();
				}
				JSONObject json = jsonObject.getJSONObject("error");
				String message = json.getString("message");
				ViewUtil.showToast(activity, message, false);
				if (!id.equals("")) { // 注册成功
					//tracker
					Tracker.getInstance()
					.event(BxEvent.REGISTER_SUBMIT)
					.append(Key.REGISTER_RESULT_STATUS, true)
					.end();
					
					UserBean user = new UserBean();
					user.setId(usrId);
					user.setPhone(accoutnEt.getText().toString());
					user.setPassword(passwordEt.getText().toString(), true);
					GlobalDataManager.getInstance().setMobile(user.getPhone());
					GlobalDataManager.getInstance().setPhoneNumber(user.getPhone());
					Util.saveDataToLocate(activity, "user", user);
					
					BxMessageCenter.defaultMessageCenter().postNotification(IBxNotificationNames.NOTIFICATION_LOGIN, user);

                    finishFragment(MSG_REGISTER_SUCCESS, null);
				} else {
					//tracker
					Tracker.getInstance()
					.event(BxEvent.REGISTER_SUBMIT)
					.append(Key.REGISTER_RESULT_STATUS, false)
					.append(Key.REGISTER_RESULT_FAIL_REASON, message)
					.end();
				}
			} catch (JSONException e) {
				e.printStackTrace();
				//tracker
				Tracker.getInstance()
				.event(BxEvent.REGISTER_SUBMIT)
				.append(Key.REGISTER_RESULT_STATUS, false)
				.append(Key.REGISTER_RESULT_FAIL_REASON, e.getMessage())
				.end();
			}
			break;
		case 2:
			ViewUtil.showToast(activity, "注册未成功，请稍后重试！", true);
			//tracker
			Tracker.getInstance()
			.event(BxEvent.REGISTER_SUBMIT)
			.append(Key.REGISTER_RESULT_STATUS, false)
			.append(Key.REGISTER_RESULT_FAIL_REASON, (String)msg.obj)
			.end();
			break;
		}
	}
	
	private void showVerifyDlg(){
		if(getFragmentManager() != null){
			if(verifyDlg == null){
				verifyDlg = new VerifyFailDialog(new VerifyFailDialog.VerifyListener() {
					
					@Override
					public void onReVerify(String mobile) {
						// TODO Auto-generated method stub
						showProgress(R.string.dialog_title_info, R.string.dialog_message_waiting, false);
						AccountService.getInstance().start();
					}

					@Override
					public void onSendVerifyCode(String code) {
						// TODO Auto-generated method stub
						verifyCode = code;
						showProgress(R.string.dialog_title_info, R.string.dialog_message_waiting, false);
						AccountService.getInstance().start(BaseAnonymousLogic.Status_Registered_UnVerified, BaseAnonymousLogic.Status_CodeReceived);						
					}
				});
			}
			verifyDlg.show(getFragmentManager(), null);
		}
	}

	@Override
	public void onResume(){
		super.onResume();
		this.pv = PV.REGISTER;
		Tracker.getInstance().pv(this.pv).end();
//		if(dlgShowing){
//
//		}
	}
	
	@Override
	public void onStackTop(boolean isBack)
	{
//		this.finishFragment();
	}
	
	protected void onFragmentBackWithData(int requestCode, Object result)
	{
		this.finishFragment(requestCode, result);
		//TODO:
	}
	
    static private byte[] decript(byte[] encryptedData, byte[] key)
            throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
	    Cipher c = Cipher.getInstance("AES/ECB/ZeroBytePadding");
	    SecretKeySpec k = new SecretKeySpec(key, "AES");
	    c.init(Cipher.DECRYPT_MODE, k);
	    return c.doFinal(encryptedData);
    }

	
    static private String getDecryptedPassword(String encryptedPwd){
		try{
			String key = "c6dd9d408c0bcbeda381d42955e08a3f";
			key = key.substring(0, 16);
			byte[] pwd = decript(Base64.decode(encryptedPwd), key.getBytes("utf-8"));
			String str = new String(pwd);
			return str;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
    }

    
	private void doLoginAfterPostSucceedSync(){
		String mobile = accoutnEt.getText().toString();
		if(mobile == null || mobile.length() == 0) return;
		UserBean bean = GlobalDataManager.getInstance().getAccountManager().getCurrentUser();
		if(bean != null && bean.getPhone() != null && bean.getPhone().equals(mobile)){
			return;
		}
		ApiParams param = new ApiParams();
		param.addParam("mobile", mobile);
//		ApiClient.getInstance().remoteCall(Api.createPost(apiName), param, this);
		String json = BaseApiCommand.createCommand("getUser", false, param).executeSync(GlobalDataManager.getInstance().getApplicationContext());
		try{
			JSONObject obj = new JSONObject(json);
			if(obj != null){
				JSONObject error = obj.getJSONObject("error");
				if(error != null){
					String code = error.getString("code");
					if(code != null && code.equals("0")){
						String password = obj.getString("password");
						String nickname = obj.getString("nickname");
						String createdTime = obj.getString("createdTime");
						String id = obj.getString("id");
						UserBean loginBean = new UserBean();
						loginBean.setId(id);
						loginBean.setPhone(mobile);
						String decPwd = getDecryptedPassword(password);
						loginBean.setPassword(decPwd, false);
						Util.saveDataToLocate(GlobalDataManager.getInstance().getApplicationContext(), "user", loginBean);
						UserProfile profile = new UserProfile();
						profile.mobile = mobile;
						profile.nickName = nickname;
						profile.userId = id;
						profile.createTime = createdTime;
						Util.saveDataToLocate(GlobalDataManager.getInstance().getApplicationContext(), "userProfile", profile);
						BxMessageCenter.defaultMessageCenter().postNotification(IBxNotificationNames.NOTIFICATION_LOGIN, loginBean);
						this.getView().postDelayed(new Runnable(){
							@Override
							public void run(){
								finishFragment(MSG_REGISTER_SUCCESS, null);
							}
						}, 0);
						return;
					}
				}
			}
		}catch(JSONException e){
			e.printStackTrace();
		}
		this.getView().postDelayed(new Runnable(){
			@Override
			public void run(){
				finishFragment();
			}
		}, 0);
	}

	private String verifyCode; 
	private VerifyFailDialog verifyDlg;
//	private boolean dlgShowing = false;
	@Override
	public void onActionDone(String action, ResponseData response) {
		// TODO Auto-generated method stub
		if(action.equals(AccountService.Action_Done)){
			doLoginAfterPostSucceedSync();
		}else{
			if(!response.success){
				if(action.equals(BaseAnonymousLogic.Action_Verify)){
						showVerifyDlg();
//						dlgShowing = true;
				}else{
					this.hideProgress();
					ViewUtil.showToast(getAppContext(), response.message, false);
				}
				
			}else if(action.equals(AnonymousAccountLogic.Action_SendSMS)){
				verifyCode = response.message;
			}
		}		
	}

	@Override
	public void beforeActionDone(String action, ApiParams outParams) {
		// TODO Auto-generated method stub
		if(action.equals(BaseAnonymousLogic.Action_Verify) && verifyCode != null){
			outParams.addParam("verifyCode", verifyCode);
			verifyCode = null;
		}else if(action.equals(BaseAnonymousLogic.Action_AutoVerifiy) || action.equals(BaseAnonymousLogic.Action_Register)){
			outParams.addParam("password", passwordEt.getText().toString());
		}
	}
}
