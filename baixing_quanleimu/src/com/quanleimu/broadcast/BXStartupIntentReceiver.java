package com.quanleimu.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import com.quanleimu.activity.R;
import com.quanleimu.util.BXUpdateService;

public class BXStartupIntentReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == null) {
            return;
        }

        if (intent.getAction().equals("bxupdate")) {
            //收到 push 后，通知栏显示，点击打开 url 去更新
            String apkUrl = intent.getStringExtra("apkUrl");
            Intent updateIntent = new Intent(Intent.ACTION_VIEW);
            updateIntent.setData(Uri.parse(apkUrl));
            context.startActivity(updateIntent);
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