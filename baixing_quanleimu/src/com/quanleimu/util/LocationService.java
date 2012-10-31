package com.quanleimu.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/*
import android.location.*;
import android.os.Bundle;
*/

import com.baidu.mapapi.*;
import com.quanleimu.activity.QuanleimuApplication;
import com.quanleimu.entity.BXLocation;

import android.location.Location;
import android.util.Log;


import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import android.content.Context;

public class LocationService{
	private BMapManager bMapManager;
	private MKLocationManager mkLocationManager;
	private Location lastKnownLocation;
	private BXLocationListener locationListener;
	
	public interface BXLocationServiceListener{
		abstract void onLocationUpdated(Location location);
	}
	
	public interface BXRgcListener{
		abstract void onRgcUpdated(BXLocation location);
	}
	
	private static LocationService s_instance;
	public static LocationService getInstance(){
		if(null == s_instance){
			s_instance = new LocationService();
		}
		return s_instance;
	}
	
	class BXSearchListener implements MKSearchListener {
		BXRgcListener rgcCallback = null;
		private float lat = 0;
		private float lon = 0;
		BXSearchListener(BXRgcListener listener){
			rgcCallback = listener;
		}
		
		BXSearchListener(BXRgcListener listener, float lat, float lon){
			rgcCallback = listener;
			this.lat = lat;
			this.lon = lon;
		}
		
	    @Override
	    public void onGetAddrResult(MKAddrInfo result, int iError) {
	    	Log.d("location", "location, onGetAddrResult reached");
	    	BXLocation location = null;
	    	if(0 == iError){//succeeded!
	    		location = new BXLocation(false);
	    		location.address = result.strBusiness;
	    		location.detailAddress = result.strAddr;
	    		location.cityName = result.addressComponents == null ? null : result.addressComponents.city.replace("市", "");
	    		location.subCityName = result.addressComponents == null ? null : result.addressComponents.district;
	    		location.adminArea = result.addressComponents == null ? null : result.addressComponents.province;
	    		if(null != location.cityName && location.cityName.length() > 0)
	    			location.geocoded = true;
	    		location.fGeoCodedLat = (float)(1.0f*result.geoPt.getLatitudeE6()/1e6);
	    		location.fGeoCodedLon = (float)(1.0f*result.geoPt.getLongitudeE6()/1e6);
	    		location.fLat = this.lat == 0 ? location.fGeoCodedLat : this.lat;
	    		location.fLon = this.lon == 0 ? location.fGeoCodedLon : this.lon;

	    	}
	    	
	    	if(null != rgcCallback){
	    		rgcCallback.onRgcUpdated(location);
	    	}
	    }
	 
	    @Override
	    public void onGetDrivingRouteResult(MKDrivingRouteResult result, int iError) {
	    	Log.d("location", "location, onGetDrivingRouteResult reached");
	    }
	 
	    @Override
	    public void onGetPoiResult(MKPoiResult result, int type, int iError) {
	    	Log.d("location", "location, onGetPoiResult reached");
	    }
	 
	    @Override
	    public void onGetTransitRouteResult(MKTransitRouteResult result, int iError) {
	    	Log.d("location", "location, onGetTransitRouteResult reached");
	    }
	 
	    @Override
	    public void onGetWalkingRouteResult(MKWalkingRouteResult result, int iError) {
	    	Log.d("location", "location, onGetWalkingRouteResult reached");
	    }
	 
	    @Override
	    public void onGetBusDetailResult(MKBusLineResult result, int iError) {
	    	Log.d("location", "location, onGetAddrResult reached");
	    }
	 
	    @Override
	    public void onGetSuggestionResult(MKSuggestionResult result, int iError) {
	    	Log.d("location", "location, onGetSuggestionResult reached");
	    }
	}
	
	public boolean geocode(String addr, String city, BXRgcListener callback){
		if(bMapManager == null) return false;
				
		MKSearch searcher = new MKSearch();
		searcher.init(bMapManager, new BXSearchListener(callback));
		bMapManager.start();	
		return searcher.geocode(addr, city) == 0;
		
	}
	
	public boolean reverseGeocode(final float lat, final float lon, BXRgcListener callback){
		if(bMapManager == null) return false;
		
		
		MKSearch searcher = new MKSearch();
		searcher.init(bMapManager, new BXSearchListener(callback, lat, lon));
		bMapManager.start();
		return 0 == searcher.reverseGeocode(new GeoPoint((int)(lat*1e6), (int)(lon*1e6)));
	}
	
	public android.location.Location getLastKnownLocation(){
		return lastKnownLocation;
	}
	
	public void start(Context context, BXLocationServiceListener listener){
//		this.userListener = listener;
		init(context, listener);
	}
	
	public void stop(){
		if(mkLocationManager != null){
			mkLocationManager.removeUpdates(locationListener);
		}
		if(bMapManager != null){
			bMapManager.stop();
		}
		bMapManager = null;
		mkLocationManager = null;
	}
	
	class BXLocationListener implements com.baidu.mapapi.LocationListener{
		private List<BXLocationServiceListener> userListeners = new ArrayList<BXLocationServiceListener>();
		
		public BXLocationListener(BXLocationServiceListener userListener){
			if(userListener != null)
				userListeners.add(userListener);
		}
		
		public void addListener(BXLocationServiceListener listener){
			if(null != listener){
				this.userListeners.remove(listener);
				this.userListeners.add(listener);
			}
		}
		
		public void removeListener(BXLocationServiceListener listener){
			if(null != listener){
				this.userListeners.remove(listener);
			}
		}
		
		@Override
		public void onLocationChanged (Location location){
			if(location != null){
				
				//纠偏以后的经纬度  
//				GeoPoint point = CoordinateConvert.bundleDecode(CoordinateConvert.fromWgs84ToBaidu(new GeoPoint((int)(location.getLatitude()*1e6), (int)(location.getLongitude()*1e6))));  
//				
//				//Log.d("LocationService", "gps encrypted from("+location.getLatitude()+", "+location.getLongitude()+") to ("+point.getLatitudeE6()/1000000.0+", "+point.getLongitudeE6()/1000000.0+") !!");
//				
//				location.setLatitude(1.0d*point.getLatitudeE6()/1e6);
//				location.setLongitude(1.0d*point.getLongitudeE6()/1e6);
//				location.setLatitude(27.900383);
//				location.setLongitude(112.577883);
				lastKnownLocation = location;
				
				for(BXLocationServiceListener listener : userListeners){
					listener.onLocationUpdated(location);
				}

			}
		}
	}
	
	public void addLocationListener(Context context, BXLocationServiceListener userListener){
		if(null == bMapManager || null == mkLocationManager || null == locationListener){
			start(context, userListener);
		}else{
			locationListener.addListener(userListener);
		}
	}
	
	public void removeLocationListener(BXLocationServiceListener userListener){
		if(null == bMapManager && null == mkLocationManager && null == locationListener){
			locationListener.removeListener(userListener);
		}		
	}
	
	private void init(Context context, BXLocationServiceListener userListener) {
		//if(locationListener == null){
		if(mkLocationManager != null && locationListener != null){
			mkLocationManager.removeUpdates(locationListener);
		}
		
		locationListener = new BXLocationListener(userListener); 
			
		//};	
		if(bMapManager == null){
			try{
				bMapManager = new BMapManager(context);
				bMapManager.init("736C4435847CB7D20DD1131064E35E8941C934F5", null);
			}catch(Throwable e){
				bMapManager = null;
				return;
			}
		}
		else{
			bMapManager.stop();
		}
		//mkSearch = new MKSearch();
		mkLocationManager = bMapManager.getLocationManager();
		mkLocationManager.requestLocationUpdates(locationListener);
		bMapManager.start();
	}
}

/*
public class LocationService{
	private BXLocationServiceListener userlistener;
	private LocationManager locationMgr;
	private LocationListener locationListener;
	
	public interface BXLocationServiceListener{
		abstract void onLocationUpdated(Location location);
	}
	
	private static LocationService s_instance;
	public static LocationService getInstance(){
		if(null == s_instance){
			s_instance = new LocationService();
		}
		return s_instance;
	}
	
	public Location getLastKnownLocation(){
		if(locationMgr != null){
			return locationMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		}
		return null;
	}
	
	public void start(Context context, BXLocationServiceListener listener){
		this.userlistener = listener;
		init(context, listener);
	}
	
	public void stop(){
		if(locationMgr != null){
			locationMgr.removeUpdates(locationListener);
		}
	}
	
	private void init(Context context, BXLocationServiceListener userListener) {
		locationMgr = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		locationListener = null;
		locationListener = new BXLocationListener(userListener);
		locationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0, locationListener);
	}
	
	public static String geocodeAddr(String latitude, String longitude) { 
	    String addr = ""; 
	    String url = String.format("http://ditu.google.cn/maps/geo?output=json&q=%s,%s",latitude, longitude);
	    URL myURL = null; 
	    URLConnection httpsConn = null; 
	    try { 
	        myURL = new URL(url); 
	    } catch (MalformedURLException e) { 
	        e.printStackTrace(); 
	        return null; 
	    } 
	    
	    try { 
	        httpsConn = (URLConnection) myURL.openConnection(); 
	        if (httpsConn != null) {
	            InputStreamReader insr = new InputStreamReader(httpsConn 
	            .getInputStream(), "UTF-8"); 
	            BufferedReader br = new BufferedReader(insr);
	            StringBuffer buffer=new StringBuffer();
	            String str;
	            while ((str=br.readLine())!=null) {
	                buffer.append(str);
	            }
	            JSONObject jsObj = new JSONObject(buffer.toString());
	            String placemarkStr = jsObj.getString("Placemark");  
	            JSONArray placemarkArray = new JSONArray(placemarkStr);  
	            String jsonDataPlacemarkStr = placemarkArray.get(0).toString();  
	            JSONObject jsonDataPlacemark = new JSONObject(jsonDataPlacemarkStr);  
	            String jsonAddressDetails = jsonDataPlacemark.getString("AddressDetails");  
	            JSONObject jsonDataAddressJDetails = new JSONObject(jsonAddressDetails);  
	            String jsonCountry = jsonDataAddressJDetails.getString("Country");  
	            JSONObject jsonDataCountry = new JSONObject(jsonCountry);  
	            String jsonAdministrativeArea = jsonDataCountry.getString("AdministrativeArea");  
	            JSONObject jsonDataAdministrativeArea = new JSONObject(jsonAdministrativeArea);    
	            String jsonLocality = jsonDataAdministrativeArea.getString("Locality");  
	            JSONObject jsonDataLocality = new JSONObject(jsonLocality);  
	            addr = jsonDataLocality.getString("LocalityName");
	        } 
	    }
		catch(JSONException e){
			e.printStackTrace();
			return null;
		}
		catch (IOException e) { 
	        e.printStackTrace(); 
	        return null; 
	    } 
	    return addr; 
	}
	
	class BXLocationListener implements LocationListener{
		private BXLocationServiceListener userListener;
		public BXLocationListener(BXLocationServiceListener userListener){
			this.userListener = userListener;
		}
		@Override
		public void onLocationChanged (Location location){
			if(location != null){
				if(userListener != null){
					userListener.onLocationUpdated(location);
				}

			}
		}
		
		@Override
		public void onProviderDisabled(String provider){
			
		}

		@Override
		public void onProviderEnabled(String provider){
			
		}
		
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras){
			
		}
		
	}
}
*/