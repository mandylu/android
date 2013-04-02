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
import android.os.CountDownTimer;
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
import com.baixing.anonymous.AnonymousExecuter;
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
import com.baixing.widget.VerifyFailDialog;
import com.quanleimu.activity.R;
import com.tencent.mm.algorithm.Base64;


public class ForgetPassFragment extends BaseFragment implements AnonymousNetworkListener {
	public static final String Forget_Type = "forget_type";///"forget", "edit"
	public static final int MSG_FORGET_PWD_SUCCEED = 0xfff10001;
    private EditText mobileEt;
    private EditText newPwdEt;
    private Button postBtn;
    private boolean isForgetType = false;

    private CountDownTimer countTimer;

    final private int MSG_NETWORK_ERROR = 0;
    final private int MSG_POST_FINISH = 3;
    final private int MSG_POST_ERROR = 1;

	@Override
	public void onResume() {
		this.pv = PV.FORGETPASSWORD;
		Tracker.getInstance().pv(this.pv).end();
		super.onResume();
		paused = false;
		if(needShowDlg){
			this.showVerifyDlg();
			needShowDlg = false;
		}
	}
	
	public void onDestory()
	{
		super.onDestroy();
		countTimer.cancel();
	}
	
	public void initTitle(TitleDef title){
		title.m_visible = true;
		Bundle bundle = getArguments();
		if(bundle != null && bundle.containsKey(ARG_COMMON_TITLE)){
			title.m_title = bundle.getString(ARG_COMMON_TITLE);
		}else{
			title.m_title = "找回密码";
		}
		title.m_leftActionHint = "返回";
	}
	@Override
	public View onInitializeView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootV = inflater.inflate(R.layout.forget_password, null);

        mobileEt = (EditText)rootV.findViewById(R.id.forgetPwdMobileEt);
        postBtn = (Button)rootV.findViewById(R.id.forgetPwdPostBtn);
        Bundle bundle = getArguments();
        if(bundle != null && bundle.containsKey(Forget_Type)){
        	if(bundle.getString(Forget_Type).equals("forget")){
        		rootV.findViewById(R.id.forgetPwdNewPwdEt).setVisibility(View.GONE);
        		postBtn.setText("找回密码");
        		isForgetType = true;
        		
        		boolean defaultNum = false;
        		if(bundle.containsKey("defaultNumber")){
        			String number = bundle.getString("defaultNumber");
        			mobileEt.setText(number);
        			defaultNum = true;
        		}
        		if(!defaultNum){
        			String deviceNum = Util.getDevicePhoneNumber();
        			if(deviceNum != null && Util.isValidMobile(deviceNum)){
        				mobileEt.setText(deviceNum);
        			}
        		}
        		
        	}else{
        		mobileEt.setVisibility(View.GONE);
        		newPwdEt = (EditText)rootV.findViewById(R.id.forgetPwdNewPwdEt);
        		postBtn.setText("修改密码");
        		isForgetType = false;
        	}
        }
        
        
        
        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isForgetType && checkAllInputs() == false) {
                    return;
                }
                showProgress(R.string.dialog_title_info, R.string.dialog_message_waiting, false);
                if(isForgetType){
                	doPostNewPwdAction();
                }else{
                	doReset();
                }    
            }
        });


        return rootV;
	}

    private boolean checkMobile() {
        String mobile = mobileEt.getText().toString();
        if (mobile == null || mobile.length() != 11) {
        	Tracker.getInstance()
			.event(BxEvent.FORGETPASSWORD_SENDCODE_RESULT)
			.append(Key.REGISTER_RESULT_STATUS, false)
			.append(Key.REGISTER_RESULT_FAIL_REASON, "mobile number not 11 bits!")
			.end();
            ViewUtil.showToast(getActivity(), "请输入11位手机号", false);
            return false;
        }
        return true;
    }

    private boolean checkAllInputs() {
        if (checkMobile() == false) {
            return false;
        }
        if(this.isForgetType) return true;

        String tip = null;
        String newPwd = newPwdEt.getText().toString();

        if (newPwd.length() <= 0) {
            tip = "请输入新密码";
        }
        
        if (tip != null) {
        	Tracker.getInstance()
			.event(BxEvent.FORGETPASSWORD_RESETPASSWORD_RESULT)
			.append(Key.REGISTER_RESULT_STATUS, false)
			.append(Key.REGISTER_RESULT_FAIL_REASON, tip)
			.end();
            ViewUtil.showToast(getActivity(), tip, false);
            return false;
        }

        return true;
    }

    private void doPostNewPwdAction() {        
        final String mobile = mobileEt.getText().toString();
//        final String pass = newPwdEt.getText().toString();

		String status = AnonymousExecuter.retreiveAccountStatusSync(mobile);
		if(status != null && (status.equals(BaseAnonymousLogic.Status_Registered_UnVerified)
								|| status.equals(BaseAnonymousLogic.Status_Registered_Verified))){
			AccountService.getInstance().initStatus(mobile);
			AccountService.getInstance().setActionListener(this);
			if(verifyCode == null || verifyCode.length() == 0){
				AccountService.getInstance().start(status, BaseAnonymousLogic.Status_ForgetPwd);
			}else{
//				AccountService.getInstance().start(status, BaseAnonymousLogic.Status_CodeReceived);
				AccountService.getInstance().start(BaseAnonymousLogic.Status_Registered_UnVerified, BaseAnonymousLogic.Status_CodeReceived);
			}
		}else{
			this.hideProgress();
			ViewUtil.showToast(this.getAppContext(), status.equals(BaseAnonymousLogic.Status_UnRegistered) ? "帐号未注册" : status, false);
		}


        
    }

    @Override
    protected void handleMessage(Message msg, Activity activity, View rootView) {
        super.handleMessage(msg, activity, rootView);

        String showMsg = "不可读的信息…";
        if(msg.obj != null && msg.obj instanceof String){
            showMsg = msg.obj.toString();
        }

        switch (msg.what) {
            case MSG_NETWORK_ERROR:
            	hideProgress();
                ViewUtil.showToast(getActivity(), showMsg, false);
                //tracker
                Tracker.getInstance()
                .event(BxEvent.FORGETPASSWORD_SENDCODE_RESULT)
                .append(Key.FORGETPASSWORD_SENDCODE_RESULT_STATUS, false)
                .append(Key.FORGETPASSWORD_SENDCODE_RESULT_FAIL_REASON, (String)msg.obj)
                .end();
                break;

            case MSG_POST_FINISH:
            	hideProgress();
                ViewUtil.showToast(getActivity(), showMsg, false);
                finishFragment(MSG_FORGET_PWD_SUCCEED, null);
              //tracker
                Tracker.getInstance()
                .event(BxEvent.FORGETPASSWORD_RESETPASSWORD_RESULT)
                .append(Key.FORGETPASSWORD_RESETPASSWORD_RESULT_STATUS, true)
                .end();
                break;
            case MSG_POST_ERROR:
            	hideProgress();
                ViewUtil.showToast(getActivity(), showMsg, false);
              //tracker
                Tracker.getInstance()
                .event(BxEvent.FORGETPASSWORD_RESETPASSWORD_RESULT)
                .append(Key.FORGETPASSWORD_RESETPASSWORD_RESULT_STATUS, false)
                .append(Key.FORGETPASSWORD_RESETPASSWORD_RESULT_FAIL_REASON, (String)msg.obj)
                .end();
                break;
        }
    }
    
    public boolean hasGlobalTab()
	{
		return false;
	}

    private void doRetreive(){
		final String mobile = mobileEt.getText().toString();
		ApiParams param = new ApiParams();
		param.addParam("mobile", mobile);
//		ApiClient.getInstance().remoteCall(Api.createPost(apiName), param, this);
		BaseApiCommand.createCommand("getUser", false, param).execute(GlobalDataManager.getInstance().getApplicationContext(), new Callback(){

			@Override
			public void onNetworkDone(String apiName, String responseData) {
				// TODO Auto-generated method stub
				try{
					JSONObject obj = new JSONObject(responseData);
					if(obj != null){
						JSONObject error = obj.getJSONObject("error");
						if(error != null){
							String code = error.getString("code");
							if(code != null && code.equals("0")){
								String password = obj.getString("password");
								String nickname = obj.getString("nickname");
								String id = obj.getString("id");
								String createdTime = obj.getString("createdTime");
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
								sendMessage(MSG_POST_FINISH, error.getString("message"));
							}else{
								sendMessage(MSG_POST_ERROR, error.getString("message"));
							}
						}
					}
				}catch(JSONException e){
					e.printStackTrace();
					sendMessage(MSG_POST_ERROR, "网络异常");
				}				
			}

			@Override
			public void onNetworkFail(String apiName, ApiError error) {
				// TODO Auto-generated method stub
				hideProgress();
				sendMessage(MSG_NETWORK_ERROR, (error == null || error.getMsg() == null ) ? "网络异常" : error.getMsg());
			}
			
		});

    }
    
    private void doReset(){
		ApiParams params = new ApiParams();
		UserBean user = GlobalDataManager.getInstance().getAccountManager().getCurrentUser();
		if(user == null || user.getPhone() == null) return;
		params.addParam("mobile", user.getPhone());
		params.addParam("password", newPwdEt.getText().toString());
		params.addParam("curpassword", user.getPassword());
		params.addParam("code", verifyCode);

    	BaseApiCommand.createCommand("resetpassword", true, params).execute(getActivity(), new Callback() {
			
			@Override
			public void onNetworkFail(String apiName, ApiError error) {
				hideProgress();
				sendMessage(MSG_NETWORK_ERROR, (error == null || error.getMsg() == null ) ? "网络异常" : error.getMsg());
			}
			
			@Override
			public void onNetworkDone(String apiName, String responseData) {
				hideProgress();
                try {
                    JSONObject obj = new JSONObject(responseData).getJSONObject("error");
                    if (!"0".equals(obj.getString("code"))) {
                        sendMessage(MSG_POST_ERROR, obj.getString("message"));
                    } else  {
                        sendMessage(MSG_POST_FINISH, obj.getString("message"));
                        BxMessageCenter.defaultMessageCenter().postNotification(IBxNotificationNames.NOTIFICATION_NEW_PASSWORD, newPwdEt.getText().toString());
                    }
                } catch (JSONException e) {
                    sendMessage(MSG_POST_ERROR, "网络异常");
                }

            }
		});
    }
    
    private String verifyCode;
    private boolean paused = false;
    private boolean needShowDlg = false;
    
    @Override
    public void onPause(){
    	super.onPause();
    	paused = true;
    }
    
    private void showVerifyDlg(){
    	if(this.paused){
    		needShowDlg = true;
    		return;
    	}
    	VerifyFailDialog dlg = new VerifyFailDialog(new VerifyFailDialog.VerifyListener() {
			
			@Override
			public void onReVerify(String mobile) {
				// TODO Auto-generated method stub
				showProgress(R.string.dialog_title_info, R.string.dialog_message_waiting, false);
				doPostNewPwdAction();
			}

			@Override
			public void onSendVerifyCode(String code) {
				// TODO Auto-generated method stub				
//				showProgress(R.string.dialog_title_info, R.string.dialog_message_waiting, false);
				verifyCode = code;
				doPostNewPwdAction();
			}
		});
		dlg.show(getFragmentManager(), null);  
		needShowDlg = false;
    }
    

	@Override
	public void onActionDone(String action, ResponseData response) {
		// TODO Auto-generated method stub
		if(action.equals(AccountService.Action_Done)){
			doRetreive();
		}else{
			if(!response.success){
				if(action.equals(BaseAnonymousLogic.Action_Verify)){
					hideProgress();
					showVerifyDlg();
				}else{
					hideProgress();
					ViewUtil.showToast(this.getAppContext(), response.message, false);
				}
			}else if(action.equals(BaseAnonymousLogic.Action_SendSMS)){
				if(response.success){
					verifyCode = response.message;
				}
			}
		}		
	}
	
	@Override
	public void beforeActionDone(String action, ApiParams outParams) {
		if(action.equals(BaseAnonymousLogic.Action_Verify)){
			outParams.addParam("verifyCode", verifyCode);
		}
	}
}
