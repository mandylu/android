package com.baixing.sharing.referral;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.List;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.baixing.data.GlobalDataManager;
import com.baixing.util.Util;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.quanleimu.activity.R;

public class ReferralLauncherActivity extends Activity {
	
	private static final String TAG = ReferralLauncherActivity.class.getSimpleName();
	
	private static final String PROMOTE_URL = "http://192.168.5.109/baixing/lunchnow/promote.php";
	
	ImageView imgQRCode;
	Button btnBluetooth;
	static ListView listInfo;
	static ReferralAdapter listAdapter;
	static Context context;
	
	public static final int MSG_JOIN = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.referral_launcher);
		
		context = this;
		
		Intent intent = getIntent();
		final String packageName = (intent.getStringExtra("packageName") == null) ? getApplicationContext().getPackageName() : intent.getStringExtra("packageName");
		Log.d(TAG, "packageName: " + packageName);
		
		final Context context = GlobalDataManager.getInstance().getApplicationContext();
		int size = Util.getWidthByContext(context) / 2;
		
		imgQRCode = (ImageView) findViewById(R.id.img_qrcode);
		try {
			imgQRCode.setImageBitmap(encodeAsBitmap(PROMOTE_URL + "?action=download&udid=" + Util.getDeviceUdid(context), BarcodeFormat.QR_CODE, size, size));
		} catch (WriterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		btnBluetooth = (Button) findViewById(R.id.btn_bluetooth);
		btnBluetooth.setWidth(size);
		btnBluetooth.setHeight(size);
		btnBluetooth.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				String version = Util.getVersion(getApplicationContext());
				if (TextUtils.isEmpty(version)) {
					version = "";
				} else {
					version = version + "-";
				}
				
				String apkName = Environment.getExternalStorageDirectory().getPath() + "/baixing-" + version + Util.getDeviceUdid(context) + ".apk";
				
				//if (!new File(apkName).exists()) {
					PackageManager pm = getApplicationContext().getPackageManager();
					List<ApplicationInfo> appinfo_list = pm
							.getInstalledApplications(0);
					String originPath = null;
					for (int x = 0; x < appinfo_list.size(); x++) {
						if (appinfo_list.get(x).publicSourceDir
								.contains("com.quanleimu.activity")) {
							originPath = appinfo_list.get(x).publicSourceDir;
							Log.d(TAG, "originPath: " + originPath);
						}
					}
					
					if (originPath != null) {
						try {
							InputStream is = new FileInputStream(new File(originPath));
							int length = is.available();
							Log.d(TAG, "is.available: " + length);
							if (length > 0) {
								FileOutputStream fos = new FileOutputStream(
										new File(apkName));
								byte[] buffer = new byte[length];
								while (true) {
									length = is.read(buffer, 0, length);
									if (length == -1) {
										break;
									}
									fos.write(buffer, 0, length);
								}
								fos.close();
							}
							is.close();
						} catch (IOException e) {
							Log.e(TAG, e.getMessage());
							e.printStackTrace();
						}
					} else {
						Log.e(TAG, "No apk found");
					}
				//}
				
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_SEND);
			    intent.setType("*/*");
			    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			    intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(apkName)));
				try {
					intent.setClassName("com.android.bluetooth", "com.android.bluetooth.opp.BluetoothOppLauncherActivity");
					startActivity(intent);
				} catch (ActivityNotFoundException ex) {
					intent.setClassName("com.mediatek.bluetooth", "com.mediatek.bluetooth.BluetoothShareGatewayActivity");
					startActivity(intent);
				}
			}
		});
		
		listInfo = (ListView) findViewById(R.id.list_info);
		listAdapter = null;
		new ReferralUtil().updateReferral("info");
	}
	
	public static void updateData(String[] list) {
		if (listAdapter == null) {
			listAdapter = new ReferralAdapter(context, list);
			listInfo.setAdapter(listAdapter);
		} else {
			listAdapter.refresh(list);
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
