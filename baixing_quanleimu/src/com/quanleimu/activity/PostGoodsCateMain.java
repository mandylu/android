package com.quanleimu.activity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.quanleimu.activity.CateMain.AllCateThread;
import com.quanleimu.adapter.AllCatesAdapter;
import com.quanleimu.entity.AllCates;
import com.quanleimu.entity.FirstStepCate;
import com.quanleimu.entity.PostMu;
import com.quanleimu.entity.SaveFirstStepCate;
import com.quanleimu.jsonutil.JsonUtil;
import com.quanleimu.util.Communication;
import com.quanleimu.util.Util;

public class PostGoodsCateMain extends BaseActivity {

	// 定义控件
	public TextView tvTitle;
	public ListView lvAllCates;
	public ProgressDialog pd;
	public ImageView ivHomePage, ivCateMain, ivPostGoods, ivMyCenter,
			ivSetMain;

	// 定义变量
	public AllCatesAdapter adapter;
	public List<String> listAllCatesName = new ArrayList<String>();
	public List<FirstStepCate> listFirst = new ArrayList<FirstStepCate>();
	public AllCates allCates = new AllCates();
	public String json = "";

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.catemain);
		super.onCreate(savedInstanceState);

		// findViewById
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		lvAllCates = (ListView) findViewById(R.id.lvAllCates);

		ivHomePage = (ImageView) findViewById(R.id.ivHomePage);
		ivCateMain = (ImageView) findViewById(R.id.ivCateMain);
		ivPostGoods = (ImageView) findViewById(R.id.ivPostGoods);
		ivMyCenter = (ImageView) findViewById(R.id.ivMyCenter);
		ivSetMain = (ImageView) findViewById(R.id.ivSetMain);
		ivPostGoods.setImageResource(R.drawable.iv_postgoods_press);

		// 设置标题
		tvTitle.setText("选择类目");

		// 设置监听器
		ivHomePage.setOnClickListener(this);
		ivCateMain.setOnClickListener(this);
		ivPostGoods.setOnClickListener(this);
		ivMyCenter.setOnClickListener(this);
		ivSetMain.setOnClickListener(this);

		lvAllCates
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						intent.setClass(PostGoodsCateMain.this, 
								PostGoodsSecondCate.class);
						bundle.putString("firstCateName", listFirst.get(arg2)
								.getName());
						bundle.putInt("firstCatePos", arg2);
						bundle.putString("backPageName", "选择类目");
						intent.putExtras(bundle);
						startActivity(intent);
					}
				});
		PostMu postMu = (PostMu) Util.loadDataFromLocate(this,
				"savePostFirstStepCate");

		if (postMu != null && !postMu.getJson().equals("")) {
			json = postMu.getJson();
			long time = postMu.getTime();
			if (time + (24 * 3600 * 100) < System.currentTimeMillis()) {
				myHandler.sendEmptyMessage(1);
				new Thread(new AllCateThread(false)).start();
			} else {
				myHandler.sendEmptyMessage(1);
			}
		} else {
			pd = ProgressDialog.show(PostGoodsCateMain.this, "提示", "请稍候...");
			pd.setCancelable(true);
			new Thread(new AllCateThread(true)).start();
		}

	}

	@Override
	public void onClick(View v) {
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
			break;
		case R.id.ivMyCenter:
			intent.setClass(this, MyCenter.class);
			intent.putExtras(bundle);
			startActivity(intent);
			break;
		case R.id.ivSetMain:
			intent.setClass(this, SetMain.class);
			intent.putExtras(bundle);
			startActivity(intent);
			break;
		}
		super.onClick(v);
	}

	// 管理线程的Handler
	Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				if (pd != null) {
					pd.dismiss();
				}
				allCates = JsonUtil.getAllCatesFromJson(Communication
						.decodeUnicode(json));
				listFirst = allCates.getChildren();

				if (listFirst == null || listFirst.size() == 0) {
					Toast.makeText(PostGoodsCateMain.this, "未获取到类目数据", 3)
							.show();
				} else {
					myApp.setListFirst(listFirst);
					adapter = new AllCatesAdapter(PostGoodsCateMain.this,
							listFirst);
					lvAllCates.setAdapter(adapter);
				}
				break;
			case 2:
				if (pd != null) {
					pd.dismiss();
				}
				Toast.makeText(PostGoodsCateMain.this, "未获取到数据", 3).show();
				break;
			case 3:
				break;
			}
			super.handleMessage(msg);
		}
	};

	class AllCateThread implements Runnable {
		private boolean isUpdate;

		public AllCateThread(boolean isUpdate) {
			this.isUpdate = isUpdate;
		}

		@Override
		public void run() {
			String apiName = "category_list";
			ArrayList<String> list = new ArrayList<String>();
			String url = Communication.getApiUrl(apiName, list);
			try {
				json = Communication.getDataByUrl(url);

				if (json != null) {
					PostMu postMu = new PostMu();
					postMu.setJson(json);
					postMu.setTime(System.currentTimeMillis());
					Util.saveDataToLocate(PostGoodsCateMain.this,
							"savePostFirstStepCate", postMu);
					if (isUpdate) {
						myHandler.sendEmptyMessage(1);
					}
				} else {
					myHandler.sendEmptyMessage(2);
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				myHandler.sendEmptyMessage(10);
				e.printStackTrace();
			}

		}
	}
}
