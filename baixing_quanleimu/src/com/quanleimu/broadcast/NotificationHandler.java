package com.quanleimu.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.quanleimu.activity.QuanleimuMainActivity;
import com.quanleimu.activity.SplashActivity;

/**
 * 
 * @author liuchong
 *
 */
public class NotificationHandler extends BroadcastReceiver {
	
    public void onReceive(Context context, Intent intent) {
    	if (CommonIntentAction.ACTION_NOTIFICATION_MESSAGE.equals(intent.getAction()))
    	{
    		handleIMMessage(context, intent);
    	}
    }
    
    private void handleIMMessage(Context context, Intent outerIntent)
    {
    	if (!QuanleimuMainActivity.isInActiveStack) //Make sure to start application from splash.
    	{
    		Intent goSplash = new Intent(context, SplashActivity.class);
    		goSplash.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    		goSplash.putExtras(outerIntent);
    		context.startActivity(goSplash);
    	}
    	else
    	{
    		Intent goMain = new Intent(context, QuanleimuMainActivity.class);
    		goMain.putExtras(outerIntent);
    		goMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
    		context.startActivity(goMain);
    	}
    }
    
    
}
