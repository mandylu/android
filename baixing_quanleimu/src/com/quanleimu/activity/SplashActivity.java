package com.quanleimu.activity;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.List;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Secure;
import android.widget.Toast;

import com.quanleimu.entity.AllCates;
import com.quanleimu.entity.CityList;
import com.quanleimu.entity.GoodsDetail;
import com.quanleimu.entity.PostMu;
import com.quanleimu.jsonutil.JsonUtil;
import com.quanleimu.util.Communication;
import com.quanleimu.util.Helper;
import com.quanleimu.util.NetworkProtocols;
import com.quanleimu.util.LocationService;
import com.quanleimu.util.Util;
import com.quanleimu.util.ErrorReporter;
public class SplashActivity extends BaseActivity implements LocationService.BXLocationServiceListener{

	// 定义经纬度
	public double Lat = 0;
	public double Lon = 0;
	public int tag = -1;

	public List<GoodsDetail> listLookHistory = new ArrayList<GoodsDetail>();

	public List<GoodsDetail> listMyStore = new ArrayList<GoodsDetail>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    //try to collect crash log
		ErrorReporter errReporter = new ErrorReporter();
		errReporter.Init(this);
		errReporter.CheckErrorAndSendMail(this);
		//end crash log sending
		
	    NetworkProtocols.getInstance().init(this);
	    
		setContentView(R.layout.main2);
		//LocationService.getInstance().start(this, this);
		super.onCreate(savedInstanceState);

		QuanleimuApplication.udid = Secure.getString(this.getContentResolver(),
				Secure.ANDROID_ID);

		PackageManager packageManager = getPackageManager();
		// getPackageName()是你当前类的包名，0代表是获取版本信息
		PackageInfo packInfo;
		try {
			packInfo = packageManager.getPackageInfo(getPackageName(), 0);
			QuanleimuApplication.version = packInfo.versionName;
		} catch (NameNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			if (JadgeConnection() == false) {
				Toast.makeText(SplashActivity.this, "网络连接异常", 3).show();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
/*
		myHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				intent.setClass(Loading2.this, HomePage.class);
				// bundle.putString("cityName", cityName);
				intent.putExtras(bundle);
				startActivity(intent);
				Loading2.this.finish();
			}
		}, 3000);*/
		new Thread(new ReadCityListThread()).start();
		new Thread(new ReadInfoThread()).start();
		new Thread(new ReadCateListThread()).start();

	}
	public String cityName1 = "";
//	class CityThread implements Runnable {
//		public void run() {
//
//		}
//	}
	
	public void onPause(){
		LocationService.getInstance().stop();
		super.onPause();
	}
	
	public void onLocationUpdated(Location location){
		String add = LocationService.geocodeAddr(Double.toString(location.getLatitude()), Double.toString(location.getLongitude()));
		if(null == add) return;
		int index = add.indexOf("市");
		String cityName = (-1 == index ? add : add.substring(0, index));
		boolean found = false;
		for(int i=0;i<myApp.getListCityDetails().size();i++)
		{
			if(cityName.equals(myApp.getListCityDetails().get(i).getName()))
			{
				found = true;
				cityName1 = myApp.getListCityDetails().get(i).getEnglishName();
				myApp.setCityEnglishName(cityName1);
				LocationService.getInstance().stop();
				break;
			}
		}
		if(!found){
			for(int i=0;i<myApp.getListCityDetails().size();i++)
			{
				if(cityName.contains(myApp.getListCityDetails().get(i).getName()))
				{
					cityName1 = myApp.getListCityDetails().get(i).getEnglishName();
					myApp.setCityEnglishName(cityName1);
					LocationService.getInstance().stop();
					break;
				}
			}
			
		}
		myApp.setCityName(cityName);
		myApp.setGpsCityName(cityName);
		
	}

	Handler myHandler = new Handler() {
		private int record1 = 0;//flag city list
		private int record2 = 0;//flag history/stored 
		private int record3 = 0;//flag for allcate list
		
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				record1 = 1;
				break;
			case 2:
				record2 = 1;
				break;
			case 3:
				record3 = 1;
				break;
			default:
				break;
			}
			
			if(1 == record1 && 1 == record2 && 1 == record3){
				LocationService.getInstance().start(SplashActivity.this, SplashActivity.this);
				intent.setClass(SplashActivity.this, QuanleimuMainActivity.class);
				// bundle.putString("cityName", cityName);
				intent.putExtras(bundle);
				startActivity(intent);
				SplashActivity.this.finish();
			}
		}
	};

	class ReadCateListThread implements Runnable {

		@Override
		public void run() {
		
			// TODO Auto-generated method stub
			PostMu postMu = (PostMu)Util.loadDataFromLocate(SplashActivity.this, "saveFirstStepCate");
			
			boolean valid = true;
			
			if(null == postMu || postMu.getJson().length() == 0){
				
				ObjectInputStream ois = null;
				InputStream is = null;
				
				try {
					is = getAssets().open("cateJson.txt");
					ois = new ObjectInputStream(is);
//					byte[] b = new byte[is.available()];
//					is.read(b);
//					String content = new String(b);	
					
					postMu = (PostMu)ois.readObject();
					
					//save to context
					if(null != postMu)
						Util.saveDataToLocate(SplashActivity.this, "saveFirstStepCate", postMu);
	
				}catch(ClassNotFoundException e){
					System.out.println("类目列表数据转存不能转换成类目表！");
				}catch (IOException e) {
					// TODO Auto-generated catch block
					valid = false;
					e.printStackTrace();
				}finally{
					try{
						if(null != ois){
							ois.close();
						}
						
						if(null != is){
							is.close();
						}
					}catch(Exception e){
						System.out.println("类目列表数据转存失败！");
					}
				}
			}

			if(valid){
				String json = postMu.getJson();
				
				if (json != null && json.length() > 0) {
					AllCates allCates = JsonUtil.getAllCatesFromJson(Communication.decodeUnicode(json));
					
					if (allCates == null) {
						System.out.println("类目列表数据有误！");
					} else {
						myApp.setListFirst(allCates.getChildren());
					}
				}
			}
			
			myHandler.sendEmptyMessage(3);
		}
	}
	
	class ReadCityListThread implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			CityList cityList = new CityList();
			try {
				InputStream is = getAssets().open("cityjson.txt");
				byte[] b = new byte[is.available()];
				is.read(b);
				String content = new String(b);

				if (content == null || content.equals("")) {
					cityList = null;
				} else {
					cityList = JsonUtil.parseCityListFromJson((content));
					if (cityList == null || cityList.getListDetails() == null
							|| cityList.getListDetails().size() == 0) {
						System.out.println("无城市列表集合");
					} else {
						myApp.setListCityDetails(cityList.getListDetails());
						
						//update current city name
						String chengshiName = (String) Helper.loadDataFromLocate(SplashActivity.this, "cityName");
						if (chengshiName == null || chengshiName.equals("")) {
							tag = 0;
						} else {
							tag = 1;
							myApp.setCityName(chengshiName);
							
							for(int i=0;i<myApp.getListCityDetails().size();i++)
							{
								if(chengshiName.equals(myApp.getListCityDetails().get(i).getName()))
								{
									cityName1 = myApp.getListCityDetails().get(i).getEnglishName();
									myApp.setCityEnglishName(cityName1);
									break;
								}
							}
						}
					}
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			myHandler.sendEmptyMessage(1);
		}
	}

	public List<String> listRemark = new ArrayList<String>();

	class ReadInfoThread implements Runnable {

		@SuppressWarnings("unchecked")
		@Override
		public void run() { 
			// 获取搜索记录
			Object objRemark = Helper.loadDataFromLocate(SplashActivity.this, "listRemark");
			if(objRemark != null)
			{
				listRemark = (List<String>)objRemark;
			}
			else
			{
				listRemark = null;
			}
			myApp.setListRemark(listRemark);

			// 获取我的浏览历史以及我的收藏
			Object objLookHistory = Helper.loadDataFromLocate(SplashActivity.this, "listLookHistory");
			if(objLookHistory != null)
			{
				listLookHistory = (List<GoodsDetail>)objLookHistory;
			}
			else
			{
				listLookHistory = null;
			}
			myApp.setListLookHistory(listLookHistory);

			Object objStore = Helper.loadDataFromLocate(SplashActivity.this, "listMyStore");
			if(objStore != null)
			{
				listMyStore = (List<GoodsDetail>)objStore;
			}
			else
			{
				listMyStore = null;
			}
			myApp.setListMyStore(listMyStore);
			myHandler.sendEmptyMessage(2);
		}

	}
}
