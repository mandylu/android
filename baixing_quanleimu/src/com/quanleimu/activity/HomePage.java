package com.quanleimu.activity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mobclick.android.MobclickAgent;
import com.quanleimu.entity.GoodsInfo;
import com.quanleimu.entity.HotList;
import com.quanleimu.entity.SecondStepCate;
import com.quanleimu.imageCache.SimpleImageLoader;
import com.quanleimu.jsonutil.JsonUtil;
import com.quanleimu.jsonutil.LocateJsonData;
import com.quanleimu.util.Communication;
import com.quanleimu.util.Helper;
import com.quanleimu.util.Util;

public class HomePage extends BaseActivity {

	// 定义控件名
	public TextView tvTitle, tvInfo;
	public ListView lvUsualCate;
	public Button btnSearch, btnChangeCity;
	public LinearLayout llgl, linearUseualCates;
	public ImageView ivHomePage, ivCateMain, ivPostGoods, ivMyCenter,
			ivSetMain;
	public Gallery glDetail;
	public ProgressDialog pd;
	public TextView tvSayHi2User;
	public RelativeLayout rlUserName;

	// 定义变量名
	public List<GoodsInfo> list = new ArrayList<GoodsInfo>();
	public String cityName = "";
	public HotList hotList = new HotList();
	public List<HotList> listHot = new ArrayList<HotList>();
	public String json = "";
	public List<ImageView> listImage = new ArrayList<ImageView>();
	public List<Bitmap> listBm = new ArrayList<Bitmap>();
	public List<String> listFileNames = new ArrayList<String>();
	public HotListAdapter adapter;
	public List<SecondStepCate> listUsualCates = new ArrayList<SecondStepCate>();

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onResume() {
		bundle.putString("backPageName", "");
		if (MyApplication.listUsualCates == null) {
			listUsualCates = (List<SecondStepCate>) Util.loadDataFromLocate(
					this, "listUsualCates");
			if (listUsualCates == null) {
				// 常用类目赋值
				listUsualCates = LocateJsonData.getUsualCatesJson();
				MyApplication.listUsualCates = listUsualCates;
				Util.saveDataToLocate(this, "listUsualCates", listUsualCates);
			} else {
				MyApplication.listUsualCates = listUsualCates;
			}
		} else {
			listUsualCates = MyApplication.listUsualCates;
			Util.saveDataToLocate(this, "listUsualCates", listUsualCates);
		}
		addUsualCate();
		super.onResume();
	} 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.homepage);
		super.onCreate(savedInstanceState);
		if(!MyApplication.update){
			MyApplication.update = true;
			MobclickAgent.setUpdateOnlyWifi(false);
			MobclickAgent.update(this);
		}
		
		// 通过ID获取控件
		tvTitle = (TextView) findViewById(R.id.tvTitle);

		tvInfo = (TextView) findViewById(R.id.tvInfo);
		tvInfo.setVisibility(View.GONE);

		btnSearch = (Button) findViewById(R.id.btnSearch);
		btnChangeCity = (Button) findViewById(R.id.btnChangeCity);
		linearUseualCates = (LinearLayout) findViewById(R.id.linearUseualCates);
		glDetail = (Gallery) findViewById(R.id.glDetail);
		tvSayHi2User = (TextView) findViewById(R.id.tvSayHi2User);
		rlUserName = (RelativeLayout) findViewById(R.id.rlUserName);

		ivHomePage = (ImageView) findViewById(R.id.ivHomePage);
		ivCateMain = (ImageView) findViewById(R.id.ivCateMain);
		ivPostGoods = (ImageView) findViewById(R.id.ivPostGoods);
		ivMyCenter = (ImageView) findViewById(R.id.ivMyCenter);
		ivSetMain = (ImageView) findViewById(R.id.ivSetMain);
		ivHomePage.setImageResource(R.drawable.iv_homepage_press);

		rlUserName.setPadding(20, 10, 20, 10);
		glDetail.setFadingEdgeLength(10);
		glDetail.setSpacing(40);
		glDetail.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if (listHot.get(arg2).getType() == 0) {
					intent.setClass(HomePage.this, SearchGoods.class);
					bundle.putString("act_type", "homepage");
					bundle.putString("name",
							(listHot.get(arg2).getHotData().getTitle()));
					bundle.putString("searchContent", (listHot.get(arg2)
							.getHotData().getKeyword()));
					intent.putExtras(bundle);
					startActivity(intent);
				} else if (listHot.get(arg2).getType() == 1) {
					Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(listHot
							.get(arg2).getHotData().getWeburl()));
					startActivity(i);
				}
			}
		});

		// 获取城市名
		// cityName = intent.getExtras().getString("cityName");
		if (myApp.getCityName() == null || myApp.getCityName().equals("")) {
			cityName = "上海";
			myApp.setCityName(cityName);
			myApp.setCityEnglishName("shanghai");
		} else {
			cityName = myApp.getCityName();
		}
		// 设置标题
		tvTitle.setText(cityName + "百姓网");

		// 获取全局的流文件名集合
		listFileNames = myApp.getListFileNames();

		// 设置监听器
		btnSearch.setOnClickListener(this);
		btnChangeCity.setOnClickListener(this);

		ivHomePage.setOnClickListener(this);
		ivCateMain.setOnClickListener(this);
		ivPostGoods.setOnClickListener(this);
		ivMyCenter.setOnClickListener(this);
		ivSetMain.setOnClickListener(this);

		
		listHot = MyApplication.listHot;
		System.out.println("listHot2--->"+listHot);
		if(listHot == null){
			pd = ProgressDialog.show(HomePage.this, "提示", "请稍候...");
			pd.setCancelable(true);
			new Thread(new HotListThread()).start(); 
		}else{
			adapter = new HotListAdapter(HomePage.this, listImage, listHot);
			glDetail.setAdapter(adapter);
			
		}
		
	}

	/**
	 * @author henry
	 */
	public void addUsualCate() {
		linearUseualCates.removeAllViews();
		LayoutInflater inflater = LayoutInflater.from(this);
		for (int i = 0; i < listUsualCates.size(); i++) {
			View v = null;
			v = inflater.inflate(R.layout.item_hotcity, null);

			if (i == 0) {
				v.setBackgroundResource(R.drawable.btn_top_bg);
			} else if (i == listUsualCates.size() - 1) {
				v.setBackgroundResource(R.drawable.btn_m_bg);
			} else {
				v.setBackgroundResource(R.drawable.btn_m_bg);
			}

			// findviewbyid
			TextView tvCityName = (TextView) v.findViewById(R.id.tvCityName);
			ImageView ivChoose = (ImageView) v.findViewById(R.id.ivChoose);

			// imageview 赋值
			ivChoose.setImageResource(R.drawable.arrow);
			// 设置标记位
			ivChoose.setTag(i);

			// 类目名称
			tvCityName.setText(listUsualCates.get(i).getName());
			v.setTag(i);
			// 设置点击事件
			v.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					int a = Integer.valueOf(v.getTag().toString());
					intent.setClass(HomePage.this, GetGoods.class);
					bundle.putString("name", (listUsualCates.get(a).getName()));
					bundle.putString("categoryEnglishName",
							(listUsualCates.get(a).getEnglishName()));
					bundle.putString("siftresult", "");
					bundle.putString("backPageName", "首页");
					intent.putExtras(bundle);
					startActivity(intent);
				}
			});
			linearUseualCates.addView(v);

		}
		View v1 = null;
		v1 = inflater.inflate(R.layout.item_hotcity, null);
		v1.setBackgroundResource(R.drawable.btn_down_bg); 
		// findviewbyid 
		TextView tv = (TextView) v1.findViewById(R.id.tvCityName);
		ImageView iv = (ImageView) v1.findViewById(R.id.ivChoose);
		tv.setText("其他类目");
		iv.setImageResource(R.drawable.arrow);
		v1.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				intent.setClass(HomePage.this, CateMain.class);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
		linearUseualCates.addView(v1);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnSearch:
			intent.setClass(HomePage.this, Search.class);
			bundle.putString("searchType", "homePage");
			intent.putExtras(bundle);
			startActivity(intent);
			break;
		case R.id.btnChangeCity:
			intent.setClass(HomePage.this, CityChange.class);
			bundle.putString("backPageName", "首页");
			bundle.putString("cityName", cityName);
			intent.putExtras(bundle);
			startActivity(intent);
			break;
		case R.id.ivHomePage:
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
				listHot = JsonUtil.parseCityHotFromJson(Communication
						.decodeUnicode(json));
				for (int i = 0; i < listHot.size(); i++) {
					Bitmap b = null;
					try {
						listBm.set(i, b);
					} catch (Exception e) {
						listBm.add(b);
					}
				}

				adapter = new HotListAdapter(HomePage.this, listImage, listHot);
				System.out.println("listHot--->"+listHot);
				MyApplication.listHot = listHot;
				glDetail.setAdapter(adapter);

				// 开启线程下载图片
//				new Thread(new HotImageThread()).start();

				break;
			case 2:
				if (pd != null) {
					pd.dismiss();
				}
				Toast.makeText(HomePage.this, "未获取到数据", 3).show();
				break;
			case 3:
				int pos = msg.arg1;
				Bitmap bit = listBm.get(pos);
				listImage.get(pos).setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
				listImage.get(pos).setImageBitmap(bit);
				bit = Helper.toRoundCorner(bit, 75f);
				listImage.get(pos).setImageBitmap(bit); 	//需要角度大一点
//				listImage.get(pos).setImageDrawable(Helper.bitmap2Drawable(bit));
				break;
			case 4:
				if (pd != null) {
					pd.dismiss();
				}
				// Toast.makeText(HomePage.this, "网络连接异常", 3).show();
				tvInfo.setVisibility(View.VISIBLE);
				break;
			}
			super.handleMessage(msg);
		}
	};

	

	class HotListThread implements Runnable {

		@Override
		public void run() {
			String apiName = "city_hotlist";
			ArrayList<String> list = new ArrayList<String>();
			String url = Communication.getApiUrl(apiName, list);
			System.out.println("url--->" + url);
			try {
				json = Communication.getDataByUrl(url);
				System.out.println("json ------->" + json);
				if (json != null) {
					myHandler.sendEmptyMessage(1);
				} else {
					myHandler.sendEmptyMessage(2);
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				myHandler.sendEmptyMessage(4);
				e.printStackTrace();
			}

		}
	}

	class HotListAdapter extends BaseAdapter {
		Context context;
		List<ImageView> listImageView = new ArrayList<ImageView>();
		List<HotList> list = new ArrayList<HotList>();

		public HotListAdapter(Context context, List<ImageView> listImage,
				List<HotList> listHot) {
			this.context = context;
			this.listImageView = listImage;
			this.list = listHot;
			for (int i = 0; i < list.size(); i++) {
				ImageView img = null;
				try {
					System.out.println("try");
					listImage.set(i, img);
				} catch (Exception e) {
					System.out.println("catch");
					listImage.add(img);
				}
			}
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = LayoutInflater.from(context);
			View v = null;
			if (convertView != null) {
				v = convertView;
			} else {
				v = inflater.inflate(R.layout.hotdetail, null);
			}
			ImageView iv = (ImageView) v.findViewById(R.id.ivHotDetail);
			iv.setLayoutParams(new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.FILL_PARENT,
					120));
			iv.setPadding(5, 0, 5, 0);
			// 设置图片填充布局
			iv.setScaleType(ScaleType.FIT_XY);
			SimpleImageLoader.showImg(iv, list.get(position).getImgUrl(), HomePage.this);
//			listImageView.set(position, iv);
			return v;
		}
	}

	Bitmap singalBitmap = null;

	class SingalImagethread implements Runnable {
		String bmpUrl = "";
		int num = -1;

		public SingalImagethread() {
			super();
			// TODO Auto-generated constructor stub
		}

		public SingalImagethread(String url, int num) {
			this.bmpUrl = url;
			this.num = num;
		}

		@Override
		public void run() {
			try {
				singalBitmap = Util.getImage(bmpUrl);
				if (singalBitmap == null) {
					System.out.println("图片下载失败");
				} else {
					Util.saveImage2File(HomePage.this, singalBitmap, listHot
							.get(num).getImgUrl());
					Message msg = myHandler.obtainMessage();
					msg.arg2 = num;
					msg.what = 4;
					myHandler.sendMessage(msg);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} catch (Error ee) {
				ee.printStackTrace();
			}
		}
	}

	class HotImageThread implements Runnable {
		String bmpUrl = "";

		public HotImageThread() {
			super();
			// TODO Auto-generated constructor stub
		}

		public HotImageThread(String url) {
			this.bmpUrl = url;
		}

		@Override
		public void run() {
			try {
				for (int i = 0; i < listHot.size(); i++) {
					String b = (listHot.get(i).getImgUrl());
					Bitmap bitmap = null;
					bitmap = Util.getImage(b);
					if (bitmap == null) {
						System.out.println("图片下载失败");
					} else {
						listBm.set(i, bitmap);
						// Util.saveImage2File(HomePage.this, bitmap,
						// listHot.get(i).getImgUrl());
						// listFileNames.add(listHot.get(i).getImgUrl());
						// myApp.setListFileNames(listFileNames);
						Message msg = myHandler.obtainMessage();
						msg.arg1 = i;
						msg.what = 3;
						myHandler.sendMessage(msg);
					}
					
				}
				MyApplication.listBm = listBm;
			} catch (Exception e) {
				e.printStackTrace();
			} catch (Error ee) {
				ee.printStackTrace();
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("提示:")
					.setMessage("是否退出?")
					.setNegativeButton("否", null)
					.setPositiveButton("是",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									if (MyApplication.list != null
											&& MyApplication.list.size() != 0) {
										for (String s : MyApplication.list) {
											deleteFile(s);
										}
										for (int i = 0; i < fileList().length; i++) {
											System.out
													.println("fileList()[i]----------->"
															+ fileList()[i]);
										}
									}
									System.exit(0);
								}
							});
			builder.create().show();
		}
		return super.onKeyDown(keyCode, event);
	}
}
