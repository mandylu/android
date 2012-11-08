package com.quanleimu.activity;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

//import net.sourceforge.simcpux.Constants;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.baidu.mapapi.MKEvent;
import com.baidu.mapapi.MKGeneralListener;
import com.quanleimu.entity.BXLocation;
import com.quanleimu.entity.CityDetail;
import com.quanleimu.entity.Filterss;
import com.quanleimu.entity.FirstStepCate;
import com.quanleimu.entity.GoodsDetail;
import com.quanleimu.entity.HotList;
import com.quanleimu.entity.SecondStepCate;
import com.quanleimu.imageCache.LazyImageLoader;
import com.quanleimu.util.ErrorHandler;
import com.quanleimu.util.LocationService;
import com.quanleimu.util.Util;
import com.quanleimu.util.BXDatabaseHelper;
import android.util.Log;
import android.telephony.TelephonyManager;
public class QuanleimuApplication implements LocationService.BXLocationServiceListener{
	public static final String kWBBaixingAppKey = "3747392969";
	public static final String kWBBaixingAppSecret = "ff394d0df1cfc41c7d89ce934b5aa8fc";
	public static String udid="";
	public static String version="";
	public static String channelId;
	public static WeakReference<Context> context;	
	private static LazyImageLoader lazyImageLoader;
	public static boolean update = false;
	public static boolean textMode = false;
	private static SharedPreferences preferences = null;
	private static LinkedHashMap<String, String> cacheNetworkRequest = null;
	private static BXDatabaseHelper dbManager = null;

    //为赌约而设
    public static int postEntryFlag = -1;
	
	protected static final String PREFS_FILE = "device_id.xml";
    protected static final String PREFS_DEVICE_ID = "device_id";
    
    public static final LazyImageLoader getImageLoader()
    {
    	if (lazyImageLoader == null)
    	{
    		lazyImageLoader = new LazyImageLoader();
    	}
    	
    	return lazyImageLoader;
    }

	
	public static String getCacheNetworkRequest(String request){
		synchronized(dbManager){
			String response = null;
			SQLiteDatabase db = null;
			try{
				db = dbManager.getReadableDatabase();
				
				Cursor c = db.rawQuery("SELECT * from " + BXDatabaseHelper.TABLENAME + " WHERE url=?", new String[]{request});
				
				while(c.moveToNext()){
					int index = c.getColumnIndex("response");
					if(index >= 0){
						response = c.getString(index);
						break;
					}
				}
				c.close();
			}catch(SQLException e){
				e.printStackTrace();
			}catch(Throwable e){
				e.printStackTrace();
			}
			if(db != null){
				db.close();
			}
			return response;
		}
		
	}
	
	public static void deleteOldRecorders(int intervalInSec){
		SQLiteDatabase db = null;
		try{
			db = dbManager.getWritableDatabase();
			db.execSQL("DELETE from " + BXDatabaseHelper.TABLENAME + " WHERE timestamp<?", new String[]{String.valueOf(System.currentTimeMillis()/1000 - intervalInSec)});
			
		}catch(SQLException e){
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}
		if(db != null){
			db.close();
		}
	}

	public static void putCacheNetworkRequest(String request, String result){
		synchronized(dbManager){
			SQLiteDatabase db = null; 
			try{
				db = dbManager.getWritableDatabase();
				String timestamp = String.valueOf(System.currentTimeMillis()/1000);
				db.execSQL("insert into " + BXDatabaseHelper.TABLENAME + "(url, response, timestamp) values(?,?,?)", new String[]{request, result, timestamp});
			}catch(SQLException e){
				e.printStackTrace();
			}
			if(db != null){
				db.close();
			}
		}
	}
	
	public static void setTextMode(boolean tMode){
		QuanleimuApplication.textMode = tMode;
		
		if(null == preferences){
			preferences = context.get() != null ? 
					context.get().getApplicationContext().getSharedPreferences("QuanleimuPreferences", Context.MODE_PRIVATE)
					: null;
		} 
		
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean("isTextMode", tMode);
		editor.commit();
	}
	
	public static boolean isTextMode(){
		return QuanleimuApplication.textMode;
	}
	
	public static List<SecondStepCate> listUsualCates;
	
//	public List<Bitmap> listBigBm = new ArrayList<Bitmap>();
//	
//	public List<Bitmap> getListBigBm() {
//		return listBigBm;
//	}
//	public void setListBigBm(List<Bitmap> listBigBm) {
//		this.listBigBm = listBigBm;
//	}
	
//	static public void setWeiboAccessToken(AccessToken token){
//		accessToken = token;
//	}
//	
//	static public AccessToken getWeiboAccessToken(){
//		return accessToken;
//	}



	//浏览历史
	public List<GoodsDetail> listLookHistory = new ArrayList<GoodsDetail>();
	public List<GoodsDetail> getListLookHistory() {
		return listLookHistory;
	}

	public void setListLookHistory(List<GoodsDetail> listLookHistory) {
		if(listLookHistory != null && listLookHistory.size() > 50){
			this.listLookHistory = new ArrayList<GoodsDetail>(listLookHistory.subList(0, 50));
		}else{
			this.listLookHistory = listLookHistory;
		}
	}

	public List<GoodsDetail> getListMyStore() {
		return listMyStore;
	}

	public void setListMyStore(List<GoodsDetail> listMyStore) {
		this.listMyStore = listMyStore;
	}

	//我的发布信息
	public List<GoodsDetail> listMyPost = null;
	
	public List<GoodsDetail> getListMyPost() {
		return listMyPost;
	}
	
	public boolean isMyAd(String adId) {
		if(null != listMyPost && null != adId){
			for(int i = 0; i < listMyPost.size(); ++ i){
				if(listMyPost.get(i).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID)
						.equals(adId)){
					return true;
				}
			}
		}
		return false;
	}

	public void setListMyPost(List<GoodsDetail> listMyPost) {
		this.listMyPost = listMyPost;
	}

	//我的收藏数据集合
	public List<GoodsDetail> listMyStore = new ArrayList<GoodsDetail>();
	
//	//搜索结果集合总数
//	public int searchCount = -1;
//	
//	public int getSearchCount() {
//		return searchCount;
//	}
//
//	public void setSearchCount(int searchCount) {
//		this.searchCount = searchCount;
//	}

//	//搜索结果集合
//	public List<GoodsDetail> listSearchGoods = new ArrayList<GoodsDetail>();
//	
//	public List<GoodsDetail> getListSearchGoods() {
//		return listSearchGoods;
//	}
//
//	public void setListSearchGoods(List<GoodsDetail> listSearchGoods) {
//		this.listSearchGoods = listSearchGoods;
//	}

	//搜索记录
	private List<String> listRemark = new ArrayList<String>();
	
	public List<String> getListRemark() {
		return listRemark;
	}

	public void setListRemark(List<String> listRemark) {
		this.listRemark = listRemark;
	}

	//登录以后的手机号码保存
	public String mobile = "";

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	//签名档
	public String personMark = "";
	
	public String getPersonMark() {
		return personMark;
	}

	public void setPersonMark(String personMark) {
		this.personMark = personMark;
	}

	//热门城市列表
	public List<CityDetail> listHotCity = new ArrayList<CityDetail>();
	
	public List<CityDetail> getListHotCity() {
		return listHotCity;
	}

	public void setListHotCity(List<CityDetail> listHotCity) {
		this.listHotCity = listHotCity;
	}

	// 定义省份和对应的城市集合
	public HashMap<String, List<CityDetail>> shengMap = new HashMap<String, List<CityDetail>>();

	public HashMap<String, List<CityDetail>> getShengMap() {
		return shengMap;
	}

	public void setShengMap(HashMap<String, List<CityDetail>> shengMap) {
		this.shengMap = shengMap;
	}

	// 定义城市列表集合
	public List<CityDetail> listCityDetails = new ArrayList<CityDetail>();

	public List<CityDetail> getListCityDetails() {
		return listCityDetails;
	}

	public void setListCityDetails(List<CityDetail> listCityDetails) {
		this.listCityDetails = listCityDetails;
	}

	// 添加Activity类型到全局变量
	//public String activity_type = "";
	// 添加图片流文件名
	//public List<String> listFileNames = new ArrayList<String>();
	// 所有类目的集合
	public List<FirstStepCate> listFirst = new ArrayList<FirstStepCate>();
//	// 二级类目集合
//	public List<SecondStepCate> listSecond = new ArrayList<SecondStepCate>();
	// 数据集合
	//public List<GoodsDetail> listGoods = new ArrayList<GoodsDetail>();

	// 筛选木板中的类型集合
	public List<Filterss> listFilterss = new ArrayList<Filterss>();

	// 城市英文名
	public String cityEnglishName = "";

	public String getCityEnglishName() {
		return cityEnglishName;
	}

	public void setCityEnglishName(String cityEnglishName) {
		this.cityEnglishName = cityEnglishName;
	}

//	// 筛选条件
//	public Map<Integer, String> savemap = new HashMap<Integer, String>();
//
//	public Map<Integer, String> getSavemap() {
//		return savemap;
//	}
//
//	public void setSavemap(Map<Integer, String> savemap) {
//		this.savemap = savemap;
//	}

//	public List<Filterss> getListFilterss() {
//		return listFilterss;
//	}

//	public void setListFilterss(List<Filterss> listFilterss) {
//		this.listFilterss = listFilterss;
//	}

//	public List<GoodsDetail> getListGoods() {
//		return listGoods;
//	}
//
//	public void setListGoods(List<GoodsDetail> listGoods) {
//		this.listGoods = listGoods;
//	}

//	public List<SecondStepCate> getListSecond() {
//		return listSecond;
//	}
//
//	public void setListSecond(List<SecondStepCate> listSecond) {
//		this.listSecond = listSecond;
//	}

	public List<FirstStepCate> getListFirst() {
		return listFirst;
	}
	
	public String queryCategoryDisplayName(String englishName){
		//this.getListFirst()
		
		for(int i = 0;i<this.listFirst.size();i++){
			FirstStepCate cate = this.listFirst.get(i);
			if(cate.englishName.equals(englishName)){
				return cate.name;
			}
			
			for(int j = 0; j< cate.children.size(); j++){
				SecondStepCate s = cate.children.get(j);
				if(s.englishName.equals(englishName)){
					return s.name;
				}
			}
		}
		return englishName;
	}

	public void setListFirst(List<FirstStepCate> listFirst) {
		this.listFirst = listFirst;
	}

//	public List<String> getListFileNames() {
//		return listFileNames;
//	}
//
//	public void setListFileNames(List<String> listFileNames) {
//		this.listFileNames = listFileNames;
//	}

	BXLocation location = null;
	boolean location_updated = false;

	public BXLocation getCurrentPosition(boolean bRealLocality) {
		if(context.get() == null) return null;
		if(null == location){
			location = (BXLocation)Util.loadDataFromLocate(context.get(), "location_data");
			
			if(null == location){
				location = new BXLocation(true);
			}else if(null == location.cityName || 0 == location.cityName.length()){
				location.geocoded = false;
			}
		}
		
		return location_updated ? 	location : 
									bRealLocality ? null : location;
	}
	
	public interface onLocationFetchedListener{
		public void onLocationFetched(BXLocation location);//null==location means location-fetching failed
		public void onGeocodedLocationFetched(BXLocation location);
	}
	
	private List<onLocationFetchedListener> locationFetchListeners = new ArrayList<onLocationFetchedListener>();
	public boolean addLocationListener(final onLocationFetchedListener listener){
		if(context.get() == null) return false;
		if(null == listener || locationFetchListeners.contains(listener))
			return false;
		
		final BXLocation curLocation = getCurrentPosition(true);
		if(null == curLocation){
			//listener.onLocationFetched(null);
			locationFetchListeners.add(listener);
			
			LocationService.getInstance().addLocationListener(context.get(), this);
			
			return false;
		}		

		listener.onLocationFetched(curLocation);
		
		if (curLocation.geocoded)
			listener.onGeocodedLocationFetched(curLocation);
		
		return true;
	}
	
	public boolean removeLocationListener(onLocationFetchedListener listener)
	{
		return this.locationFetchListeners.remove(listener);
	}

	public void setLocation(BXLocation location_) {
		if(context.get() == null) return;
		if(null == location)
			getCurrentPosition(false);
		
		if(null != location_){
			if(location_.geocoded){
				location = location_;
			}else{
				location.fLat = location_.fLat;
				location.fLon = location_.fLon;
				
				if(location.geocoded == true){
					float results[] = {0, 0, 0};
					Location.distanceBetween(location.fGeoCodedLat, location.fGeoCodedLon, location_.fLat, location_.fLon, results);
					if(results[0] > 50){
						location.geocoded = false;
						//Log.d("kkkkkk", "location geocoding has been invalidated, since location distance is: "+results[0]+">50m");
					}
				}
			}
			
			Util.saveDataToLocate(context.get(), "location_data", location);

			location_updated = true;
		}
	}

	public String cityName = "";
	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

//	// 经纬度添加到全局变量
//	public double lat = 0;
//
//	public double getLat() {
//		return lat;
//	}
//
//	public void setLat(double lat) {
//		this.lat = lat;
//	}
//
//	public double getLon() {
//		return lon;
//	}
//
//	public void setLon(double lon) {
//		this.lon = lon;
//	}
//
//	public double lon = 0;

//	public String getActivity_type() {
//		return activity_type;
//	}
//
//	public void setActivity_type(String activity_type) {
//		this.activity_type = activity_type;
//	}

	static public QuanleimuApplication getApplication(){
		if(null == preferences){
			preferences = context.get().getApplicationContext().getSharedPreferences("QuanleimuPreferences", Context.MODE_PRIVATE);
			textMode = preferences.getBoolean("isTextMode", false);
		}
		if(mDemoApp == null){
			mDemoApp = new QuanleimuApplication();
			if(context != null && context.get() != null){
				dbManager = new BXDatabaseHelper(context.get(), "network.db", null, 1);
				try{
					PackageManager packageManager = QuanleimuApplication.getApplication().getApplicationContext().getPackageManager();
					ApplicationInfo ai = packageManager.getApplicationInfo(context.get().getPackageName(), PackageManager.GET_META_DATA);
					channelId = (String)ai.metaData.get("UMENG_CHANNEL");
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		return mDemoApp;
	}
	
	public QuanleimuApplication(){
		
	}
	static QuanleimuApplication mDemoApp = null;

	
//	protected ViewStack viewStack;
//	public ViewStack getViewStack(){
//		if(null == viewStack){
//			viewStack = new ViewStack(this.getApplicationContext());
//		}
//		
//		return viewStack;
//	}
	
	protected ErrorHandler handler;
	public void setErrorHandler(Context context){
		handler = new ErrorHandler(context);
	}
	public ErrorHandler getErrorHandler(){
		return handler;
	}
	
	
	public void ClearCache(){
		SQLiteDatabase db = dbManager.getWritableDatabase();
		try{
			db.execSQL("DELETE from " + BXDatabaseHelper.TABLENAME, new String[]{});
			
		}catch(SQLException e){
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}
		db.close();		
	}
	
	
	public Context getApplicationContext(){
		return (QuanleimuApplication.context == null || QuanleimuApplication.context.get() == null) ? 
				null : QuanleimuApplication.context.get();
	}

	// 百度MapAPI的管理类
	//BMapManager mBMapMan = null;

	// 授权Key
	// TODO: 请输入您的Key,
	// 申请地址：http://dev.baidu.com/wiki/static/imap/key/
	//713E99B1CD54866996162791BA789A0D9A13791B	
	public String mStrKey = "736C4435847CB7D20DD1131064E35E8941C934F5";
	boolean m_bKeyRight = true; // 授权Key正确，验证通过

	// 常用事件监听，用来处理通常的网络错误，授权验证错误等
	public static class MyGeneralListener implements MKGeneralListener {
		@Override
		public void onGetNetworkState(int iError) {
			Toast.makeText(QuanleimuApplication.getApplication().getApplicationContext(),
					"您的网络出错啦！", Toast.LENGTH_LONG).show();
		}

		@Override
		public void onGetPermissionState(int iError) {
			if (iError == MKEvent.ERROR_PERMISSION_DENIED) {
				// 授权Key错误：
				Toast.makeText(QuanleimuApplication.mDemoApp.getApplicationContext(),
						"请在BMapApiDemoApp.java文件输入正确的授权Key！", Toast.LENGTH_LONG)
						.show();
				QuanleimuApplication.mDemoApp.m_bKeyRight = false;
			}
		}

	}
//	static public com.tencent.mm.sdk.openapi.IWXAPI wxapi;

//	@Override
//	public void onCreate() {
////		Profiler.markStart("appcreate");
//		mDemoApp = this;
//		
////		mBMapMan = new BMapManager(this);
////		mBMapMan.init(this.mStrKey, new MyGeneralListener());
//
//		context = this.getApplicationContext();
////		ProfileUtil.markStart("imgLoaderCreate");
////		lazyImageLoader = new LazyImageLoader();
////		ProfileUtil.markEnd("imgLoaderCreate");
//		
//		dbManager = new BXDatabaseHelper(this, "network.db", null, 1);
//		
////		LocationService.getInstance().start(context, this);
//
//		try{
//			PackageManager packageManager = QuanleimuApplication.getApplication().getPackageManager();
//			ApplicationInfo ai = packageManager.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
//			channelId = (String)ai.metaData.get("UMENG_CHANNEL");
//		}catch(Exception e){
//			e.printStackTrace();
//		}		
//		
//		super.onCreate();
////		Profiler.markEnd("appcreate");
//	}
	
//	static public void sendWXRequest(WXMediaMessage msg){
//		SendMessageToWX.Req req = new SendMessageToWX.Req();
//		req.transaction = String.valueOf(System.currentTimeMillis());
//		req.message = msg;
////		boolean b = wxapi.sendReq(req);
////		b = false;
//	}

//	@Override
	// 建议在您app的退出之前调用mapadpi的destroy()函数，避免重复初始化带来的时间消耗
//	public void onTerminate() {
//		LocationService.getInstance().stop();
//		
////		if (mBMapMan != null) {
////			mBMapMan.destroy();
////			mBMapMan = null;
////		}
//		super.onTerminate();
//	}

	@Override
	public void onLocationUpdated(Location location_) {
		BXLocation newLocation = new BXLocation(false);
		newLocation.fLat = (float)location_.getLatitude();
		newLocation.fLon = (float)location_.getLongitude();
		
		setLocation(newLocation);
		
		LocationService.getInstance().reverseGeocode(newLocation.fLat, newLocation.fLon, new LocationService.BXRgcListener() {
			
			@Override
			public void onRgcUpdated(BXLocation location) {
				if (null != location) {
					setLocation(location);
				}
				for (onLocationFetchedListener listener : locationFetchListeners)
				{
					listener.onGeocodedLocationFetched(location);
				}					
			}
		});
		
		for (onLocationFetchedListener listener : locationFetchListeners)
		{
			listener.onLocationFetched(newLocation);
		}
		
		Log.d("kkkkkk", "new location arrived at QuanleimuApplication: (" + location_.getLatitude() + ", " + location_.getLongitude() + ") !!!");
	}
}
