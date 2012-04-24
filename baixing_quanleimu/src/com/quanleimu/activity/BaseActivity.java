package com.quanleimu.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.baidu.mapapi.MapActivity;
import com.mobclick.android.MobclickAgent;
import com.yx.imageUtils.LoadImage;

/**
 * 父类Activity
 * @author henry_yang
 *
 */
public class BaseActivity extends MapActivity implements OnClickListener{

	//定义Intent和Bundle
	protected Intent intent = null;
	protected Bundle bundle = null;
	protected MyApplication myApp; 
	protected ImageView ivHomePage,ivCateMain,ivPostGoods,ivMyCenter,ivSetMain;
	protected View v = null; 
	protected ProgressDialog pd;
	public LoadImage LoadImage;
	
	//防止滑盖手机滑盖刷新
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	} 
	 
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onResume(this);
	}



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
//		v =findViewById(R.id.linearBottom);
		MobclickAgent.onError(this);
		myApp = (MyApplication) getApplication();
		LoadImage = new LoadImage();
		//判断Intent和Bundle
		intent = getIntent();
		if(intent == null)
		{
			intent = new Intent();
		}
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		bundle = intent.getExtras();
		if(bundle == null)
		{
			bundle = new Bundle();
		}
		
//		//通过ID获取控件
//		
//		ivHomePage = (ImageView)v.findViewById(R.id.ivHomePage);
//		ivCateMain = (ImageView)v.findViewById(R.id.ivCateMain);
//		ivPostGoods = (ImageView)v.findViewById(R.id.ivPostGoods);
//		ivMyCenter = (ImageView)v.findViewById(R.id.ivMyCenter);
//		ivSetMain = (ImageView)v.findViewById(R.id.ivSetMain);
//		
//		//设置监听器
//		ivHomePage.setOnClickListener(this);
//		ivCateMain.setOnClickListener(this);
//		ivPostGoods.setOnClickListener(this);
//		ivMyCenter.setOnClickListener(this);
//		ivSetMain.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId())
		{
//			case R.id.ivHomePage:
//				if(!myApp.getActivity_type().equals("homepage"))
//				{
//					intent.setClass(this, HomePage.class);
//					intent.putExtras(bundle);
//					startActivity(intent);
//					ivHomePage.setImageResource(R.drawable.iv_homepage_press);
//					ivCateMain.setImageResource(R.drawable.iv_cate);
//					ivPostGoods.setImageResource(R.drawable.iv_postgoods);
//					ivMyCenter.setImageResource(R.drawable.iv_mycenter);
//					ivSetMain.setImageResource(R.drawable.iv_setmain);
//				}
//				break;
//			case R.id.ivCateMain:
//				if(!myApp.getActivity_type().equals("catemain"))
//				{
//					intent.setClass(this, CateMain.class);
//					intent.putExtras(bundle);
//					startActivity(intent);
//					ivHomePage.setImageResource(R.drawable.iv_homepage);
//					ivCateMain.setImageResource(R.drawable.iv_cate_press);
//					ivPostGoods.setImageResource(R.drawable.iv_postgoods);
//					ivMyCenter.setImageResource(R.drawable.iv_mycenter);
//					ivSetMain.setImageResource(R.drawable.iv_setmain);
//				}
//				break;
//			case R.id.ivPostGoods:
//				if(!myApp.getActivity_type().equals("postgoods"))
//				{
//					intent.setClass(this, PostGoods.class);
//					intent.putExtras(bundle);
//					startActivity(intent);
//					ivHomePage.setImageResource(R.drawable.iv_homepage);
//					ivCateMain.setImageResource(R.drawable.iv_cate);
//					ivPostGoods.setImageResource(R.drawable.iv_postgoods_press);
//					ivMyCenter.setImageResource(R.drawable.iv_mycenter);
//					ivSetMain.setImageResource(R.drawable.iv_setmain);
//				}
//				break;
//			case R.id.ivMyCenter:
//				if(!myApp.getActivity_type().equals("mycenter"))
//				{
//					intent.setClass(this, MyCenter.class);
//					intent.putExtras(bundle);
//					startActivity(intent);
//					ivHomePage.setImageResource(R.drawable.iv_homepage);
//					ivCateMain.setImageResource(R.drawable.iv_cate);
//					ivPostGoods.setImageResource(R.drawable.iv_postgoods);
//					ivMyCenter.setImageResource(R.drawable.iv_mycenter_press);
//					ivSetMain.setImageResource(R.drawable.iv_setmain);
//				}
//				break;
//			case R.id.ivSetMain:
//				if(!myApp.getActivity_type().equals("setmain"))
//				{
//					intent.setClass(this, SetMain.class);
//					intent.putExtras(bundle);
//					startActivity(intent);
//					ivHomePage.setImageResource(R.drawable.iv_homepage);
//					ivCateMain.setImageResource(R.drawable.iv_cate);
//					ivPostGoods.setImageResource(R.drawable.iv_postgoods);
//					ivMyCenter.setImageResource(R.drawable.iv_mycenter);
//					ivSetMain.setImageResource(R.drawable.iv_setmain_press);
//				}
//				break;
		}
	}
	
	//判断网络是否连接成功
	public boolean JadgeConnection ()throws Exception
	{
		
		boolean a = false;
		ConnectivityManager connManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		State mobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
		
		State wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
		
		if(mobile.toString().equals("CONNECTED") || wifi.toString().equals("CONNECTED"))
		{
			a = true;
		}
		else 
		{
			a = false;
		}
		return a;
	}
	
	//弹出等待框
	public void pdShow(Context context)
	{
		pd = ProgressDialog.show(context, "提示", "请稍候...");
		pd.setCancelable(true);
	}
	
	//取消等待框
	public void pdDismiss(Context context)
	{
		pd.dismiss();
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
//	public static String cn2Spell(String chinese) {
//		StringBuffer pybf = new StringBuffer();
//		char[] arr = chinese.toCharArray();
//		HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
//		defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
//		defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
//		for (int i = 0; i < arr.length; i++) {
//			if (arr[i] > 128) {
//				try {
//					pybf.append(PinyinHelper.toHanyuPinyinStringArray(arr[i],
//							defaultFormat)[0]);
//				} catch (BadHanyuPinyinOutputFormatCombination e) {
//					e.printStackTrace();
//				}
//			} else {
//				pybf.append(arr[i]);
//			}
//		}
//		return pybf.toString();
//	}
	
}
