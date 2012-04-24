package com.quanleimu.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.widget.Toast;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKEvent;
import com.baidu.mapapi.MKGeneralListener;
import com.quanleimu.entity.CityDetail;
import com.quanleimu.entity.Filterss;
import com.quanleimu.entity.FirstStepCate;
import com.quanleimu.entity.GoodsDetail;
import com.quanleimu.entity.HotList;
import com.quanleimu.entity.SecondStepCate;
import com.quanleimu.imageCache.LazyImageLoader;
import com.quanleimu.imageCache2SD.LazyImageLoader2SD;
import com.quanleimu.util.MyUncaughtExceptionHandler;

public class MyApplication extends Application {
	
	public static String udid="";
	public static String version="";
	public static Context context;	
	public static LazyImageLoader lazyImageLoader;
	public static LazyImageLoader2SD lazyImageLoader2SD;
	public static List<String> list;
	public static List<HotList> listHot;
	public static List<Bitmap> listBm;
	public static boolean update = false;
	
	public static List<SecondStepCate> listUsualCates;
	
	public List<Bitmap> listBigBm = new ArrayList<Bitmap>();
	
	public List<Bitmap> getListBigBm() {
		return listBigBm;
	}

	public void setListBigBm(List<Bitmap> listBigBm) {
		this.listBigBm = listBigBm;
	}

	//浏览历史
	public List<GoodsDetail> listLookHistory = new ArrayList<GoodsDetail>();
	public List<GoodsDetail> getListLookHistory() {
		return listLookHistory;
	}

	public void setListLookHistory(List<GoodsDetail> listLookHistory) {
		this.listLookHistory = listLookHistory;
	}

	public List<GoodsDetail> getListMyStore() {
		return listMyStore;
	}

	public void setListMyStore(List<GoodsDetail> listMyStore) {
		this.listMyStore = listMyStore;
	}

	//我的发布信息
	public List<GoodsDetail> listMyPost = new ArrayList<GoodsDetail>();
	
	public List<GoodsDetail> getListMyPost() {
		return listMyPost;
	}

	public void setListMyPost(List<GoodsDetail> listMyPost) {
		this.listMyPost = listMyPost;
	}

	//我的收藏数据集合
	public List<GoodsDetail> listMyStore = new ArrayList<GoodsDetail>();
	
	//搜索结果集合总数
	public int searchCount = -1;
	
	public int getSearchCount() {
		return searchCount;
	}

	public void setSearchCount(int searchCount) {
		this.searchCount = searchCount;
	}

	//搜索结果集合
	public List<GoodsDetail> listSearchGoods = new ArrayList<GoodsDetail>();
	
	public List<GoodsDetail> getListSearchGoods() {
		return listSearchGoods;
	}

	public void setListSearchGoods(List<GoodsDetail> listSearchGoods) {
		this.listSearchGoods = listSearchGoods;
	}

	//搜索记录
	private List<String> listRemark = new ArrayList<String>();
	
	public List<String> getListRemark() {
		return listRemark;
	}

	public void setListRemark(List<String> listRemark) {
		this.listRemark = listRemark;
	}

	//登陆以后的手机号码保存
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
	public String activity_type = "";
	// 添加图片流文件名
	public List<String> listFileNames = new ArrayList<String>();
	// 所有类目的集合
	public List<FirstStepCate> listFirst = new ArrayList<FirstStepCate>();
	// 二级类目集合
	public List<SecondStepCate> listSecond = new ArrayList<SecondStepCate>();
	// 数据集合
	public List<GoodsDetail> listGoods = new ArrayList<GoodsDetail>();

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

	// 筛选条件
	public Map<Integer, String> savemap = new HashMap<Integer, String>();

	public Map<Integer, String> getSavemap() {
		return savemap;
	}

	public void setSavemap(Map<Integer, String> savemap) {
		this.savemap = savemap;
	}

	public List<Filterss> getListFilterss() {
		return listFilterss;
	}

	public void setListFilterss(List<Filterss> listFilterss) {
		this.listFilterss = listFilterss;
	}

	public List<GoodsDetail> getListGoods() {
		return listGoods;
	}

	public void setListGoods(List<GoodsDetail> listGoods) {
		this.listGoods = listGoods;
	}

	public List<SecondStepCate> getListSecond() {
		return listSecond;
	}

	public void setListSecond(List<SecondStepCate> listSecond) {
		this.listSecond = listSecond;
	}

	public List<FirstStepCate> getListFirst() {
		return listFirst;
	}

	public void setListFirst(List<FirstStepCate> listFirst) {
		this.listFirst = listFirst;
	}

	public List<String> getListFileNames() {
		return listFileNames;
	}

	public void setListFileNames(List<String> listFileNames) {
		this.listFileNames = listFileNames;
	}

	public String cityName = "";
	public String gpsCityName = "";

	public String getGpsCityName() {
		return gpsCityName;
	}

	public void setGpsCityName(String gpsCityName) {
		this.gpsCityName = gpsCityName;
	}

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	// 经纬度添加到全局变量
	public double lat = 0;

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLon() {
		return lon;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

	public double lon = 0;

	public String getActivity_type() {
		return activity_type;
	}

	public void setActivity_type(String activity_type) {
		this.activity_type = activity_type;
	}

	static MyApplication mDemoApp;
	public MyUncaughtExceptionHandler handler = null;

	// 百度MapAPI的管理类
	BMapManager mBMapMan = null;

	// 授权Key
	// TODO: 请输入您的Key,
	// 申请地址：http://dev.baidu.com/wiki/static/imap/key/
	//713E99B1CD54866996162791BA789A0D9A13791B	
	String mStrKey = "736C4435847CB7D20DD1131064E35E8941C934F5";
	boolean m_bKeyRight = true; // 授权Key正确，验证通过

	// 常用事件监听，用来处理通常的网络错误，授权验证错误等
	static class MyGeneralListener implements MKGeneralListener {
		@Override
		public void onGetNetworkState(int iError) {
			Toast.makeText(MyApplication.mDemoApp.getApplicationContext(),
					"您的网络出错啦！", Toast.LENGTH_LONG).show();
		}

		@Override
		public void onGetPermissionState(int iError) {
			if (iError == MKEvent.ERROR_PERMISSION_DENIED) {
				// 授权Key错误：
				Toast.makeText(MyApplication.mDemoApp.getApplicationContext(),
						"请在BMapApiDemoApp.java文件输入正确的授权Key！", Toast.LENGTH_LONG)
						.show();
				MyApplication.mDemoApp.m_bKeyRight = false;
			}
		}

	}

	@Override
	public void onCreate() {
		mDemoApp = this;
		mBMapMan = new BMapManager(this);
		mBMapMan.init(this.mStrKey, new MyGeneralListener());

		// handler = MyUncaughtExceptionHandler.getInstance();
		// handler.init(getApplicationContext());
		// handler.sendPreviousReportsToServer();
		context = this.getApplicationContext();
		lazyImageLoader = new LazyImageLoader();
		super.onCreate();
	}

	@Override
	// 建议在您app的退出之前调用mapadpi的destroy()函数，避免重复初始化带来的时间消耗
	public void onTerminate() {
		// TODO Auto-generated method stub
		if (mBMapMan != null) {
			mBMapMan.destroy();
			mBMapMan = null;
		}
		super.onTerminate();
	}
}
