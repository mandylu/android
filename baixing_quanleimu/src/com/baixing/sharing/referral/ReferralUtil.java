package com.baixing.sharing.referral;

import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	
	public static final String REFERRAL_STATUS = "REFERRAL_STATUS";
	public static final String PROMOTER_KEY = "com.baixing.sharing.referral.promoter";
	public static final String IS_PROMO_KEY = "com.baixing.sharing.referral.ispromoter";
	public static final String DLGSHOWN_KEY = "com.baixing.sharing.referral.dlgshown";
	public static final String CURPHONE_KEY = "com.baixing.sharing.referral.phone";
	public static final String ACTIVATE_KEY = "com.baixing.sharing.referral.activated";
	public static final String SHARETYPE_KEY = "com.baixing.sharing.referral.appsharetype";
	
	public static final int TASK_APP = 1;
	public static final int TASK_HAIBAO = 2;
	public static final int TASK_POST = 3;
	
	public static final int ROLE_NORMAL = 0x0;
	public static final int ROLE_PROMOTER = 0x1;
	public static final int ROLE_BUSINESS = 0x2;
	
	public static final int SHARE_BY_QRCODE = 1;
	public static final int SHARE_BY_HAIBAO = 2;
	public static final int SHARE_BY_BLUETOOTH = 3;

	public static ReferralUtil getInstance() {
		if (instance != null) {
			return instance;
		}
		instance = new ReferralUtil();
		return instance;
	}
	
	public void activating() {
		Log.d(TAG, "activating");
		ReferralNetwork.getInstance().savePromoLog(ReferralPromoter.getInstance().ID(), ReferralUtil.TASK_APP, null, null, null, Util.getDeviceUdid(GlobalDataManager.getInstance().getApplicationContext()), null, null);
	}
	
	public static boolean isValidQRCodeID(String codeId) {
		Pattern p = Pattern.compile("\\w{8}");
        Matcher matcher = p.matcher(codeId);
        return matcher.matches();
	}

	public static boolean isPromoter() {
		
		AccountManager am = GlobalDataManager.getInstance().getAccountManager();
		if (!am.isUserLogin()) {
			return false;
		}
		
		Context context = GlobalDataManager.getInstance().getApplicationContext();
		SharedPreferences preferences = context.getSharedPreferences(
				ReferralUtil.REFERRAL_STATUS, Context.MODE_PRIVATE);
		String mobile = am.getCurrentUser().getPhone();
		
		if (preferences.contains(ReferralUtil.CURPHONE_KEY) && preferences.getString(ReferralUtil.CURPHONE_KEY, "").equals(mobile)) {
			return preferences.getBoolean(ReferralUtil.IS_PROMO_KEY, false);
		}
		
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
								editor.putBoolean(ReferralUtil.IS_PROMO_KEY, true);
								editor.putString(ReferralUtil.CURPHONE_KEY, mobile);
								editor.commit();
								return true;
							} else {
								Editor editor = preferences.edit();
								editor.putBoolean(ReferralUtil.IS_PROMO_KEY, false);
								editor.putString(ReferralUtil.CURPHONE_KEY, mobile);
								editor.commit();
							}
						}
					}
				}
			}catch(JSONException e){
				e.printStackTrace();
				return false;
			}
		}
		
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
