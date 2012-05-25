package com.quanleimu.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/*
import android.location.*;
import android.os.Bundle;
*/

import com.baidu.mapapi.*;
import android.location.Location;


import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import com.quanleimu.activity.Loading2;

import android.content.Context;

public class LocationService{
	private BMapManager bMapManager;
	private MKLocationManager mkLocationManager;
	private Location lastKnownLocation;
	private MKSearch mkSearch;
	private LocationListener locationListener;
	private GeoPoint gp;
	private BXLocationServiceListener userListener;
	
	
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
	
	
	public android.location.Location getLastKnownLocation(){
		return lastKnownLocation;
	}
	
	public void start(Context context, BXLocationServiceListener listener){
		this.userListener = listener;
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
		private BXLocationServiceListener userListener;
		public BXLocationListener(BXLocationServiceListener userListener){
			this.userListener = userListener;
		}
		@Override
		public void onLocationChanged (Location location){
			if(location != null){
				lastKnownLocation = location;
				if(userListener != null){
					userListener.onLocationUpdated(location);
				}

			}
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
			bMapManager = new BMapManager(context);
			bMapManager.init("736C4435847CB7D20DD1131064E35E8941C934F5", null);
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