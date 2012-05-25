package com.quanleimu.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import java.text.SimpleDateFormat;

import com.quanleimu.activity.BaseActivity;
import com.quanleimu.activity.MyApplication;
import com.quanleimu.activity.R;
import com.quanleimu.activity.R.id;
import com.quanleimu.activity.R.layout;

public class AboutUs extends BaseView {

	private Button backBtn; 
	
	public TextView tvTitle, rlVersion;
	
	public AboutUs(BaseActivity context){
		super(context);
		
		Init(context);
	}
	
	public AboutUs(BaseActivity context, Bundle bundle){
		super(context, bundle);
		
		Init(context);
	}
	
	@Override
	public TitleDef getTitleDef(){
		TitleDef title = new TitleDef();
		title.m_visible = true;
		title.m_title = "关于我们";
		
		return title;
	}
	
	@Override
	public TabDef getTabDef(){
		TabDef tabDef = new TabDef();
		tabDef.m_visible = false;
		tabDef.m_tabSelected = ETAB_TYPE.ETAB_TYPE_SETTING;
		
		return tabDef;
	}	
	
	protected void Init(BaseActivity context) {
		context.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		LayoutInflater inflator = LayoutInflater.from(context);
		
		RelativeLayout relAbout = (RelativeLayout)inflator.inflate(R.layout.aboutus, null);
		this.addView(relAbout);		
		
		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		rlVersion = (TextView) findViewById(R.id.rlVersion);
		try {
			rlVersion.setText("版本信息：v" + this.getVersionName() + " " + sDateFormat.format(new java.util.Date()));
		} catch (Exception ex) {
			rlVersion.setText("版本信息：v1.01 " + sDateFormat.format(new java.util.Date()));
		}
	}
	
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		setContentView(R.layout.aboutus);
//		super.onCreate(savedInstanceState);
//		// 解决自动弹出输入法
//		getWindow().setSoftInputMode(
//				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
//
//		tvTitle = (TextView) findViewById(R.id.tvTitle);
//		tvTitle.setText("关于我们");
//		
//		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
//		rlVersion = (TextView) findViewById(R.id.rlVersion);
//		try {
//			rlVersion.setText("版本信息：v" + this.getVersionName() + " " + sDateFormat.format(new java.util.Date()));
//		} catch (Exception ex) {
//			rlVersion.setText("版本信息：v1.01 " + sDateFormat.format(new java.util.Date()));
//		}
//		
//		
//		backBtn = (Button)findViewById(R.id.backBtn);
//		backBtn.setOnClickListener(new View.OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				AboutUs.this.finish();
//			}
//		});
//	}
	
	private String getVersionName() throws Exception
	   {
	           // 获取PackageManager的实例
	           PackageManager packageManager = MyApplication.getApplication().getPackageManager();
	           // getPackageName()是你当前类的包名，0代表是获取版本信息
	           PackageInfo packInfo = packageManager.getPackageInfo( MyApplication.getApplication().getPackageName(),0);
	           String version = packInfo.versionName;
	           return version;
	   }
}
