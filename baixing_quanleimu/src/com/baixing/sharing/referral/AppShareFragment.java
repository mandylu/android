package com.baixing.sharing.referral;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baixing.activity.BaseFragment;
import com.baixing.data.GlobalDataManager;
import com.baixing.entity.UserBean;
import com.baixing.network.api.ApiParams;
import com.baixing.network.api.BaseApiCommand;
import com.baixing.util.Util;
import com.baixing.view.fragment.LoginFragment;
import com.baixing.widget.EditUsernameDialogFragment.ICallback;
import com.quanleimu.activity.R;

public class AppShareFragment extends BaseFragment implements
		View.OnClickListener, ICallback, Observer {
	
	private String APP_DOWN_BASE = "http://pages.baixing.com/mobile/dituishenqi/?udid=";

	private Context context;
	private String promoterId;
	
	private ImageView qrcodeImageView;
	private TextView txtLoginShare;
	private Button bluetoothButton;
	private Button appDetailButton;
	
	private boolean isPromo = false;
	private Spanned promoInfo = null;
	private String promoUrl = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
		context = GlobalDataManager.getInstance().getApplicationContext();
		
		txtLoginShare = (TextView) referralmain.findViewById(R.id.txt_login_to_share);
		int size = Util.getWidthByContext(context) * 2 / 3;
		txtLoginShare.setWidth(size);
		txtLoginShare.setHeight(size);
		txtLoginShare.setBackgroundColor(Color.GRAY);
		txtLoginShare.getBackground().setAlpha(208);
		txtLoginShare.getPaint().setFakeBoldText(true);
		txtLoginShare.setClickable(true);
		txtLoginShare.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				pushFragment(new LoginFragment(), createArguments("登录", ""));
			}
		});
		
		appDetailButton = (Button) referralmain.findViewById(R.id.btn_referral_share_detail);
		appDetailButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Bundle bundle=new Bundle();
				bundle.putString("title", getString(R.string.button_referral_detail_app));
				bundle.putString("url", ReferralDetailFragment.SHARE_DETIAL_URL + "?mobile=" + promoterId);
				pushFragment(new ReferralDetailFragment(), bundle);	
			}
		});

		TextView textView = (TextView) referralmain.findViewById(R.id.txt_app_share_tips);
		isPromo = getPromoInfo();
		if (isPromo) {
			textView.setText(promoInfo);
			textView.setVisibility(View.VISIBLE);
			textView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Bundle bundle=new Bundle();
					bundle.putString("title", getString(R.string.title_referral_intro));
					bundle.putString("url", promoUrl);
					pushFragment(new ReferralDetailFragment(), bundle);	
				}
			});
			txtLoginShare.setVisibility(View.VISIBLE);
			appDetailButton.setVisibility(View.VISIBLE);
		} else {
			textView.setVisibility(View.GONE);
			txtLoginShare.setVisibility(View.GONE);
			appDetailButton.setVisibility(View.GONE);
		}
		
		qrcodeImageView = (ImageView) referralmain.findViewById(R.id.img_referral_qrcode);
		UserBean curUser = GlobalDataManager.getInstance().getAccountManager()
				.getCurrentUser();
		if (curUser != null && Util.isValidMobile(curUser.getPhone())) {
			promoterId = curUser.getPhone();
		} else {
			promoterId = Util.getDeviceUdid(context);
		}
		String qrCodeContent = APP_DOWN_BASE + promoterId;
		qrcodeImageView
				.setImageBitmap(ReferralUtil.getQRCodeBitmap(GlobalDataManager
						.getInstance().getApplicationContext(), qrCodeContent));
		
		bluetoothButton = (Button) referralmain.findViewById(R.id.btn_referral_bluetooth);		
		
		return referralmain;
	}

	@Override
	public void onResume() {
		super.onResume();
		
		loginToDisplayInfo();
	}
	
	private void loginToDisplayInfo() {
		
		if (!GlobalDataManager.getInstance().getAccountManager().isUserLogin()) {
			bluetoothButton.setEnabled(false);
			bluetoothButton.setBackgroundResource(R.drawable.btn_sms_on);
			appDetailButton.setEnabled(false);
			appDetailButton.setBackgroundResource(R.drawable.btn_sms_on);
			txtLoginShare.setVisibility(View.VISIBLE);
			return;
		}
		
		txtLoginShare.setVisibility(View.GONE);
		bluetoothButton.setEnabled(true);
		appDetailButton.setEnabled(true);
		//lumengdi@baixing.net
		bluetoothButton.setBackgroundResource(R.drawable.post_finish_btn);
		appDetailButton.setBackgroundResource(R.drawable.post_finish_btn);
		
		UserBean curUser = GlobalDataManager.getInstance().getAccountManager()
				.getCurrentUser();
		if (curUser != null && Util.isValidMobile(curUser.getPhone())) {
			promoterId = curUser.getPhone();
		} else {
			promoterId = Util.getDeviceUdid(context);
		}
		String qrCodeContent = APP_DOWN_BASE + promoterId;
		qrcodeImageView
				.setImageBitmap(ReferralUtil.getQRCodeBitmap(GlobalDataManager
						.getInstance().getApplicationContext(), qrCodeContent));
		bluetoothButton
				.setOnClickListener(new BtnBluetoothOnClickListener());
	}

	private boolean getPromoInfo() {
		
		GlobalDataManager gdm = GlobalDataManager.getInstance();
		
		ApiParams params = new ApiParams();
		params.addParam("city", gdm.getCityEnglishName());
		params.addParam("udid", Util.getDeviceUdid(context));
		params.addParam("userId", gdm.getAccountManager().isUserLogin() ? gdm.getAccountManager().getCurrentUser().getId() : null);
		params.addParam("version", gdm.getVersion());
		
		String jsonResponse = BaseApiCommand.createCommand("promo_compaign", false, params).executeSync(context);
		try {
			JSONObject obj = new JSONObject(jsonResponse);
			if (obj != null) {
				JSONObject error = obj.getJSONObject("error");
				if (error != null) {
					String code = error.getString("code");
					if (code != null && code.equals("0")) {
						promoInfo = Html.fromHtml("<u>" + obj.getString("content") + "</u>");
						promoUrl = obj.getString("url");
						return true;
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
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
