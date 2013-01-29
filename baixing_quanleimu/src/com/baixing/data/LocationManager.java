//liuchong@baixing.com
package com.baixing.data;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.location.Location;
import android.util.Pair;

import com.baixing.entity.BXLocation;
import com.baixing.entity.CityDetail;
import com.baixing.util.LocationService;
import com.baixing.util.Util;

public class LocationManager implements LocationService.BXLocationServiceListener {

	public static interface onLocationFetchedListener{
		public void onLocationFetched(BXLocation location);//null==location means location-fetching failed
		public void onGeocodedLocationFetched(BXLocation location);
	}
	
	private List<onLocationFetchedListener> locationFetchListeners = new ArrayList<onLocationFetchedListener>();
	private WeakReference<Context> context;
	
	BXLocation location = null;
	boolean location_updated = false;
	String currentCity;
	
	LocationManager(WeakReference<Context> cxt) {
		this.context = cxt;
	}
	
	public String getCurrentCity() {
		return this.currentCity;
	}
	
	public boolean addLocationListener(final onLocationFetchedListener listener){
		if(context.get() == null) return false;
		if(null == listener || locationFetchListeners.contains(listener))
			return false;
		
		locationFetchListeners.add(listener);		
		LocationService.getInstance().addLocationListener(context.get(), this);
		
		final BXLocation curLocation = getCurrentPosition(true);
		if(null == curLocation){
			return false;
		}		

		listener.onLocationFetched(curLocation);

		if (curLocation.geocoded){
//			Log.d("currentlocation", "xixi, curLocation.geocoded");
			listener.onGeocodedLocationFetched(curLocation);
		}else{
//			Log.d("currentlocation", "xixi, curLocation not geocoded");
		}
		
		return true;
	}
	
	public boolean removeLocationListener(onLocationFetchedListener listener)
	{
		return this.locationFetchListeners.remove(listener);
	}

	public void setLocation(BXLocation location_) {
		if(context.get() == null) return;
		if(null == location)
			getCurrentPosition(false);
		
		if(null != location_){
			if(location_.geocoded){
				location = location_;
			}else{
				location.fLat = location_.fLat;
				location.fLon = location_.fLon;
				
				if(location.geocoded == true){
					float results[] = {0, 0, 0};
					Location.distanceBetween(location.fGeoCodedLat, location.fGeoCodedLon, location_.fLat, location_.fLon, results);
					if(results[0] > 50){
						location.geocoded = false;
						//Log.d("kkkkkk", "location geocoding has been invalidated, since location distance is: "+results[0]+">50m");
					}
				}
			}
			
			Util.saveDataToLocateDelay(context.get(), "location_data", location);

			location_updated = true;
		}
	}
	
	public BXLocation getCurrentPosition(boolean bRealLocality) {
		if(context.get() == null) return null;
		if(null == location){
			location = (BXLocation)Util.loadDataFromLocate(context.get(), "location_data", BXLocation.class);
			
			if(null == location){
				location = new BXLocation(true);
			}else if(null == location.cityName || 0 == location.cityName.length()){
				location.geocoded = false;
			}
		}
		
		return location_updated ? 	location : 
									bRealLocality ? null : location;
	}
	
	@Override
	public void onLocationUpdated(Location location_) {
		BXLocation newLocation = new BXLocation(false);
		newLocation.fLat = (float)location_.getLatitude();
		newLocation.fLon = (float)location_.getLongitude();
		
		setLocation(newLocation);
		
		LocationService.getInstance().reverseGeocode(newLocation.fLat, newLocation.fLon, new LocationService.BXRgcListener() {
			
			@Override
			public void onRgcUpdated(BXLocation location) {
				
				parse4City(location);
				
//				Log.d("currentlocation", "xixi, onRgcUpdated");
				if (null != location) {
					setLocation(location);
				}
				for (onLocationFetchedListener listener : locationFetchListeners)
				{
					listener.onGeocodedLocationFetched(location);
				}					
			}
		});
		
		for (onLocationFetchedListener listener : locationFetchListeners)
		{
			listener.onLocationFetched(newLocation);
		}
		
//		Log.d("kkkkkk", "new location arrived at QuanleimuApplication: (" + location_.getLatitude() + ", " + location_.getLongitude() + ") !!!");
	}

	
	private void parse4City(BXLocation location) {
		if (location != null && location.cityName != null) {
			this.currentCity = location.cityName;
		}
	}
}
