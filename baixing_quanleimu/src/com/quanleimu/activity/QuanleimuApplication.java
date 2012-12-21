//liuchong@baixing.com
package com.quanleimu.activity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.baidu.mapapi.MKEvent;
import com.baidu.mapapi.MKGeneralListener;
import com.baixing.entity.AllCates;
import com.baixing.entity.BXLocation;
import com.baixing.entity.CityDetail;
import com.baixing.entity.CityList;
import com.baixing.entity.Filterss;
import com.baixing.entity.FirstStepCate;
import com.baixing.entity.GoodsDetail;
import com.baixing.entity.HotList;
import com.baixing.entity.SecondStepCate;
import com.baixing.entity.UserBean;
import com.baixing.imageCache.LazyImageLoader;
import com.baixing.jsonutil.JsonUtil;
import com.baixing.message.BxMessageCenter;
import com.baixing.message.IBxNotificationNames;
import com.baixing.message.BxMessageCenter.IBxNotification;
import com.baixing.util.BXDatabaseHelper;
import com.baixing.util.Communication;
import com.baixing.util.ErrorHandler;
import com.baixing.util.Helper;
import com.baixing.util.LocationService;
import com.baixing.util.Util;
public class QuanleimuApplication implements LocationService.BXLocationServiceListener, Observer {
	public static final String kWBBaixingAppKey = "3747392969";
	public static final String kWBBaixingAppSecret = "ff394d0df1cfc41c7d89ce934b5aa8fc";
	public static String version="";
	public static String channelId;
	public static WeakReference<Context> context;	
	private static LazyImageLoader lazyImageLoader;
	public static boolean update = false;
	private static boolean textMode = false;
	private static boolean needNotifiySwitchMode = true;
	private static SharedPreferences preferences = null;
	private static LinkedHashMap<String, String> cacheNetworkRequest = null;
	private static BXDatabaseHelper dbManager = null;
	private static QuanleimuApplication mDemoApp = null;
	private static int lastDestoryInstanceHash = 0;
	
    //为赌约而设
    public static int postEntryFlag = -1;
	
	protected static final String PREFS_FILE = "device_id.xml";
    protected static final String PREFS_DEVICE_ID = "device_id";
    
    protected static final List<Pair<String, String>> storeList = new ArrayList<Pair<String,String>>();
    
    static {
    	/**
    	 * do IO on network request will prolong user's time waiting network. This thread do simple IO work on a separate thread.
    	 */
    	Thread t = new Thread(new Runnable() {
			
			public void run() {
				while(true) {
					Pair<String, String> item = null;
					synchronized (storeList) {
						if (storeList.size() > 0)
						{
							item = storeList.remove(0);
						}
						else
						{
							try {
								storeList.wait(5 * 60 * 1000);
//								Log.d("QLMAPP", "wakeup to handle store");
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
					
					if (item != null)
					{
//						Log.d("QLMAPP", "oh yeah, store it.");
						storeCacheNetworkRequest(item.first, item.second);
					}
				}
			}
		});
    	t.start();
    }
    
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
		synchronized (storeList) {
			storeList.add(Pair.create(request, result));
			storeList.notifyAll();
		}
	}
	
	private static void storeCacheNetworkRequest(String request, String result){
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
		QuanleimuApplication.needNotifiySwitchMode = false;
		
		if(null == preferences){
			preferences = context.get() != null ? 
					context.get().getApplicationContext().getSharedPreferences("QuanleimuPreferences", Context.MODE_PRIVATE)
					: null;
		} 
		
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean("isTextMode", tMode);
		editor.putBoolean("needNotifyUser", false);
		editor.commit();
	}
	
	public static boolean isTextMode(){
		return QuanleimuApplication.textMode;
	}
	
	public static boolean needNotifySwitchMode()
	{
		return QuanleimuApplication.needNotifiySwitchMode;
	}
	public static List<SecondStepCate> listUsualCates;

	//浏览历史 //FIXME: remove me later , keep it because we do not want change code a lot at one time.
	public List<GoodsDetail> listLookHistory = new ArrayList<GoodsDetail>();
	public List<GoodsDetail> getListLookHistory() {
		return listLookHistory;
	}

	public List<GoodsDetail> getListMyStore() {
		return listMyStore;
	}
	
	public void clearMyStore()
	{
		if (this.listMyStore != null)
		{
			this.listMyStore.clear();
		}
	}
	
	public List<GoodsDetail> addFav(GoodsDetail detail)
	{
		if (this.listMyStore == null)
		{
			this.listMyStore = new ArrayList<GoodsDetail>();
		}
		
		this.listMyStore.add(0, detail);
		BxMessageCenter.defaultMessageCenter().postNotification(IBxNotificationNames.NOTIFICATION_FAV_ADDED, detail);
		return this.listMyStore;
	}
	
	public List<GoodsDetail> removeFav(GoodsDetail detail)
	{
		if (this.listMyStore == null || detail == null)
		{
			return this.listMyStore;
		}
		
		for (int i = 0; i < listMyStore.size(); i++) {
			if (detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID)
					.equals(listMyStore.get(i).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID))) {
				listMyStore.remove(i);
				BxMessageCenter.defaultMessageCenter().postNotification(IBxNotificationNames.NOTIFICATION_FAV_REMOVE, detail);
				break;
			}
		}
		
		return this.listMyStore;
	}
	
	public void updateFav(List<GoodsDetail> favs)
	{
		this.listMyStore = new ArrayList<GoodsDetail>();
		
		if (favs != null)
		{
			for (int i=0; i<favs.size() && i<=50; i++)
			{
				this.listMyStore.add(favs.get(i));
			}
		}
	}

	public void updateFav(GoodsDetail[] list) {
		this.listMyStore = new ArrayList<GoodsDetail>();

		if (list != null)
		{
			int i=0;
			for (GoodsDetail item : list)
			{
				this.listMyStore.add(item);
				i++;
				if (i == 50)
				{
					break;
				}
			}
		}
					
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
	private List<GoodsDetail> listMyStore = new ArrayList<GoodsDetail>();
	

	//搜索记录
	private List<String> listRemark = new ArrayList<String>();
	
	public List<String> getListRemark() {
		return listRemark;
	}

	public void updateRemark(String[] list) {
		
		this.listRemark = new ArrayList<String>();
		
		if (list != null)
		{
			for (String s : list)
			{
				this.listRemark.add(s);
			}
		}
	}
	
	public void updateRemark(List<String> list) {
		
		this.listRemark = new ArrayList<String>();
		
		if (list != null)
		{
			this.listRemark.addAll(list);
		}
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
	
	public void updateCityList(CityList cityList)
	{
		if (cityList == null || cityList.getListDetails() == null
				|| cityList.getListDetails().size() == 0) {
		} else {
			QuanleimuApplication.getApplication().setListCityDetails(cityList.getListDetails());
			
			//update current city name
			byte[] cityData = Util.loadData(getApplicationContext(), "cityName");
			String cityName = cityData == null ? null : new String(cityData); //(String) Util.loadDataFromLocate(getApplicationContext(), "cityName", String.class);
			if (cityName == null || cityName.equals("")) {
			} else {
				List<CityDetail> cityDetails = QuanleimuApplication.getApplication().getListCityDetails();
				boolean exist = false;
				for(int i = 0;i< cityDetails.size();i++)
				{
					if(cityName.equals(cityDetails.get(i).getName()))
					{
						String englishCityName = cityDetails.get(i).getEnglishName();
						QuanleimuApplication.getApplication().setCityEnglishName(englishCityName);
						QuanleimuApplication.getApplication().setCityName(cityName);
						exist = true;
						break;
					}
				}
				if (!exist) { // FIXME: @zhongjiawu
					QuanleimuApplication.getApplication().setCityEnglishName("shanghai");
					QuanleimuApplication.getApplication().setCityName("上海");
				}
			}
		}
	}
	
	private String phoneNumber = "";
	
	public void setPhoneNumber(String number){
		phoneNumber = number;
	}
	
	public String getPhoneNumber(){
		return phoneNumber;
	}
	
	private String address = "";
	
	public void setAddress(String ad){
		address = ad;
	}
	
	public String getAddress(){
		return address;
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

	public List<FirstStepCate> listFirst = new ArrayList<FirstStepCate>();
	
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
	public List<FirstStepCate> getListFirst() {
		return listFirst;
	}
	public String queryCategoryDisplayName(String englishName){
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

	BXLocation location = null;
	boolean location_updated = false;

	public BXLocation getCurrentPosition(boolean bRealLocality) {
		if(context.get() == null) return null;
		if(null == location){
			location = (BXLocation)Util.loadDataFromLocate(context.get(), "location_data", BXLocation.class);
			
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
		
		locationFetchListeners.add(listener);		
		LocationService.getInstance().addLocationListener(context.get(), this);
		
		final BXLocation curLocation = getCurrentPosition(true);
		if(null == curLocation){
			return false;
		}		

		listener.onLocationFetched(curLocation);

		if (curLocation.geocoded){
//			Log.d("currentlocation", "xixi, curLocation.geocoded");
			listener.onGeocodedLocationFetched(curLocation);
		}else{
//			Log.d("currentlocation", "xixi, curLocation not geocoded");
		}
		
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
			
			Util.saveDataToLocateDelay(context.get(), "location_data", location);

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
	
	static public void resetApplication()
	{
		if (mDemoApp != null)
		{
			lastDestoryInstanceHash = mDemoApp.hashCode();
		}
		QuanleimuApplication.mDemoApp = null;
	}
	
	static void initStaticFields()
	{
		lastDestoryInstanceHash = 0;
	}
	
	static public boolean isAppDestroy(int appHash)
	{
		return appHash !=0 && appHash == lastDestoryInstanceHash;
	}

	static public QuanleimuApplication getApplication(){
		if(null == preferences){
			preferences = context.get().getApplicationContext().getSharedPreferences("QuanleimuPreferences", Context.MODE_PRIVATE);
			textMode = preferences.getBoolean("isTextMode", false);
			needNotifiySwitchMode = preferences.getBoolean("needNotifyUser", true);
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
		BxMessageCenter.defaultMessageCenter().registerObserver(this, IBxNotificationNames.NOTIFICATION_LOGIN);
		BxMessageCenter.defaultMessageCenter().registerObserver(this, IBxNotificationNames.NOTIFICATION_LOGOUT);
	}
	
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

	@Override
	public void onLocationUpdated(Location location_) {
		BXLocation newLocation = new BXLocation(false);
		newLocation.fLat = (float)location_.getLatitude();
		newLocation.fLon = (float)location_.getLongitude();
		
		setLocation(newLocation);
		
		LocationService.getInstance().reverseGeocode(newLocation.fLat, newLocation.fLon, new LocationService.BXRgcListener() {
			
			@Override
			public void onRgcUpdated(BXLocation location) {
//				Log.d("currentlocation", "xixi, onRgcUpdated");
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
		
//		Log.d("kkkkkk", "new location arrived at QuanleimuApplication: (" + location_.getLatitude() + ", " + location_.getLongitude() + ") !!!");
	}


	@Override
	public void update(Observable observable, Object data) {
		if (data instanceof IBxNotification)
		{
			IBxNotification note = (IBxNotification) data;
			if (IBxNotificationNames.NOTIFICATION_USER_CREATE.equals(note.getName())
					|| IBxNotificationNames.NOTIFICATION_LOGIN.equals(note.getName())
					|| IBxNotificationNames.NOTIFICATION_LOGOUT.equals(note.getName())) {
				
				Context cxt = context.get();
				if (cxt != null)
				{
					Util.refreshAndGetMyId(cxt);
				}
			}
		}
	}
	
	public void loadCategorySync(){
		Pair<Long, String> pair = Util.loadJsonAndTimestampFromLocate(this.getApplicationContext(), 
				"saveFirstStepCate");
		
		if (pair.second == null || pair.second.length() == 0){
			pair = Util.loadDataAndTimestampFromAssets(this.getApplicationContext(), "cateJson.txt");
		}
		
		String json = pair.second;
		if (json != null && json.length() > 0) {
			AllCates allCates = JsonUtil.getAllCatesFromJson(Communication.decodeUnicode(json));
			QuanleimuApplication.getApplication().setListFirst(allCates.getChildren());
		}
	
	}
	
	public void loadCitySync(){
		CityList cityList = new CityList();
		// 1. load from locate.
		Pair<Long, String> pair = Util.loadJsonAndTimestampFromLocate(getApplicationContext(), "cityjson");
		
		// 2. load from asset
		if (pair.second == null || pair.second.length() == 0)
		{	
			pair = Util.loadDataAndTimestampFromAssets(getApplicationContext(), "cityjson.txt");
		}
		
		if (pair.second == null || pair.second.length() == 0) {
			cityList = null;
		} else {
			cityList = JsonUtil.parseCityListFromJson((pair.second));
			QuanleimuApplication.getApplication().updateCityList(cityList);
		}
	}
	
	public void loadPersonalSync(){
		// 获取搜索记录
		String[] objRemark = (String[]) Util.loadDataFromLocate(getApplicationContext(), "listRemark", String[].class);
		QuanleimuApplication.getApplication().updateRemark(objRemark);

		GoodsDetail[] objStore = (GoodsDetail[]) Util.loadDataFromLocate(getApplicationContext(), "listMyStore", GoodsDetail[].class);
		QuanleimuApplication.getApplication().updateFav(objStore);
		
		byte[] personalMark = Util.loadData(getApplicationContext(), "personMark");//.loadDataFromLocate(parentActivity, "personMark");
		if(personalMark != null){
			QuanleimuApplication.getApplication().setPersonMark(new String(personalMark));
		}

	}
	
}
