package com.quanleimu.view;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.webkit.WebView;
import com.quanleimu.activity.R;

public class ForgetPasswordView extends BaseView {

	public String backPageName = "";
	public String categoryEnglishName = "";
	private WebView web;
	//private ProgressDialog pd;

	public ForgetPasswordView(Context context, Bundle bundle){
		super(context);
		
		Init();
	}
	
	protected void Init(){
		LayoutInflater inflater = LayoutInflater.from(getContext());
		this.addView(inflater.inflate(R.layout.forget_password, null));
		
		web = (WebView) findViewById(R.id.web);
		web.loadUrl("http://www.baixing.com/auth/findPassword/");
		
//		pd = ProgressDialog.show(getContext(), "提示", "数据下载中，请稍后。。。");
//		pd.setCancelable(true);
	}

	public TitleDef getTitleDef(){
		TitleDef title = new TitleDef();
		title.m_visible = true;
		title.m_title = "忘记密码";
		title.m_leftActionHint = "返回";
		return title;
	}
	public TabDef getTabDef(){
		TabDef tab = new TabDef();
		tab.m_visible = false;
		return tab;
	}

//	// {"id":"79703763","error":{"message":"用户登录成功","code":0}}
//	class LoginThread implements Runnable {
//		public void run() {
//			String apiName = "user_login";
//			ArrayList<String> list = new ArrayList<String>();
//
//			String url = Communication.getApiUrl(apiName, list);
//			
//			Message msg = Message.obtain();
//			msg.obj = pd;
//			
//			try {
//		
//				String json = Communication.getDataByUrl(url);
//				if (json != null) {
//					ForgetPasswordView.this.parseResponse(json, msg);
//					return;
//				} else {
//					msg.what = ErrorHandler.ERROR_SERVICE_UNAVAILABLE;
//					Bundle bundle = new Bundle();
//					bundle.putString("popup_message", "登陆未成功，请稍后重试！");
//					msg.setData(new Bundle());
//				}
//			} catch (UnsupportedEncodingException e) {
//				msg.what = ErrorHandler.ERROR_NETWORK_UNAVAILABLE;
//				//e.printStackTrace();
//			} catch (Exception e) {
//				msg.what = ErrorHandler.ERROR_NETWORK_UNAVAILABLE;
//				//e.printStackTrace();
//			}
//			
//			QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg);
//		}
//	}
//	
//	protected void parseResponse(String jsonString, Message msg){
//		try {
//			JSONObject jsonObject = new JSONObject(jsonString);
//			System.out.println("jsonObject--->" + jsonObject);
//			
//			String id = "";
//			try {
//				id = jsonObject.getString("id");
//			} catch (Exception e) {
//				id = "";
//				e.printStackTrace();
//			}
//			
//			JSONObject json = jsonObject.getJSONObject("error");
//			String message = json.getString("message");
//			
//			Bundle bundle = new Bundle();
//			bundle.putString("popup_message", message);
//			msg.setData(bundle);
//			
//			
//		} catch (JSONException e) {
//			msg.what = ErrorHandler.ERROR_COMMON_FAILURE;
//			e.printStackTrace();
//		}	
//		
//		QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg);
//	}
}
