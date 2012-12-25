//liuchong@baixing.com
package com.baixing.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.baixing.activity.MainActivity;


/**
 * 
 * @author liuchong
 *
 */
public class NotificationHandler extends BroadcastReceiver {
	
    public void onReceive(Context context, Intent intent) {
    	if (CommonIntentAction.ACTION_NOTIFICATION_MESSAGE.equals(intent.getAction()))
    	{
//    		handleIMMessage(context, intent);
    		startApp(context, intent);
    	}
    	else if (CommonIntentAction.ACTION_NOTIFICATION_BXINFO.equals(intent.getAction()))
    	{
//    		handleBXInfoMessage(context, intent);
    		startApp(context, intent);
    	}
    	
    }
    
    private void startApp(Context context, Intent outerIntent)
    {
		Intent goMain = new Intent(context, MainActivity.class);
		goMain.putExtras(outerIntent);
		goMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_NO_HISTORY);
		goMain.putExtras(outerIntent);
		context.startActivity(goMain);
    }
    
//    private void resumeMain(Context context, Intent outerIntent)
//    {
//		Intent goMain = new Intent(context, QuanleimuMainActivity.class);
//		goMain.putExtras(outerIntent);
//		goMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
//		context.startActivity(goMain);
//    }
//    
//    private void handleIMMessage(Context context, Intent outerIntent)
//    {
//    	if (!QuanleimuMainActivity.isInActiveStack) //Make sure to start application from splash.
//    	{
//    		startApp(context, outerIntent);
//    	}
//    	else
//    	{
//    		resumeMain(context, outerIntent);
//    	}
//    }
//
//	private void handleBXInfoMessage(Context context, Intent outerIntent) {
//		if (!QuanleimuMainActivity.isInActiveStack) // Make sure to start
//													// application from splash.
//		{
//			startApp(context, outerIntent);
//		} else {
//			resumeMain(context, outerIntent);
//		}
//	}
    
    
}
