package com.quanleimu.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.quanleimu.activity.QuanleimuApplication;
import com.quanleimu.activity.QuanleimuMainActivity;
import com.quanleimu.activity.R;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created with IntelliJ IDEA.
 * User: zengming
 * Date: 12-11-9
 * Time: AM10:36
 * To change this template use File | Settings | File Templates.
 */
public class UpdateHelper {
    private static UpdateHelper ourInstance = new UpdateHelper();
    Activity activity = null;
    ProgressDialog pd = null;

    private final int MSG_NETWORK_ERROR = 0;
    private final int MSG_DOWNLOAD_APP = 1;
    private final int MSG_INSTALL_APP = 3;
    private final int MSG_HAS_NEW_VERSION = 4;

    private String serverVersion = null;
    private String apkUrl = null;

    public static UpdateHelper getInstance() {
        return ourInstance;
    }

    private UpdateHelper() {

    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (activity != null) {
                ourInstance.handleMessage(msg);
            }
        }
    };

    private void sendMessage(int msgCode, Object obj) {
        Message msg = handler.obtainMessage(msgCode, obj);
        handler.sendMessage(msg);
    }

    private void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_NETWORK_ERROR:
                Toast.makeText(activity, msg.obj.toString(), 1).show();
                break;
            case MSG_DOWNLOAD_APP:
                updateAppDownload();
                break;
            case MSG_INSTALL_APP:
                updateAppInstall();
                break;
            case MSG_HAS_NEW_VERSION:
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("检查更新")
                        .setMessage("当前版本: " + QuanleimuApplication.version
                                + "\n发现新版本: " + serverVersion
                                + "\n是否更新？")
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                sendMessage(MSG_DOWNLOAD_APP, apkUrl);
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        }).create().show();
                break;
        }

        if (pd != null) {
            pd.hide();
        }
    }



    /**
     * 检查版本更新
     */
    public void checkNewVersion(Activity currentActivity) {
        this.activity = currentActivity;

        ParameterHolder params = new ParameterHolder();
        params.addParameter("clientVersion", QuanleimuApplication.version);

        pd = ProgressDialog.show(activity, "提示", "请稍候...");
        pd.show();
        Communication.executeAsyncGetTask("check_version", params, new Communication.CommandListener() {

            @Override
            public void onServerResponse(String serverMessage) {
                try {
                    JSONObject respond = new JSONObject(serverMessage);
                    JSONObject error = respond.getJSONObject("error");

                    serverVersion = respond.getString("serverVersion");
                    apkUrl = respond.getString("apkUrl");

                    if (!"0".equals(error.getString("code"))) {
                        sendMessage(MSG_NETWORK_ERROR, error.getString("message"));
                    } else {
                        if (respond.getBoolean("hasNew")) {
                            sendMessage(MSG_HAS_NEW_VERSION, null);
                        } else {
                            sendMessage(MSG_NETWORK_ERROR, "已经安装最新版本");
                        }
                    }
                } catch (JSONException e) {
                    sendMessage(MSG_NETWORK_ERROR, "网络异常");
                }

            }

            @Override
            public void onException(Exception ex) {
                sendMessage(MSG_NETWORK_ERROR, "网络异常");
            }
        });
    }

    private void updateAppDownload() {
        //开启更新服务UpdateService
        //这里为了把update更好模块化，可以传一些updateService依赖的值
        //如布局ID，资源ID，动态获取的标题,这里以app_name为例
        Intent updateIntent =new Intent(activity, BXUpdateService.class);
        updateIntent.putExtra("titleId", R.string.app_name);
        updateIntent.putExtra("apkUrl", apkUrl);
        activity.startService(updateIntent);
    }

    private void updateAppInstall() {

    }
}
