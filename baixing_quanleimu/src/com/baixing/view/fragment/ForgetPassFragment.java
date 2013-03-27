package com.baixing.view.fragment;

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
import com.baixing.util.ViewUtil;
import com.baixing.widget.VerifyFailDialog;
import com.quanleimu.activity.R;


public class ForgetPassFragment extends BaseFragment implements AnonymousNetworkListener {
	
    private EditText mobileEt;
    private EditText newPwdEt;
    private EditText codeEt;
    private Button postBtn;

    private CountDownTimer countTimer;

    final private int MSG_NETWORK_ERROR = 0;
    final private int MSG_POST_FINISH = 3;
    final private int MSG_POST_ERROR = 1;

	@Override
	public void onResume() {
		this.pv = PV.FORGETPASSWORD;
		Tracker.getInstance().pv(this.pv).end();
		super.onResume();
	}
	
	public void onDestory()
	{
		super.onDestroy();
		countTimer.cancel();
	}
	
	public void initTitle(TitleDef title){
		title.m_visible = true;
		title.m_title = "找回密码";
		title.m_leftActionHint = "返回";
	}
	@Override
	public View onInitializeView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootV = inflater.inflate(R.layout.forget_password, null);

        mobileEt = (EditText)rootV.findViewById(R.id.forgetPwdMobileEt);
        newPwdEt = (EditText)rootV.findViewById(R.id.forgetPwdNewPwdEt);
        postBtn = (Button)rootV.findViewById(R.id.forgetPwdPostBtn);
        codeEt = (EditText)rootV.findViewById(R.id.forgetPwdCodeEt);
        
        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkAllInputs() == false) {
                    return;
                }
                showProgress(R.string.dialog_title_info, R.string.dialog_message_waiting, false);
            	if(codeEt.getText() != null && codeEt.getText().length() > 0){
            		doReset();
            	}else{
            		doPostNewPwdAction();
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
			AccountService.getInstance().start(status, BaseAnonymousLogic.Status_ForgetPwd);
		}else{
			this.hideProgress();
			ViewUtil.showToast(this.getAppContext(), "帐号未注册", false);
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
                ViewUtil.showToast(getActivity(), showMsg, false);
                //tracker
                Tracker.getInstance()
                .event(BxEvent.FORGETPASSWORD_SENDCODE_RESULT)
                .append(Key.FORGETPASSWORD_SENDCODE_RESULT_STATUS, false)
                .append(Key.FORGETPASSWORD_SENDCODE_RESULT_FAIL_REASON, (String)msg.obj)
                .end();
                break;

            case MSG_POST_FINISH:
                ViewUtil.showToast(getActivity(), showMsg, false);
                finishFragment();
              //tracker
                Tracker.getInstance()
                .event(BxEvent.FORGETPASSWORD_RESETPASSWORD_RESULT)
                .append(Key.FORGETPASSWORD_RESETPASSWORD_RESULT_STATUS, true)
                .end();
                break;
            case MSG_POST_ERROR:
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
    
    private void doReset(){
		ApiParams params = new ApiParams();
		params.addParam("mobile", mobileEt.getText().toString());
		params.addParam("password", newPwdEt.getText().toString());
		params.addParam("code", codeEt.getText().toString());

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

	@Override
	public void onActionDone(String action, ResponseData response) {
		// TODO Auto-generated method stub
		if(action.equals(AccountService.Action_Done)){
			doReset();    
		}else{
			if(!response.success){
				if(action.equals(BaseAnonymousLogic.Action_AutoVerifiy) 
						|| action.equals(BaseAnonymousLogic.Action_Verify)){
					hideProgress();
					sendMessage(MSG_NETWORK_ERROR, response.message);
				}else{
					hideProgress();
					ViewUtil.showToast(this.getAppContext(), response.message, false);
				}
			}else if(action.equals(BaseAnonymousLogic.Action_SendSMS)){
				if(response.success){
					codeEt.setText(response.message);
				}
			}
		}		
	}
	
	@Override
	public void beforeActionDone(String action, ApiParams outParams) {
	}
}
