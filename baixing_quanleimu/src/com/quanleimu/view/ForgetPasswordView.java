package com.quanleimu.view;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.quanleimu.activity.R;
import android.graphics.Bitmap;

public class ForgetPasswordView extends BaseView {

	public class ForgetPasswordWebClient extends WebViewClient{
		public ForgetPasswordView mView;
		
		ForgetPasswordWebClient(ForgetPasswordView view){
			mView = view;
		}
		
		@Override
		public void onPageStarted (WebView view, String url, Bitmap favicon){
			if(url != null && url.startsWith("http://www.baixing.com/auth/denglu/?username=")){
				if(mView.m_viewInfoListener != null){
//					Toast.makeText(mView.getContext(), "重置密码成功，请重新登陆", 2);
					mView.m_viewInfoListener.onBack(1, null);
				}
			}
		}
		
		@Override
		public void onPageFinished(WebView view, String url){
			mView.onDataLoaded();
		}
	}
	
	public String backPageName = "";
	public String categoryEnglishName = "";
	private WebView web;
	private ProgressDialog pd;

	public ForgetPasswordView(Context context, Bundle bundle){
		super(context);
		
		Init();
	}
	
	protected void Init(){
		LayoutInflater inflater = LayoutInflater.from(getContext());
		this.addView(inflater.inflate(R.layout.forget_password, null));		
	
		pd = ProgressDialog.show(getContext(), "提示", "数据下载中，请稍后。。。");
		pd.setCancelable(false);
		
		web = (WebView) findViewById(R.id.web);
		web.setWebViewClient(new ForgetPasswordWebClient(this));
		web.loadUrl("http://www.baixing.com/auth/findPassword/");
		web.getSettings().setJavaScriptEnabled(true);
		web.requestFocus();
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
	
	public void onDataLoaded(){
		
		if(null != pd){
			pd.hide();
		}
	}
	
//	@Override
//	public void onAttachedToWindow(){
//		super.onAttachedToWindow();
//		
//        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE); 
//        imm.showSoftInput(this, InputMethodManager.SHOW_FORCED); 
//        imm.toggleSoftInput(0,InputMethodManager.HIDE_NOT_ALWAYS);
//	}
	
//	@Override
//	public boolean onTouchEvent(MotionEvent event){
//		if(event.getAction() == MotionEvent.ACTION_MOVE){
//			bMoved = true;
//		}else if(event.getAction() == MotionEvent.ACTION_UP){
//			if(!bMoved && web.getHitTestResult().getType() == WebView.HitTestResult.EDIT_TEXT_TYPE){
//		        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE); 
//		        imm.showSoftInput(this, InputMethodManager.SHOW_FORCED); 
//		        imm.toggleSoftInput(0,InputMethodManager.HIDE_NOT_ALWAYS);
//			}				
//		}
//		
//		return super.onTouchEvent(event);
//	}

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
//					bundle.putString("popup_message", "登录未成功，请稍后重试！");
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
