package com.quanleimu.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.LocationListener;
import com.baidu.mapapi.MKSearch;
import com.baidu.mapapi.MapController;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.Overlay;
import com.baidu.mapapi.Projection;
import com.quanleimu.entity.GoodsDetail;
import com.quanleimu.util.Communication;
import com.quanleimu.util.Helper;
import com.quanleimu.util.Util;

public class GetGoodsDetail2 extends BaseActivity {

	// 定义控件
	public TextView tvTitle;
	public Button btnBack, btnStore;
	public MainAdapter adapter;

	// 定义变量
	public String backPageName = "";
	public int tag = 0;

	private LinearLayout ll_meta;
	private TextView txt_tittle;
	private TextView txt_message1;
	private RelativeLayout rl_phone, rl_address, rl_test,llgl;
	private TextView txt_phone, txt_address;
	private ImageView im_x;
	
	private BMapManager bMapManager;// 百度地图

	private MapView mapview;
	private List<Overlay> overlays = new ArrayList<Overlay>();
	private MapController mapController;
	private Projection projection;
	private GeoPoint endGeoPoint;
	public List<String> listIds = new ArrayList<String>();
	public List<String> listStoreId = new ArrayList<String>();
	public int type = -1;
	public int postIndex = -1;
	// 定义百度地图管理器
	MKSearch mSearch = null;
	// location监听器
	public LocationListener mLocationListener = null;
	public List<GoodsDetail> listGoods = new ArrayList<GoodsDetail>();

	public List<GoodsDetail> listLookHistory = new ArrayList<GoodsDetail>();

	public List<GoodsDetail> listMyStore = new ArrayList<GoodsDetail>();

	public int pos = -1;
	public GoodsDetail goodsDetail = new GoodsDetail();
	public Gallery glDetail;
	public double lat = 0;
	public double lon = 0;
	public List<String> listUrl = new ArrayList<String>();
	public List<Bitmap> listBm = new ArrayList<Bitmap>();
	public List<Bitmap> listBigBm = new ArrayList<Bitmap>();
	
	public String detail_type = "";

	@Override
	protected void onPause() {
		// 移除listener
		if(bMapManager!=null){
			bMapManager.stop();
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		bundle.putString("backPageName", backPageName);
//		bMapManager.start();
		super.onResume();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.detail2);
		super.onCreate(savedInstanceState);

		type = Util.getWidth(this);
		
		//获取全局中的浏览历史和我的收藏
		listLookHistory = myApp.getListLookHistory();
		listMyStore = myApp.getListMyStore();
		
		detail_type = intent.getExtras().getString("detail_type");
		if(detail_type.equals("searchgoods"))
		{
			listGoods = myApp.getListSearchGoods();
		}
		else if(detail_type.equals("getgoods"))
		{
			listGoods = myApp.getListGoods();
		}
		pos = intent.getExtras().getInt("detail_pos");
		if(listGoods != null && listGoods.size() != 0)
		{
			
			goodsDetail = listGoods.get(pos);
			
			if(goodsDetail.getImageList() != null){
				System.out.println("goodsDetail.getImageList()--->"+goodsDetail.toString());
				String b = (goodsDetail.getImageList().getResize180()).substring(1, (goodsDetail.getImageList().getResize180()).length()-1);
				System.out.println("bbbb-<"+b);
				b = Communication.replace(b);
				System.out.println("bbbb->"+b);
				if(b.contains(","))
				{
					String[] c = b.split(",");
					for(int i=0;i<c.length;i++) 
					{
//					listBm.add(null);
						listUrl.add(c[i]);
					} 
				}
				else
				{
//				listBm.add(null);
					listUrl.add(b);
				}
				System.out.println("listUrl-->"+listUrl.size());
				if(listUrl.size() == 0){
					llgl = (RelativeLayout) findViewById(R.id.llgl);
					llgl.setVisibility(View.GONE);
				}else{
					glDetail = (Gallery) findViewById(R.id.glDetail);
					glDetail.setFadingEdgeLength(10);
					glDetail.setSpacing(40);
					
					adapter = new MainAdapter(GetGoodsDetail2.this);
					glDetail.setAdapter(adapter);
					
					glDetail.setOnItemClickListener(new AdapterView.OnItemClickListener() {
						
						@Override
						public void onItemClick(AdapterView<?> arg0, View arg1, 
								int arg2, long arg3) {
							postIndex = arg2;
							bundle.putInt("postIndex", postIndex);
							bundle.putSerializable("goodsDetail", goodsDetail);
							intent.setClass(GetGoodsDetail2.this, BigGallery.class);
							intent.putExtras(bundle); 
							startActivity(intent); 
						}
					});
//				new Thread(new Imagethread()).start();
				}
			}else{
				llgl = (RelativeLayout) findViewById(R.id.llgl);
				llgl.setVisibility(View.GONE);
			}
			
			
			
			
			
			backPageName = intent.getExtras().getString("backPageName");
			
			//获取控件
			rl_test = (RelativeLayout) findViewById(R.id.test);
			
			
			
			tvTitle = (TextView) findViewById(R.id.tvTitle);
			btnBack = (Button) findViewById(R.id.btnBack);
			btnStore = (Button) findViewById(R.id.btnStore);
			
			btnStore.setText("收藏");
			//设置grallery的边距
			
			
			txt_tittle = (TextView) findViewById(R.id.goods_tittle);
			txt_message1 = (TextView) findViewById(R.id.sendmess1);
			txt_phone = (TextView) findViewById(R.id.address1);
			txt_address = (TextView) findViewById(R.id.address2);
			rl_phone = (RelativeLayout) findViewById(R.id.showphone);
			rl_address = (RelativeLayout) findViewById(R.id.showmap);
			im_x = (ImageView) findViewById(R.id.ivCancel);
			
			ll_meta = (LinearLayout) findViewById(R.id.meta);
			
			mapview = (MapView) findViewById(R.id.mymap);
			
			if (bMapManager == null) 
			{
				bMapManager = new BMapManager(getApplication());
			    bMapManager.init(myApp.mStrKey, new MyApplication.MyGeneralListener());
			}
			bMapManager.start();
			// 如果使用地图SDK，请初始化地图Activity
	        super.initMapActivity(bMapManager);
			
			mapController = mapview.getController();
			mapview.setBuiltInZoomControls(true);
			
			overlays = mapview.getOverlays();
			projection = mapview.getProjection();
			
			//判断浏览历史中是否有当前这条信息
			if(listLookHistory == null || listLookHistory.size() == 0)
			{
				listLookHistory = new ArrayList<GoodsDetail>();
				listLookHistory.add(goodsDetail);
			}
			else
			{
				for(int i=0;i<listLookHistory.size();i++)
				{
//				if(listIds != null && listIds.size() != 0)
//				{
//					listIds = new ArrayList<String>();
//				}
					listIds.add(listLookHistory.get(i).getId());
				}
				
				if(!listIds.contains(goodsDetail.getId()))
				{
					System.out.println("不存在,添加");
					
					if(listLookHistory.size() >= 100)
					{
						listLookHistory.remove(0);
						listLookHistory.add(goodsDetail);
					}
					else
					{
						listLookHistory.add(goodsDetail);
					}
				}
				else
				{
					System.out.println("已经存在了");
				}
			}
			myApp.setListLookHistory(listLookHistory);
			Helper.saveDataToLocate(GetGoodsDetail2.this, "listLookHistory", listLookHistory);
			
			//判断当前物品是否在我的收藏中
			if(listMyStore != null && listMyStore.size() != 0)
			{
				for(int i=0;i<listMyStore.size();i++)
				{
//				if(listIds != null && listIds.size() != 0)
//				{
//					listStoreId = new ArrayList<String>();
//				}
					listStoreId.add(listMyStore.get(i).getId());
				}
				
				if(!listStoreId.contains(goodsDetail.getId()))
				{
					tag = 0;
					btnStore.setText("收藏");
				}
				else
				{
					tag = 1;
					btnStore.setText("已收藏");
				}
				
			}
			else
			{
				listMyStore = new ArrayList<GoodsDetail>();
				listStoreId = null;
				tag = 0;
				btnStore.setText("收藏");
			}
			
			
			//设置监听器
			im_x.setOnClickListener(this);
			btnBack.setOnClickListener(this);
			btnStore.setOnClickListener(this);
			
			// new add
			LayoutInflater inflater = LayoutInflater.from(this);
			if (goodsDetail.getMetaData() == null) {
				ll_meta.setVisibility(View.GONE);
			} else {
				for (int i = 0; i < goodsDetail.getMetaData().size(); i++) {
					View v = null;
					v = inflater.inflate(R.layout.item_meta, null);
					
					TextView tvmetatxt = (TextView) v.findViewById(R.id.tvmetatxt);
					TextView tvmeta = (TextView) v.findViewById(R.id.tvmeta);
					
					tvmetatxt.setText(goodsDetail.getMetaData().keySet().toArray()[i].toString() + "：");
					tvmeta.setText(goodsDetail.getMetaData().get(goodsDetail.getMetaData().keySet().toArray()[i].toString()));
					v.setTag(i);
					ll_meta.addView(v);
				}
				
			}
			//赋值
			tvTitle.setText("详细信息");
			txt_message1.setText(goodsDetail.getDescription());
			txt_tittle.setText(goodsDetail.getTitle());
			btnBack.setText(backPageName);
			
			//判断当前是否有地域内容
			if (goodsDetail.getAreaNames() != null && !goodsDetail.getAreaNames().equals("")) 
			{
				txt_address.setText(goodsDetail.getAreaNames());
				
				//判断当前的物品是否有经纬度
				if(goodsDetail.getLat() != null && !goodsDetail.getLat().equals("false") && !goodsDetail.getLat().equals("") && goodsDetail.getLng() != null && !goodsDetail.getLng().equals("false") && !goodsDetail.getLng().equals(""))
				{
					try {
						lat = Double.valueOf(goodsDetail.getLat().toString());
					} catch (NumberFormatException e) {
					}
					try {
						lon = Double.valueOf(goodsDetail.getLng().toString());
					} catch (NumberFormatException e) {
					}
					rl_address.setOnClickListener(this);
				}
				else
				{
					rl_address.setBackgroundResource(R.drawable.iv_bg_unclickable);
				}
			} 
			else 
			{
				txt_address.setText("无");
				rl_address.setBackgroundResource(R.drawable.iv_bg_unclickable);
			}
			
			if (goodsDetail.getMobile() != null && !goodsDetail.getMobile().equals("") && !goodsDetail.getMobile().equals("无")) 
			{
				txt_phone.setText(goodsDetail.getMobile());
				rl_phone.setOnClickListener(this);
			} 
			else 
			{
				rl_phone.setVisibility(View.GONE);
//			txt_phone.setText("无");
//			rl_phone.setBackgroundResource(R.drawable.iv_bg_unclickable);
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnBack:
			GetGoodsDetail2.this.finish();
			break;
		case R.id.btnStore:
			if (tag == 0) 
			{
				btnStore.setText("已收藏");
				Toast.makeText(GetGoodsDetail2.this, "收藏成功", 3).show();
				tag = 1;
				if(listMyStore ==null || listMyStore.size() == 0)
				{
					listMyStore = new ArrayList<GoodsDetail>();
					listMyStore.add(goodsDetail);
				}
				else
				{
					if(!listStoreId.contains(goodsDetail.getId())) 
					{
						if(listMyStore.size() >= 100)
						{
							listMyStore.remove(0);
							listMyStore.add(goodsDetail);
						}
						else
						{
							listMyStore.add(goodsDetail);
						}
					}
				}
				myApp.setListMyStore(listMyStore);
				Helper.saveDataToLocate(GetGoodsDetail2.this, "listMyStore", listMyStore);
			} 
			else if (tag == 1) 
			{
				btnStore.setText("收藏");
				Toast.makeText(GetGoodsDetail2.this, "取消收藏", 3).show();
				tag = 0;
				
				for(int i=0;i<listMyStore.size();i++)
				{
					if(goodsDetail.getId().equals(listMyStore.get(i).getId()))
					{
						listMyStore.remove(i);
					}
				}
				myApp.setListMyStore(listMyStore);
				Helper.saveDataToLocate(GetGoodsDetail2.this, "listMyStore", listMyStore);
			}
			break;
		case R.id.showphone:
			Uri uri = Uri.parse("tel:" + txt_phone.getText().toString());
			Intent intent = new Intent(Intent.ACTION_DIAL, uri);
			startActivity(intent);
			break;
		case R.id.showmap:
			
			
			//判断当前的物品是否有经纬度
			if(goodsDetail.getLat() != null && !goodsDetail.getLat().equals("false") && !goodsDetail.getLat().equals("") && goodsDetail.getLng() != null && !goodsDetail.getLng().equals("false") && !goodsDetail.getLng().equals(""))
			{
				lat = Double.valueOf(goodsDetail.getLat().toString());
				lon = Double.valueOf(goodsDetail.getLng().toString());
				rl_address.setOnClickListener(this);
				
				endGeoPoint = new GeoPoint((int)(lat*1E6),(int)(lon*1E6));
				overlays.add(new MyLocationOverlays(endGeoPoint));
				mapController.animateTo(endGeoPoint);
				mapController.setZoom(15);
			}
			else
			{
				rl_address.setBackgroundResource(R.drawable.iv_bg_unclickable);
			}
			myHandler.sendEmptyMessage(1);
			break;
		case R.id.ivCancel:
			myHandler.sendEmptyMessage(2);
			break;
		}
		super.onClick(v);
	}
	
	public Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				mapview.setVisibility(View.VISIBLE);
				im_x.setVisibility(View.VISIBLE);
				rl_test.setVisibility(View.GONE);
				break;
			case 2:
				mapview.setVisibility(View.GONE);
				im_x.setVisibility(View.GONE);
				rl_test.setVisibility(View.VISIBLE);
				break;
			case 3:
				if(listBm != null && listBm.size() != 0)
				{
//					adapter.notifyDataSetChanged();
					
					new Thread(new BigImageThread()).start();
				}else{
					llgl.setVisibility(View.GONE);
				}
				
				break;
			case 4:
//				llgl.setVisibility(View.VISIBLE);
				
				myApp.setListBigBm(listBigBm);
				adapter.notifyDataSetChanged();
				
				glDetail.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						postIndex = arg2;
						bundle.putInt("postIndex", postIndex);
						intent.setClass(GetGoodsDetail2.this, BigGallery.class);
						intent.putExtras(bundle);
						startActivity(intent);
					}
				});
				break;
			}
			super.handleMessage(msg);
		}
	};
	
	//开启线程下载图片
	class Imagethread implements Runnable
	{
		@Override
		public void run() {
			try {
				String b = (goodsDetail.getImageList().getResize180()).substring(1, (goodsDetail.getImageList().getResize180()).length()-1);
				b = Communication.replace(b);
				if(b.contains(","))
				{
					String[] c = b.split(",");
					for(int i=0;i<c.length;i++) 
					{
						Bitmap bitmap = Util.getImage(c[i]);
						listBm.add(bitmap);
					}
				}
				else
				{
					Bitmap bitmap = Util.getImage(b);
					listBm.add(bitmap);
				}
				myHandler.sendEmptyMessage(3);
			} catch (Exception e) {
				e.printStackTrace();
			} catch (Error ee){
				ee.printStackTrace();
			}
		}
	}
	
	//开启线程下载大图片
	class BigImageThread implements Runnable
	{
		@Override
		public void run() {
			try {
				String b = (goodsDetail.getImageList().getBig()).substring(1, (goodsDetail.getImageList().getBig()).length()-1);
				b = Communication.replace(b);
				if(b.contains(","))
				{
					String[] c = b.split(",");
					for(int i=0;i<c.length;i++) 
					{
						Bitmap bitmap = Util.getImage(c[i]);
						listBigBm.add(bitmap);
					}
				}
				else
				{
					Bitmap bitmap = Util.getImage(b);
					listBigBm.add(bitmap);
				}
				myHandler.sendEmptyMessage(4);
			} catch (Exception e) {
				e.printStackTrace();
			} catch (Error ee){
				ee.printStackTrace();
			}
		}
	}

	class MyLocationOverlays extends Overlay {
		GeoPoint geoPoint;

		public MyLocationOverlays(GeoPoint geoPoint) {
			super();
			this.geoPoint = geoPoint;
		}

		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow) {
			super.draw(canvas, mapView, shadow);
			Point point = new Point();
			projection.toPixels(geoPoint, point);
			Paint paint = new Paint();
			Bitmap bmp = BitmapFactory.decodeResource(getResources(),
					R.drawable.red);
			canvas.drawBitmap(bmp, point.x, point.y, paint);
		}
	}
	
	class MainAdapter extends BaseAdapter
	{
		Context context;
		public MainAdapter(Context context)
		{
			this.context = context;
		}
	
		@Override
		public int getCount() {
			return listUrl.size();
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
//			if(convertView != null){
//				v = (ImageView)convertView;
//			}else{
				v = inflater.inflate(R.layout.item_detailview, null);
//			}
			ImageView iv = (ImageView)v.findViewById(R.id.ivGoods);
			
			Bitmap mb = BitmapFactory.decodeResource(context.getResources(),R.drawable.moren1);
			mb= Helper.toRoundCorner(mb, 20);
			
			Bitmap mb1 = BitmapFactory.decodeResource(context.getResources(),R.drawable.moren);
			mb1= Helper.toRoundCorner(mb1, 20);
			
			iv.setImageBitmap(mb);
			
			if(type == 1)
			{
				iv.setLayoutParams(new Gallery.LayoutParams(86,86));
			}
			else if(type == 2)
			{
				iv.setLayoutParams(new Gallery.LayoutParams(145,145));
			}
			else if(type == 3)
			{
				iv.setLayoutParams(new Gallery.LayoutParams(210,210));
			}
			else if(type == 4)
			{
				iv.setLayoutParams(new Gallery.LayoutParams(235,235));
			}
			else if(type == 5)
			{
				iv.setLayoutParams(new Gallery.LayoutParams(240,240));
			}else{
				iv.setLayoutParams(new Gallery.LayoutParams(245,245));
			}
			if(listUrl.get(position) != null && listUrl.size()!=0){
				iv.setTag(listUrl.get(position));
				LoadImage.addTask(listUrl.get(position), iv);
				LoadImage.doTask();
//				SimpleImageLoader.showImg(iv,listUrl.get(position),context);
			}else{
				iv.setImageBitmap(mb1);
			}
			
			return iv;
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Util.recycle(listBm);
		Util.recycle(listBigBm);
		listBm = null;
		listBigBm = null;
	}
}
