package com.quanleimu.view.fragment;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.quanleimu.activity.BaseFragment;
import com.quanleimu.activity.BaseFragment.TabDef;
import com.quanleimu.activity.BaseFragment.TitleDef;
import com.quanleimu.activity.R;


public class ForgetPassFragment extends BaseFragment {
	public void initTitle(TitleDef title){
		title.m_visible = true;
		title.m_title = "忘记密码";
		title.m_leftActionHint = "返回";
	}
	public void initTab(TabDef tab){
		tab.m_visible = false;
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootV = inflater.inflate(R.layout.forget_password, null);		
	
		
		WebView web = (WebView) rootV.findViewById(R.id.web);
		web.setWebViewClient(new ForgetPasswordWebClient());
		web.getSettings().setJavaScriptEnabled(true);
		web.requestFocus();
	
		return rootV;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		showProgress(R.string.dialog_title_info, R.string.dialog_message_data_loading, false);
		
		WebView web = (WebView) getView().findViewById(R.id.web);
		web.loadUrl("http://www.baixing.com/auth/password/?redirect=http%3A%2F%2Fshanghai.baixing.com%2F");
	}
	public void doFinish(){	
		View rootV = getView();
		if (rootV != null)
		{
			WebView web = (WebView) rootV.findViewById(R.id.web);
			web.setWebViewClient(null);
			web.stopLoading();
			
		}
	}
	
	public class ForgetPasswordWebClient extends WebViewClient{
		private boolean resetted = false;
		ForgetPasswordWebClient(){
		}
		
		@Override
		public void onPageStarted (WebView view, String url, Bitmap favicon){
			if(url != null && url.startsWith("http://www.baixing.com/auth/password/reset/")){
				resetted = true;
			}
			
			if(url != null && resetted && !url.startsWith("http://www.baixing.com/auth/password/reset/")){
				View rootV = getView();
				if(rootV != null){
					doFinish(); //Detach webview
					finishFragment(1, null);
				}
			}
		}
		
		@Override
		public void onPageFinished(WebView view, String url){
			onDataLoaded();
		}
	}
	
	public void onDataLoaded(){
		hideProgress();
	}
	
}
