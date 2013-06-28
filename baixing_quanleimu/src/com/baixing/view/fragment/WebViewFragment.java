package com.baixing.view.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import com.baixing.activity.BaseFragment;
import com.quanleimu.activity.R;

public class WebViewFragment extends BaseFragment {

	@Override
	public void initTitle(TitleDef title) {
		title.m_visible = true;
		title.m_leftActionHint = "返回";
		title.m_title = getArguments().getString("title");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onInitializeView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		RelativeLayout relWebView = (RelativeLayout) inflater.inflate(R.layout.webview, null);
		return relWebView;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState){
		super.onViewCreated(view, savedInstanceState);
		if(getArguments() != null && getArguments().containsKey("url")){
			WebView wv = (WebView)getView().findViewById(R.id.webview);
			wv.setWebViewClient(new WebViewClient(){
				public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
					view.loadUrl(url);
	                return true;   
	            }
			});
			
			wv.setWebChromeClient(new WebChromeClient(){
	        	public void onProgressChanged(WebView view,int progress){
	        		
	             	if(progress==100){
	            		getTitleDef().m_title = view.getTitle();
	            		refreshHeader();
	            		handler.sendEmptyMessage(1);
	            	}   
	                super.onProgressChanged(view, progress);   
	            }   
	        });
			
			wv.loadUrl(getArguments().getString("url"));
			
	    	pd=new ProgressDialog(this.getAppContext());
	        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	        pd.setMessage("数据载入中，请稍候！");
	        pd.show();
		}
		
	}
	
	ProgressDialog pd;
	Handler handler = new Handler(){
    	public void handleMessage(Message msg){
	        if (!Thread.currentThread().isInterrupted()){
		        switch (msg.what)
		        {
		        case 0:
		        	pd.show();//显示进度对话框        	
		        	break;
		        case 1:
		        	pd.hide();//隐藏进度对话框，不可使用dismiss()、cancel(),否则再次调用show()时，显示的对话框小圆圈不会动。
		        	break;
		        }
	        }
	        super.handleMessage(msg);
    	}
	};
	
	@Override
	public void onResume(){
		super.onResume();

	}
}
