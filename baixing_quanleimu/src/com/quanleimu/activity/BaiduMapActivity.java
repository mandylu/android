package com.quanleimu.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.baidu.mapapi.MapActivity;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.MapController;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.Overlay;
import com.baidu.mapapi.Projection;
import java.util.List;

public class BaiduMapActivity extends MapActivity{
	
	BMapManager mBMapMan = null;
	@Override
	protected void onDestroy() {
	    if (mBMapMan != null) {
	        mBMapMan.destroy();
	        mBMapMan = null;
	    }
	    super.onDestroy();
	}
	@Override
	protected void onPause() {
	    if (mBMapMan != null) {
	        mBMapMan.stop();
	    }
	    super.onPause();
	}
	@Override
	protected void onResume() {
	    if (mBMapMan != null) {
	        mBMapMan.start();
	    }
	    
        MapView mapView = (MapView) findViewById(R.id.bmapsView);
         
        MapController mapController = mapView.getController();
        mapView.setBuiltInZoomControls(true);

        Bundle bundle = this.getIntent().getExtras();
        if(bundle != null){
	        String position = bundle.getString("detailPosition");
	        String[] positions = position.split(",");
	        if(positions.length == 2){
	        	GeoPoint endGeoPoint = new GeoPoint(Integer.parseInt(positions[0]), Integer.parseInt(positions[1]));
				mapController.animateTo(endGeoPoint);
				mapController.setZoom(15);
				
		        List<Overlay> overlays = mapView.getOverlays();
		        overlays.add(new MyLocationOverlays(endGeoPoint));
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
		this.findViewById(R.id.btnLeft).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				BaiduMapActivity.this.finish();
			}
		});
		Bundle bundle = this.getIntent().getExtras();
		if(bundle != null){
			String title = bundle.getString("title");
			if(title != null && !title.equals("")){
				((TextView)findViewById(R.id.tvTitle)).setText(title);
			}
		}
		
        super.initMapActivity(mBMapMan);
        
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
}
