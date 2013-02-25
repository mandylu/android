//liuchong@baixing.com
package com.baixing.activity;

import java.lang.ref.WeakReference;

import com.baixing.data.GlobalDataManager;
import com.baixing.network.api.ApiConfiguration;
import com.baixing.network.api.BaseApiCommand;
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
//		ApiClient.getInstance().init(mangerInstance.getApplicationContext(),
//				Util.getDeviceUdid(mangerInstance.getApplicationContext()),
//				mangerInstance.getAccountManager().getMyId(mangerInstance.getApplicationContext()),
//				mangerInstance.getVersion(), 
//				mangerInstance.getChannelId(),
//				mangerInstance.getCityEnglishName(),
//				GlobalDataManager.getInstance().getNetworkCacheManager());
		ApiConfiguration.config("www.baixing.com", GlobalDataManager
				.getInstance().getNetworkCacheManager(), "api_mobile_android",
				"c6dd9d408c0bcbeda381d42955e08a3f");
		BaseApiCommand.init(
				Util.getDeviceUdid(mangerInstance.getApplicationContext()),
				mangerInstance.getAccountManager().getMyId(
						mangerInstance.getApplicationContext()),
				mangerInstance.getVersion(), mangerInstance.getChannelId(),
				mangerInstance.getCityEnglishName());
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
