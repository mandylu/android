package com.baixing.sharing.referral;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.baixing.data.GlobalDataManager;

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

	private void savePromoterId(Context context, String mPromoterID) {
		SharedPreferences preferences = context.getSharedPreferences(
				ReferralUtil.REFERRAL_STATUS, Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putString(ReferralUtil.PROMOTER_KEY, mPromoterID);
		editor.commit();
	}
	
}
