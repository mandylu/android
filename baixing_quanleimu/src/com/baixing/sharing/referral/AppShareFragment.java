package com.baixing.sharing.referral;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.baixing.activity.BaseFragment;
import com.baixing.data.GlobalDataManager;
import com.baixing.entity.UserBean;
import com.baixing.message.BxMessageCenter;
import com.baixing.message.IBxNotificationNames;
import com.baixing.util.Util;
import com.baixing.widget.EditUsernameDialogFragment.ICallback;
import com.quanleimu.activity.R;

public class AppShareFragment extends BaseFragment implements
		View.OnClickListener, ICallback, Observer {

	private static Context context;
	private static String promoterId;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		BxMessageCenter.defaultMessageCenter().registerObserver(this,
				IBxNotificationNames.NOTIFICATION_LOGOUT);

		context = GlobalDataManager.getInstance().getApplicationContext();
		UserBean curUser = GlobalDataManager.getInstance().getAccountManager()
				.getCurrentUser();
		if (curUser != null && !TextUtils.isEmpty(curUser.getPhone())) {
			promoterId = curUser.getPhone();
		} else {
			promoterId = Util.getDeviceUdid(context);
		}
	}

	@Override
	public void initTitle(TitleDef title) {
		title.m_visible = true;
		title.m_title = getString(R.string.title_referral_promote);
		title.m_leftActionHint = "完成";
	}

	@Override
	public boolean hasGlobalTab() {
		return true;
	}

	@Override
	public View onInitializeView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View referralmain = inflater.inflate(R.layout.referral_share, null);

		String qrCodeContent = ReferralNetwork.PROMOTE_URL
				+ "?action=download&udid=" + promoterId;
		((ImageView) referralmain.findViewById(R.id.img_referral_qrcode))
				.setImageBitmap(ReferralUtil.getQRCodeBitmap(GlobalDataManager
						.getInstance().getApplicationContext(), qrCodeContent));
		((Button) referralmain.findViewById(R.id.btn_referral_bluetooth))
				.setOnClickListener(new BtnBluetoothOnClickListener());

		return referralmain;
	}

	class BtnBluetoothOnClickListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			String version = Util.getVersion(context);
			if (TextUtils.isEmpty(version)) {
				version = "";
			} else {
				version = version + "-";
			}

			String apkName = Environment.getExternalStorageDirectory()
					.getPath()
					+ "/baixing-"
					+ version
					+ promoterId + ".apk";

			// if (!new File(apkName).exists()) {
			PackageManager pm = context.getPackageManager();
			List<ApplicationInfo> appinfo_list = pm.getInstalledApplications(0);
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
						FileOutputStream fos = new FileOutputStream(new File(
								apkName));
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
					Toast.makeText(context, "写SD卡失败", Toast.LENGTH_SHORT)
							.show();
				}
			} else {
				Log.e(TAG, "No apk found");
				Toast.makeText(context, "找不到百姓网安装包", Toast.LENGTH_SHORT).show();
				return;
			}
			// }

			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_SEND);
			intent.setType("*/*");
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra(Intent.EXTRA_STREAM,
					Uri.fromFile(new File(apkName)));
			try {
				intent.setClassName("com.android.bluetooth",
						"com.android.bluetooth.opp.BluetoothOppLauncherActivity");
				startActivity(intent);
			} catch (ActivityNotFoundException ex) {
				intent.setClassName("com.mediatek.bluetooth",
						"com.mediatek.bluetooth.BluetoothShareGatewayActivity");
				startActivity(intent);
			}
		}

	}

	@Override
	public void update(Observable observable, Object data) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onEditSucced(String newUserName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}
}
