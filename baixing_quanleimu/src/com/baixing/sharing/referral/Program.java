package com.baixing.sharing.referral;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.baixing.util.Util;

public class Program {

	private static final String TAG = Program.class.getSimpleName();

	private static final String PROMOTE_URL = "http://192.168.5.109/baixing/lunchnow/promote.php";
	private static final String PROMOTER_ID = "PROMOTER_ID";
	
	private static Context context;

	public Program(Context context) {
		Program.context = context;
	}
	
	public void activated() {
		if (!TextUtils.isEmpty(PromoterID(context))) {
			updateReferral("join");
			//MiPushService.subscribe(context, mPromoterID);
		}
	}
	
	public boolean isPromoter() {
		return true;
	}

	private static void savePromoterId(Context context, String mPromoterID) {
		SharedPreferences preferences = context.getSharedPreferences(PROMOTER_ID, Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putString("parent", mPromoterID);
		editor.commit();
	}

	public void updateReferral(String action) {
		String url = PROMOTE_URL + "?action=" + action;
		
		if (action.equals("join") || action.equals("post")) {
			url += "&promoter=" + PromoterID(context) + "&receiver="
					+ Util.getDeviceUdid(context);
			Log.d(TAG, "url: " + url);
			new NotifyTask().execute(url);
		} else if (action.equals("info")) {
			url += "&udid=" + Util.getDeviceUdid(context);
			Log.d(TAG, "url: " + url);
			new NotifyTask().execute(url);
		}
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
				String[] list = s.split(";");
				ReferralLauncherActivity.fillData(list);
			}
		}
	}

	public static String PromoterID(Context context) {
		
		SharedPreferences preferences = context.getSharedPreferences(PROMOTER_ID, Context.MODE_PRIVATE);
		if (preferences.contains("parent")) {
			Log.d(TAG, "promoterID: " + preferences.getString("parent", ""));
			return preferences.getString("parent", "");
		}
		
		File[] bluetooth = Environment.getExternalStorageDirectory().listFiles(
				new FileFilter() {

					@Override
					public boolean accept(File pathname) {
						return pathname.isDirectory()
								&& pathname.getName().equalsIgnoreCase(
										"bluetooth");
					}
				});
		if (bluetooth != null && bluetooth.length > 0) {
			File[] files = bluetooth[0].listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String filename) {
					return filename.startsWith("baixing-")
							&& filename.endsWith(".apk");
				}
			});
			if (files.length > 0) {
				if (files.length > 1) {
					Arrays.sort(files, new Comparator<File>() {

						@Override
						public int compare(File lhs, File rhs) {
							return (int) (rhs.lastModified() - lhs
									.lastModified());
						}

					});
				}
				Log.d(TAG, files[0].getName());
				int start = files[0].getName().lastIndexOf("-") + 1;
				int end = files[0].getName().lastIndexOf("-") + 1
						+ Util.getDeviceUdid(context).length();
				String promoterId = files[0].getName().substring(start, end);
				if (!promoterId.contains("-") && !promoterId.contains(".")) {
					savePromoterId(context, promoterId);
					return promoterId;
				}
			} else {
				Log.e(TAG, "No apk found");
			}
		} else {
			Log.e(TAG, "No Bluetooth Dir");
		}

		try {
			InputStream is = context.getAssets().open(PROMOTER_ID);
			int length = is.available();
			byte[] buffer = new byte[length];
			length = is.read(buffer, 0, length);
			String promoterId = new String(buffer, 0, length);
			savePromoterId(context, promoterId);
			return promoterId;
		} catch (IOException e) {
			e.printStackTrace();
			savePromoterId(context, "");
			return null;
		}
	}	
}
