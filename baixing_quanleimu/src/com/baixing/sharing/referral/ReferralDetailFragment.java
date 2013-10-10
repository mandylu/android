// lumengdi@baixing.net
package com.baixing.sharing.referral;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;

import com.baixing.activity.BaseFragment;
import com.baixing.data.GlobalDataManager;
import com.quanleimu.activity.R;

public class ReferralDetailFragment extends BaseFragment {
	
	public static final String SHARE_INTRO_URL  = "http://192.168.5.95/appRecords.php";
	public static final String SHARE_DETIAL_URL = "http://www.chenliangyu.baixing.com/pages/promo/appRecords.php";
	public static final String PROMO_DETIAL_URL = "http://www.chenliangyu.baixing.com/pages/promo/PromoterRecords.php";
	
	 private static Context context;
	 private String mTitle;
	 private String mUrl;
	 
	 private WebView web;
	 private View referraldetail;
	 private Button btnRefresh;

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
		referraldetail = inflater.inflate(R.layout.referral_detail, null);
		web = (WebView)referraldetail.findViewById(R.id.web_referral_detail);
		btnRefresh = (Button)referraldetail.findViewById(R.id.refresh_button);
		btnRefresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new loadUrlTask().execute(mUrl);
			}
		});
		btnRefresh.setVisibility(View.GONE);
		new loadUrlTask().execute(mUrl);
		return referraldetail;
	}
	
	class loadUrlTask extends AsyncTask<String, Integer, Integer> {

		@Override
		protected Integer doInBackground(String... urls) {
			HttpGet httpRequest = new HttpGet(urls[0]);
			HttpClient httpclient = new DefaultHttpClient();
			try {
				HttpResponse response = httpclient.execute(httpRequest);
				return response.getStatusLine().getStatusCode();
			} catch (ClientProtocolException e) {
				return -1;
			} catch (IOException e) {
				return -2;
			}
		}

		@Override
		protected void onPostExecute(Integer statusCode) {
			if (statusCode != HttpStatus.SC_OK) {
				btnRefresh.setVisibility(View.VISIBLE);
				((ProgressBar)referraldetail.findViewById(R.id.circleProgressBar)).setVisibility(View.GONE);
			} else {
				btnRefresh.setVisibility(View.GONE);
				web.getSettings().setJavaScriptEnabled(true); 
		        web.loadUrl(mUrl);
		        web.setWebViewClient(new WebViewClient() {
					@Override
		        	public void onPageFinished(WebView view, String url) {
						super.onPageFinished(view, url);
		        		((ProgressBar)referraldetail.findViewById(R.id.circleProgressBar)).setVisibility(View.GONE);
		        	}
		        });
			}
		}
	}
}