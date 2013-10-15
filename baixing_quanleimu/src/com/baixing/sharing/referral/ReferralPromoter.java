package com.baixing.sharing.referral;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.baixing.data.GlobalDataManager;
import com.baixing.network.api.ApiParams;
import com.baixing.network.api.BaseApiCommand;
import com.baixing.util.Util;

public class ReferralPromoter {
	
	private static final String TAG = ReferralPromoter.class.getSimpleName();
	
	private static ReferralPromoter instance = null;
	
	private static final String PROMOTER_ID = "PROMOTER_ID";
	
	public static ReferralPromoter getInstance() {
		if (instance != null) {
			return instance;
		}
		instance = new ReferralPromoter();
		return instance;
	}

	public String ID() {

		Context context = GlobalDataManager.getInstance()
				.getApplicationContext();

		String promoter_id = getPromoterId(context);
		if (promoter_id != null) {
			return promoter_id;
		}

		promoter_id = getPromoterIdByAssets(context);
		if (!TextUtils.isEmpty(promoter_id)) {
			if (Util.isValidMobile(promoter_id)) {
				saveAppShareType(ReferralUtil.SHARE_BY_QRCODE, context);
			} else if (ReferralUtil.isValidQRCodeID(promoter_id)) {
				saveAppShareType(ReferralUtil.SHARE_BY_HAIBAO, context);
			}
			return savePromoterId(context, promoter_id);
		}
		
		promoter_id = getPromoterIdByBluetooth(context);
		if (!TextUtils.isEmpty(promoter_id)) {
			saveAppShareType(ReferralUtil.SHARE_BY_BLUETOOTH, context);
			return savePromoterId(context, promoter_id);
		}

		return savePromoterId(context, "");
	}
	
	private void saveAppShareType(int shareType, Context context) {
		SharedPreferences preferences = context.getSharedPreferences(
				ReferralUtil.REFERRAL_STATUS, Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putInt(ReferralUtil.SHARETYPE_KEY, shareType);
		editor.commit();
	}
	
	private String getPromoterId(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(
				ReferralUtil.REFERRAL_STATUS, Context.MODE_PRIVATE);
		if (preferences.contains(ReferralUtil.PROMOTER_KEY)) {
			return preferences.getString(ReferralUtil.PROMOTER_KEY, "");
		}
		return null;
	}

	private String getPromoterIdByBluetooth(Context context) {
		File[] bluetooth = Environment.getExternalStorageDirectory().listFiles(
				new FileFilter() {
					@Override
					public boolean accept(File pathname) {
						return pathname.isDirectory()
								&& pathname.getName().equalsIgnoreCase(
										"bluetooth");
					}
				});
		if (bluetooth == null || bluetooth.length == 0) {
			bluetooth = getBluetoothDirInHTC();
		}
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
							long d1 = lhs.lastModified();
							long d2 = rhs.lastModified();
							if (d1 == d2) {
								return 0;
							} else {
								return d1 < d2 ? 1 : -1;
							}
						}
					});
				}
				Log.d(TAG, files[0].getName());
				Pattern phonePattern = Pattern.compile("(1(3|4|5|8))\\d{9}");
				Matcher phoneMatcher = phonePattern.matcher(files[0].getName());
				if (phoneMatcher.find()) {
					return phoneMatcher.group();
				}
			} else {
				Log.e(TAG, "No apk found");
			}
		} else {
			Log.e(TAG, "No Bluetooth Dir");
		}
		return null;
	}
	
	private File[] getBluetoothDirInHTC() {
		File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		for (File file : downloads.listFiles()) {
			if (file.isDirectory() && !file.isHidden()) {
				if (file.getName().equalsIgnoreCase("bluetooth")) {
					return new File[]{file};
				}
			}
		}
		return null;
	}

	private String getPromoterIdByAssets(Context context) {
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

	private String savePromoterId(Context context, String mPromoterID) {
		String parentPhone = mPromoterID;
		
		if (Util.isValidMobile(mPromoterID)) {
			parentPhone = mPromoterID;
		} else if (ReferralUtil.isValidQRCodeID(mPromoterID)) {
			ApiParams params = new ApiParams();
			params.addParam("qrcodeId", mPromoterID);
			String response = BaseApiCommand.createCommand("get_bound_mobile", false, params).executeSync(GlobalDataManager.getInstance().getApplicationContext());
			try {
				JSONObject obj = new JSONObject(response);
				if (obj != null) {
					JSONObject error = obj.getJSONObject("error");
					if (error != null) {
						String code = error.getString("code");
						if (code != null && code.equals("0")) {
							parentPhone = obj.getString("storeMobile");
						}
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		SharedPreferences preferences = context.getSharedPreferences(
				ReferralUtil.REFERRAL_STATUS, Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putString(ReferralUtil.PROMOTER_KEY, parentPhone);
		editor.commit();
		
		return parentPhone;
	}
}
