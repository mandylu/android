/**
 *
 * Copyright 2012 baixing, Inc. All rights reserved.
 * NetworkProtocols.java
 *
 */
package com.baixing.util;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.params.HttpConnectionParams;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * @author fqming
 * @date 2012-4-28
 */
public class NetworkProtocols
{
    // public static final String CTWAP_GATEWAY_IP = "10.0.0.200";
    //
    // public static final String OTHERWAP_GATEWAY_IP = "10.0.0.172";
    //
    // public static final int GATEWAY_PORT = 80;

    private static final String CTWAP = "ctwap";

    private static final String CMWAP = "cmwap";

    private static final String UNIWAP = "uniwap";

    private static final String THREEGWAP = "3gwap";

    private static final Uri PREFERRED_APN_URI = Uri.parse("content://telephony/carriers/preferapn");

    private Context context;
    
    private NetworkProtocols()
    {
        
    }
    
    private static NetworkProtocols instance = new NetworkProtocols();
    
    public static NetworkProtocols getInstance()
    {
        return instance;
    }
    
    public void init(Context context)
    {
        this.context = context;
    }

    private String[] getWapProxy()
    {
        try
        {
            if(context == null)
                return null;
            
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (!wifiManager.isWifiEnabled())
            {
                Cursor cursor_current = context.getContentResolver().query(PREFERRED_APN_URI, null, null, null, null);
                if (cursor_current != null && cursor_current.moveToFirst())
                {
                    String proxy = cursor_current.getString(cursor_current.getColumnIndex("proxy"));
                    String apn = cursor_current.getString(cursor_current.getColumnIndex("apn"));
                    String port = cursor_current.getString(cursor_current.getColumnIndex("port"));
                    if (CTWAP.equals(apn) || CMWAP.equals(apn) || UNIWAP.equals(apn) || THREEGWAP.equals(apn))
                    {
                    	cursor_current.close();
                        return new String[]
                        { proxy, port };
                    }
                }
                cursor_current.close();
            }
        }
        catch (Throwable e)
        {
            Log.e("quanleimu", "isWapConnection error", e);
        }

        return null;
    }

    public HttpClient getHttpClient()
    {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        String[] proxyData = getWapProxy();
        if (proxyData != null && proxyData.length > 0)
        {
            HttpHost proxy = new HttpHost(proxyData[0], 80);
            httpClient.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY, proxy);
        }

        HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 60 * 1000);
        HttpConnectionParams.setSoTimeout(httpClient.getParams(), 60 * 1000);
        
        httpClient.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(2, true));

        return httpClient;
    }
}
