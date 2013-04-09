package com.baixing.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import org.jivesoftware.smack.util.Base64;

import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.MapActivity;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.CoordinateConvert;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.LocationListener;
import com.baidu.mapapi.MKEvent;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.MKLocationManager;
import com.baidu.mapapi.MKPoiInfo;
import com.baidu.mapapi.MapController;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.MapView.LayoutParams;
import com.baidu.mapapi.MyLocationOverlay;
import com.baidu.mapapi.Overlay;
import com.baidu.mapapi.PoiOverlay;
import com.baidu.mapapi.Projection;
import com.baixing.data.GlobalDataManager;
import com.baixing.entity.Ad;
import com.baixing.entity.Ad.EDATAKEYS;
import com.baixing.entity.BXLocation;
import com.baixing.network.NetworkCommand;
import com.baixing.tracking.Tracker;
import com.baixing.tracking.TrackConfig.TrackMobile.BxEvent;
import com.baixing.tracking.TrackConfig.TrackMobile.Key;
import com.baixing.util.LocationService;
import com.baixing.util.ViewUtil;
import com.baixing.util.post.PostLocationService;
import com.quanleimu.activity.R;
import com.quanleimu.activity.R.drawable;
import com.quanleimu.activity.R.id;
import com.quanleimu.activity.R.layout;
import com.quanleimu.activity.R.style;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

import org.json.JSONObject;
          
public class BaiduMapActivity extends MapActivity implements LocationListener{

		// 授权Key
		// TODO: 请输入您的Key,
		// 申请地址：http://dev.baidu.com/wiki/static/imap/key/
		//713E99B1CD54866996162791BA789A0D9A13791B	
		public static String mStrKey = "736C4435847CB7D20DD1131064E35E8941C934F5";
		
		private View popView;

		// 常用事件监听，用来处理通常的网络错误，授权验证错误等
		public static class MyGeneralListener implements MKGeneralListener {
			boolean m_bKeyRight = true; // 授权Key正确，验证通过
			private Context context;
			MyGeneralListener(Context cxt) {
				context = cxt;
			}
			
			@Override
			public void onGetNetworkState(int iError) {
				ViewUtil.showToast((BaiduMapActivity)context, "您的网络出错啦！", true);
			}

			@Override
			public void onGetPermissionState(int iError) {
				if (iError == MKEvent.ERROR_PERMISSION_DENIED) {
					// 授权Key错误：
					ViewUtil.showToast((BaiduMapActivity)context, "请在BMapApiDemoApp.java文件输入正确的授权Key！", true);
					this.m_bKeyRight = false;
				}
			}

		}
	
	BMapManager mBMapMan = null;
	private GeoPoint endGeoPoint;
	@Override
	protected void onDestroy() {
	    if (mBMapMan != null) {
	    	mBMapMan.getLocationManager().removeUpdates(this);
	        mBMapMan.destroy();
	        mBMapMan = null;
	    }
	    super.onDestroy();
	}
	@Override
	protected void onPause() {
	    if (mBMapMan != null) {
			mBMapMan.getLocationManager().removeUpdates(this);	    	
	        mBMapMan.stop();
	    }
	    super.onPause();
	}
	
	private void setTargetCoordinate(final Ad detail){
		final String latV = detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_LAT);
		final String lonV = detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_LON);
		if(latV != null && !latV.equals("false") && !latV.equals("") && !latV.equals("0") && !latV.equals("0.0") 
				&& lonV != null && !lonV.equals("false") && !lonV.equals("") && !lonV.equals("0") && !lonV.equals("0.0"))
		{
//			final double lat = Double.valueOf(latV);
//			final double lon = Double.valueOf(lonV);
			Thread convertThread = new Thread(new Runnable(){
				@Override
				public void run(){
//					String baiduUrl = String.format("http://api.map.baidu.com/ag/coord/convert?from=2&to=4&x=%s&y=%s", 
//							String.valueOf(lat), String.valueOf(lon));
					String baiduUrl = String.format("http://api.map.baidu.com/ag/coord/convert?from=2&to=4&x=%s&y=%s", 
							latV, lonV);
					
					try{
						String baiduJsn = NetworkCommand.doGet(BaiduMapActivity.this, baiduUrl);
						JSONObject js = new JSONObject(baiduJsn);
						Object errorCode = js.get("error");
						if(errorCode instanceof Integer && (Integer)errorCode == 0){
							String x = (String)js.get("x");
							String y = (String)js.get("y");
							byte[] bytes = Base64.decode(x);
							x = new String(bytes, "UTF-8");
							
							bytes = Base64.decode(y);
							y = new String(bytes, "UTF-8");
							
							Double dx = Double.valueOf(x);
							Double dy = Double.valueOf(y);
							
							int ix = (int)(dx * 1E6);
							int iy = (int)(dy * 1E6);
							
							x = String.valueOf(ix);
							y = String.valueOf(iy);
							
							applyToMap(x, y);
							return;
						}

					}catch(UnsupportedEncodingException e){
						e.printStackTrace();
					}catch(Exception e){
						e.printStackTrace();
					}
					applyToMap(String.valueOf((int)((Double.valueOf(latV))*1E6)), String.valueOf((int)((Double.valueOf(lonV)*1E6))));
				}
			});
			convertThread.start();
		}
		else{
			Thread getCoordinate = new Thread(new Runnable(){
	            @Override
	            public void run() {
	            	if(GlobalDataManager.getInstance().getApplicationContext() == null) return;
					String city = GlobalDataManager.getInstance().cityName;
					String address = city + detail.getValueByKey(EDATAKEYS.EDATAKEYS_AREANAME);
					
					if(!address.equals("")){
						BXLocation bxloc = LocationService.retreiveCoorFromGoogle(address);
//						String googleUrl = String.format("http://maps.google.com/maps/geo?q=%s&output=csv", address);
						String x = String.valueOf((int)(bxloc.fGeoCodedLat * 1e6));
						String y = String.valueOf((int)(bxloc.fGeoCodedLon * 1e6));
						applyToMap(x, y);
					}	
	            }
			});
			getCoordinate.start();

		}
	}
	
	private void applyToMap(final String x, final String y){
		this.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				try//Add try catch block to make sure to avoid uncaught exception.
				{
					endGeoPoint = new GeoPoint(Integer.parseInt(x), Integer.parseInt(y));
					
					MapView mapView = (MapView) findViewById(R.id.bmapsView);
					BMapManager manager = mBMapMan;
					if (mapView == null || manager == null) //This runnable may execute after activity is destroyed.
					{
						return;
					}
					
					MapController mapController = mapView.getController();
					mapController.animateTo(endGeoPoint);
					mapController.setZoom(15);
					
					List<Overlay> overlays = mapView.getOverlays();
					if (overlays != null){
						overlays.add(new MyPositionOverlays(endGeoPoint));
					}
					
					MKLocationManager locationManager = manager.getLocationManager();
					locationManager.setLocationCoordinateType(MKLocationManager.MK_COORDINATE_WGS84);
					Location location = locationManager.getLocationInfo();
					if (location != null){
						GeoPoint point = CoordinateConvert.bundleDecode(CoordinateConvert.fromWgs84ToBaidu(new GeoPoint((int)(location.getLatitude()*1e6), (int)(location.getLongitude()*1e6))));
						location.setLatitude(1.0d*point.getLatitudeE6()/1e6);
						location.setLongitude(1.0d*point.getLongitudeE6()/1e6);
						updateMyLocationOverlay(location);
					} else{
						locationManager.requestLocationUpdates(BaiduMapActivity.this);
					}
					manager.start();
					
					if(popView != null){
						MapView.LayoutParams geoLP = (MapView.LayoutParams) popView.getLayoutParams();  
				        geoLP.point = endGeoPoint;  
				        mapView.updateViewLayout(popView, geoLP);
				        popView.setVisibility(View.VISIBLE);  
					}
				}
				catch(Throwable t)
				{
					//Ignore any exception
				}
			}
		});

	}
    

	
	@Override
	protected void onResume() {
		super.onResume();
	    if (mBMapMan != null) {
	        mBMapMan.start();
	    }
	    
        MapView mapView = (MapView) findViewById(R.id.bmapsView);
         
        MapController mapController = mapView.getController();
        Location location = LocationService.getInstance().getLastKnownLocation();
        if(location != null){
			GeoPoint point = CoordinateConvert.bundleDecode(CoordinateConvert.fromWgs84ToBaidu(new GeoPoint((int)(location.getLatitude()*1e6), (int)(location.getLongitude()*1e6))));
			GeoPoint gp = new GeoPoint((int)(point.getLatitudeE6()), (int)(point.getLongitudeE6()));
//	        GeoPoint gp = new GeoPoint((int)(location.getLatitude() * 1E6), (int)(location.getLongitude() * 1E6));			
	        mapController.setCenter(gp);
        }
        mapView.setBuiltInZoomControls(true);

        Bundle bundle = this.getIntent().getExtras();
        if(bundle != null){
	        Ad position = (Ad)bundle.getSerializable("detail");
	        if(position == null) return;
			String areaname = position.getValueByKey(EDATAKEYS.EDATAKEYS_AREANAME);
			if(areaname != null){
				String[] aryArea = areaname.split(",");
				if(aryArea != null && aryArea.length > 0){
					((TextView)findViewById(R.id.tvTitle)).setText(aryArea[aryArea.length - 1]);
				}
			}
	        setTargetCoordinate(position);
	        
	        if(popView != null){
	        	((TextView)popView.findViewById(R.id.map_bubbleTitle)).setText(position.getValueByKey(EDATAKEYS.EDATAKEYS_TITLE));
	        	((TextView)popView.findViewById(R.id.map_bubbleText)).setText(position.getValueByKey(EDATAKEYS.EDATAKEYS_AREANAME));
	        }
        }	    
	    
	} 
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.setTheme(R.style.lightTheme);
		super.onCreate(savedInstanceState);
		if(GlobalDataManager.context == null){
			GlobalDataManager.context = new WeakReference<Context>(this);
		}
		this.setContentView(R.layout.baidumaplayout);
		findViewById(R.id.search_action).setVisibility(View.GONE);
		findViewById(R.id.right_action).setVisibility(View.GONE);
		if (savedInstanceState == null)
		{
			TextView tTitle = (TextView) findViewById(R.id.tvTitle);
			tTitle.setText("位置");
		}
		
		if (mBMapMan == null) 
		{
			mBMapMan = new BMapManager(GlobalDataManager.getInstance().getApplicationContext());
			mBMapMan.init(mStrKey, new MyGeneralListener(this));
		}
		this.findViewById(R.id.left_action).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				BaiduMapActivity.this.finish();
			}
		});
		this.findViewById(R.id.left_action).setPadding(0, 0, 0, 0);		
        super.initMapActivity(mBMapMan);
        
        MapView mapView = (MapView) findViewById(R.id.bmapsView);
		if(popView == null){
			LayoutInflater inflater = LayoutInflater.from(BaiduMapActivity.this);
	        popView = inflater.inflate(R.layout.map_bubble, null);
		}
        mapView.addView( popView,  
              new MapView.LayoutParams(MapView.LayoutParams.WRAP_CONTENT, MapView.LayoutParams.WRAP_CONTENT,  
            		  null, MapView.LayoutParams.BOTTOM_CENTER));  
        popView.setVisibility(View.GONE);  

	}
	
	private void updateMyLocationOverlay(Location location)
	{
		try //Add try catch block to make sure to avoid uncaught exception.
		{
			if (location == null || this.endGeoPoint == null)
				return;
			
			GeoPoint geoPoint = new GeoPoint((int)(location.getLatitude()*1E6), (int)(location.getLongitude()*1E6));
			GeoPoint midPoint = new GeoPoint((endGeoPoint.getLatitudeE6()+geoPoint.getLatitudeE6())/2, (endGeoPoint.getLongitudeE6()+geoPoint.getLongitudeE6())/2);
			int latSpan = Math.abs(endGeoPoint.getLatitudeE6()-geoPoint.getLatitudeE6());
			int longSpan = Math.abs(endGeoPoint.getLongitudeE6()-geoPoint.getLongitudeE6());
			
			MapView mapView = (MapView) findViewById(R.id.bmapsView);
			if (mapView == null)
			{
				return;
			}
			
			MyLocationOverlay mylocationOverlay = new MyLocationOverlay(this, mapView);
			mylocationOverlay.enableMyLocation();
			mapView.getOverlays().add(mylocationOverlay);	
			mapView.getController().animateTo(midPoint);
			mapView.getController().zoomToSpan(latSpan*2, longSpan*2);			
		}
		catch(Throwable t)
		{
			//Ignore any exception.
		}
	}
	
//	class AdOverlay extends Overlay{
//		private GeoPoint geoPoint;
//		private String content;
//		public AdOverlay(GeoPoint geoPoint, String content){
//			geoPoint = geoPoint;
//			this.content = content;
//		}
//		
//		@Override
//		public void draw(Canvas canvas, MapView mapView, boolean shadow) {
//			super.draw(canvas, mapView, shadow);
//			Point point = new Point();
//			Projection projection = mapView.getProjection();
//			projection.toPixels(geoPoint, point);
//			Paint paint = new Paint();
//			BitmapFactory.Options o =  new BitmapFactory.Options();
//            o.inPurgeable = true;
//			Bitmap bmp = BitmapFactory.decodeResource(getResources(),
//					R.drawable.red, o);
//			canvas.drawBitmap(bmp, point.x, point.y, paint);
//			bmp.recycle();
//		}		
//	}
	
	class MyPositionOverlays extends Overlay {
		GeoPoint geoPoint;

		public MyPositionOverlays(GeoPoint geoPoint) {
			super();
			this.geoPoint = geoPoint;
		}

		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow) {
			super.draw(canvas, mapView, shadow);
			Point point = new Point();
			Projection projection = mapView.getProjection();
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

	@Override
	public void onLocationChanged(Location location) {
		if (location != null) {
			Tracker.getInstance().event(BxEvent.GPS).append(Key.GPS_RESULT, true)
					.append(Key.GPS_GEO, String.format("(%f,%f)", location.getLatitude(),location.getLongitude())).end();
		}else
		{
			Tracker.getInstance().event(BxEvent.GPS).append(Key.GPS_RESULT, false).end();
		}
		if (location != null && isInChina(location)) {
			GeoPoint point = CoordinateConvert.bundleDecode(CoordinateConvert.fromWgs84ToBaidu(new GeoPoint((int)(location.getLatitude()*1e6), (int)(location.getLongitude()*1e6))));
			location.setLatitude(1.0d*point.getLatitudeE6()/1e6);
			location.setLongitude(1.0d*point.getLongitudeE6()/1e6);
			if (isInChina(location)) {
				this.updateMyLocationOverlay(location);
			}
		} 
		mBMapMan.getLocationManager().removeUpdates(this);
	}
	
	private boolean isInChina(Location location) {
		return !(location.getLatitude() < 3 || location.getLatitude() > 54 || location.getLongitude() < 73 || location.getLongitude() > 136);
	}
	
	
}
