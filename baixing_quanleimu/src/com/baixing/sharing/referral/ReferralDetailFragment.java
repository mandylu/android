// lumengdi@baixing.net
package com.baixing.sharing.referral;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.baixing.activity.BaseFragment;
import com.baixing.data.GlobalDataManager;
import com.quanleimu.activity.R;

public class ReferralDetailFragment extends BaseFragment {
	 private static Context context;
	 private String mTitle;
	 private String mUrl;

	    @Override
	    public void onCreate(Bundle savedInstanceState){
	    	super.onCreate(savedInstanceState);
	    	context = GlobalDataManager.getInstance().getApplicationContext();
	    	Bundle bundle=getArguments();
	    	mTitle=bundle.getString("title");
	    	mUrl=bundle.getString("url");
	    	Log.v("bundle", (mTitle==null?"null":mTitle));
	    	Log.v("bundle", (mUrl==null?"null":mUrl));
	    }

	    @Override
	    public void initTitle(TitleDef title) {
	        title.m_visible = true;
	        title.m_title = mTitle;
	        title.m_leftActionHint = "完成";
	    }

	    @Override
	    public boolean hasGlobalTab() {
			return true;
		}

	@Override
	protected View onInitializeView(LayoutInflater inflater,
			ViewGroup container, Bundle savedInstanceState) {
		final View referraldetail = inflater.inflate(R.layout.referral_detail, null);

		WebView web=(WebView)referraldetail.findViewById(R.id.web_referral_detail);
		web.getSettings().setJavaScriptEnabled(true); 
        web.loadUrl(mUrl);
        web.setWebViewClient(new WebViewClient() {
        	@Override
        	public void onPageFinished(WebView view, String url) {
        		super.onPageFinished(view, url);
        		((ProgressBar)referraldetail.findViewById(R.id.circleProgressBar)).setVisibility(View.GONE);
        		
        	}
        });
		return referraldetail;
	}

}