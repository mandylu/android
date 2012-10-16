package com.quanleimu.view.fragment;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.quanleimu.activity.BaseFragment;
import com.quanleimu.activity.BaseFragment.TabDef;
import com.quanleimu.activity.BaseFragment.TitleDef;
import com.quanleimu.activity.QuanleimuApplication;
import com.quanleimu.activity.R;
import com.quanleimu.entity.UserBean;
import com.quanleimu.entity.UserProfile;
import com.quanleimu.util.Communication;
import com.quanleimu.util.Util;
import android.util.Log;

public class RegisterFragment extends BaseFragment {
	private EditText accoutnEt, passwordEt,repasswordEt;
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
		
		return v;
	}
	
	public void handleRightAction(){
//		if ("13512135857".equalsIgnoreCase(accoutnEt.getText().toString()))
//		{
//			UserBean user = new UserBean();
//			user.setId("13512135857");
//			user.setPhone("13512135857");
//			user.setPassword("123456");
//			QuanleimuApplication.getApplication().setMobile(user.getPhone());
//			Util.saveDataToLocate(getContext(), "user", user);
//			finishFragment(requestCode, null);
//			return;
//		}
		if (check()) {
			showSimpleProgress();
			new Thread(new RegisterThread()).start();
		}
	}//called when right button on title bar pressed, return true if handled already, false otherwise
	
	public void initTitle(TitleDef title){
		title.m_title = "注册账号";
		title.m_visible = true;
		title.m_leftActionHint = "登录";
//		title.m_rightActionHint = "提交";
		title.m_rightActionImg = -1;//FIXME:
	}
	
	public void initTab(TabDef tab){
		tab.m_visible = false;
	}


	private boolean check() {
		if (accoutnEt.getText().toString().trim().equals("")) {
			Toast.makeText(getActivity(), "账号不能为空！", Toast.LENGTH_SHORT).show();
			return false;
		} else if (passwordEt.getText().toString().trim().equals("")) {
			Toast.makeText(getActivity(), "密码不能为空！", Toast.LENGTH_SHORT).show();
			return false;
		} else if (!repasswordEt.getText().toString().equals(passwordEt.getText().toString())) {
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
					sendMessage(2, null);
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
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
				if (!id.equals("")) {
					// 注册成功
					
					UserBean user = new UserBean();
//					user.setId(accoutnEt.getText().toString());
					user.setId(usrId);
					user.setPhone(accoutnEt.getText().toString());
					user.setPassword(passwordEt.getText().toString());
					QuanleimuApplication.getApplication().setMobile(user.getPhone());
					Util.saveDataToLocate(activity, "user", user);
					
					if(usrId != null && !usrId.equals("")){
						UserProfile up = new UserProfile();
						up.createTime = String.valueOf(System.currentTimeMillis() / 1000);
						up.userId = usrId;
						up.nickName = usrNick;
						registered = true;
						Bundle bundle = createArguments(null, null);
						bundle.putSerializable("profile", up);
						bundle.putInt(ARG_COMMON_REQ_CODE, requestCode);
						pushFragment(new ProfileEditFragment(), bundle);
					}else{
						finishFragment(requestCode, null);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			break;
		case 2:
			Toast.makeText(activity, "注册未成功，请稍后重试！", 3).show();
			break;
		}
	}

	@Override
	public void onResume(){
		super.onResume();
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
