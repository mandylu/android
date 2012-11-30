/**
 *
 * Copyright 2012 Baixing, Inc. All rights reserved.
 * ShortcutUtil.java
 *
 */
package com.baixing.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcelable;

import com.quanleimu.activity.R;

/**
 * @author fqming
 * @date 2012-5-7
 */
public class ShortcutUtil
{
    public static boolean hasShortcut(Context cx)
    {
        boolean result = false;
        String title = null;
        try
        {
            final PackageManager pm = cx.getPackageManager();
            title = pm.getApplicationLabel(pm.getApplicationInfo(cx.getPackageName(), PackageManager.GET_META_DATA)).toString();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        String uriStr = "";
        if (android.os.Build.VERSION.SDK_INT < 8)
        {
            uriStr = "content://com.android.launcher.settings/favorites?notify=true";
        }
        else
        {
            uriStr = "content://com.android.launcher2.settings/favorites?notify=true";
        }

        Uri CONTENT_URI = Uri.parse(uriStr);

        Cursor c = cx.getContentResolver().query(CONTENT_URI, null, "title=?", new String[]
        { title }, null);

        if (c != null && c.getCount() > 0)
        {
            result = true;
        }

        return result;
    }

    public static void addShortcut(Context cx)
    {
        Intent shortcut = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");

        Intent shortcutIntent = cx.getPackageManager().getLaunchIntentForPackage(cx.getPackageName());
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);

        String title = null;
        try
        {
            final PackageManager pm = cx.getPackageManager();
            title = pm.getApplicationLabel(pm.getApplicationInfo(cx.getPackageName(), PackageManager.GET_META_DATA)).toString();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);

        shortcut.putExtra("duplicate", false);

        Parcelable iconResource = Intent.ShortcutIconResource.fromContext(cx, R.drawable.app_icon);
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);

        delShortcut(cx);

        cx.sendBroadcast(shortcut);
    }

    public static void delShortcut(Context cx)
    {
        Intent shortcut = new Intent("com.android.launcher.action.UNINSTALL_SHORTCUT");

        String title = null;
        try
        {
            final PackageManager pm = cx.getPackageManager();
            title = pm.getApplicationLabel(pm.getApplicationInfo(cx.getPackageName(), PackageManager.GET_META_DATA)).toString();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
        Intent shortcutIntent = cx.getPackageManager().getLaunchIntentForPackage(cx.getPackageName());
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        cx.sendBroadcast(shortcut);
    }
}
