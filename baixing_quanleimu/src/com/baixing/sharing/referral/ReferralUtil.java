package com.baixing.sharing.referral;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.baixing.data.AccountManager;
import com.baixing.data.GlobalDataManager;
import com.baixing.util.Util;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

public class ReferralUtil {

	private static final String TAG = ReferralUtil.class.getSimpleName();

	private static final String PROMOTE_URL = "http://192.168.5.109/baixing/lunchnow/promote.php";
	
	private static ReferralUtil instance = null;

	public static ReferralUtil getInstance() {
		if (instance != null) {
			return instance;
		}
		instance = new ReferralUtil();
		return instance;
	}
	
	public void activating() {
		Log.d(TAG, "activated");
		new ReferralAutoLogin(GlobalDataManager.getInstance().getApplicationContext()).execute();
		if (!TextUtils.isEmpty(ReferralPromoter.getInstance().ID())) {
			updateReferral("join");
			//new ReferralAutoLogin(GlobalDataManager.getInstance().getApplicationContext()).execute();
		}
	}

	public static boolean isPromoter() {
		AccountManager am = GlobalDataManager.getInstance().getAccountManager(); 
		if (am.isUserLogin() && am.getCurrentUser().getPhone().equals("13661812580")) {
			return true;
		}
		return false;
	}
	
	public static Bitmap getQRCodeBitmap(Context context) {
		int size = Util.getWidthByContext(context) * 2 / 3;
		try {
			return encodeAsBitmap(PROMOTE_URL + "?action=download&udid=" + Util.getDeviceUdid(context), BarcodeFormat.QR_CODE, size, size);
		} catch (WriterException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void updateReferral(String action) {

		Context context = GlobalDataManager.getInstance()
				.getApplicationContext();

		String url = PROMOTE_URL + "?action=" + action;

		if (action.equals("join") || action.equals("post")) {
			url += "&promoter=" + ReferralPromoter.getInstance().ID() + "&receiver="
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
				//ReferralFragment.updateRecords(s.split(";"));
			}
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
