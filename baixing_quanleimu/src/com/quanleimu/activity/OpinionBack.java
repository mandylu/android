package com.quanleimu.activity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.quanleimu.entity.UserBean;
import com.quanleimu.util.Communication;
import com.quanleimu.util.Helper;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Region.Op;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class OpinionBack extends BaseActivity {

	private Intent intent = null;
	private Bundle bundle = null;
	private Button backBtn;
	private Button btnFinish;
	private EditText etOpinion;
	private String content = "";
	private String phoneMark = "";
	private String mobile = "";
	private UserBean user;
	private String result;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.opinionback);
		super.onCreate(savedInstanceState);

		user = (UserBean) Helper.loadDataFromLocate(this, "user");
		if (user != null) {
			mobile = user.getPhone();
		}

		// 手机管理器
		TelephonyManager tm = (TelephonyManager) this
				.getSystemService(TELEPHONY_SERVICE);
		phoneMark = tm.getDeviceId();

		etOpinion = (EditText) findViewById(R.id.etOpinion);
		etOpinion.findFocus();
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

		backBtn = (Button) findViewById(R.id.backBtn);
		backBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				OpinionBack.this.finish();
			}
		});

		btnFinish = (Button) findViewById(R.id.btnFinish);
		btnFinish.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				content = etOpinion.getText().toString();
				if (content.equals("")) {
					Toast.makeText(OpinionBack.this, "内容不能为空",
							Toast.LENGTH_SHORT).show();
				} else {
					
					pd = ProgressDialog.show(OpinionBack.this, "提示", "请稍候...");
					pd.setCancelable(true);
					new Thread(new OpinionBackThread()).start();
				}
			}
		});

	}

	Handler myHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if(pd!=null){
				pd.dismiss();
			}
			switch (msg.what) {
			case 0:
				
				OpinionBack.this.finish();
				Toast.makeText(OpinionBack.this, "提交成功！", Toast.LENGTH_SHORT)
						.show();
				break;
			case 1:
				Toast.makeText(OpinionBack.this, "提交失败！", Toast.LENGTH_SHORT)
				.show();
				
				break;
			}
			super.handleMessage(msg);
		}

	}; 

	class OpinionBackThread implements Runnable {
		@Override
		public void run() {
			// String url =
			// "http://www.baixing.com/iphone/feedback/v1/?device=android";
			// url = url + "&content="+URLEncoder.encode(content)
			// +"&androidUniqueIdentifier="+phoneMark+"&mobile="+mobile;
			String apiName = "feedback";
			ArrayList<String> list = new ArrayList<String>();

			list.add("mobile=" + mobile);
			list.add("feedback="
					+ URLEncoder.encode(etOpinion.getText().toString()));

			String url = Communication.getApiUrl(apiName, list);
			System.out.println("url--->" + url);
			try {
				result = Communication.getDataByUrl(url);
				if (result != null) {
					myHandler.sendEmptyMessage(0);
				} else {
					myHandler.sendEmptyMessage(1);
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
}
