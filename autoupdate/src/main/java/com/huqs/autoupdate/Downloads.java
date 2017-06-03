package com.huqs.autoupdate;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;

import com.huqs.autoupdate.utlis.APKTools;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
/**
 * Created by Yoojia.Chen
 * yoojia.chen@gmail.com
 * 2015-01-04
 */
class Downloads {
    static final Set<Long> KEEPS = new HashSet<>();
    public void destroy(Context context) {
        DownloadManager download = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        for (long id : KEEPS) {
            download.remove(id);
            KEEPS.remove(id);
        }
    }
    public void submit(Context context, Version version) {
        DownloadManager download = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(version.getDownloadUrl());
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle(String.format("%s%s", APKTools.getAppName(context),version.getVersionName()));
        boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED); //判断sd卡是否存在
        if (sdCardExist) {
            File file = Environment.getExternalStorageDirectory();//获取跟目录
            String path = file + "/Download/" + APKTools.getAppName(context) + version.getVersionName() + ".apk";
            //设置APK下载位置
            request.setDestinationUri(Uri.fromFile(new File(path)));
            PackageManager pm = context.getPackageManager();
            //如果本地已经存在了apk安装包就读取安装的信息
            PackageInfo info = pm.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
            if (info != null) {
                ApplicationInfo appInfo = info.applicationInfo;
                String packageName = appInfo.packageName;  //得到安装包名称
                int versionCode = info.versionCode;//得到版本信息
                //如果本地存在的apk和服务器中的apk包名和版本号相同即视为相同的apk不用下载直接安装
                if (version.getVersionCode() == versionCode && packageName.equals(version.getPackageName())) {
                    // System.out.println("apk文件已经存在");
                    Intent intent = new Intent("android.intent.action.DOWNLOAD_COMPLETE_LOCAL");
                    intent.putExtra("path", path);
                    context.sendBroadcast(intent);
                    return;
                }
            }
        }
        long id = download.enqueue(request);
        KEEPS.add(id);
    }
}
