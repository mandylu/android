//liuchong@baixing.com
package com.baixing.util;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.baixing.activity.BaiduMapActivity;
import com.baixing.entity.Ad;
import com.baixing.entity.Ad.EDATAKEYS;

/**
 * 
 * @author liuchong
 *
 */
public class ViewUtil {
	
	public static void postShortToastMessage(final View parent, final String message, long delay)
	{
		if (parent == null || message == null)
		{
			return;
		}
		
		final long delayTime = delay > 0 ? delay : 0;
		
		parent.postDelayed(new Runnable() {

			public void run() {
//				Toast.makeText(parent.getContext(), message, Toast.LENGTH_SHORT).show();
				Toast t = Toast.makeText(parent.getContext(), message, Toast.LENGTH_SHORT);
				t.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
				t.show();
			}
			
		}, delayTime);
	}
	
	public static void postShortToastMessage(final View parent,final int resId, long delay)
	{
		if (parent == null)
		{
			return;
		}
		
		final long delayTime = delay > 0 ? delay : 0;
		
		parent.postDelayed(new Runnable() {

			public void run() {
				Toast t = Toast.makeText(parent.getContext(), resId, Toast.LENGTH_SHORT);
				t.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
				t.show();
			}
			
		}, delayTime);
	}
	
	public static void removeNotification(Context context, final int notificationid)
	{
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		WakeLock wk = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "NOTIFY_WAKEUP");
		wk.acquire(1000);
		
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(notificationid);
	}
	
	public static void startMapForAds(Context context, Ad ad) {
		
		if (ad == null)
		{
            Toast.makeText(context, "无信息无法显示地图", 1).show();
			return;
		}
		
//		if(keepSilent) { //FIXME:
//            Toast.makeText(getActivity(), "当前无法显示地图", 1).show();
//            return;
//        }
		
		final Activity baseActivity = (Activity)context; //FIXME: should not be activity.
		if (baseActivity != null){
			if (Build.VERSION.SDK_INT >  16)//Fix baidu map SDK crash on android4.2 device.
			{
				final String latV = ad.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_LAT);
				final String lonV = ad.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_LON);
				String query = null;
				if(latV != null && !latV.equals("false") && !latV.equals("") && !latV.equals("0") && lonV != null && !lonV.equals("false") && !lonV.equals("") && !lonV.equals("0"))
				{
					query = latV + "," + lonV;
				}
				else
				{
					String area = ad.getValueByKey(EDATAKEYS.EDATAKEYS_AREANAME);
					String address = ad.getMetaValueByKey("具体地点");
					if (address != null && address.trim().length() > 0)
					{
						query = address.trim();
					}
					else if (area != null && area.trim().length() > 0)
					{
						query = area.trim();
					}
				}
				
				if (query != null)
				{
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri.parse("http://maps.google.com/?q=" + query));
					context.startActivity(intent);
				}
			}
			else
			{
				Bundle bundle = new Bundle();
				bundle.putSerializable("detail", ad);
				baseActivity.getIntent().putExtras(bundle);
				
				baseActivity.getIntent().setClass(baseActivity, BaiduMapActivity.class);
				baseActivity.startActivity(baseActivity.getIntent());
			}
			
		} else {
            Toast.makeText(context, "显示地图失败", Toast.LENGTH_SHORT).show();
        }
	}
	
	static public Bitmap createThumbnail(Bitmap srcBmp, int thumbHeight)
	{
		Float width  = Float.valueOf(srcBmp.getWidth());
		Float height = Float.valueOf(srcBmp.getHeight());
		Float ratio = width/height;
		Bitmap thumbnail = Bitmap.createScaledBitmap(srcBmp, (int)(thumbHeight*ratio), thumbHeight, true);

		return thumbnail;
	}
	
	static public void showToast(final Context context, final String msg, final boolean longDuration){
		if (context == null || msg == null) {
			return;
		}
		
		Runnable task = new Runnable(){
			@Override
			public void run(){
				Toast t = Toast.makeText(context, msg, longDuration ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
				t.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
				t.show();
			}
		};
		
		if (context instanceof Activity) {
			((Activity) context).runOnUiThread(task);
		} else {
			task.run();
		}
		
	}
}
