package com.quanleimu.activity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.quanleimu.entity.GoodsList;
import com.quanleimu.entity.UserBean;
import com.quanleimu.imageCache.SimpleImageLoader;
import com.quanleimu.jsonutil.JsonUtil;
import com.quanleimu.util.Communication;
import com.quanleimu.util.Helper;
import com.quanleimu.util.Util;
public class GoodDetail extends BaseActivity implements DialogInterface.OnClickListener{
	final private String strCollect = "收藏";
	final private String strCancelCollect = "取消收藏";
	final private String strCollected = "取消收藏";
	final private String strManager = "管理";
	final private int msgShowMap = 1;
	final private int msgCancelMap = 2;
	final private int msgRefresh = 5;
	final private int msgUpdate = 6;
	final private int msgDelete = 7;
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
	private RelativeLayout rl_phone, rl_address, rl_test, llgl;
	private TextView txt_phone, txt_address;
	private ImageView im_x;

	private BMapManager bMapManager;

	private MapView mapview;
	private List<Overlay> overlays = new ArrayList<Overlay>();
	private MapController mapController;
	private Projection projection;
	private GeoPoint endGeoPoint;
	public List<String> listIds = new ArrayList<String>();
	public List<String> listStoreId = new ArrayList<String>();
	public int type = -1;


	MKSearch mSearch = null;

	public LocationListener mLocationListener = null;

	public GoodsDetail detail = new GoodsDetail();
	public Gallery glDetail;
	public List<Bitmap> listBm = new ArrayList<Bitmap>();
	public List<Bitmap> listBigBm = new ArrayList<Bitmap>();
	public String mycenter_type = "";
	
	private String json = "";
	
	enum REQUEST_TYPE{
		REQUEST_TYPE_REFRESH,
		REQUEST_TYPE_UPDATE,
		REQUEST_TYPE_DELETE
	}

	@Override
	protected void onPause() {
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

	private boolean isMyAd(){
		if(detail == null) return false;
		List<GoodsDetail> myPost = myApp.getListMyPost();
		for(int i = 0; i < myPost.size(); ++ i){
			if(myPost.get(i).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID)
					.equals(detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID))){
				return true;
			}
		}
		return false;
	}
	private boolean isInMyStore(){
		if(detail == null) return false;
		List<GoodsDetail> myStore = myApp.getListMyStore();
		if(myStore == null) return false;
		for(int i = 0; i < myStore.size(); ++ i){
			if(myStore.get(i).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID)
					.equals(detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID))){
				return true;
			}
		}
		return false;		
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.detail2);
		super.onCreate(savedInstanceState);

		type = Util.getWidth(this);
		detail = (GoodsDetail)intent.getExtras().getSerializable("currentGoodsDetail");
		
		if(detail.getImageList() != null){
			String b = (detail.getImageList().getResize180()).substring(1, (detail.getImageList().getResize180()).length()-1);
			b = Communication.replace(b);
			List<String> listUrl = new ArrayList<String>();
			String[] c = b.split(",");
			for(int i=0;i<c.length;i++) 
			{
				listUrl.add(c[i]);
			}
			if(listUrl.size() == 0){
				llgl = (RelativeLayout) findViewById(R.id.llgl);
				llgl.setVisibility(View.GONE);
			}else{
				glDetail = (Gallery) findViewById(R.id.glDetail);
				glDetail.setFadingEdgeLength(10);
				glDetail.setSpacing(40);
				
				adapter = new MainAdapter(GoodDetail.this, listUrl);
				glDetail.setAdapter(adapter);
				
				glDetail.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						bundle.putInt("postIndex", arg2);
						bundle.putSerializable("goodsDetail", detail);
						intent.setClass(GoodDetail.this, BigGallery.class);
						intent.putExtras(bundle);
						startActivity(intent);
					}
				});
			}
		}else{
			llgl = (RelativeLayout) findViewById(R.id.llgl);
			llgl.setVisibility(View.GONE);
		}
		backPageName = intent.getExtras().getString("backPageName");
		rl_test = (RelativeLayout) findViewById(R.id.test);
		llgl = (RelativeLayout) findViewById(R.id.llgl);

		tvTitle = (TextView) findViewById(R.id.tvTitle);
		btnBack = (Button) findViewById(R.id.btnBack);
		btnStore = (Button) findViewById(R.id.btnStore);

		txt_tittle = (TextView) findViewById(R.id.goods_tittle);
		txt_message1 = (TextView) findViewById(R.id.sendmess1);
		txt_phone = (TextView) findViewById(R.id.address1);
		txt_address = (TextView) findViewById(R.id.address2);
		rl_phone = (RelativeLayout) findViewById(R.id.showphone);
		rl_address = (RelativeLayout) findViewById(R.id.showmap);
		im_x = (ImageView) findViewById(R.id.ivCancel);

		ll_meta = (LinearLayout) findViewById(R.id.meta);
		
//		mapController = mapview.getController();
//		mapview.setBuiltInZoomControls(true);
//		
//		overlays = mapview.getOverlays();
//		projection = mapview.getProjection();

		if(isMyAd()){
			btnStore.setText(strManager);
		}
		else{
			if(isInMyStore()){
				btnStore.setText(strCancelCollect);
			}
			else{
				btnStore.setText(strCollect);
			}
		}

		// 设置监听器
		im_x.setOnClickListener(this);
		btnBack.setOnClickListener(this);
		btnStore.setOnClickListener(this);

		this.setMetaObject();
		
		// 赋值
		tvTitle.setText("详细信息");
		txt_message1.setText(detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_DESCRIPTION));
		txt_tittle.setText(detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_TITLE));
		btnBack.setText(backPageName);

		//判断当前是否有地域内容
		String areaNamesV = detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_AREANAME);
		if (areaNamesV != null && !areaNamesV.equals("")) 
		{
			txt_address.setText(areaNamesV);
			
			//判断当前的物品是否有经纬度
			String latV = detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_LAT);
			String lonV = detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_LON);
			if(latV != null && !latV.equals("false") && !latV.equals("") && lonV != null && !lonV.equals("false") && !lonV.equals(""))
			{
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

		String mobileV = detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_MOBILE);
		if (mobileV != null
				&& !mobileV.equals("")
				&& !mobileV.equals("无")) {
			txt_phone.setText(mobileV);
			rl_phone.setOnClickListener(this);
		} else {
			rl_phone.setVisibility(View.GONE);
//			txt_phone.setText("无");
//			rl_phone.setBackgroundResource(R.drawable.iv_bg_unclickable);
		}
	}
	
	private void handleStoreBtnClicked(){
		if(btnStore.getText().equals(strCollect)){
			List<GoodsDetail> myStore = myApp.getListMyStore();
			btnStore.setText(strCollected);
			if (myStore == null){
				myStore = new ArrayList<GoodsDetail>();
				myStore.add(detail);
			} else {
				if (myStore.size() >= 100) {
					myStore.remove(0);
				}
				myStore.add(detail);
			}		
			myApp.setListMyStore(myStore);
			Helper.saveDataToLocate(GoodDetail.this, "listMyStore", myStore);
			Toast.makeText(GoodDetail.this, "收藏成功", 3).show();
		}
		else if (btnStore.getText().equals(strCancelCollect)) {
			List<GoodsDetail> myStore = myApp.getListMyStore();
			for (int i = 0; i < myStore.size(); i++) {
				if (detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID)
						.equals(myStore.get(i).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID))) {
					myStore.remove(i);
					break;
				}
			}
			myApp.setListMyStore(myStore);
			Helper.saveDataToLocate(GoodDetail.this, "listMyStore", myStore);
			btnStore.setText(strCollect);
			Toast.makeText(GoodDetail.this, "取消收藏", 3).show();
		}
		else if(btnStore.getText().equals(strManager)){
			final String[] names = {"编辑","刷新","删除"};
			new AlertDialog.Builder(this).setTitle("选择操作")
					.setItems(names, new DialogInterface.OnClickListener(){
						public void onClick(DialogInterface dialog, int which){
							switch(which){
								case 0:
									Bundle bundle = new Bundle();
									bundle.putSerializable("goodsDetail", detail);
									bundle.putString("categoryEnglishName",detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_CATEGORYENGLISHNAME));
									intent.putExtras(bundle);									
									intent.setClass(GoodDetail.this, PostGoods.class);
									startActivity(intent);									
									dialog.dismiss();
									break;
								case 1:
									pd = ProgressDialog.show(GoodDetail.this, "提示", "请稍候...");
									pd.setCancelable(true);
									new Thread(new RequestThread(REQUEST_TYPE.REQUEST_TYPE_REFRESH)).start();
									dialog.dismiss();
									break;									
								case 2:
									pd = ProgressDialog.show(GoodDetail.this, "提示", "请稍候...");
									pd.setCancelable(true);
									new Thread(new RequestThread(REQUEST_TYPE.REQUEST_TYPE_DELETE)).start();
									dialog.dismiss();
									break;
								default:
									break;
							}
						}
					})
					.setNegativeButton(
				     "取消", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();							
						}
					}).show();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnBack:
			GoodDetail.this.finish();
			break;
		case R.id.btnStore:
			handleStoreBtnClicked();
			break;
		case R.id.showphone:
			final String[] names = {"打电话","发短信"};
			new AlertDialog.Builder(this).setTitle("选择联系方式")
					.setItems(names, this)
					.setNegativeButton(
				     "取消", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();							
						}
					})
				     .show();
			break;
		case R.id.showmap:
			String latV = detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_LAT);
			String lonV = detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_LON);
			if(latV != null && !latV.equals("false") && !latV.equals("") && lonV != null && !lonV.equals("false") && !lonV.equals(""))
			{
				bMapManager.start();
				double lat = Double.valueOf(latV);
				double lon = Double.valueOf(lonV);
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
			myHandler.sendEmptyMessage(msgShowMap);
			break;
		case R.id.ivCancel:
			if(bMapManager != null){
				bMapManager.stop();
			}
			myHandler.sendEmptyMessage(msgCancelMap);
			break;
		}
		super.onClick(v);
	}
	
	private void setMetaObject(){
		if(ll_meta == null) return;
		ll_meta.removeAllViews();
		LayoutInflater inflater = LayoutInflater.from(this);
		for (int i = 0; i < detail.getMetaData().size(); i++) {
			View v = null;
			v = inflater.inflate(R.layout.item_meta, null);

			TextView tvmetatxt = (TextView) v.findViewById(R.id.tvmetatxt);
			TextView tvmeta = (TextView) v.findViewById(R.id.tvmeta);

			tvmetatxt.setText(detail.getMetaData().get(i).split(" ")[0].toString() + "：");
			tvmeta.setText(detail.getMetaData().get(i).split(" ")[1].toString());
			v.setTag(i);
			ll_meta.addView(v);
		}
		Date date = new Date(Long.parseLong(detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_DATE)) * 1000);
		SimpleDateFormat df = new SimpleDateFormat("MM月dd日 HH:mm:ss",
				Locale.SIMPLIFIED_CHINESE);
		String strTime = df.format(date);
		View time = inflater.inflate(R.layout.item_meta, null);
		TextView timetxt = (TextView) time.findViewById(R.id.tvmetatxt);
		TextView timevalue = (TextView) time.findViewById(R.id.tvmeta);
		timetxt.setText("更新时间： ");
		timevalue.setText(strTime);
		ll_meta.addView(time);
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which){
		if(0 == which){
			Uri uri = Uri.parse("tel:" + txt_phone.getText().toString());
			Intent intent = new Intent(Intent.ACTION_DIAL, uri);
			startActivity(intent);
		}
		else if(1 == which){
			Uri uri = Uri.parse("smsto:" + txt_phone.getText().toString());
			Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
			startActivity(intent);
		}
	}

	public Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case msgShowMap:
				mapview.setVisibility(View.VISIBLE);
				im_x.setVisibility(View.VISIBLE);
				rl_test.setVisibility(View.GONE);
				break;
			case msgCancelMap:
				mapview.setVisibility(View.GONE);
				im_x.setVisibility(View.GONE);
				rl_test.setVisibility(View.VISIBLE);
				break;
			case msgRefresh:
				if(json == null){
					Toast.makeText(GoodDetail.this, "刷新失败，请稍后重试！", 0).show();
					break;
				}
				try {
					JSONObject jb = new JSONObject(json);
					JSONObject js = jb.getJSONObject("error");
					String message = js.getString("message");
					int code = js.getInt("code");
					if (code == 0) {
						new Thread(new RequestThread(REQUEST_TYPE.REQUEST_TYPE_UPDATE)).start();
						Toast.makeText(GoodDetail.this, message, 0).show();
					}else if(2 == code){
						if(pd != null){
							pd.dismiss();
						}
						new AlertDialog.Builder(GoodDetail.this).setTitle("提醒")
						.setMessage(message)
						.setPositiveButton("确定", new DialogInterface.OnClickListener() {							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								pd = ProgressDialog.show(GoodDetail.this, "提示", "请稍候...");
								pd.setCancelable(true);

								new Thread(new RequestThread(REQUEST_TYPE.REQUEST_TYPE_REFRESH, 1)).start();
								dialog.dismiss();
							}
						})
						.setNegativeButton(
					     "取消", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.cancel();							
							}
						})
					     .show();

					}else {
						if(pd != null){
							pd.dismiss();
						}
						Toast.makeText(GoodDetail.this, message, 0).show();
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;			
			case msgUpdate:
				if(pd!=null){
					pd.dismiss();
				}
				GoodsList goods = JsonUtil.getGoodsListFromJson(json);
				List<GoodsDetail> goodsDetails = goods.getData();
				if(goodsDetails != null && goodsDetails.size() > 0){
					for(int i = 0; i < goodsDetails.size(); ++ i){
						if(goodsDetails.get(i).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID)
								.equals(GoodDetail.this.detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID))){
							GoodDetail.this.detail = goodsDetails.get(i);
							break;
						}
					}
					List<GoodsDetail>listMyPost = myApp.getListMyPost();
					if(listMyPost != null){
						for(int i = 0; i < listMyPost.size(); ++ i){
							if(listMyPost.get(i).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID)
									.equals(GoodDetail.this.detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID))){
								listMyPost.set(i, GoodDetail.this.detail);
								break;
							}
						}
					}
					myApp.setListMyPost(listMyPost);
				}

				setMetaObject();
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

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
			BitmapFactory.Options o =  new BitmapFactory.Options();
            o.inPurgeable = true;
			Bitmap bmp = BitmapFactory.decodeResource(getResources(),
					R.drawable.red, o);
			canvas.drawBitmap(bmp, point.x, point.y, paint);
			bmp.recycle();
		}
	}
	
	class RequestThread implements Runnable{
		private REQUEST_TYPE type;
		private int pay = 0;
		public RequestThread(REQUEST_TYPE type){
			this.type = type;
		}
		public RequestThread(REQUEST_TYPE type, int pay) {
			this.type = type;
			this.pay = pay;
		}
		@Override
		public void run(){
			synchronized(GoodDetail.this){
				ArrayList<String> requests = null;
				String apiName = null;
				int msgToSend = -1;
				if(REQUEST_TYPE.REQUEST_TYPE_DELETE == type){
					requests = doDelete();
					apiName = "ad_delete";
					msgToSend = msgDelete;
				}
				else if(REQUEST_TYPE.REQUEST_TYPE_REFRESH == type){
					requests = doRefresh(this.pay);
					apiName = "ad_refresh";
					msgToSend = msgRefresh;
				}
				else if(REQUEST_TYPE.REQUEST_TYPE_UPDATE == type){
					requests = doUpdate();
					apiName = "ad_list";
					msgToSend = msgUpdate;
				}
				if(requests != null){
					String url = Communication.getApiUrl(apiName, requests);
					System.out.println("url--->" + url);
					try {
						json = Communication.getDataByUrl(url);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					myHandler.sendEmptyMessage(msgToSend);
				}
			}
		}
	}
	
	private ArrayList<String> doRefresh(int pay){
		json = "";
		ArrayList<String> list = new ArrayList<String>();

		UserBean user = (UserBean) Util.loadDataFromLocate(GoodDetail.this, "user");
		String mobile = user.getPhone();
		String password = user.getPassword();

		list.add("mobile=" + mobile);
		String password1 = Communication.getMD5(password);
		password1 += Communication.apiSecret;
		String userToken = Communication.getMD5(password1);
		list.add("userToken=" + userToken);
		list.add("adId=" + detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID));
		list.add("rt=1");
		if(pay != 0){
			list.add("pay=1");
		}

		return list;
	}
	
	private ArrayList<String> doUpdate(){
		json = "";
		ArrayList<String> list = new ArrayList<String>();
		
		UserBean user = (UserBean) Util.loadDataFromLocate(GoodDetail.this, "user");
		String mobile = user.getPhone();
		String password = user.getPassword();

		list.add("mobile=" + mobile);
		String password1 = Communication.getMD5(password);
		password1 += Communication.apiSecret;
		String userToken = Communication.getMD5(password1);
		list.add("userToken=" + userToken);
		list.add("query=id:" + detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID));
		list.add("rt=1");
		return list;		
	}
	
	private ArrayList<String> doDelete(){
		// TODO Auto-generated method stub
		UserBean user = (UserBean) Util.loadDataFromLocate(GoodDetail.this, "user");
		String mobile = user.getPhone();
		String password = user.getPassword();

		json = "";
//		String apiName = "ad_delete";
		ArrayList<String> list = new ArrayList<String>();
		list.add("mobile=" + mobile);
		String password1 = Communication.getMD5(password);
		password1 += Communication.apiSecret;
		String userToken = Communication.getMD5(password1);
		list.add("userToken=" + userToken);
		list.add("adId=" + detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID));
		list.add("rt=1");
		
		return list;		
	}

	class MainAdapter extends BaseAdapter {
		Context context;
		List<String> listUrl;

		public MainAdapter(Context context, List<String> listUrl) {
			this.context = context;
			this.listUrl = listUrl;
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
//			if (convertView != null) {
//				v = (ImageView) convertView;
//			} else {
				v = inflater.inflate(R.layout.item_detailview, null);
//			}
			ImageView iv = (ImageView) v.findViewById(R.id.ivGoods);
			
			BitmapFactory.Options o =  new BitmapFactory.Options();
            o.inPurgeable = true;
			Bitmap tmb = BitmapFactory.decodeResource(context.getResources(),R.drawable.moren1, o);
			Bitmap mb= Helper.toRoundCorner(tmb, 20);
			tmb.recycle();
			
			
			Bitmap tmb1 = BitmapFactory.decodeResource(context.getResources(),R.drawable.moren, o);
			Bitmap mb1= Helper.toRoundCorner(tmb1, 20);
			tmb1.recycle();
			
			iv.setImageBitmap(mb);
			
			
			if (type == 1) {
				iv.setLayoutParams(new Gallery.LayoutParams(86, 86));
			} else if (type == 2) {
				iv.setLayoutParams(new Gallery.LayoutParams(145, 145));
			} else if (type == 3) {
				iv.setLayoutParams(new Gallery.LayoutParams(210, 210));
			} else if (type == 4) {
				iv.setLayoutParams(new Gallery.LayoutParams(235, 235));
			} else if (type == 5) {
				iv.setLayoutParams(new Gallery.LayoutParams(240, 240));
			}else{
				iv.setLayoutParams(new Gallery.LayoutParams(245,245));
			}

			
			if (listUrl.size() != 0 && listUrl.get(position) != null) {
				iv.setTag(listUrl.get(position));
				SimpleImageLoader.showImg(iv, listUrl.get(position), GoodDetail.this);
			} else {
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
