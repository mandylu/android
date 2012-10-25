package com.quanleimu.activity;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.quanleimu.entity.AllCates;
import com.quanleimu.entity.CityList;
import com.quanleimu.entity.GoodsDetail;
import com.quanleimu.entity.PostMu;
import com.quanleimu.imageCache.LazyImageLoader;
import com.quanleimu.jsonutil.JsonUtil;
import com.quanleimu.util.Communication;
import com.quanleimu.util.Helper;
import com.quanleimu.util.LocationService;
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
		
		new Thread(new ReadCityListThread()).start();
		new Thread(new ReadInfoThread()).start();
		new Thread(new ReadCateListThread()).start();
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
						parentActivity.myApp.setListFirst(allCates.getChildren());
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
				InputStream is = parentActivity.getAssets().open("cityjson.txt");
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
						parentActivity.myApp.setListCityDetails(cityList.getListDetails());
						
						//update current city name
						String chengshiName = (String) Helper.loadDataFromLocate(parentActivity, "cityName");
						if (chengshiName == null || chengshiName.equals("")) {
						} else {
							parentActivity.myApp.setCityName(chengshiName);
							
							for(int i=0;i<parentActivity.myApp.getListCityDetails().size();i++)
							{
								if(chengshiName.equals(parentActivity.myApp.getListCityDetails().get(i).getName()))
								{
									String cityName1 = parentActivity.myApp.getListCityDetails().get(i).getEnglishName();
									parentActivity.myApp.setCityEnglishName(cityName1);
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
			Object objRemark = Helper.loadDataFromLocate(parentActivity, "listRemark");
			if(objRemark != null)
			{
				listRemark = (List<String>)objRemark;
			}
			else
			{
				listRemark = null;
			}
			parentActivity.myApp.setListRemark(listRemark);

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
			parentActivity.myApp.setListLookHistory(listLookHistory);

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
			parentActivity.myApp.setListMyStore(listMyStore);
			
			Object personalMark = Helper.loadDataFromLocate(parentActivity, "personMark");
			if(personalMark != null){
				parentActivity.myApp.setPersonMark((String)personalMark);
			}
			myHandler.sendEmptyMessage(2);
		}

	}
}
