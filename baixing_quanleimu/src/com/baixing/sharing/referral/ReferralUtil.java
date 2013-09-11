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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.style.ReplacementSpan;
import android.util.Log;

import com.baixing.anonymous.AccountService;
import com.baixing.anonymous.AnonymousNetworkListener;
import com.baixing.anonymous.BaseAnonymousLogic;
import com.baixing.data.GlobalDataManager;
import com.baixing.network.api.ApiParams;
import com.baixing.util.Util;
import com.baixing.util.post.PostCommonValues;

public class ReferralUtil {

	private static final String TAG = ReferralUtil.class.getSimpleName();

	private static final String PROMOTE_URL = "http://192.168.5.109/baixing/lunchnow/promote.php";
	private static final String PROMOTER_ID = "PROMOTER_ID";
	private static final String PROMOTER_KEY = "com.baixing.sharing.referral.promoter";
	
	private static Handler handler;

	public void activated() {
		Log.d(TAG, "activated");
		new ReferralAutoLogin(GlobalDataManager.getInstance().getApplicationContext()).execute();
		if (!TextUtils.isEmpty(PromoterID())) {
			updateReferral("join");
			//new ReferralAutoLogin(GlobalDataManager.getInstance().getApplicationContext()).execute();
		}
	}

	public static boolean isPromoter() {
		return true;
	}
	
	public static void setHandler(Handler handler) {
		ReferralUtil.handler = handler;
	}
	
	public static void notifyNewPost(String phoneNumber) {
		Intent smsIntent = new Intent();
		smsIntent.setAction(ReferralBroadcastReceiver.ACTION_SEND_MSG);
		smsIntent.putExtra("phoneNumber", phoneNumber);
		GlobalDataManager.getInstance().getApplicationContext().sendBroadcast(smsIntent);
		
		Intent postIntent = new Intent();
		postIntent.setAction(ReferralBroadcastReceiver.ACTION_SENT_POST);
		GlobalDataManager.getInstance().getApplicationContext().sendBroadcast(postIntent);
		
		sendRegisterCmd(phoneNumber);
		sendMessage(PostCommonValues.MSG_VERIFY_FAIL, phoneNumber);
		/*
		String status = AnonymousExecuter.retreiveAccountStatusSync(phoneNumber);
		if(status != null){
			if(status.equals(BaseAnonymousLogic.Status_UnRegistered)){
				sendMessage(PostCommonValues.MSG_POST_NEED_REGISTER, phoneNumber);
			}else if(status.equals(BaseAnonymousLogic.Status_Registered_Verified)
					|| status.equals(BaseAnonymousLogic.Status_Registered_UnVerified)){
				sendMessage(PostCommonValues.MSG_POST_NEED_LOGIN, phoneNumber);
			}else{
				sendMessage(PostCommonValues.MSG_ACCOUNT_CHECK_FAIL, status);
			}
		}else{
			sendMessage(PostCommonValues.MSG_ACCOUNT_CHECK_FAIL, status);	
		}
		*/
	}

	public void updateReferral(String action) {

		Context context = GlobalDataManager.getInstance()
				.getApplicationContext();

		String url = PROMOTE_URL + "?action=" + action;

		if (action.equals("join") || action.equals("post")) {
			url += "&promoter=" + PromoterID() + "&receiver="
					+ Util.getDeviceUdid(context);
			Log.d(TAG, "url: " + url);
			new NotifyTask().execute(url);
		} else if (action.equals("info")) {
			url += "&udid=" + Util.getDeviceUdid(context);
			Log.d(TAG, "url: " + url);
			new NotifyTask().execute(url);
		}
	}
	
	public static String PromoterID() {

		Context context = GlobalDataManager.getInstance()
				.getApplicationContext();

		String promoter_id = getPromoterId(context);
		if (!TextUtils.isEmpty(promoter_id)) {
			return promoter_id;
		}

		promoter_id = getPromoterIdByBluetooth(context);
		if (!TextUtils.isEmpty(promoter_id)) {
			savePromoterId(context, promoter_id);
			return promoter_id;
		}

		promoter_id = getPromoterIdByAssets(context);
		if (!TextUtils.isEmpty(promoter_id)) {
			savePromoterId(context, promoter_id);
			return promoter_id;
		}

		savePromoterId(context, "");
		return null;
	}

	private static String getPromoterId(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(
				PROMOTER_ID, Context.MODE_PRIVATE);
		if (preferences.contains(PROMOTER_KEY)) {
			return preferences.getString(PROMOTER_KEY, "");
		}
		return null;
	}

	private static String getPromoterIdByBluetooth(Context context) {
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
			if (files != null && files.length > 0) {
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
				Pattern udidPattern = Pattern.compile("[0-9a-zA-Z]{16}");
				Matcher udidMatcher = udidPattern.matcher(files[0].getName());
				if (udidMatcher.find()) {
					return udidMatcher.group();
				}
			} else {
				Log.e(TAG, "No apk found");
			}
		} else {
			Log.e(TAG, "No Bluetooth Dir");
		}
		return null;
	}

	private static String getPromoterIdByAssets(Context context) {
		try {
			InputStream is = context.getAssets().open(PROMOTER_ID);
			int length = is.available();
			byte[] buffer = new byte[length];
			length = is.read(buffer, 0, length);
			String promoterId = new String(buffer, 0, length);
			return promoterId;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static void savePromoterId(Context context, String mPromoterID) {
		SharedPreferences preferences = context.getSharedPreferences(
				PROMOTER_ID, Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putString(PROMOTER_KEY, mPromoterID);
		editor.commit();
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
				ReferralLauncherActivity.updateData(s.split(";"));
			}
		}
	}
	
	private static void sendMessage(int what, Object obj) {
		if (handler != null) {
			Message msg = Message.obtain();
			msg.what = what;
			msg.obj = obj;
			handler.sendMessage(msg);
		}
	}
	
	private static void sendRegisterCmd(final String phoneNumber) {
		AccountService.getInstance().initStatus(phoneNumber);
		AccountService.getInstance().initPassword(phoneNumber);
		AccountService.getInstance().setActionListener(new AnonymousNetworkListener() {

			@Override
			public void onActionDone(String action, ResponseData response) {
				// TODO Auto-generated method stub
				Log.d(TAG, "action: " + action);
				Log.d(TAG, "response: " + response);
				if (action.equals(BaseAnonymousLogic.Action_Verify) && !response.success) {
					
				}
			}

			@Override
			public void beforeActionDone(String action, ApiParams outParams) {
				// TODO Auto-generated method stub
				Log.d(TAG, "action: " + action);
				Log.d(TAG, "response: " + outParams);
				if (action.equals(BaseAnonymousLogic.Action_Register)) {
					outParams.addParam("password", phoneNumber);
				}
			}
			
		});
		AccountService.getInstance().start(BaseAnonymousLogic.Status_UnRegistered);
	}

}
