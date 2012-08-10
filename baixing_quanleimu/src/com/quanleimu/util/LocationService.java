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
	
	public void reverseGeocode(final float lat, final float lon, BXRgcListener callback){
		
		class BXSearchListener implements MKSearchListener {
			BXRgcListener rgcCallback = null;
			
			BXSearchListener(BXRgcListener listener){
				rgcCallback = listener;
			}
			
		    @Override
		    public void onGetAddrResult(MKAddrInfo result, int iError) {
		    	BXLocation location = null;
		    	if(0 == iError){//succeeded!
		    		location = new BXLocation(false);
		    		location.fLat = lat;
		    		location.fLon = lon;
		    		location.address = result.strBusiness;
		    		location.detailAddress=result.strAddr;
		    		location.cityName=result.addressComponents.city.replace("市", "");
		    		location.subCityName=result.addressComponents.district;
		    		location.adminArea=result.addressComponents.province;
		    		if(null != location.cityName && location.cityName.length() > 0)
		    			location.geocoded = true;
		    		location.fGeoCodedLat=(float)(1.0f*result.geoPt.getLatitudeE6()/1e6);
		    		location.fGeoCodedLon=(float)(1.0f*result.geoPt.getLongitudeE6()/1e6);
		    	}
		    	
		    	if(null != rgcCallback){
		    		rgcCallback.onRgcUpdated(location);
		    	}
		    }
		 
		    @Override
		    public void onGetDrivingRouteResult(MKDrivingRouteResult result, int iError) {
		    }
		 
		    @Override
		    public void onGetPoiResult(MKPoiResult result, int type, int iError) {
		    }
		 
		    @Override
		    public void onGetTransitRouteResult(MKTransitRouteResult result, int iError) {
		    }
		 
		    @Override
		    public void onGetWalkingRouteResult(MKWalkingRouteResult result, int iError) {
		    }
		 
		    @Override
		    public void onGetBusDetailResult(MKBusLineResult result, int iError) {
		    }
		 
		    @Override
		    public void onGetSuggestionResult(MKSuggestionResult result, int iError) {
		    }
		}
		
		MKSearch searcher = new MKSearch();
		searcher.init(bMapManager, new BXSearchListener(callback));
			
		searcher.reverseGeocode(new GeoPoint((int)(lat*1e6), (int)(lon*1e6)));
	}
	
	
//	public static BXLocation geocodeAddr(String latitude, String longitude) { 
//	    BXLocation location = new BXLocation(false);
//	    location.fLat = Float.valueOf(latitude);
//	    location.fLon = Float.valueOf(longitude);
//
//	    String url = String.format("http://ditu.google.cn/maps/geo?output=json&q=%s,%s",latitude, longitude);
//	    URL myURL = null; 
//	    URLConnection httpsConn = null; 
//	    try { 
//	        myURL = new URL(url); 
//	    } catch (MalformedURLException e) { 
//	        e.printStackTrace(); 
//	        return null; 
//	    } 
//	    
//	    try { 
//	        httpsConn = (URLConnection) myURL.openConnection(); 
//	        if (httpsConn != null) {
//	            InputStreamReader insr = new InputStreamReader(httpsConn 
//	            .getInputStream(), "UTF-8"); 
//	            BufferedReader br = new BufferedReader(insr);
//	            StringBuffer buffer=new StringBuffer();
//	            String str;
//	            while ((str=br.readLine())!=null) {
//	                buffer.append(str);
//	            }
//	            
//	            /*{
//					  "name": "31.2087610008053180, 121.4526269709240723094",
//					  "Status": {
//					    "code": 200,
//					    "request": "geocode"
//					  },
//					  "Placemark": [ {
//					    "id": "p1",
//					    "address": "华氏大药房 Xuhui, Shanghai, China, 200031",
//					    "AddressDetails": {
//					   "Accuracy" : 9,
//					   "Country" : {
//					      "CountryName" : "中国",
//					      "CountryNameCode" : "CN",
//					      "Locality" : {
//					         "DependentLocality" : {
//					            "AddressLine" : [ "华氏大药房" ],
//					            "DependentLocalityName" : "徐汇区",
//					            "PostalCode" : {
//					               "PostalCodeNumber" : "200031"
//					            }
//					         },
//					         "LocalityName" : "上海市"
//					      }
//					   }
//					},
//					    "ExtendedData": {
//					      "LatLonBox": {
//					        "north": 31.2186897,
//					        "south": 31.1996033,
//					        "east": 121.4683644,
//					        "west": 121.4363496
//					      }
//					    },
//					    "Point": {
//					      "coordinates": [ 121.4523570, 31.2091470, 0 ]
//					    }
//					  } ]
//					}
//	             */
//	            if(buffer.length() > 0){
//		            JSONObject jsObj = new JSONObject(buffer.toString());
//		            
//		            if(null != jsObj.getJSONObject("Status") && jsObj.getJSONObject("Status").getInt("code") == 200){
//		            	
//		            	JSONArray placemarkArray = jsObj.getJSONArray("Placemark");  
//		            	if(placemarkArray.length() > 0){
//		            		location.geocoded = true;
//		            		
//		            		//parent node for place description -- take the first matching result
//			            	JSONObject jsonDataPlacemark = placemarkArray.getJSONObject(0);
//				            location.detailAddress = jsonDataPlacemark.getString("address");  
//				            
//				            JSONArray coord = jsonDataPlacemark.getJSONObject("Point").getJSONArray("coordinates");
//				            location.fGeoCodedLon = (float)coord.getDouble(0);
//				            location.fGeoCodedLat = (float)coord.getDouble(1);
//				            
//				            JSONObject jsonAdministrativeArea = jsonDataPlacemark.getJSONObject("AddressDetails").getJSONObject("Country").getJSONObject("AdministrativeArea");
//				            location.adminArea = jsonAdministrativeArea.getString("AdministrativeAreaName");
//				            
//				            JSONObject jsonLocality = jsonAdministrativeArea.getJSONObject("Locality");
//				            location.cityName = jsonLocality.getString("LocalityName").replace("市", "");
//				            
//				            JSONObject jsonDependentLocality = jsonLocality.getJSONObject("DependentLocality"); 
//				            location.subCityName = jsonDependentLocality.getString("DependentLocalityName");
//				            location.postCode = jsonDependentLocality.getJSONObject("PostalCode").getString("PostalCodeNumber");
//				            location.address = jsonDependentLocality.getJSONObject("Thoroughfare").getString("ThoroughfareName");
//		            	}
//		            }
//	            }
//	        } 
//	    }
//		catch(JSONException e){
//			e.printStackTrace();
//		}
//		catch (IOException e) { 
//	        e.printStackTrace(); 
//	    } 
//	    
//	    return location; 
//	}
	
	
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
				GeoPoint point = CoordinateConvert.bundleDecode(CoordinateConvert.fromWgs84ToBaidu(new GeoPoint((int)(location.getLatitude()*1e6), (int)(location.getLongitude()*1e6))));  
				
				//Log.d("LocationService", "gps encrypted from("+location.getLatitude()+", "+location.getLongitude()+") to ("+point.getLatitudeE6()/1000000.0+", "+point.getLongitudeE6()/1000000.0+") !!");
				
				location.setLatitude(1.0d*point.getLatitudeE6()/1e6);
				location.setLongitude(1.0d*point.getLongitudeE6()/1e6);
				
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