//liuchong@baixing.com
package com.baixing.activity;

import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.baixing.util.LocationService;
import com.baixing.util.MobileConfig;
import com.baixing.util.Util;

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
		
		LocationService.getInstance().start(parentActivity, GlobalDataManager.getApplication());
		GlobalDataManager.version = Util.getVersion(parentActivity);

		try {
			if (parentActivity.checkConnection() == false) {
				Toast.makeText(parentActivity, "网络连接异常", 3).show();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		new Thread(new LoadConfigTask()).start();
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
				GlobalDataManager.getImageLoader();// = new LazyImageLoader();
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
			GlobalDataManager.getApplication().loadCategorySync();
			myHandler.sendEmptyMessage(MSG_LOAD_ALLCATEGORY_LIST);
		}
	}
	
	class LoadConfigTask implements Runnable {

		public void run() {
			try
			{
				MobileConfig.getInstance().syncMobileConfig();
			}
			catch(Throwable t)
			{
				
			}
			finally
			{
				new Thread(new ReadCityListThread()).start();
				new Thread(new ReadInfoThread()).start();
				new Thread(new ReadCateListThread()).start();
			}
		}
		
	}
	
	class ReadCityListThread implements Runnable {
		
		@Override
		public void run() {
			GlobalDataManager.getApplication().loadCitySync();
			myHandler.sendEmptyMessage(MSG_LOAD_CITY_LIST);
		}
	}

	public String[] listRemark = new String[]{};//new ArrayList<String>();

	class ReadInfoThread implements Runnable {

		@SuppressWarnings("unchecked")
		@Override
		public void run() { 
			GlobalDataManager.getApplication().loadPersonalSync();
			myHandler.sendEmptyMessage(MSG_LOAD_HISTORY_STORED);
		}

	}
}
