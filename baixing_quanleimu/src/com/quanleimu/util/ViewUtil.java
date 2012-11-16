package com.quanleimu.util;

import java.io.File;

import com.quanleimu.activity.R;
import com.quanleimu.broadcast.CommonIntentAction;
import com.quanleimu.broadcast.NotificationIds;
import com.quanleimu.entity.GoodsDetail;
import com.quanleimu.entity.GoodsDetail.EDATAKEYS;
import com.quanleimu.view.fragment.LoginFragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

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
				Toast.makeText(parent.getContext(), message, Toast.LENGTH_SHORT).show();
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
				Toast.makeText(parent.getContext(), resId, Toast.LENGTH_SHORT).show();
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
	
	public static void putOrUpdateNotification(final Context context, final int notificationId, String notificationType, String title, String msg, final Bundle extras, final boolean isPersistant)
	{
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		WakeLock wk = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "NOTIFY_WAKEUP");
		wk.acquire(1000);
		
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		String tickerText = msg;//message.getMessage();
		int icon = R.drawable.push_icon;

		String contentTitle = title == null ? "" : title;
		String contentText = msg;

		Intent notificationIntent = notificationType == null ? new Intent() : new Intent(notificationType);
		if (extras != null)
		{
			notificationIntent.putExtras(extras);
		}
		if(notificationId == NotificationIds.NOTIFICATION_ID_BXINFO){
			notificationIntent.putExtra("fromNotification", true);
		}
		PendingIntent contentIntent = PendingIntent.getBroadcast(context, 0,
				notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		Notification notification = new Notification(icon, tickerText,
				System.currentTimeMillis());
		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		if (isPersistant)
		{
			notification.flags |= Notification.FLAG_NO_CLEAR;
		}
		else
		{
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
		}

		notification.setLatestEventInfo(context, contentTitle, contentText,
				contentIntent);
		
		mNotificationManager.notify(notificationId, notification);
	}
	
	public static void pickupPhoto(final Activity context, final int tmpFileIndex)
	{
		final String[] names = {"拍照","相册"};
		new AlertDialog.Builder(context).setTitle("请选择")//.setMessage("无法确定当前位置")
		.setItems(names, new DialogInterface.OnClickListener(){
			
			@Override
			public void onClick(DialogInterface dialog, int which){
				switch(which){
					case 0:
						Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
						intent2.putExtra(MediaStore.EXTRA_OUTPUT,
								Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "temp" + tmpFileIndex + ".jpg")));
						context.startActivityForResult(intent2, CommonIntentAction.PhotoReqCode.PHOTOHRAPH);
						break;						
					case 1:
						Intent intent3 = new Intent(Intent.ACTION_GET_CONTENT);
						intent3.addCategory(Intent.CATEGORY_OPENABLE);
						intent3.setType("image/*");
						context.startActivityForResult(Intent.createChooser(intent3, "选择图片"), CommonIntentAction.PhotoReqCode.PHOTOZOOM);
						break;

				}				
			}
		})
		.setNegativeButton("取消", new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which){
				dialog.dismiss();
			}
		}).show();			
	}
	
	public static void startMapForAds(Context context, GoodsDetail ad) {
		final String latV = ad.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_LAT);
		final String lonV = ad.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_LON);
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
	
	static public Bitmap createThumbnail(Bitmap srcBmp, int thumbHeight)
	{
		Float width  = Float.valueOf(srcBmp.getWidth());
		Float height = Float.valueOf(srcBmp.getHeight());
		Float ratio = width/height;
		Bitmap thumbnail = Bitmap.createScaledBitmap(srcBmp, (int)(thumbHeight*ratio), thumbHeight, true);

		return thumbnail;
	}
	
}
