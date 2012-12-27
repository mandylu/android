//liuchong@baixing.com
package com.baixing.activity;

import java.lang.ref.WeakReference;

import com.baixing.android.api.ApiClient;
import com.baixing.data.GlobalDataManager;
import com.baixing.util.ErrorHandler;
import com.baixing.util.Util;

import android.app.Application;
import android.content.Context;

public class EntryApplication extends Application {

	/* (non-Javadoc)
	 * @see android.app.Application#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		
		GlobalDataManager.context = new WeakReference<Context>(getApplicationContext());
		ErrorHandler.getInstance().initContext(getApplicationContext());

		//Init api.
		GlobalDataManager mangerInstance = GlobalDataManager.getInstance();
		ApiClient.getInstance().init(mangerInstance.getApplicationContext(),
				Util.getDeviceUdid(mangerInstance.getApplicationContext()), 
				mangerInstance.getVersion(), 
				mangerInstance.getChannelId(),
				mangerInstance.getCityEnglishName(),
				GlobalDataManager.getInstance().getNetworkCacheManager());
	}

	/* (non-Javadoc)
	 * @see android.app.Application#onLowMemory()
	 */
	@Override
	public void onLowMemory() {
		// TODO Auto-generated method stub
		super.onLowMemory();
	}

	/* (non-Javadoc)
	 * @see android.app.Application#onTerminate()
	 */
	@Override
	public void onTerminate() {
		super.onTerminate();
	}

}
