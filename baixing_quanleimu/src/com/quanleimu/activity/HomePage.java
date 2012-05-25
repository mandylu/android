package com.quanleimu.activity;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mobclick.android.MobclickAgent;
//import com.quanleimu.entity.GoodsInfo;
import com.quanleimu.entity.HotList;
import com.quanleimu.entity.SecondStepCate;
import com.quanleimu.imageCache.LazyImageLoader;
import com.quanleimu.jsonutil.JsonUtil;
import com.quanleimu.jsonutil.LocateJsonData;
import com.quanleimu.util.Communication;
import com.quanleimu.util.Helper;
import com.quanleimu.util.LocationService;
import com.quanleimu.util.ShortcutUtil;
import com.quanleimu.util.Util;
import com.quanleimu.imageCache.ImageLoaderCallback;

public class HomePage extends BaseActivity implements LocationService.BXLocationServiceListener, DialogInterface.OnClickListener{

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
//	public List<GoodsInfo> list = new ArrayList<GoodsInfo>();
	public String cityName = "";
	public HotList hotList = new HotList();
	public List<HotList> listHot = new ArrayList<HotList>();
	public List<HotList> tempListHot = new ArrayList<HotList>();
	public List<Boolean> tempUpdated = new ArrayList<Boolean>();
	public String json = "";
	public List<ImageView> listImage = new ArrayList<ImageView>();
	public List<Bitmap> listBm = new ArrayList<Bitmap>();
	public List<String> listFileNames = new ArrayList<String>();
	public HotListAdapter adapter;
	public List<SecondStepCate> listUsualCates = new ArrayList<SecondStepCate>();
	static public String locationAddr = "";

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		LocationService.getInstance().stop();
		super.onPause();
	}
	

	public void onClick(DialogInterface dialog, int id) {    
		cityName = locationAddr;
		myApp.setCityName(cityName);
		
		boolean found = false;
		for(int i=0;i<myApp.getListCityDetails().size();i++)
		{
			if(cityName.equals(myApp.getListCityDetails().get(i).getName()))
			{
				found = true; 
				myApp.setCityEnglishName(myApp.getListCityDetails().get(i).getEnglishName());
				Helper.saveDataToLocate(this, "cityName", cityName);
				break;
			}
		}
		if(!found){
			for(int i=0;i<myApp.getListCityDetails().size();i++)
			{
				if(cityName.contains(myApp.getListCityDetails().get(i).getName()))
				{
					myApp.setCityEnglishName(myApp.getListCityDetails().get(i).getEnglishName());
					Helper.saveDataToLocate(this, "cityName", cityName);
					break;
				}
			}
			
		}		
		tvTitle.setText(cityName + "百姓网");
	}
	
	@Override
	public void onLocationUpdated(final Location location){
		if(location == null || (locationAddr != null && !locationAddr.equals(""))) return;
		new Runnable(){
			@Override
			public void run(){
				locationAddr = LocationService.geocodeAddr(Double.toString(location.getLatitude()), Double.toString(location.getLongitude()));
				if(null == locationAddr) return;
				int index = locationAddr.indexOf("市");
				locationAddr = (-1 == index ? locationAddr : locationAddr.substring(0, index));
				System.out.println("onLocationUpdated of HomePage, set gpscityname " + locationAddr);
				myApp.setGpsCityName(locationAddr);
				LocationService.getInstance().stop();
				if(HomePage.this.cityName != null && !HomePage.this.cityName.equals(locationAddr)){
					AlertDialog.Builder builder = new AlertDialog.Builder(HomePage.this);  
					builder.setMessage("检测到您在" + locationAddr + "，" + "需要切换吗?")
					.setCancelable(false)  
					.setPositiveButton("是", HomePage.this)  
					.setNegativeButton("否", new DialogInterface.OnClickListener() {  
						public void onClick(DialogInterface dialog, int id) {  
							dialog.cancel();  
							LocationService.getInstance().stop();
						}  
					});
					AlertDialog alert = builder.create();
					alert.show();
				}				
			}
		};
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

		final Location lastLocation = LocationService.getInstance().getLastKnownLocation();
		if(lastLocation != null){
			new Runnable(){
				@Override
				public void run(){
					String lastAddr = LocationService.geocodeAddr(Double.toString(lastLocation.getLatitude()), Double.toString(lastLocation.getLongitude()));
					lastAddr = lastAddr == null ? "" : lastAddr;
					int index = lastAddr.indexOf("市");
					lastAddr = (-1 == index ? lastAddr : lastAddr.substring(0, index));
					if(!lastAddr.equals(HomePage.this.locationAddr)){
						LocationService.getInstance().start(HomePage.this, HomePage.this);
					}					
				}
			};
		}
		else{
			LocationService.getInstance().start(this, this);
		}
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
		if(listHot == null){
			
			//try to load from last-saved hot-list file
			boolean lastSavedValid = true;
			try {
				FileInputStream jsonFile = myApp.getApplicationContext().openFileInput("hotlist.json");
				BufferedInputStream bufferedStream = new BufferedInputStream(jsonFile);
				int bytesJson = bufferedStream.available();
				byte[] json = new byte[bytesJson];
				int readBytes = bufferedStream.read(json, 0, bytesJson);
				
				String jsonDecoded = Communication.decodeUnicode(new String(json, 0, readBytes));
				listHot = JsonUtil.parseCityHotFromJson(jsonDecoded);
				
				for(int i = 0; i < listHot.size(); ++i){
					if(!MyApplication.lazyImageLoader.checkWithImmediateIO(listHot.get(i).imgUrl)){
						lastSavedValid = false;
					}
				}
				
				if(listHot == null || listHot.size() == 0)	lastSavedValid = false;
				
			} catch (FileNotFoundException e) {
				lastSavedValid = false;
			}catch(IOException e){
				lastSavedValid = false;
			}
			
			//if last-saved is not ready, parse from initial hot-list in asset 
			if(!lastSavedValid){
				try {
					InputStream jsonFile = myApp.getApplicationContext().getAssets().open("hotlist.json");
					BufferedInputStream bufferedStream = new BufferedInputStream(jsonFile);
					int bytesJson = bufferedStream.available();
					byte[] json = new byte[bytesJson];
					int readBytes = bufferedStream.read(json, 0, bytesJson);
					
					String jsonDecoded = Communication.decodeUnicode(new String(json, 0, readBytes));
					listHot = JsonUtil.parseCityHotFromJson(jsonDecoded);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			

			if(listHot == null)	listHot = new ArrayList<HotList>();
			
			new Thread(new HotListThread()).start(); 
		}
		
		adapter = new HotListAdapter(HomePage.this, listHot, tempListHot, MyApplication.lazyImageLoader);
		glDetail.setAdapter(adapter);
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
			TextView tvCityName = (TextView) v.findViewById(R.id.tvItemName);
			ImageView ivChoose = (ImageView) v.findViewById(R.id.ivItemIcon);

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
		TextView tv = (TextView) v1.findViewById(R.id.tvItemName);
		ImageView iv = (ImageView) v1.findViewById(R.id.ivItemIcon);
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
			overridePendingTransition(0, 0);
			break;
		case R.id.ivPostGoods:
			intent.setClass(this, PostGoodsCateMain.class);
			intent.putExtras(bundle);
			startActivity(intent);
			overridePendingTransition(0, 0);
			break;
		case R.id.ivMyCenter:
			intent.setClass(this, MyCenter.class);
			intent.putExtras(bundle);
			startActivity(intent);
			overridePendingTransition(0, 0);
			break;
		case R.id.ivSetMain:
			intent.setClass(this, SetMain.class);
			intent.putExtras(bundle);
			startActivity(intent);
			overridePendingTransition(0, 0);
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
				tempListHot = JsonUtil.parseCityHotFromJson(Communication
						.decodeUnicode(json));
				if(tempListHot != null){
					for(int i = 0; i < tempListHot.size(); ++i){
						tempUpdated.add(false);
					}
	
					if(adapter != null)	adapter.SetLoadingHotList(tempListHot);
					MyApplication.listHot = tempListHot;
					
					//save to context data
					MyApplication.context.deleteFile("hotlist.json");
					try {
						BufferedOutputStream outFileStream = new BufferedOutputStream(MyApplication.context.openFileOutput("hotlist.json", MODE_PRIVATE));
						outFileStream.write(json.getBytes(), 0, json.length());
						outFileStream.flush();
						outFileStream.close();
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}catch(IOException e){
						e.printStackTrace();
					}
				}
				

				break;
			case 2:
				if (pd != null) {
					pd.dismiss();
				}
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
				 Toast.makeText(HomePage.this, "网络连接失败，请检查设置！", 3).show();
				//tvInfo.setVisibility(View.VISIBLE);
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
			try {
				json = Communication.getDataByUrl(url);
				if (json != null) {
					myHandler.sendEmptyMessage(1);
				} else {
					myHandler.sendEmptyMessage(2);
				}
			} catch (UnsupportedEncodingException e) {
				myHandler.sendEmptyMessage(4);
			} catch (IOException e) {
				myHandler.sendEmptyMessage(4);
			}

		}
	}

	class HotListAdapter extends BaseAdapter {
		Context context;
		List<HotList> curList = new ArrayList<HotList>();
		List<HotList> loadingList = new ArrayList<HotList>();
		
		final LazyImageLoader imgLoader;

		private class AdapterNotifyChange extends AsyncTask<Boolean, Boolean, Boolean> { 
			private HotListAdapter adapter = null;
			
			public AdapterNotifyChange(HotListAdapter adapter_){
				this.adapter = adapter_;
			}
			
			protected Boolean doInBackground(Boolean... bs) {   
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return true;
			}
			
			protected void onProgressUpdate(Boolean... bs) {
				}    
			
			protected void onPostExecute(Boolean bool) {  
				this.adapter.notifyDataSetChanged();
			}
		};
		
		public HotListAdapter(Context context, 
				List<HotList> listHot,
				List<HotList> loadingListHot,
				LazyImageLoader imgLoader_) {
			this.context = context;
			this.curList = listHot;
			this.loadingList = loadingListHot;
			this.imgLoader = imgLoader_;
		}

		@Override
		public int getCount() {
			return curList.size() > 0 ?	curList.size() : loadingList.size() > 0 ? 1 : 0;
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}
		
		public void SetLoadingHotList(List<HotList> loadingListHot_){
			this.loadingList = loadingListHot_;
			(new AdapterNotifyChange(this)).execute(true);
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
			
			ImageView iv = null;
			
			if(position < curList.size()){
				
				int height = HomePage.this.getWindowManager().getDefaultDisplay().getHeight();
				int fixHotHeight = height / 6;
				if(fixHotHeight < 50)
				{
				    fixHotHeight = 50;
				}
				
				iv = (ImageView) v.findViewById(R.id.ivHotDetail);
				iv.setLayoutParams(new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.FILL_PARENT,
						fixHotHeight));
				iv.setPadding(5, 0, 5, 0);
				// 设置图片填充布局
				iv.setScaleType(ScaleType.FIT_XY);
				iv.setTag(curList.get(position).getImgUrl());
			}
			
			if(imgLoader != null){
				//check whether real image has been valid

				boolean needNotify = false;
				Bitmap bitmapReal =  null;
				final HotListAdapter thisAdapter = this;
				
				if(position < loadingList.size()){
					final int position_f = position;					
					final ImageView iv_f = iv;
					
					bitmapReal = imgLoader.getWithImmediateIO(loadingList.get(position).getImgUrl(), new ImageLoaderCallback(){					
						
						public void refresh(final String url, final Bitmap bitmap){										
							
							if(position_f < curList.size()){
								curList.set(position_f, loadingList.get(position_f));
								tempUpdated.set(position_f, true);
							}
							else if(position_f == curList.size()){
								curList.add(loadingList.get(position_f));
								tempUpdated.set(position_f, true);
							}
							if(iv_f != null)	iv_f.setImageBitmap(bitmap);
	
							(new AdapterNotifyChange(thisAdapter)).execute(true);
						}					
					});
				}
				
				if(bitmapReal != null){
					
					if(position < curList.size() && !tempUpdated.get(position)){
						curList.set(position, loadingList.get(position));
						tempUpdated.set(position, true);
						needNotify = true;		
					}
					else if(position == curList.size()){
						curList.add(loadingList.get(position));
						tempUpdated.set(position, true);
						needNotify = true;
						}
					if(iv != null)	iv.setImageBitmap(bitmapReal);
						
					
					//if this is the last seen item, try to load next
					int position_cur = position + 1;
					while(position_cur == curList.size() && position_cur < loadingList.size()){										
						
						final int position_next = position_cur;
						Bitmap bitmapNext = imgLoader.getWithImmediateIO(loadingList.get(position_next).getImgUrl(), new ImageLoaderCallback(){
										
							public void refresh(final String url, final Bitmap bitmap){	
															
								if(position_next == curList.size()){
									curList.add(loadingList.get(position_next));
									tempUpdated.set(position_next, true);
								}								
								(new AdapterNotifyChange(thisAdapter)).execute(true);
							}
						});
						
						if(bitmapNext != null){
							curList.add(loadingList.get(position_next));
							tempUpdated.set(position_next, true);
							needNotify = true;
							position_cur++;
						}
					}
				}else if(position < curList.size()){				
				
					final Bitmap bitmap =  imgLoader.getWithImmediateIO(curList.get(position).getImgUrl(), new ImageLoaderCallback(){
					
						public void refresh(final String url, final Bitmap bitmap){	
								Log.d( "HotList original image loader 1", "original hotlist picture missing!!");
					    }					
					});
					
					if(bitmap != null){
						if(iv != null)	iv.setImageBitmap(bitmap);		
					}else{
						Log.d( "HotList original image loader 2", "original hotlist picture missing!!");
					}
				}
				
				if(needNotify)	(new AdapterNotifyChange(this)).execute(true);
			}			
			
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
					Log.i("图片下载失败", null);
				} else {
					Util.saveImage2File(HomePage.this, singalBitmap, listHot
							.get(num).getImgUrl());
					Message msg = myHandler.obtainMessage();
					msg.arg2 = num;
					msg.what = 4;
					myHandler.sendMessage(msg);
				}
			} catch (Exception e) {
			} catch (Error ee) {
			}
		}
	}

/*	class HotImageThread implements Runnable {
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
	}*/

	private final static String SHARE_PREFS_NAME = "baixing_shortcut_app";
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {

            SharedPreferences settings = getSharedPreferences(SHARE_PREFS_NAME, 0);
            String hasShowShortcutMessage = settings.getString("hasShowShortcut", "no");

            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            LayoutInflater adbInflater = LayoutInflater.from(HomePage.this);
            View shortcutLayout = adbInflater.inflate(R.layout.shortcutshow, null);

            final CheckBox shortcutCheckBox = (CheckBox) shortcutLayout.findViewById(R.id.shortcut);
            final boolean needShowShortcut = "no".equals(hasShowShortcutMessage) && !ShortcutUtil.hasShortcut(this);
            if (needShowShortcut)
            {
                builder.setView(shortcutLayout);
            }

            builder.setTitle("提示:").setMessage("是否退出?").setNegativeButton("否", null).setPositiveButton("是", new DialogInterface.OnClickListener()
            {

                @Override
                public void onClick(DialogInterface dialog, int which)
                {

                    if (needShowShortcut && shortcutCheckBox.isChecked())
                    {
                        ShortcutUtil.addShortcut(HomePage.this);
                    }

                    if (MyApplication.list != null && MyApplication.list.size() != 0)
                    {
                        for (String s : MyApplication.list)
                        {
                            deleteFile(s);
                        }
                        for (int i = 0; i < fileList().length; i++)
                        {
                            System.out.println("fileList()[i]----------->" + fileList()[i]);
                        }
                    }

                    SharedPreferences settings = getSharedPreferences(SHARE_PREFS_NAME, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("hasShowShortcut", "yes");
                    // Commit the edits!
                    editor.commit();

                    System.exit(0);
                }
            });
            builder.create().show();
        }
        return super.onKeyDown(keyCode, event);
    }
}
