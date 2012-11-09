package com.quanleimu.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.quanleimu.activity.R;
import com.quanleimu.util.BXUpdateService;

public class BXStartupIntentReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == null) {
            return;
        }

        if (intent.getAction().equals("bxupdate")) {
            String apkUrl = intent.getStringExtra("apkUrl");
            Intent updateIntent =new Intent(context, BXUpdateService.class);
            updateIntent.putExtra("titleId", R.string.app_name);
            updateIntent.putExtra("apkUrl", apkUrl);
            context.startService(updateIntent);
        }

		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			Intent serviceIntent = new Intent(context,
					BXNotificationService.class);
			context.startService(serviceIntent);
			
	        Intent startPush = new Intent(PushMessageService.ACTION_CONNECT);
	        startPush.putExtra("updateToken", true);
	        context.startService(startPush);
		}
	}
}