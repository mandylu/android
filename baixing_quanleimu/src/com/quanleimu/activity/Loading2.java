package com.quanleimu.activity;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import java.util.ArrayList;
import java.util.List;
import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Secure;
import android.view.View;
import android.widget.Toast;

import android.location.*;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import android.content.Context;

import com.quanleimu.entity.CityList;
import com.quanleimu.entity.GoodsDetail;
import com.quanleimu.jsonutil.JsonUtil;
import com.quanleimu.util.Helper;
import com.quanleimu.util.NetworkProtocols;
import com.quanleimu.util.LocationService;

public class Loading2 extends BaseActivity implements LocationService.BXLocationServiceListener{

	private LocationListener listener;
	private LocationListener locationListener;
	private LocationManager locationMgr;

	// 定义经纬度
	public double Lat = 0;
	public double Lon = 0;
	public String cityName = "";
	public String json = "";
	public String content = "";
	public String chengshiName = "";
	public int tag = -1;

	public List<GoodsDetail> listLookHistory = new ArrayList<GoodsDetail>();

	public List<GoodsDetail> listMyStore = new ArrayList<GoodsDetail>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    
	    NetworkProtocols.getInstance().init(this);
	    
		setContentView(R.layout.main2);
		//LocationService.getInstance().start(this, this);
		super.onCreate(savedInstanceState);

		MyApplication.udid = Secure.getString(this.getContentResolver(),
				Secure.ANDROID_ID);

		PackageManager packageManager = getPackageManager();
		// getPackageName()是你当前类的包名，0代表是获取版本信息
		PackageInfo packInfo;
		try {
			packInfo = packageManager.getPackageInfo(getPackageName(), 0);
			MyApplication.version = packInfo.versionName;
		} catch (NameNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			if (JadgeConnection() == false) {
				Toast.makeText(Loading2.this, "网络连接异常", 3).show();
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

	}
	public String cityName1 = "";
	class CityThread implements Runnable {
		public void run() {
			chengshiName = (String) Helper.loadDataFromLocate(Loading2.this,
					"cityName");
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
			myHandler.sendEmptyMessage(1);
		}

	}
	
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
		private int record1 = 0;
		private int record2 = 0;
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				record1 = 1;
				break;
			case 2:
				record2 = 1;
				break;
			default:
				break;
			}
			if(1 == record1 && 1 == record2){
				LocationService.getInstance().start(Loading2.this, Loading2.this);
				intent.setClass(Loading2.this, HomePage.class);
				// bundle.putString("cityName", cityName);
				intent.putExtras(bundle);
				startActivity(intent);
				Loading2.this.finish();
			}
		}
	};

	class ReadCityListThread implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			CityList cityList = new CityList();
			try {
				InputStream is = getAssets().open("cityjson.txt");
				byte[] b = new byte[is.available()];
				is.read(b);
				content = new String(b);

				if (content == null || content.equals("")) {
					cityList = null;
				} else {
					cityList = JsonUtil.parseCityListFromJson((content));
					if (cityList == null || cityList.getListDetails() == null
							|| cityList.getListDetails().size() == 0) {
						System.out.println("无城市列表集合");
					} else {
						myApp.setListCityDetails(cityList.getListDetails());
						new Thread(new CityThread()).start();
						return;
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
			Object objRemark = Helper.loadDataFromLocate(Loading2.this, "listRemark");
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
			Object objLookHistory = Helper.loadDataFromLocate(Loading2.this, "listLookHistory");
			if(objLookHistory != null)
			{
				listLookHistory = (List<GoodsDetail>)objLookHistory;
			}
			else
			{
				listLookHistory = null;
			}
			myApp.setListLookHistory(listLookHistory);

			Object objStore = Helper.loadDataFromLocate(Loading2.this, "listMyStore");
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
