package com.quanleimu.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
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
import com.quanleimu.util.Tracker;
import com.quanleimu.util.TrackConfig.TrackMobile.BxEvent;
import com.quanleimu.util.TrackConfig.TrackMobile.Key;

import java.util.List;

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
	@Override
	protected void onResume() {
	    if (mBMapMan != null) {
	        mBMapMan.start();
	    }
	    
        final MapView mapView = (MapView) findViewById(R.id.bmapsView);
         
        MapController mapController = mapView.getController();
        mapView.setBuiltInZoomControls(true);

        Bundle bundle = this.getIntent().getExtras();
        if(bundle != null){
	        String position = bundle.getString("detailPosition");
	        String[] positions = position.split(",");
	        if(positions.length == 2){
	        	endGeoPoint = new GeoPoint(Integer.parseInt(positions[0]), Integer.parseInt(positions[1]));
				mapController.animateTo(endGeoPoint);
				mapController.setZoom(15);
				
		        List<Overlay> overlays = mapView.getOverlays();
		        overlays.add(new MyLocationOverlays(endGeoPoint));
		        
		        MKLocationManager locationManager = mBMapMan.getLocationManager();	        
		        Location location = locationManager.getLocationInfo();
		        if (location != null)
		        {
		        	this.updateMyLocationOverlay(location);
		        }else
		        {
			        locationManager.requestLocationUpdates(this);
		        }
	        }
	        

        }	    
	    super.onResume();
	} 
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.baidumaplayout);
		if (mBMapMan == null) 
		{
			mBMapMan = new BMapManager(QuanleimuApplication.getApplication());
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
		Bundle bundle = this.getIntent().getExtras();
		if(bundle != null){
			String title = bundle.getString("title");
			if(title != null && !title.equals("")){
				((TextView)findViewById(R.id.tvTitle)).setText(title);
			}
		}
		
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
