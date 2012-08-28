package com.quanleimu.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkConnectivityReceiver extends BroadcastReceiver {
	public static final String TAG = "receiver.network";

    @Override
    public void onReceive(Context context, Intent intent) {
    	boolean debugLog = true;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            Log.e(TAG, "NetworkConnectivityReceiver: Connectivity Manager is null!");
            return;
        }
        
        if (debugLog) {
            for (NetworkInfo network : cm.getAllNetworkInfo()) {
                Log.d(TAG, "NetworkConnectivityReceiver: "
                        + " available=" + (network.isAvailable()?1:0)
                        + ", connected=" + (network.isConnected()?1:0)
                        + ", connectedOrConnecting=" + (network.isConnectedOrConnecting()?1:0)
                        + ", failover=" + (network.isFailover()?1:0)
                        + ", roaming=" + (network.isRoaming()?1:0)
                        + ", networkName=" + network.getTypeName());
            } 
        }
        
        NetworkInfo network = cm.getActiveNetworkInfo();
        if (network != null && PushMessageService.IsRunning) {
            Log.d(TAG, "NetworkConnectivityReceiver: " + PushMessageService.ACTION_NETWORK_CHANGED + " " + network.getTypeName());
            Intent svcintent = new Intent(PushMessageService.ACTION_NETWORK_CHANGED);
            svcintent.putExtra("available", network.isConnected());
            svcintent.putExtra("failover", network.isFailover());
            context.startService(svcintent);
        }

        //TODO: maybe we will using following logic to get better user experience.
//        network = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
//        if (network.getTypeName().equals("WIFI") && network.isConnected()) {
//            context.startService(new Intent(PushMessageService.ACTION_CONNECT));
//        } else if (network.getTypeName().equals("WIFI") && !network.isConnected()) {            
//            context.startService(new Intent(PushMessageService.ACTION_DISCONNECT));
//        }
    }
}