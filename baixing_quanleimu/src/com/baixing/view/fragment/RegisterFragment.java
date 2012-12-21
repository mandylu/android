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
import com.baixing.activity.QuanleimuApplication;
import com.baixing.entity.UserBean;
import com.baixing.message.BxMessageCenter;
import com.baixing.message.IBxNotificationNames;
import com.baixing.util.Communication;
import com.baixing.util.TrackConfig.TrackMobile.BxEvent;
import com.baixing.util.TrackConfig.TrackMobile.Key;
import com.baixing.util.TrackConfig.TrackMobile.PV;
import com.baixing.util.Tracker;
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
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
                    new Thread(new RegisterThread()).start();
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

	class RegisterThread implements Runnable {
		public void run() {

			String apiName = "user_register";
			ArrayList<String> list = new ArrayList<String>();

			list.add("mobile=" + accoutnEt.getText().toString());
			list.add("password=" + passwordEt.getText().toString());
			list.add("isRegister=1");
			String url = Communication.getApiUrl(apiName, list);
			try {
				json = Communication.getDataByUrl(url, true);
				if (json != null) {
					sendMessage(1, null);
				} else {
					sendMessage(2, "response json is null!");
				}
				return;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				sendMessage(2, e.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
				sendMessage(2, e.getMessage());
			}
		}
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
					QuanleimuApplication.getApplication().setMobile(user.getPhone());
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
