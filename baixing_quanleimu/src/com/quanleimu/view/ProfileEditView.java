package com.quanleimu.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import java.text.SimpleDateFormat;

import com.quanleimu.activity.QuanleimuApplication;
import com.quanleimu.activity.R;
import com.quanleimu.entity.UserProfile;

public class ProfileEditView extends BaseView {

	private UserProfile up;
	
	public ProfileEditView(Context context, UserProfile up){
		super(context);
		this.up = up;
		init();
	}
	
	private void init(){
		
	}
	
	@Override
	public TitleDef getTitleDef(){
		TitleDef title = new TitleDef();
		title.m_visible = true;
		title.m_title = "编辑个人信息";
		title.m_rightActionHint = "完成";
		title.m_leftActionHint = "返回";
		
		return title;
	}
	
	@Override
	public TabDef getTabDef(){
		TabDef tabDef = new TabDef();
		tabDef.m_visible = true;
		
		return tabDef;
	}	
	
	protected void init(){
	
		LayoutInflater inflator = LayoutInflater.from(context);
		RelativeLayout relAbout = (RelativeLayout)
		this.addView(inflator.inflate(R.layout.aboutus, null););		
		
//		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		rlVersion = (TextView) findViewById(R.id.rlVersion);
		try {
			PackageManager packageManager = QuanleimuApplication.getApplication().getPackageManager();
			ApplicationInfo ai = packageManager.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
			String pt = (String)ai.metaData.get("publishTime");
			rlVersion.setText("版本信息：v" + this.getVersionName() + (pt == null ? "" : (" " + pt)));
		} catch (Exception ex) {
			rlVersion.setText("版本信息：v1.01 ");
		}
	}
	
	private String getVersionName() throws Exception
	   {
	           // 获取PackageManager的实例
	           PackageManager packageManager = QuanleimuApplication.getApplication().getPackageManager();
	           // getPackageName()是你当前类的包名，0代表是获取版本信息
	           PackageInfo packInfo = packageManager.getPackageInfo( QuanleimuApplication.getApplication().getPackageName(),0);
	           String version = packInfo.versionName;
	           return version;
	   }
}
