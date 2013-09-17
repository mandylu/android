package com.baixing.sharing.referral;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Observable;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.baixing.data.GlobalDataManager;
import com.baixing.entity.UserBean;
import com.baixing.util.Util;

public class ReferralNetwork extends Observable {

	private static final String TAG = ReferralNetwork.class.getSimpleName();

	public static final String PROMOTE_URL = "http://192.168.5.109/baixing/referral/promote.php";
	
	private static ReferralNetwork instance = null;

	public static ReferralNetwork getInstance() {
		if (instance != null) {
			return instance;
		}
		instance = new ReferralNetwork();
		return instance;
	}

	public void updateReferral(String action, String content) {

		Context context = GlobalDataManager.getInstance()
				.getApplicationContext();

		String url = PROMOTE_URL + "?action=" + action;

		if (action.equals("join")) {
			url += "&promoterId=" + ReferralPromoter.getInstance().ID() + "&newUdid="
					+ Util.getDeviceUdid(context);
		} else if (action.equals("post")) {
			url += "&promoterId=" + ReferralPromoter.getInstance().ID() + "&newPhone="
					+ content;
		} else if (action.equals("info")) {
			UserBean curUser = GlobalDataManager.getInstance().getAccountManager().getCurrentUser();
			if (curUser != null && !TextUtils.isEmpty(curUser.getPhone())) {
				url += "&udid=" + curUser.getPhone();
			} else {
				url += "&udid=" + Util.getDeviceUdid(context);
			}
		}
		
		Log.d(TAG, "url: " + url);
		new NotifyTask().execute(url);
	}

	private class NotifyTask extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... params) {
			try {
				HttpURLConnection conn = (HttpURLConnection) new URL(params[0])
						.openConnection();
				conn.setConnectTimeout(20000);
				conn.setRequestMethod("GET");
				conn.connect();

				InputStream is = conn.getInputStream();
				Reader reader = new InputStreamReader(is);
				char[] buffer = new char[1000];
				reader.read(buffer);
				return new String(buffer).trim();

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return null;
			} catch (MalformedURLException e) {
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(String s) {
			super.onPostExecute(s);
			if (!TextUtils.isEmpty(s)) {
				Log.d(TAG, s);
				setChanged();
				notifyObservers(s);
			}
		}
	}
}
