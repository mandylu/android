package com.quanleimu.activity;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings.Secure;
import android.view.View;
import android.widget.Toast;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.LocationListener;
import com.baidu.mapapi.MKAddrInfo;
import com.baidu.mapapi.MKDrivingRouteResult;
import com.baidu.mapapi.MKLocationManager;
import com.baidu.mapapi.MKPoiResult;
import com.baidu.mapapi.MKSearch;
import com.baidu.mapapi.MKSearchListener;
import com.baidu.mapapi.MKTransitRouteResult;
import com.baidu.mapapi.MKWalkingRouteResult;
import com.baidu.mapapi.MapView;
import com.quanleimu.entity.CityList;
import com.quanleimu.entity.GoodsDetail;
import com.quanleimu.jsonutil.JsonUtil;
import com.quanleimu.util.Helper;

public class Loading2 extends BaseActivity {

	private BMapManager bMapManager;// 百度地图
	private MKLocationManager mkLocationManager;// 百度地图管理
	private MKSearch mkSearch;// 百度搜索
	private LocationListener locationListener;// 定位监听
	private GeoPoint gp;

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
		setContentView(R.layout.main2);
		initMap();
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

		myHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				intent.setClass(Loading2.this, HomePage.class);
				// bundle.putString("cityName", cityName);
				intent.putExtras(bundle);
				startActivity(intent);
				Loading2.this.finish();
			}
		}, 3000);
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
		}

	}

	public void initMap() {
		bMapManager = new BMapManager(getApplication());
		bMapManager.init("736C4435847CB7D20DD1131064E35E8941C934F5", null);
		MapView mapView = (MapView) findViewById(R.id.mymap);
		mapView.setVisibility(View.GONE);
		super.initMapActivity(bMapManager);
		mkSearch = new MKSearch();
		mkLocationManager = bMapManager.getLocationManager();
		mkSearch.init(bMapManager, new MKSearchListener() {

			@Override
			public void onGetWalkingRouteResult(MKWalkingRouteResult res,
					int error) {
			}

			@Override
			public void onGetTransitRouteResult(MKTransitRouteResult arg0,
					int arg1) {
			}

			@Override
			public void onGetPoiResult(MKPoiResult arg0, int arg1, int arg2) {
			}

			@Override
			public void onGetDrivingRouteResult(MKDrivingRouteResult arg0,
					int arg1) {
			}

			@Override
			public void onGetAddrResult(MKAddrInfo mkAdd, int error) {
				if (error != 0) {
					String str = String.format("错误号：%d", error);
					// Toast.makeText(Loading2.this, str,
					// Toast.LENGTH_LONG).show();
					return;
				}

				if (mkAdd.strAddr == null || mkAdd.strAddr.equals("")) {
//					Toast.makeText(Loading2.this, "mkAdd.strAddr 为空", 3).show();
					cityName = "上海";
					myApp.setCityName(cityName);
					myApp.setGpsCityName(cityName);
					myApp.setCityEnglishName("shanghai");
				} else {
					// tag == 0 是本地没有城市名保存
//					Toast.makeText(Loading2.this, "mkAdd.strAddr不为空", 3).show();
					if (tag == 0) {
						cityName = mkAdd.strAddr.substring(0,
								mkAdd.strAddr.indexOf("市"));
						
						for(int i=0;i<myApp.getListCityDetails().size();i++)
						{
							if(cityName.equals(myApp.getListCityDetails().get(i).getName()))
							{
								cityName1 = myApp.getListCityDetails().get(i).getEnglishName();
								myApp.setCityEnglishName(cityName1);
								System.out.println("Location cityName1----->" +cityName1);
								break;
							}
						}
						myApp.setCityName(cityName);
						myApp.setGpsCityName(cityName);
					} else if (tag == 1) {
						myApp.setGpsCityName(cityName);
					}
				}
			}
		});

		locationListener = new LocationListener() {

			@Override
			public void onLocationChanged(Location location) {
				if (location != null) {
					location = mkLocationManager.getLocationInfo();
					gp = new GeoPoint((int) (location.getLatitude() * 1e6),
							(int) (location.getLongitude() * 1e6));
					// 搜查我的定位位置
					mkSearch.reverseGeocode(gp);
				}
			}
		};
	}

	@Override
	protected void onPause() {
		// 移除listener
		bMapManager.getLocationManager().removeUpdates(locationListener);
		bMapManager.stop();
		super.onPause();
	}

	@Override
	protected void onResume() {
		// 注册Listener
		bMapManager.getLocationManager().requestLocationUpdates(
				locationListener);
		bMapManager.start();
		super.onResume();
	}

	Handler myHandler = new Handler() {

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
					}
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
			
		}

	}
}
