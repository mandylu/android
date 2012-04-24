package com.quanleimu.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.quanleimu.entity.UserBean;
import com.quanleimu.util.Helper;
import com.quanleimu.util.Util;

public class SetMain extends BaseActivity {

	// 定义控件
	public TextView tvTitle, tvPhoneNum, personMark;
	public RelativeLayout rlMark, rlTelNum, rlClearCache, rlBack;
	public ImageView ivHomePage,ivCateMain,ivPostGoods,ivMyCenter,ivSetMain;
	
	public Dialog changePhoneDialog;
	private UserBean user;

	// 定义变量
 
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		myApp.setActivity_type("setmain");
		user = (UserBean) Util.loadDataFromLocate(SetMain.this, "user");
		if (user != null) {
			tvPhoneNum.setText(user.getPhone());
		}
		personMark.setText(myApp.getPersonMark());
		super.onResume();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.setmain);
		super.onCreate(savedInstanceState);
		String mark = (String) Helper.loadDataFromLocate(this, "personMark");
		myApp.setPersonMark(mark);
		// 设置标题
		rlMark = (RelativeLayout) findViewById(R.id.rlMark);
		rlTelNum = (RelativeLayout) findViewById(R.id.rlTelNum);
		rlClearCache = (RelativeLayout) findViewById(R.id.rlClearCache);
//		rlAbout = (RelativeLayout) findViewById(R.id.rlAbout);
		rlBack = (RelativeLayout) findViewById(R.id.rlBack);

		tvPhoneNum = (TextView) findViewById(R.id.tvPhoneNum);
		personMark = (TextView) findViewById(R.id.personMark);
		
		ivHomePage = (ImageView)findViewById(R.id.ivHomePage);
		ivCateMain = (ImageView)findViewById(R.id.ivCateMain);
		ivPostGoods = (ImageView)findViewById(R.id.ivPostGoods);
		ivMyCenter = (ImageView)findViewById(R.id.ivMyCenter);
		ivSetMain = (ImageView)findViewById(R.id.ivSetMain);
		ivSetMain.setImageResource(R.drawable.iv_setmain_press);
		
		rlTelNum.setOnClickListener(this);
		ivHomePage.setOnClickListener(this);
		ivCateMain.setOnClickListener(this);
		ivPostGoods.setOnClickListener(this);
		ivMyCenter.setOnClickListener(this);
		ivSetMain.setOnClickListener(this);
		rlClearCache.setOnClickListener(this);
//		rlAbout.setOnClickListener(this);
		rlBack.setOnClickListener(this);
		rlMark.setOnClickListener(this);
		

	}

	@Override
	public void onClick(View v) {
		// 手机号码
		if (v.getId() == rlTelNum.getId()) {
			if (tvPhoneNum.getText().equals("")) {
				// 跳转登录界面
				bundle.putString("back", "设置");
				intent.putExtras(bundle);
				intent.setClass(SetMain.this, Login.class);
				intent.putExtras(bundle);
				startActivityForResult(intent, 1);
			} else {
				// 修改对话框

				LayoutInflater inflater = getLayoutInflater();
				View linearlayout = inflater.inflate(
						R.layout.changephonedialog, null);
				TextView tvTelNum = (TextView) linearlayout
						.findViewById(R.id.tvTelNum);
				tvTelNum.setText("您已经绑定" + tvPhoneNum.getText().toString()
						+ ",确定要修改吗？");
				Button btnChange = (Button) linearlayout
						.findViewById(R.id.btnChange);
				Button btnCancel = (Button) linearlayout
						.findViewById(R.id.btnCancel);

				btnChange.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// 点击换号码，重新跳转到登录
						changePhoneDialog.dismiss();
						bundle.putString("back", "设置");
						intent.putExtras(bundle);
						intent.setClass(SetMain.this, Login.class);
						intent.putExtras(bundle);
						startActivity(intent);
					}
				});

				btnCancel.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						changePhoneDialog.dismiss();
					}
				});
				changePhoneDialog = new AlertDialog.Builder(this).setView(
						linearlayout).create();
				changePhoneDialog.show();
			}
		}

		// 签名档
		if (v.getId() == rlMark.getId()) {
			intent.setClass(SetMain.this, MarkLable.class);
			intent.putExtras(bundle);
			startActivity(intent);
		}

		// 清空缓存
		if (v.getId() == rlClearCache.getId()) {

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("提示:")
					.setMessage("是否清空缓存？")
					.setNegativeButton("否", null)
					.setPositiveButton("是",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									String[] files = fileList();
									for(int i=0;i<files.length;i++){
										String file_path = files[i];
										deleteFile(file_path);
									}
									//清空手机号码
									tvPhoneNum.setText("");
									//清空签名档
									personMark.setText("");
								}
							});
			builder.create().show();
		}

		// 关于
//		if (v.getId() == rlAbout.getId()) {
//			intent.setClass(SetMain.this, AboutUs.class);
//			intent.putExtras(bundle);
//			startActivity(intent);
//		}

		// 反馈
		if (v.getId() == rlBack.getId()) {
			intent.setClass(SetMain.this, OpinionBack.class);
			intent.putExtras(bundle);
			startActivity(intent);
		}
		switch (v.getId()) {
		case R.id.ivHomePage:
			intent.setClass(this, HomePage.class);
			intent.putExtras(bundle);
			startActivity(intent);
			break;
		case R.id.ivCateMain:
			intent.setClass(this, CateMain.class);
			intent.putExtras(bundle);
			startActivity(intent);
			break;
		case R.id.ivPostGoods:
			intent.setClass(this, PostGoodsCateMain.class);
			intent.putExtras(bundle);
			startActivity(intent);
			break;
		case R.id.ivMyCenter:
			intent.setClass(this, MyCenter.class);
			intent.putExtras(bundle);
			startActivity(intent);
			break;
		case R.id.ivSetMain:
			break;
		}
		super.onClick(v);
	}

}
