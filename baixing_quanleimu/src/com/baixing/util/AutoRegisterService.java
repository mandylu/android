package com.baixing.util;

import java.util.Observable;
import java.util.Observer;

import org.json.JSONObject;

import com.baixing.data.GlobalDataManager;
import com.baixing.entity.UserBean;
import com.baixing.message.BxMessageCenter;
import com.baixing.message.IBxNotificationNames;
import com.baixing.network.NetworkUtil;
import com.baixing.network.api.ApiParams;
import com.baixing.network.api.BaseApiCommand;
import com.tencent.mm.sdk.platformtools.Log;
//Life cycle for this class is only 2 weeks, add it because we have 2 sprint of version android3.3.
public class AutoRegisterService implements Observer, Runnable {
	
	private Object mutex = new Object();
	
	public static void start() {
		UserBean user = (UserBean) Util.loadDataFromLocate(GlobalDataManager.getInstance().getApplicationContext(), "user", UserBean.class);
		if (user == null) {
			Thread t = new Thread(new AutoRegisterService());
			t.start();
		}
	}
	
	@Override
	public void update(Observable observable, Object data) {
		synchronized (mutex) {
			BxMessageCenter.defaultMessageCenter().removeObserver(this);
			mutex.notifyAll();
		}
	}

	@Override
	public void run() {
		UserBean bean = (UserBean) Util.loadDataFromLocate(GlobalDataManager.getInstance().getApplicationContext(), "anonymousUser", UserBean.class);
		while (bean == null) {
			
			ApiParams params = new ApiParams();
			BaseApiCommand cmd = BaseApiCommand.createCommand("user_autoregister", false, params);
			String json_response = cmd.executeSync(GlobalDataManager.getInstance().getApplicationContext());
			
			try {
				if (json_response != null) {
					JSONObject jsonObject = new JSONObject(json_response);

					JSONObject userObj = null;
					try {
						userObj = jsonObject.getJSONObject("user");
					} catch (Exception e) {
						e.printStackTrace();
					}

					if (userObj != null) {
						
						// 登录成功
						UserBean user = new UserBean();
						user.setId(userObj.getString("id"));
						
						if (GlobalDataManager.getInstance().getAccountManager().getCurrentUser() == null) {
							Util.saveDataToLocate(GlobalDataManager.getInstance().getApplicationContext(), "user", user);
						}
						Util.saveDataToLocate(GlobalDataManager.getInstance().getApplicationContext(), "anonymousUser", user);
						bean = user;
						BxMessageCenter.defaultMessageCenter().postNotification(IBxNotificationNames.NOTIFICATION_USER_CREATE, user);
					}
				}
			} catch (Throwable t) {
				Log.d("AUTOREG", "exception when do auto registration.");
			}
			
			if (bean == null && !NetworkUtil.isNetworkActive(GlobalDataManager.getInstance().getApplicationContext())) {
				synchronized (mutex) {
					try {
						BxMessageCenter.defaultMessageCenter().registerObserver(this, IBxNotificationNames.NOTIFICATION_NETWORK_CHANGE);
						mutex.wait(2 * 60 * 1000);
					} catch (InterruptedException e) {
						Log.d("AUTOREG", "wait exception, " + e.getMessage());
					}
				}
			}
			
		}
	}

}
