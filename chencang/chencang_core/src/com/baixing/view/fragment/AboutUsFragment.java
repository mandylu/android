package com.baixing.view.fragment;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baixing.activity.BaseFragment;
import com.baixing.data.GlobalDataManager;
import com.chencang.core.R;

public class AboutUsFragment extends BaseFragment {
	
	public TextView tvTitle, rlVersion;
	
	@Override
	public void initTitle(TitleDef title){
		title.m_visible = true;
		title.m_title = "关于我们";
		
		title.m_leftActionHint = "返回";
	}
	
	@Override
	public View onInitializeView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		RelativeLayout relAbout = (RelativeLayout)inflater.inflate(R.layout.aboutus, null);
		
//		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		rlVersion = (TextView) relAbout.findViewById(R.id.rlVersion);
		try {
			PackageManager packageManager = GlobalDataManager.getInstance().getApplicationContext().getPackageManager();
			ApplicationInfo ai = packageManager.getApplicationInfo(getActivity().getPackageName(), PackageManager.GET_META_DATA);
			String pt = (String)ai.metaData.get("publishTime");
			rlVersion.setText("版本信息：v" + this.getVersionName() + (pt == null ? "" : (" " + pt)));
		} catch (Exception ex) {
			rlVersion.setText("版本信息：v1.01 ");
		}
		
		return relAbout;
	
	}	
	
	
	private String getVersionName() throws Exception {
		// 获取PackageManager的实例
		PackageManager packageManager = GlobalDataManager.getInstance().getApplicationContext()
				.getPackageManager();
		// getPackageName()是你当前类的包名，0代表是获取版本信息
		PackageInfo packInfo = packageManager.getPackageInfo(
				GlobalDataManager.getInstance().getApplicationContext().getPackageName(), 0);
		String version = packInfo.versionName;
		return version;
	}
	
	public boolean hasGlobalTab()
	{
		return false;
	}
}
