package com.quanleimu.activity;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

public class AboutUs extends BaseActivity {

	private Button backBtn; 
	private WebView web;
	public TextView tvTitle;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.aboutus);
		super.onCreate(savedInstanceState);
		// 解决自动弹出输入法
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText("关于我们");
		
		web = (WebView) findViewById(R.id.web);
		web.loadUrl("http://shanghai.baixing.com/iphone/about/v1/?app=baixing");
		
		
		backBtn = (Button)findViewById(R.id.backBtn);
		backBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				AboutUs.this.finish();
			}
		});
	}
}
