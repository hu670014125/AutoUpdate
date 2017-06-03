package com.huqs.autoupdate.utlis;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;

import java.io.File;

/**
 * Created by huqs on 2017/5/15.
 */

public class APKTools {
    /**
     * 安装app
     *
     * @param context
     * @param file
     */
    public static void installApkFile(Context context, File file) {
        Intent intent1 = new Intent(Intent.ACTION_VIEW);
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ) {
            intent1.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
            intent1.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent1.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        if ( context.getPackageManager().queryIntentActivities(intent1, 0).size() > 0 ) {
            context.startActivity(intent1);
        }
    }

    /**
     * 获得apkPackgeName
     *
     * @param context
     * @return
     */
    public static String getPackgeName(Context context) {
        String packName = "";
        PackageInfo packInfo = getPackInfo(context);
        if ( packInfo != null ) {
            packName = packInfo.packageName;
        }
        return packName;
    }
    /**
     * 获得apkPackgeName
     *
     * @param context
     * @return
     */
    public static String getAppName(Context context) {
        String appName = "";
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packInfo = getPackInfo(context);
        if ( packInfo != null ) {
            appName = (String) packageManager.getApplicationLabel(packInfo.applicationInfo);;
        }
        return appName;
    }

    private static String getVersionName(Context context) {
        String versionName = "";
        PackageInfo packInfo = getPackInfo(context);
        if ( packInfo != null ) {
            versionName = packInfo.versionName;
        }
        return versionName;
    }

    /**
     * 获得apk版本号
     *
     * @param context
     * @return
     */
    public static int getVersionCode(Context context) {
        int versionCode = 0;
        PackageInfo packInfo = getPackInfo(context);
        if ( packInfo != null ) {
            versionCode = packInfo.versionCode;
        }
        return versionCode;
    }


    /**
     * 获得apkinfo
     *
     * @param context
     * @return
     */
    public static PackageInfo getPackInfo(Context context) {
        // 获取packagemanager的实例
        PackageManager packageManager = context.getPackageManager();
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo = null;
        try {
            packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);

        } catch ( PackageManager.NameNotFoundException e ) {
            e.printStackTrace();
        }
        return packInfo;
    }
}
