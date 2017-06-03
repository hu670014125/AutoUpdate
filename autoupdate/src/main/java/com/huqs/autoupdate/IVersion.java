package com.huqs.autoupdate;

/**
 * Created by cretin on 2017/4/20.
 * @author  huqs
 *
 */

public interface IVersion {
    //获取包名
    String getPackageName();

    //获取版本号
    int getVersionCode();

    //获取版版名称
    String getVersionName();

    //是否强制更新 false 不强制更新
    boolean isForceUpdate();

    //获取上一次版本号
    int getPreBaselineCode();

    //获取APK下载地址
    String getDownloadUrl();

    //获取更新日志
    String getUpdateLog();

    //获取安装包文件字节长度
    long getSize();

    //获取安装包文件大小
    String getFileSize();

    //获取本次更新影响的版本号
    String getHasAffectCodes();

    //获取本次更新发布的时间
    String getUpdateTime();
}
