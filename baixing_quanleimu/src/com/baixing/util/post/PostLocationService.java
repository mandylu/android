//xumengyi@baixing.com
package com.baixing.util.post;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.json.JSONObject;

import android.os.Handler;
import android.os.Message;
import android.util.Pair;

import com.baidu.mapapi.CoordinateConvert;
import com.baidu.mapapi.GeoPoint;
import com.baixing.data.GlobalDataManager;
import com.baixing.data.LocationManager;
import com.baixing.entity.BXLocation;
import com.baixing.network.NetworkCommand;
import com.baixing.util.LocationService;
import com.baixing.util.LocationService.BXRgcListener;

public class PostLocationService implements BXRgcListener, LocationManager.onLocationFetchedListener {
	private boolean gettingLocationFromBaidu = false;
	private boolean inreverse = false;
	private Handler handler;
	
	public PostLocationService(Handler handler){
		this.handler = handler;
	}
	
	public void start(){
		inreverse = false;
		GlobalDataManager.getInstance().getLocationManager().addLocationListener(this);
	}
	
	public void stop(){
		GlobalDataManager.getInstance().getLocationManager().removeLocationListener(this);
	}
	
	public boolean retreiveLocation(String city, String addr){
		this.gettingLocationFromBaidu = true;
		return LocationService.getInstance().geocode(addr, city, this);
	}
	
	static public Pair<Double, Double> retreiveCoorFromGoogle(String addr){
		if(addr == null || addr.equals("")){
			return new Pair<Double, Double>((double)0, (double)0);
		}
		String googleUrl = String.format("http://maps.google.com/maps/geo?q=%s&output=csv", URLEncoder.encode(addr));
		try{
			String googleJsn = NetworkCommand.doGet(GlobalDataManager.getInstance().getApplicationContext(), googleUrl);//Communication.getDataByUrlGet(googleUrl);
//			String googleJsn = WebUtils.doGet(GlobalDataManager.getInstance().getApplicationContext(), googleUrl, null);//Communication.getDataByUrlGet(googleUrl);
			String[] info = googleJsn.split(",");
			if(info != null && info.length == 4){
				return new Pair<Double, Double>(Double.parseDouble(info[2]), Double.parseDouble(info[3]));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return new Pair<Double, Double>((double)0, (double)0);
	}
	
	static public Pair<Double, Double> getGeoFromBaidu(String addr, String city) {
		if(addr == null || addr.equals("")){
			return new Pair<Double, Double>((double)0, (double)0);
		}
		try{
			String sub = String.format("address=%s&output=json&key=736C4435847CB7D20DD1131064E35E8941C934F5&city=%s", URLEncoder.encode(addr), URLEncoder.encode(city));
			String url = "http://api.map.baidu.com/geocoder?" + sub;
			String response = NetworkCommand.doGet(GlobalDataManager.getInstance().getApplicationContext(), url);
			
			JSONObject json = new JSONObject(response);
			if ("OK".equals(json.getString("status"))) {
				JSONObject locObj = json.getJSONObject("result").getJSONObject("location");
				if(locObj != null){
					return new Pair<Double, Double>(locObj.getDouble("lat"), locObj.getDouble("lng"));
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return new Pair<Double, Double>((double)0, (double)0);
	}
	
	@Override
	public void onRgcUpdated(BXLocation location) {
		// TODO Auto-generated method stub
		if(!this.gettingLocationFromBaidu) return;
		// TODO Auto-generated method stub
		if(!inreverse && location != null && (location.subCityName == null || location.subCityName.equals(""))){
			GeoPoint point = CoordinateConvert.bundleDecode(CoordinateConvert.fromWgs84ToBaidu(new GeoPoint((int)(location.fLat*1e6), (int)(location.fLon*1e6))));
//			GeoPoint gp = new GeoPoint((int)(point.getLatitudeE6()), (int)(point.getLongitudeE6()));
			float transferredLat = (float) (1.0d*point.getLatitudeE6()/1e6);
			float transferredLon = (float)(1.0d*point.getLongitudeE6()/1e6); 

//			LocationService.getInstance().reverseGeocode(location.fLat, location.fLon, this);
			LocationService.getInstance().reverseGeocode(transferredLat, transferredLon, this);			
			inreverse = true;
		}else{
			Message msg = Message.obtain();
			msg.what = PostCommonValues.MSG_GEOCODING_FETCHED;
			msg.obj = location;
			handler.sendMessage(msg);
		}		
		gettingLocationFromBaidu = false;
	}

	@Override
	public void onLocationFetched(BXLocation location) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onGeocodedLocationFetched(BXLocation location) {
		// TODO Auto-generated method stub
		if(location == null) return;
		Message msg = Message.obtain();
		msg.what = PostCommonValues.MSG_GPS_LOC_FETCHED;
		msg.obj = location;
		handler.sendMessage(msg);
	}
}