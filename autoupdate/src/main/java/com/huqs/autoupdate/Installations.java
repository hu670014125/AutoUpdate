package com.huqs.autoupdate;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;

import java.io.File;

/**
 * Created by Yoojia.Chen
 * yoojia.chen@gmail.com
 * 2015-01-04
 */
class Installations {

    private final BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String fileName = null;
            switch (intent.getAction()) {
                case "android.intent.action.DOWNLOAD_COMPLETE":
                    long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                    if (!Downloads.KEEPS.contains(reference)) return;
                    // 下载完成，自动安装
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(reference);
                    DownloadManager download = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                    Cursor cursor = download.query(query);
                    if (cursor.moveToFirst()) {
                        int fileNameIdx = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
                        fileName = cursor.getString(fileNameIdx);
                    }
                    cursor.close();
                    break;
                case "android.intent.action.DOWNLOAD_COMPLETE_LOCAL":
                    fileName=intent.getStringExtra("path");
                    break;
            }
            if(TextUtils.isEmpty(fileName)){
                return;
            }

            if (fileName.endsWith(".apk")) {
                Intent install = new Intent(Intent.ACTION_VIEW);
                install.setDataAndType(Uri.fromFile(new File(fileName)), "application/vnd.android.package-archive");
                install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(install);
            }
        }

    };

    public void register(Context context) {
        Preconditions.requiredMainUIThread();
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        filter.addAction("android.intent.action.DOWNLOAD_COMPLETE_LOCAL");
        context.getApplicationContext().registerReceiver(downloadReceiver, filter);
    }

    public void unregister(Context context) {
        Preconditions.requiredMainUIThread();
        context.getApplicationContext().unregisterReceiver(downloadReceiver);
    }
}
