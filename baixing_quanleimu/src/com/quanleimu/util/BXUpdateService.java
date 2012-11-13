package com.quanleimu.util;

import android.app.*;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import com.quanleimu.activity.QuanleimuApplication;
import com.quanleimu.activity.QuanleimuMainActivity;
import com.quanleimu.activity.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: zengming
 * Date: 12-10-24
 * Time: PM2:24
 * 自动更新服务，完成：
 * 1、下载 app，进度通知栏提示
 * 2、点击直接安装
 */
public class BXUpdateService extends Service {
    public IBinder onBind(Intent intent) {
        return null;
    }

    //标题
    private int titleId = 0;

    private String apkUrl = "";

    //文件存储
    private File updateDir = null;
    private File updateFile = null;

    private boolean hasSdCard = false;

    //通知栏
    private NotificationManager updateNotificationManager = null;
    private Notification updateNotification = null;
    //通知栏跳转Intent
    private Intent updateIntent = null;
    private PendingIntent updatePendingIntent = null;

    //下载状态
    private final static int DOWNLOAD_COMPLETE = 0;
    private final static int DOWNLOAD_FAIL = 1;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //获取传值
        titleId = intent.getIntExtra("titleId", 0);
        apkUrl = intent.getStringExtra("apkUrl");

        hasSdCard = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
        //创建文件
        if (hasSdCard) {
            updateDir = new File(Environment.getExternalStorageDirectory(), "/tmp/baixing_tmp/");
        } else {
            String dirPath = getCacheDir().getPath();
            updateDir = new File(dirPath, "/tmp/baixing_tmp/");
        }

        updateFile = new File(updateDir.getPath(),  "baixing_app_tmp.apk");

        this.updateNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        this.updateNotification = new Notification();

        //设置下载过程中，点击通知栏，回到主界面
        updateIntent = new Intent(this, QuanleimuMainActivity.class);
        updatePendingIntent = PendingIntent.getActivity(this, 0, updateIntent, 0);
        //设置通知栏显示内容
        updateNotification.icon = R.drawable.app_icon;
        updateNotification.tickerText = "开始下载";
        updateNotification.setLatestEventInfo(this, "百姓网客户端", "0%", updatePendingIntent);
        //发出通知
        updateNotificationManager.notify(0, updateNotification);

        //开启一个新的线程下载，如果使用Service同步下载，会导致ANR问题，Service本身也会阻塞
        new Thread(new updateRunnable()).start();//这个是下载的重点，是下载的过程

        return super.onStartCommand(intent, flags, startId);
    }

    class updateRunnable implements Runnable {
        Message message = updateHandler.obtainMessage();

        public void run() {
            message.what = DOWNLOAD_COMPLETE;
            try {
                //增加权限<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE">;
                if (!updateDir.exists()) {
                    updateDir.mkdirs();
                }

                //todo ming 下载失败的处理
//                apkUrl = "http://pages.baixing.com/mobile/android_baixing_wap_V2.7.2.apk";
                long downloadSize = downloadUpdateFile(apkUrl, updateFile);
                if (downloadSize > 0) {
                    //下载成功
                    updateHandler.sendMessage(message);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                message.what = DOWNLOAD_FAIL;
                //下载失败
                updateHandler.sendMessage(message);
            }
        }
    }

    public long downloadUpdateFile(String downloadUrl, File saveFile) throws Exception {
        //这样的下载代码很多，我就不做过多的说明
        int downloadCount = 0;
        int currentSize = 0;
        long totalSize = 0;
        int updateTotalSize = 0;

        HttpURLConnection httpConnection = null;
        InputStream is = null;
        FileOutputStream fos = null;

        try {
            URL url = new URL(downloadUrl);
            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestProperty("User-Agent", "baixing java HttpClient");
            httpConnection.setRequestProperty("Accept-Encoding", "identity");
            httpConnection.setConnectTimeout(30000);
            httpConnection.setReadTimeout(60000);

            updateTotalSize = httpConnection.getContentLength();
            if (httpConnection.getResponseCode() == 404) {
                throw new Exception("fail!");
            }
            is = httpConnection.getInputStream();

            if (hasSdCard) {
                fos = new FileOutputStream(saveFile, false);
            } else {
                fos = this.openFileOutput("baixing_in_tmp.apk", Context.MODE_WORLD_READABLE);
            }

            byte buffer[] = new byte[4096];
            int readsize = 0;
            String updateInfo = null;

            while ((readsize = is.read(buffer)) > 0) {
                fos.write(buffer, 0, readsize);
                totalSize += readsize;
                //为了防止频繁的通知导致应用吃紧，百分比增加10才通知一次
                if ((downloadCount == 0) || (totalSize / (updateTotalSize*1.0)) * 100 - 1 > downloadCount) {
                    downloadCount += 1;
                    updateInfo = (int) totalSize * 100 / updateTotalSize + "% "
                            + (int)(totalSize/1000) + "KB/" + (int)(updateTotalSize/1000) + "KB";
                    updateNotification.setLatestEventInfo(BXUpdateService.this, "正在下载新版客户端", updateInfo, updatePendingIntent);
                    updateNotificationManager.notify(0, updateNotification);
                }
            }
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
            if (is != null) {
                is.close();
            }
            if (fos != null) {
                fos.flush();
                fos.close();
            }
        }
        return totalSize;
    }

    private Handler updateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWNLOAD_COMPLETE:
                    //点击安装PendingIntent
                    String apkStr = "";
                    if (hasSdCard) {
                        apkStr = updateFile.getPath();
                    } else {
                        apkStr = "/data/data/com.quanleimu.activity/files/baixing_in_tmp.apk";
                    }
                    Uri uri = Uri.fromFile(new File(apkStr));
                    Intent installIntent = new Intent(Intent.ACTION_VIEW, uri);
                    installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    installIntent.setDataAndType(uri, "application/vnd.android.package-archive");
                    startActivity(installIntent);
                    updateNotificationManager.cancel(0);
                    //停止服务
                    stopService(updateIntent);
                    break;
                case DOWNLOAD_FAIL:
                    //下载失败
                    updateNotification.setLatestEventInfo(BXUpdateService.this, "下载失败，请重试", "请检查您的网络连接", updatePendingIntent);
                    updateNotificationManager.notify(0, updateNotification);

                    break;
                default:
                    break;
            }
        }
    };
}



