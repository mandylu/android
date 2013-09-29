package com.baixing.sharing.referral;

import java.util.Hashtable;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;

import com.baixing.data.AccountManager;
import com.baixing.data.GlobalDataManager;
import com.baixing.network.api.ApiParams;
import com.baixing.network.api.BaseApiCommand;
import com.baixing.util.Util;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

public class ReferralUtil {

	private static final String TAG = ReferralUtil.class.getSimpleName();
	
	private static ReferralUtil instance = null;
	
	private static final String IS_PROMOTER = "IS_PROMOTER";
	private static final String IS_PROMO_KEY = "com.baixing.sharing.referral.ispromoter";
	
	public static final int TASK_APP = 1;
	public static final int TASK_HAIBAO = 2;
	public static final int TASK_POST = 3;
	
	public static final int ROLE_NORMAL = 0x0;
	public static final int ROLE_PROMOTER = 0x1;
	public static final int ROLE_BUSINESS = 0x2;

	public static ReferralUtil getInstance() {
		if (instance != null) {
			return instance;
		}
		instance = new ReferralUtil();
		return instance;
	}
	
	public void activating() {
		Log.d(TAG, "activating");
		//new ReferralAutoLogin(GlobalDataManager.getInstance().getApplicationContext()).execute();
		if (!TextUtils.isEmpty(ReferralPromoter.getInstance().ID())) {
			ReferralNetwork.getInstance().updateReferral("join", null);
		}
	}

	public static boolean isPromoter() {
		
		Context context = GlobalDataManager.getInstance().getApplicationContext();
		SharedPreferences preferences = context.getSharedPreferences(
				IS_PROMOTER, Context.MODE_PRIVATE);
		if (preferences.contains(IS_PROMO_KEY)) {
			return preferences.getBoolean(IS_PROMO_KEY, false);
		}
		
		AccountManager am = GlobalDataManager.getInstance().getAccountManager();
		if (am.isUserLogin()) {
			String mobile = am.getCurrentUser().getPhone();
			if (!TextUtils.isEmpty(mobile) && Util.isValidMobile(mobile)) {
				ApiParams params = new ApiParams();
				params.addParam("mobile", mobile);
				String jsonResult = BaseApiCommand.createCommand("get_promo_user_type", true, params).executeSync(GlobalDataManager.getInstance().getApplicationContext());
				try{
					JSONObject obj = new JSONObject(jsonResult);
					if(obj != null){
						JSONObject error = obj.getJSONObject("error");
						if(error != null){
							String code = error.getString("code");
							if(code != null && code.equals("0")){
								if ((obj.getInt("type") & ROLE_PROMOTER) == ROLE_PROMOTER) {
									Editor editor = preferences.edit();
									editor.putBoolean(IS_PROMO_KEY, true);
									editor.commit();
									return true;
								}
							}
						}
					}
				}catch(JSONException e){
					e.printStackTrace();
					Editor editor = preferences.edit();
					editor.putBoolean(IS_PROMO_KEY, false);
					editor.commit();
					return false;
				}
			}
		}
		Editor editor = preferences.edit();
		editor.putBoolean(IS_PROMO_KEY, false);
		editor.commit();
		return false;
	}
	
	public static Bitmap getQRCodeBitmap(Context context, String content) {
		int size = Util.getWidthByContext(context) * 2 / 3;
		try {
			return encodeAsBitmap(content, BarcodeFormat.QR_CODE, size, size);
		} catch (WriterException e) {
			e.printStackTrace();
			return null;
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
