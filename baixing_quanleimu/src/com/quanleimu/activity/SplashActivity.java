package com.quanleimu.activity;


import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
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
import com.quanleimu.entity.BXLocation;
import com.quanleimu.entity.CityList;
import com.quanleimu.entity.GoodsDetail;
import com.quanleimu.entity.PostMu;
import com.quanleimu.jsonutil.JsonUtil;
import com.quanleimu.util.BXStatsHelper;
import com.quanleimu.util.Communication;
import com.quanleimu.util.ErrorHandler;
import com.quanleimu.util.Helper;
import com.quanleimu.util.NetworkProtocols;
import com.quanleimu.util.LocationService;
import com.quanleimu.util.Util;
import com.quanleimu.view.CategorySelectionView;
public class SplashActivity extends BaseActivity{

	// 定义经纬度
	public double Lat = 0;
	public double Lon = 0;
	public int tag = -1;

	public List<GoodsDetail> listLookHistory = new ArrayList<GoodsDetail>();

	public List<GoodsDetail> listMyStore = new ArrayList<GoodsDetail>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    //try to collect crash log
		//ErrorReporter errReporter = new ErrorReporter();
		//errReporter.Init(this);
		//errReporter.CheckErrorAndSendMail(this);
		//end crash log sending
		
	    NetworkProtocols.getInstance().init(this);
	    
		setContentView(R.layout.main2);
		//LocationService.getInstance().start(this, this);
		super.onCreate(savedInstanceState);

		QuanleimuApplication.udid = QuanleimuApplication.getDeviceUdid(this);
//				Secure.getString(this.getContentResolver(),
//				Secure.ANDROID_ID);

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
		//LocationService.getInstance().removeLocationListener(this);
		super.onPause();
	}
	
	
//	@Override
//	public void onLocationFetched(BXLocation location) {
//		if(null != location){
//			if(location.cityName.length() > 0){
//				for(int i=0;i<myApp.getListCityDetails().size();i++)
//				{
//					if(location.cityName.equals(myApp.getListCityDetails().get(i).getName()) ||
//							location.cityName.contains(myApp.getListCityDetails().get(i).getName())	)
//					{
//						cityName1 = myApp.getListCityDetails().get(i).getEnglishName();
//						myApp.setCityEnglishName(cityName1);
//						LocationService.getInstance().removeLocationListener(this);
//						break;
//					}
//				}
//			}
//		}
//	}
//	
//	public void onLocationUpdated(Location location){
//		BXLocation locationBX  = LocationService.geocodeAddr(Double.toString(location.getLatitude()), Double.toString(location.getLongitude()));
//		
//		if(locationBX.cityName.length() > 0){
//			for(int i=0;i<myApp.getListCityDetails().size();i++)
//			{
//				if(locationBX.cityName.equals(myApp.getListCityDetails().get(i).getName()) ||
//						locationBX.cityName.contains(myApp.getListCityDetails().get(i).getName())	)
//				{
//					cityName1 = myApp.getListCityDetails().get(i).getEnglishName();
//					myApp.setCityEnglishName(cityName1);
//					LocationService.getInstance().removeLocationListener(this);
//					break;
//				}
//			}
//		}
//	
//		//myApp.setCityName(locationBX.cityName);
//		myApp.setLocation(locationBX);
//	}

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
//				if(!QuanleimuApplication.getApplication().getCurrentLocation(SplashActivity.this)){
//					LocationService.getInstance().addLocationListener(SplashActivity.this, SplashActivity.this);
//				}
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
					}
				}
			}

			if(postMu != null){
				long time = postMu.getTime();
				if (time + (7 * 24 * 3600 * 1000) < System.currentTimeMillis()) {
					String apiName = "category_list";
					ArrayList<String> list = new ArrayList<String>();
					String url = Communication.getApiUrl(apiName, list);
					try {
						String json = Communication.getDataByUrl(url, false);
						if (json != null) {
							postMu.setJson(json);
							postMu.setTime(System.currentTimeMillis());
							Util.saveDataToLocate(SplashActivity.this, "saveFirstStepCate", postMu);
							valid = true;
						}
					} catch(Exception e){
						
					}
				}
			}
			if(valid){
				String json = postMu.getJson();
				
				if (json != null && json.length() > 0) {
					AllCates allCates = JsonUtil.getAllCatesFromJson(Communication.decodeUnicode(json));
					
					if (allCates == null) {
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
			
			Object personalMark = Helper.loadDataFromLocate(SplashActivity.this, "personMark");
			if(personalMark != null){
				myApp.setPersonMark((String)personalMark);
			}
			myHandler.sendEmptyMessage(2);
			
			BXStatsHelper.getInstance().load(SplashActivity.this);
			BXStatsHelper.getInstance().send();//Send log each startup.
		}

	}
}
