package com.baixing.view.fragment;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

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
import android.widget.Toast;

import com.baixing.activity.BaseFragment;
import com.baixing.android.api.ApiError;
import com.baixing.android.api.ApiParams;
import com.baixing.android.api.cmd.BaseCommand;
import com.baixing.android.api.cmd.BaseCommand.Callback;
import com.baixing.android.api.cmd.HttpPostCommand;
import com.baixing.data.GlobalDataManager;
import com.baixing.entity.UserBean;
import com.baixing.message.BxMessageCenter;
import com.baixing.message.IBxNotificationNames;
import com.baixing.tracking.TrackConfig.TrackMobile.BxEvent;
import com.baixing.tracking.TrackConfig.TrackMobile.Key;
import com.baixing.tracking.TrackConfig.TrackMobile.PV;
import com.baixing.tracking.Tracker;
import com.baixing.util.Communication;
import com.baixing.util.Util;
import com.quanleimu.activity.R;

public class RegisterFragment extends BaseFragment {
	private EditText accoutnEt, passwordEt,repasswordEt;
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
		passwordEt = (EditText) v.findViewById(R.id.passwordEt);
		repasswordEt = (EditText) v.findViewById(R.id.repasswordEt);
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
		if (accoutnEt.getText().toString().trim().equals("")) {
			Tracker.getInstance()
			.event(BxEvent.REGISTER_SUBMIT)
			.append(Key.REGISTER_RESULT_STATUS, false)
			.append(Key.REGISTER_RESULT_FAIL_REASON, "account is empty!")
			.end();
			Toast.makeText(getActivity(), "账号不能为空！", Toast.LENGTH_SHORT).show();
			return false;
		} else if (passwordEt.getText().toString().trim().equals("")) {
			Tracker.getInstance()
			.event(BxEvent.REGISTER_SUBMIT)
			.append(Key.REGISTER_RESULT_STATUS, false)
			.append(Key.REGISTER_RESULT_FAIL_REASON, "password is empty!")
			.end();
			Toast.makeText(getActivity(), "密码不能为空！", Toast.LENGTH_SHORT).show();
			return false;
		} else if (!repasswordEt.getText().toString().equals(passwordEt.getText().toString())) {
			Tracker.getInstance()
			.event(BxEvent.REGISTER_SUBMIT)
			.append(Key.REGISTER_RESULT_STATUS, false)
			.append(Key.REGISTER_RESULT_FAIL_REASON, "password not matches repassword!")
			.end();
			Toast.makeText(getActivity(), "密码不一致！", Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}

	// 13564852987//{"id":{"nickname":"API_2129712564","userId":"79703682"},"error":{"message":"用户注册成功","code":0}}
	// 13564852977//{"id":{"nickname":"API_2130603956","userId":"79703763"},"error":{"message":"用户注册成功","code":0}}
	
	private void sendRegisterCmd() {
		ApiParams params = new ApiParams();
		params.addParam("mobile", accoutnEt.getText().toString());
		params.addParam("password", passwordEt.getText().toString());
		params.addParam("isRegister", 1);
		
		HttpPostCommand.createCommand(0, "user_register", params).execute(new Callback() {
			
			@Override
			public void onNetworkFail(int requstCode, ApiError error) {
				sendMessage(2, error == null ? "注册失败"  : error.getMsg());
			}
			
			@Override
			public void onNetworkDone(int requstCode, String responseData) {
				json = responseData;
				if (json != null) {
					sendMessage(1, null);
				} else {
					sendMessage(2, "response json is null!");
				}
			}
		});
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
				Toast.makeText(activity, message, 0).show();
				if (!id.equals("")) { // 注册成功
					//tracker
					Tracker.getInstance()
					.event(BxEvent.REGISTER_SUBMIT)
					.append(Key.REGISTER_RESULT_STATUS, true)
					.end();
					
					UserBean user = new UserBean();
					user.setId(usrId);
					user.setPhone(accoutnEt.getText().toString());
					user.setPassword(passwordEt.getText().toString());
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
			Toast.makeText(activity, "注册未成功，请稍后重试！", 3).show();
			//tracker
			Tracker.getInstance()
			.event(BxEvent.REGISTER_SUBMIT)
			.append(Key.REGISTER_RESULT_STATUS, false)
			.append(Key.REGISTER_RESULT_FAIL_REASON, (String)msg.obj)
			.end();
			break;
		}
	}

	@Override
	public void onResume(){
		super.onResume();
		this.pv = PV.REGISTER;
		Tracker.getInstance().pv(this.pv).end();
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

	
}
