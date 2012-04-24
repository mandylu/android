package com.quanleimu.activity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.LocationListener;
import com.baidu.mapapi.MKLocationManager;
import com.baidu.mapapi.MKSearch;
import com.baidu.mapapi.MapController;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.MyLocationOverlay;
import com.baidu.mapapi.Overlay;
import com.quanleimu.entity.GoodsDetail;
import com.quanleimu.entity.UserBean;
import com.quanleimu.util.Communication;
import com.quanleimu.util.Util;

public class CenterDetail extends BaseActivity {

	// 定义控件
	public TextView tvTitle;
	public Button btnBack, btnStore;
 
	// 定义变量
	public String backPageName = ""; 
	public int tag = 0;

	private LinearLayout ll_meta;
	private TextView txt_tittle;
	private TextView txt_message1, txt_message2;
	private RelativeLayout rl_phone, rl_address, rl_test;
	private TextView txt_phone, txt_address;
	private ImageView im_x;
	public Dialog changePhoneDialog;

	// private MapView mapview;
	private List<Overlay> overlays = new ArrayList<Overlay>();
	// private MapController mapController;
	// private Projection projection;
	// private GeoPoint endGeoPoint, gp;
	// private MKLocationManager mkLocationManager;// 百度地图管理
	// // 定义百度地图管理器
	// public BMapManager mBMapMan;
	// MKSearch mSearch = null;
	// // location监听器
	// public LocationListener mLocationListener = null;

	private BMapManager bMapManager2;// 百度地图
	private String myKey = "713E99B1CD54866996162791BA789A0D9A13791B";// key
	private static MapView mapView;// 地图View
	private MKLocationManager mkLocationManager;// 百度地图管理
	private MKSearch mkSearch;// 百度搜索
	private LocationListener locationListener;// 定位监听
	private MyLocationOverlay myLocationOverlay;// 定位画图
	private String json;
	private MapController mapconter;
	private GeoPoint gp;
	private String mobile,password;
	public List<GoodsDetail> listGoods = new ArrayList<GoodsDetail>();
	public int pos = -1;
	public GoodsDetail goodsDetail = new GoodsDetail();

	@Override
	protected void onPause() {
		// 移除listener
		bMapManager2.getLocationManager().removeUpdates(locationListener);
		bMapManager2.stop();
		super.onPause();
	}

	@Override
	protected void onResume() {
		bundle.putString("backPageName", backPageName);
		// 注册Listener
		bMapManager2.getLocationManager().requestLocationUpdates(
				locationListener);
		bMapManager2.start();
		super.onResume();
	}

	private Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
//				mapView.setVisibility(View.VISIBLE);
//				im_x.setVisibility(View.VISIBLE);
//				rl_test.setVisibility(View.GONE);
				try {
				JSONObject jb = new JSONObject(json);
				String message = jb.getString("message");
				int code = jb.getInt("code");
				if(code == 0){
					//删除成功
					finish();
					Toast.makeText(CenterDetail.this, message, 0).show();
				}else{
					//删除失败
					Toast.makeText(CenterDetail.this, "删除失败", 0).show();
				}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case 2:
//				mapView.setVisibility(View.GONE);
//				im_x.setVisibility(View.GONE);
//				rl_test.setVisibility(View.VISIBLE);
				Toast.makeText(CenterDetail.this, "删除失败", 0).show();
				break;
			}
			super.handleMessage(msg);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.center_detail2);
		super.onCreate(savedInstanceState);

		listGoods = myApp.getListGoods();
		pos = intent.getExtras().getInt("pos");
		goodsDetail = listGoods.get(pos);
		// v.setVisibility(View.GONE);

		backPageName = intent.getExtras().getString("backPageName");

		// findviewbyid
		rl_test = (RelativeLayout) findViewById(R.id.test);

		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText("详细信息");
		btnBack = (Button) findViewById(R.id.btnBack);
		btnStore = (Button) findViewById(R.id.btnStore);

		// new add
		ll_meta = (LinearLayout) findViewById(R.id.meta);
		LayoutInflater inflater = LayoutInflater.from(this);
		if (goodsDetail.getMetaData() == null) {
			ll_meta.setVisibility(View.GONE);
		} else {
			for (int i = 0; i < goodsDetail.getMetaData().size(); i++) {
				View v = null;
				v = inflater.inflate(R.layout.item_meta, null);

				// findviewbyid tvmeta
				TextView tvmetatxt = (TextView) v.findViewById(R.id.tvmetatxt);
				TextView tvmeta = (TextView) v.findViewById(R.id.tvmeta);

				tvmetatxt
						.setText(goodsDetail.getMetaData().keySet().toArray()[i]
								.toString() + "：");
				tvmeta.setText(goodsDetail.getMetaData().get(
						goodsDetail.getMetaData().keySet().toArray()[i]
								.toString()));
				v.setTag(i);
				ll_meta.addView(v);
			}

		}

		txt_tittle = (TextView) findViewById(R.id.goods_tittle);
		txt_tittle.setText(goodsDetail.getTitle());
		txt_message1 = (TextView) findViewById(R.id.sendmess1);
		txt_message1.setText(goodsDetail.getDescription());

		rl_phone = (RelativeLayout) findViewById(R.id.showphone);
		rl_phone.setOnClickListener(this);

		rl_address = (RelativeLayout) findViewById(R.id.showmap);
		rl_address.setOnClickListener(this);

		txt_phone = (TextView) findViewById(R.id.address1);
		if (!goodsDetail.getMobile().equals("")) {
			txt_phone.setText(goodsDetail.getMobile());
		} else {
			txt_phone.setText("无");
		}

		txt_address = (TextView) findViewById(R.id.address2);
		if (!goodsDetail.getAreaNames().equals("")) {
			txt_address.setText(goodsDetail.getAreaNames());
		} else {
			txt_address.setText("无");
		}

		im_x = (ImageView) findViewById(R.id.ivCancel);
		im_x.setOnClickListener(this);

		// mapView = (MapView) findViewById(R.id.mymap);

		// 赋值
		tvTitle.setText("详细信息");
		btnBack.setText(backPageName);

		btnBack.setOnClickListener(this);
		btnStore.setOnClickListener(this);

		bMapManager2 = new BMapManager(getApplication());
		mkSearch = new MKSearch();
		mapView = (MapView) findViewById(R.id.mymap);
		bMapManager2.init(myKey, null);
		mapconter = mapView.getController();
		// myLocationOverlay = new MyLocationOverlay(this, mapView);
		super.initMapActivity(bMapManager2);

		mapView.setBuiltInZoomControls(true);// 地图可以缩放
		mapView.setDrawOverlayWhenZooming(true); // 设置在缩放动画过程中也显示overlay,默认为不绘制

		mkLocationManager = bMapManager2.getLocationManager();

		// mapView.getOverlays().add(myLocationOverlay);// 在mapview上绘制我的位置
		mkLocationManager = bMapManager2.getLocationManager();

		locationListener = new LocationListener() {

			@Override
			public void onLocationChanged(Location location) {
				if (location != null) {
					location = mkLocationManager.getLocationInfo();

					// gp = new GeoPoint((int) (location.getLatitude() * 1e6),
					// (int) (location.getLongitude() * 1e6));
					gp = new GeoPoint((int) (myApp.getLon() * 1E6),
							(int) (myApp.getLat() * 1E6));
					// overlays.add(new MyLocationOverlays(gp));
					// mapView.getController().animateTo(gp);

				}

			}
		};

		UserBean user = (UserBean) Util.loadDataFromLocate(
				CenterDetail.this, "user");
		mobile = user.getPhone();
		password = user.getPassword();
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnBack:
			CenterDetail.this.finish();
			break;
		case R.id.btnStore:

			LayoutInflater inflater = getLayoutInflater();
			View linearlayout = inflater.inflate(R.layout.changephonedialog,
					null);
			TextView tvTelNum = (TextView) linearlayout
					.findViewById(R.id.tvTelNum);
			tvTelNum.setText("");
			Button btnChange = (Button) linearlayout
					.findViewById(R.id.btnChange);
			btnChange.setText("编辑");
			Button btnCancel = (Button) linearlayout
					.findViewById(R.id.btnCancel);
			btnCancel.setText("删除");

			btnChange.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// 点击编辑
					changePhoneDialog.dismiss();
				}
			});

			btnCancel.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// 点击删除
					new Thread(new MyMessageDeleteThread()).start();
					changePhoneDialog.dismiss();
				}
			});
			changePhoneDialog = new AlertDialog.Builder(this).setView(
					linearlayout).create();
			changePhoneDialog.show();
			break;
		case R.id.showphone:
			Uri uri = Uri.parse("tel:" + txt_phone.getText().toString());
			Intent intent = new Intent(Intent.ACTION_DIAL, uri);
			startActivity(intent);
			break;
		case R.id.showmap:
			myHandler.sendEmptyMessage(1);
			break; 
		case R.id.ivCancel:
			myHandler.sendEmptyMessage(2);
			break;
		}
		super.onClick(v);
	}

	//{"error":{"message":"删除信息成功。","code":0}}
	//{"error":{"message":"can not update deleted Ad","code":504}}

	class MyMessageDeleteThread implements Runnable {
		@Override
		public void run() {
			// TODO Auto-generated method stub

			String apiName = "ad_delete";
			ArrayList<String> list = new ArrayList<String>();
			list.add("mobile=" + mobile);
			String password1 = Communication.getMD5(password);
			password1 += Communication.apiSecret;
			String userToken = Communication.getMD5(password1);
			list.add("userToken=" + userToken);
			list.add("adId=" + goodsDetail.getId());

			String url = Communication.getApiUrl(apiName, list);
			System.out.println("url--->" + url);
			try {
				json = Communication.getDataByUrl(url);
				System.out.println("result --->" + json);
				try {
					JSONObject jb = new JSONObject(json);
					System.out.println("jb--->" + jb);
					if (json != null) {
						myHandler.sendEmptyMessage(1);
					} else {
						myHandler.sendEmptyMessage(2);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
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
			// projection.toPixels(geoPoint, point);
			Paint paint = new Paint();
			Bitmap bmp = BitmapFactory.decodeResource(getResources(),
					R.drawable.red);
			canvas.drawBitmap(bmp, point.x, point.y, paint);
		}
	}

}
