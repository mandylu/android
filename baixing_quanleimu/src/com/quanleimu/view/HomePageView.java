package com.quanleimu.view;

import java.io.BufferedInputStream;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;

import com.quanleimu.activity.CateMain;
import com.quanleimu.activity.GetGoods;
import com.quanleimu.activity.HomePage;
import com.quanleimu.activity.MyApplication;
import com.quanleimu.activity.R;
import com.quanleimu.activity.SearchGoods;
import com.quanleimu.entity.HotList;
import com.quanleimu.entity.SecondStepCate;
import com.quanleimu.imageCache.ImageLoaderCallback;
import com.quanleimu.imageCache.LazyImageLoader;
import com.quanleimu.jsonutil.JsonUtil;
import com.quanleimu.jsonutil.LocateJsonData;
import com.quanleimu.util.Communication;
import com.quanleimu.util.Helper;
import com.quanleimu.util.LocationService;
import com.quanleimu.util.Util;
import com.quanleimu.view.BaseView;
import com.quanleimu.view.BaseView.ViewInfoListener;
import com.quanleimu.activity.BaseActivity;

public class HomePageView extends BaseView implements LocationService.BXLocationServiceListener, DialogInterface.OnClickListener{
	private Gallery glDetail;
	private LinearLayout linearUseualCates;
	private List<HotList> listHot = new ArrayList<HotList>();
	private String cityName;
	private String json;
	private HotListAdapter adapter;
	private List<HotList> tempListHot = new ArrayList<HotList>();
	private ProgressDialog pd;
	private List<Boolean> tempUpdated = new ArrayList<Boolean>();
	private List<SecondStepCate> listUsualCates = new ArrayList<SecondStepCate>();
	static private String locationAddr = "";
	private ViewInfoListener infoListener = null;
	
	@Override
	public void setInfoChangeListener(ViewInfoListener listener){
		infoListener = listener;
	}
	
	public HomePageView(BaseActivity activity, Bundle bundle){
		super(activity, bundle);
		init();
	}
	
	@Override
	public void onClick(DialogInterface dialog, int id) {    
		cityName = locationAddr;
		MyApplication.getApplication().setCityName(cityName);
		
		boolean found = false;
		for(int i=0;i<MyApplication.getApplication().getListCityDetails().size();i++)
		{
			if(cityName.equals(MyApplication.getApplication().getListCityDetails().get(i).getName()))
			{
				found = true; 
				MyApplication.getApplication().setCityEnglishName(MyApplication.getApplication().getListCityDetails().get(i).getEnglishName());
				Helper.saveDataToLocate(MyApplication.getApplication().getApplicationContext(), "cityName", cityName);
				break;
			}
		}
		if(!found){
			for(int i=0;i<MyApplication.getApplication().getListCityDetails().size();i++)
			{
				if(cityName.contains(MyApplication.getApplication().getListCityDetails().get(i).getName()))
				{
					MyApplication.getApplication().setCityEnglishName(MyApplication.getApplication().getListCityDetails().get(i).getEnglishName());
					Helper.saveDataToLocate(MyApplication.getApplication().getApplicationContext(), "cityName", cityName);
					break;
				}
			}
			
		}		
//		tvTitle.setText(cityName + "百姓网");
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
				MyApplication.getApplication().setGpsCityName(locationAddr);
				LocationService.getInstance().stop();
				if(HomePageView.this.cityName != null && !MyApplication.getApplication().cityName.equals(locationAddr)){
					AlertDialog.Builder builder = new AlertDialog.Builder(MyApplication.getApplication());  
					builder.setMessage("检测到您在" + locationAddr + "，" + "需要切换吗?")
					.setCancelable(false)  
					.setPositiveButton("是", HomePageView.this)  
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
	
	@Override
	protected void onAttachedToWindow(){
		if (MyApplication.listUsualCates == null) {
			listUsualCates = (List<SecondStepCate>) Util.loadDataFromLocate(MyApplication.getApplication().getApplicationContext(), "listUsualCates");
			if (listUsualCates == null) {
				// 常用类目赋值
				listUsualCates = LocateJsonData.getUsualCatesJson();
				MyApplication.listUsualCates = listUsualCates;
				Util.saveDataToLocate(MyApplication.getApplication().getApplicationContext(), "listUsualCates", listUsualCates);
			} else {
				MyApplication.listUsualCates = listUsualCates;
			}
		} else {
			listUsualCates = MyApplication.listUsualCates;
			Util.saveDataToLocate(MyApplication.getApplication().getApplicationContext(), "listUsualCates", listUsualCates);
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
					if(!lastAddr.equals(locationAddr)){
						LocationService.getInstance().start(MyApplication.getApplication().getApplicationContext(), HomePageView.this);
					}					
				}
			};
		}
		else{
			LocationService.getInstance().start(MyApplication.getApplication().getApplicationContext(), this);
		}		
		super.onAttachedToWindow();
	}
	
	public void addUsualCate() {
		linearUseualCates.removeAllViews();
		LayoutInflater inflater = LayoutInflater.from(MyApplication.getApplication().getApplicationContext());
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
//					int a = Integer.valueOf(v.getTag().toString());
//					intent.setClass(HomePage.this, GetGoods.class);
//					bundle.putString("name", (listUsualCates.get(a).getName()));
//					bundle.putString("categoryEnglishName",
//							(listUsualCates.get(a).getEnglishName()));
//					bundle.putString("siftresult", "");
//					bundle.putString("backPageName", "首页");
//					intent.putExtras(bundle);
//					startActivity(intent);
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
//				intent.setClass(HomePage.this, CateMain.class);
//				intent.putExtras(bundle);
//				startActivity(intent);
			}
		});
		
		linearUseualCates.addView(v1);
	}

	
	private void init(){
		LayoutInflater inflater = LayoutInflater.from(this.getContext());
		View v = inflater.inflate(R.layout.homepageview, null);
		this.addView(v);
		TextView tvInfo = (TextView) findViewById(R.id.tvInfo);
		tvInfo.setVisibility(View.GONE);
		
		linearUseualCates = (LinearLayout)v.findViewById(R.id.linearUseualCates);
		glDetail = (Gallery) v.findViewById(R.id.glDetail);

		glDetail.setFadingEdgeLength(10);
		glDetail.setSpacing(40);
		glDetail.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if (listHot.get(arg2).getType() == 0) {
//					intent.setClass(HomePage.this, SearchGoods.class);
//					bundle.putString("act_type", "homepage");
//					bundle.putString("name",
//							(listHot.get(arg2).getHotData().getTitle()));
//					bundle.putString("searchContent", (listHot.get(arg2)
//							.getHotData().getKeyword()));
//					intent.putExtras(bundle);
//					startActivity(intent);
				} else if (listHot.get(arg2).getType() == 1) {
//					Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(listHot
//							.get(arg2).getHotData().getWeburl()));
//					startActivity(i);
				}
			}
		});

		if (MyApplication.getApplication().getCityName() == null || MyApplication.getApplication().getCityName().equals("")) {
			cityName = "上海";
			MyApplication.getApplication().setCityName(cityName);
			MyApplication.getApplication().setCityEnglishName("shanghai");
		} else {
			cityName = MyApplication.getApplication().getCityName();
		}

		listHot = MyApplication.listHot;
		if(listHot == null){	
			//try to load from last-saved hot-list file
			boolean lastSavedValid = true;
			try {
				FileInputStream jsonFile = MyApplication.getApplication().getApplicationContext().openFileInput("hotlist.json");
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
					InputStream jsonFile = MyApplication.getApplication().getApplicationContext().getAssets().open("hotlist.json");
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
		
		adapter = new HotListAdapter(MyApplication.getApplication().getApplicationContext(), 
				listHot, 
				tempListHot, 
				MyApplication.lazyImageLoader);
		glDetail.setAdapter(adapter);		
	}
	
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
						BufferedOutputStream outFileStream = new BufferedOutputStream(MyApplication.context.openFileOutput("hotlist.json", Context.MODE_PRIVATE));
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
			case 4:
				if (pd != null) {
					pd.dismiss();
				}
				 Toast.makeText(MyApplication.getApplication().getApplicationContext(), "网络连接失败，请检查设置！", 3).show();
				//tvInfo.setVisibility(View.VISIBLE);
				break;
			}
			super.handleMessage(msg);
		}
	};

	
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
				WindowManager wm = 
						(WindowManager)MyApplication.getApplication().getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
				int height = wm.getDefaultDisplay().getHeight();
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
	
	@Override
	public void onDestroy(){}
	
	@Override
	public void onPause(){}//called before put into stack
	
	@Override
	public boolean onBack(){
		return false;
	}
	
	@Override
	public boolean onLeftActionPressed(){
		return false;
	}
	
	@Override
	public boolean onRightActionPressed(){return false;}//called when right button on title bar pressed, return true if handled already, false otherwise
	
	@Override
	public TitleDef getTitleDef(){
		TitleDef title = new TitleDef();
		title.m_leftActionHint = "切换城市";
		title.m_rightActionHint = "搜索";
		title.m_title = "百姓网";
		title.m_visible = true;
		return title;
	}
	
	@Override
	public TabDef getTabDef(){
		TabDef tab = new TabDef();
		tab.m_visible = true;
		return tab;
	}
	
};