package com.quanleimu.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import org.jivesoftware.smack.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.baidu.mapapi.MapActivity;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.LocationListener;
import com.baidu.mapapi.MKLocationManager;
import com.baidu.mapapi.MapController;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.MyLocationOverlay;
import com.baidu.mapapi.Overlay;
import com.baidu.mapapi.Projection;
import com.quanleimu.entity.GoodsDetail;
import com.quanleimu.entity.GoodsDetail.EDATAKEYS;
import com.quanleimu.util.Communication;
import com.quanleimu.util.LocationService;
import com.quanleimu.util.Tracker;
import com.quanleimu.util.TrackConfig.TrackMobile.BxEvent;
import com.quanleimu.util.TrackConfig.TrackMobile.Key;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.List;

import org.json.JSONObject;

public class BaiduMapActivity extends MapActivity implements LocationListener{
	
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
	
	private void setTargetCoordinate(final GoodsDetail detail){
		final String latV = detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_LAT);
		final String lonV = detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_LON);
		if(latV != null && !latV.equals("false") && !latV.equals("") && !latV.equals("0") && lonV != null && !lonV.equals("false") && !lonV.equals("") && !lonV.equals("0"))
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
						String baiduJsn = Communication.getDataByUrlGet(baiduUrl);
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
					applyToMap(String.valueOf((int)((Double.valueOf(latV))*1E6)), String.valueOf((int)((Double.valueOf(latV)*1E6))));
				}
			});
			convertThread.start();
		}
		else{
			Thread getCoordinate = new Thread(new Runnable(){
	            @Override
	            public void run() {
	            	if(QuanleimuApplication.getApplication().getApplicationContext() == null) return;
					String city = QuanleimuApplication.getApplication().cityName;
					if(!city.equals("")){
						String googleUrl = String.format("http://maps.google.com/maps/geo?q=%s&output=csv", city);
						try{
							String googleJsn = Communication.getDataByUrlGet(googleUrl);
							String[] info = googleJsn.split(",");
							if(info != null && info.length == 4){
								String x = Integer.toString((int)(Double.parseDouble(info[2]) * 1E6));
								String y = Integer.toString((int)(Double.parseDouble(info[3]) * 1E6));
								applyToMap(x, y);
							}
						}catch(UnsupportedEncodingException e){
							e.printStackTrace();
						}catch(Exception e){
							e.printStackTrace();
						}
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
		    	endGeoPoint = new GeoPoint(Integer.parseInt(x), Integer.parseInt(y));
		    	
		    	MapView mapView = (MapView) findViewById(R.id.bmapsView);
		    	MapController mapController = mapView.getController();
				mapController.animateTo(endGeoPoint);
				mapController.setZoom(15);
				
		        List<Overlay> overlays = mapView.getOverlays();
		        overlays.add(new MyLocationOverlays(endGeoPoint));
		        
		        MKLocationManager locationManager = mBMapMan.getLocationManager();	        
		        Location location = locationManager.getLocationInfo();
		        if (location != null){
		        	updateMyLocationOverlay(location);
		        } else{
			        locationManager.requestLocationUpdates(BaiduMapActivity.this);
		        }
		        mBMapMan.start();
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
	        GeoPoint gp = new GeoPoint((int)(location.getLatitude() * 1E6), (int)(location.getLongitude() * 1E6));
	        mapController.setCenter(gp);
        }
        mapView.setBuiltInZoomControls(true);

        Bundle bundle = this.getIntent().getExtras();
        if(bundle != null){
	        GoodsDetail position = (GoodsDetail)bundle.getSerializable("detail");
	        if(position == null) return;
			String areaname = position.getValueByKey(EDATAKEYS.EDATAKEYS_AREANAME);
			if(areaname != null){
				String[] aryArea = areaname.split(",");
				if(aryArea != null && aryArea.length > 0){
					((TextView)findViewById(R.id.tvTitle)).setText(aryArea[aryArea.length - 1]);
				}
			}
	        setTargetCoordinate(position);
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
		if(QuanleimuApplication.context == null){
			QuanleimuApplication.context = new WeakReference<Context>(this);
		}
		this.setContentView(R.layout.baidumaplayout);
		if (mBMapMan == null) 
		{
			mBMapMan = new BMapManager(QuanleimuApplication.getApplication().getApplicationContext());
			mBMapMan.init(QuanleimuApplication.getApplication().mStrKey, new QuanleimuApplication.MyGeneralListener());
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
        
	}
	
	private void updateMyLocationOverlay(Location location)
	{
		if (location == null || this.endGeoPoint == null)
			return;
		
		GeoPoint geoPoint = new GeoPoint((int)(location.getLatitude()*1E6), (int)(location.getLongitude()*1E6));
		GeoPoint midPoint = new GeoPoint((endGeoPoint.getLatitudeE6()+geoPoint.getLatitudeE6())/2, (endGeoPoint.getLongitudeE6()+geoPoint.getLongitudeE6())/2);
		int latSpan = Math.abs(endGeoPoint.getLatitudeE6()-geoPoint.getLatitudeE6());
		int longSpan = Math.abs(endGeoPoint.getLongitudeE6()-geoPoint.getLongitudeE6());
		
		MapView mapView = (MapView) findViewById(R.id.bmapsView);
		
        MyLocationOverlay mylocationOverlay = new MyLocationOverlay(this, mapView);
        mylocationOverlay.enableMyLocation();
        mapView.getOverlays().add(mylocationOverlay);	
		mapView.getController().animateTo(midPoint);
		mapView.getController().zoomToSpan(latSpan*2, longSpan*2);
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
		this.updateMyLocationOverlay(location);
		mBMapMan.getLocationManager().removeUpdates(this);
	}
	
	
}
