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
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Pair;
import android.widget.Toast;

import com.quanleimu.entity.AllCates;
import com.quanleimu.entity.CityDetail;
import com.quanleimu.entity.CityList;
import com.quanleimu.entity.GoodsDetail;
import com.quanleimu.entity.PostMu;
import com.quanleimu.imageCache.LazyImageLoader;
import com.quanleimu.imageCache.SimpleImageLoader;
import com.quanleimu.jsonutil.JsonUtil;
import com.quanleimu.util.Communication;
import com.quanleimu.util.Communication.BXHttpException;
import com.quanleimu.util.Helper;
import com.quanleimu.util.LocationService;
import com.quanleimu.util.MobileConfig;
import com.quanleimu.util.Util;

public class SplashJob {

	public static interface JobDoneListener {
		public void onJobDone();
	}
	
	JobDoneListener jobListener;
	
	private BaseActivity parentActivity;
	
	private boolean isJobDone;
	private boolean isJobStarted;
	
	SplashJob(BaseActivity activity, JobDoneListener listener)
	{
		this.parentActivity = activity;
		this.jobListener = listener;
	}
	
	public void doSplashWork()
	{
		if (isJobStarted || isJobDone)
		{
			return;  //Avoid deprecate start.
		}
		
		isJobStarted = true;
		
		LocationService.getInstance().start(parentActivity, QuanleimuApplication.mDemoApp);
		QuanleimuApplication.udid = Util.getDeviceUdid(parentActivity);

		QuanleimuApplication.version = Util.getVersion(parentActivity);

		try {
			if (parentActivity.checkConnection() == false) {
				Toast.makeText(parentActivity, "网络连接异常", 3).show();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		MobileConfig.getInstance().syncMobileConfig();
		
		new Thread(new ReadCityListThread()).start();
		new Thread(new ReadInfoThread()).start();
		new Thread(new ReadCateListThread()).start();
	}
	
	
	protected final int MSG_LOAD_CITY_LIST 			= 1;
	protected final int MSG_LOAD_HISTORY_STORED 	= 2;
	protected final int MSG_LOAD_ALLCATEGORY_LIST 	= 3;
	
	
	Handler myHandler = new Handler() {
		private int record1 = 0;//flag city list
		private int record2 = 0;//flag history/stored 
		private int record3 = 0;//flag for allcate list


		
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_LOAD_CITY_LIST:
				record1 = 1;
				break;
			case MSG_LOAD_HISTORY_STORED:
				record2 = 1;
				break;
			case MSG_LOAD_ALLCATEGORY_LIST:
				record3 = 1;
                //todo load first category config 这块有问题
				break;
			default:
				break;
			}
			
			if(1 == record1 && 1 == record2 && 1 == record3){
				isJobDone = true;
				QuanleimuApplication.getImageLoader();// = new LazyImageLoader();
				jobListener.onJobDone();
			}
		}
	};
	
	public boolean isJobDone()
	{
		return isJobDone;
	}

	class ReadCateListThread implements Runnable {

		@Override
		public void run() {
		
			// TODO Auto-generated method stub
			PostMu postMu = (PostMu)Util.loadDataFromLocate(parentActivity, "saveFirstStepCate");
			
			boolean valid = true;
			
			if(null == postMu || postMu.getJson().length() == 0){
				
				ObjectInputStream ois = null;
				InputStream is = null;
				
				try {
					is = parentActivity.getAssets().open("cateJson.txt");
					ois = new ObjectInputStream(is);
//					byte[] b = new byte[is.available()];
//					is.read(b);
//					String content = new String(b);	
					
					postMu = (PostMu)ois.readObject();
					postMu.setTime(System.currentTimeMillis());
					
					//save to context
					if(null != postMu)
						Util.saveDataToLocate(parentActivity, "saveFirstStepCate", postMu);
					
	
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
							Util.saveDataToLocate(parentActivity, "saveFirstStepCate", postMu);
							
//							String tmpPath = "/quanleimu/favorites/百姓网收藏图片/";
//							Util.saveDataToSdCard(tmpPath, "catetemp", postMu);
							
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
					
//					if (allCates == null) {
//					} else {
                    //todo 不做保护，拿不到类目的情况时有发生，需要处理
					QuanleimuApplication.getApplication().setListFirst(allCates.getChildren());
//					}
				}
			}
			
			myHandler.sendEmptyMessage(MSG_LOAD_ALLCATEGORY_LIST);
		}
	}
	
	private void updateCityList(CityList cityList)
	{
		if (cityList == null || cityList.getListDetails() == null
				|| cityList.getListDetails().size() == 0) {
		} else {
			QuanleimuApplication.getApplication().setListCityDetails(cityList.getListDetails());
			
			//update current city name
			String cityName = (String) Helper.loadDataFromLocate(parentActivity, "cityName");
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
	
	class ReadCityListThread implements Runnable {
		
		@Override
		public void run() {
			CityList cityList = new CityList();
			String content = null;
			// load content
			try {
				// 1. load from locate.
				Pair<Long, Object> pair = Util.loadDataAndTimestampFromLocate(parentActivity, "cityjson");
				
				long timestamp = pair.first;
				content = (String) pair.second;
				
				// 2. load from server.
				long updateTimestamp = MobileConfig.getInstance().getCityTimestamp();
				if (timestamp < updateTimestamp || content == null || content.length() == 0) {
					String apiName = "city_list";
					String url = Communication.getApiUrl(apiName, new ArrayList<String>());
					content = Communication.getDataByUrl(url, true);
					if (content != null && content.length() > 0) 
					{
						Util.saveDataToLocate(parentActivity, "cityjson", content);
					}
				}
	
			} catch (IOException e) {
				e.printStackTrace();
			} catch (BXHttpException e) {
				e.printStackTrace();
			}
			
			// 3. load from asset (if citylist not cached & network failed)
			if (content == null || content.length() == 0)
			{
				try {
					InputStream is = parentActivity.getAssets().open("cityjson.txt");
					byte[] b = new byte[is.available()];
					is.read(b);
					content = new String(b);
				}catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			if (content == null || content.equals("")) {
				cityList = null;
			} else {
				cityList = JsonUtil.parseCityListFromJson((content));
				SplashJob.this.updateCityList(cityList);
			}
			myHandler.sendEmptyMessage(MSG_LOAD_CITY_LIST);
		}
		
	}

	public List<String> listRemark = new ArrayList<String>();

	class ReadInfoThread implements Runnable {

		@SuppressWarnings("unchecked")
		@Override
		public void run() { 
			// 获取搜索记录
			Object objRemark = Helper.loadDataFromLocate(parentActivity, "listRemark");
			if(objRemark != null)
			{
				listRemark = (List<String>)objRemark;
			}
			else
			{
				listRemark = null;
			}
			QuanleimuApplication.getApplication().setListRemark(listRemark);

			// 获取我的浏览历史以及我的收藏
			Object objLookHistory = Helper.loadDataFromLocate(parentActivity, "listLookHistory");
			List<GoodsDetail> listLookHistory;
			if(objLookHistory != null)
			{
				listLookHistory = (List<GoodsDetail>)objLookHistory;
			}
			else
			{
				listLookHistory = null;
			}
			QuanleimuApplication.getApplication().setListLookHistory(listLookHistory);

			Object objStore = Helper.loadDataFromLocate(parentActivity, "listMyStore");
			List<GoodsDetail> listMyStore;
			if(objStore != null)
			{
				listMyStore = (List<GoodsDetail>)objStore;
			}
			else
			{
				listMyStore = null;
			}
			QuanleimuApplication.getApplication().setListMyStore(listMyStore);
			
			Object personalMark = Helper.loadDataFromLocate(parentActivity, "personMark");
			if(personalMark != null){
				QuanleimuApplication.getApplication().setPersonMark((String)personalMark);
			}
			myHandler.sendEmptyMessage(MSG_LOAD_HISTORY_STORED);
		}

	}
}
