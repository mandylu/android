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
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;

import com.baixing.anonymous.AccountService;
import com.baixing.anonymous.AnonymousNetworkListener;
import com.baixing.anonymous.BaseAnonymousLogic;
import com.baixing.data.GlobalDataManager;
import com.baixing.entity.UserBean;
import com.baixing.network.api.ApiParams;
import com.baixing.util.Util;
import com.baixing.util.post.PostNetworkService;
import com.baixing.widget.VerifyFailDialog;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

public class ReferralUtil {

	private static final String TAG = ReferralUtil.class.getSimpleName();

	private static final String PROMOTE_URL = "http://192.168.5.109/baixing/lunchnow/promote.php";
	private static final String PROMOTER_ID = "PROMOTER_ID";
	private static final String PROMOTER_KEY = "com.baixing.sharing.referral.promoter";
	
	private static Handler handler;
	private static FragmentManager fragmentManager;
	private static PostNetworkService postNetworkService;
	private static String verifyCode; 

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
	
	public static void setFragmentManager(FragmentManager fragmentManager) {
		ReferralUtil.fragmentManager = fragmentManager;
	}
	
	public static void setPostNetworkService(PostNetworkService postNS) {
		ReferralUtil.postNetworkService = postNS;
	}
	
	public static Bitmap getQRCodeBitmap(Context context) {
		int size = Util.getWidthByContext(context) / 2;
		try {
			return encodeAsBitmap(PROMOTE_URL + "?action=download&udid=" + Util.getDeviceUdid(context), BarcodeFormat.QR_CODE, size, size);
		} catch (WriterException e) {
			e.printStackTrace();
			return null;
		}
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
		//sendMessage(PostCommonValues.MSG_VERIFY_FAIL, phoneNumber);
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
				//ReferralFragment.updateRecords(s.split(";"));
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
				if (response.success) {
					if (action.equals(BaseAnonymousLogic.Action_Register)) {
						showVerifyDlg();
					} else if (action.equals(BaseAnonymousLogic.Action_Verify)) {
						UserBean loginBean = new UserBean();
						loginBean.setPhone(phoneNumber);
						loginBean.setPassword("test1234", true);
						Util.saveDataToLocate(GlobalDataManager.getInstance().getApplicationContext(), "user", loginBean);
						postNetworkService.doRegisterAndVerify(phoneNumber);
					}
				} else {
					if (action.equals(BaseAnonymousLogic.Action_Verify)) {
						showVerifyDlg();
					} else if (action.equals(BaseAnonymousLogic.Action_Register)) {
						showVerifyDlg();
					}
				}
			}

			@Override
			public void beforeActionDone(String action, ApiParams outParams) {
				// TODO Auto-generated method stub
				Log.d(TAG, "action: " + action);
				Log.d(TAG, "response: " + outParams);
				if (action.equals(BaseAnonymousLogic.Action_Register)) {
					outParams.addParam("password", "test1234");
				} else if (action.equals(BaseAnonymousLogic.Action_Verify) && verifyCode != null) {
					outParams.addParam("verifyCode", verifyCode);
					verifyCode = null;
				}
			}
			
		});
		AccountService.getInstance().start(BaseAnonymousLogic.Status_UnRegistered);
	}

	private static void showVerifyDlg(){

		if(fragmentManager != null){
			new VerifyFailDialog(new VerifyFailDialog.VerifyListener() {
				
				@Override
				public void onReVerify(String mobile) {
					AccountService.getInstance().start();
				}

				@Override
				public void onSendVerifyCode(String code) {
					// TODO Auto-generated method stub
					verifyCode = code;
					AccountService.getInstance().start(BaseAnonymousLogic.Status_Registered_UnVerified, BaseAnonymousLogic.Status_CodeReceived);						
				}
			}).show(fragmentManager, null);
		}
	}
	
	static Bitmap encodeAsBitmap(String contents, BarcodeFormat format,
			int desiredWidth, int desiredHeight) throws WriterException {
		Hashtable<EncodeHintType, String> hints = null;
		String encoding = guessAppropriateEncoding(contents);
		if (encoding != null) {
			hints = new Hashtable<EncodeHintType, String>(2);
			hints.put(EncodeHintType.CHARACTER_SET, encoding);
		}
		MultiFormatWriter writer = new MultiFormatWriter();
		BitMatrix result = writer.encode(contents, format, desiredWidth,
				desiredHeight, hints);
		int width = result.getWidth();
		int height = result.getHeight();
		int[] pixels = new int[width * height];
		// All are 0, or black, by default
		for (int y = 0; y < height; y++) {
			int offset = y * width;
			for (int x = 0; x < width; x++) {
				pixels[offset + x] = result.get(x, y) ? Color.BLACK : Color.WHITE;
			}
		}
		
		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}
	
	private static String guessAppropriateEncoding(CharSequence contents) {
		// Very crude at the moment
		for (int i = 0; i < contents.length(); i++) {
			if (contents.charAt(i) > 0xFF) {
				return "UTF-8";
			}
		}
		return null;
	}

}
