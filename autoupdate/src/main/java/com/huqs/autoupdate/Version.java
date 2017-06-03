package com.huqs.autoupdate;

import android.os.Parcel;
import android.os.Parcelable;

public class Version implements IVersion ,Parcelable {
    //应用程序的包名
    private String packageName;
    //应用程序版本号
    private int versionCode ;
    //应用程序版本名称
    private String versionName ;
    //是否强制更新 0 不强制更新 1 hasAffectCodes拥有字段强制更新 2 所有版本强制更新
    private boolean forceUpdate ;
    //上一个版本版本号
    private int preBaselineCode ;

    //新安装包的下载地址
    private String downloadUrl ;
    //更新日志
    private String updateLog ;
    //安装包大小 单位字节
    private long  size ;
    //安装包大小 字符串
    private String   fileSize ;
    //受影响的版本号 如果开启强制更新 那么这个字段包含的所有版本都会被强制更新 格式 2|3|4
    private String hasAffectCodes="" ;
    //发布更新的时间
    private String updateTime ;

    public Version(IVersion version) {
        this.packageName=version.getPackageName();
        this.versionCode=version.getVersionCode();
        this.versionName=version.getVersionName();
        this.forceUpdate=version.isForceUpdate();
        this.preBaselineCode=version.getPreBaselineCode();
        this.downloadUrl=version.getDownloadUrl();
        this.updateLog=version.getUpdateLog();
        this.size=version.getSize();
        this.fileSize=version.getFileSize();
        this.hasAffectCodes=version.getHasAffectCodes();
        this.updateTime=version.getUpdateTime();
    }

    public Version() {

    }

    protected Version(Parcel in) {
        packageName = in.readString();
        versionCode = in.readInt();
        versionName = in.readString();
        forceUpdate = in.readByte() != 0;
        preBaselineCode = in.readInt();
        downloadUrl = in.readString();
        updateLog = in.readString();
        size = in.readLong();
        fileSize = in.readString();
        hasAffectCodes = in.readString();
        updateTime = in.readString();
    }

    public static final Creator<Version> CREATOR = new Creator<Version>() {
        @Override
        public Version createFromParcel(Parcel in) {
            return new Version(in);
        }

        @Override
        public Version[] newArray(int size) {
            return new Version[size];
        }
    };

    @Override
    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    @Override
    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    @Override
    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    @Override
    public boolean isForceUpdate() {
        return forceUpdate;
    }

    public void setForceUpdate(boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
    }

    @Override
    public int getPreBaselineCode() {
        return preBaselineCode;
    }

    public void setPreBaselineCode(int preBaselineCode) {
        this.preBaselineCode = preBaselineCode;
    }

    @Override
    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    @Override
    public String getUpdateLog() {
        return updateLog;
    }

    public void setUpdateLog(String updateLog) {
        this.updateLog = updateLog;
    }

    @Override
    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    @Override
    public String getHasAffectCodes() {
        return hasAffectCodes;
    }

    public void setHasAffectCodes(String hasAffectCodes) {
        this.hasAffectCodes = hasAffectCodes;
    }

    @Override
    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(packageName);
        dest.writeInt(versionCode);
        dest.writeString(versionName);
        dest.writeByte((byte) (forceUpdate ? 1 : 0));
        dest.writeInt(preBaselineCode);
        dest.writeString(downloadUrl);
        dest.writeString(updateLog);
        dest.writeLong(size);
        dest.writeString(fileSize);
        dest.writeString(hasAffectCodes);
        dest.writeString(updateTime);
    }
public boolean isEmpty(){

    if (packageName.isEmpty()||versionName.isEmpty()||versionCode==0)return true;
    return false;
}

    @Override
    public String toString() {
        return "Version{" +
                "packageName='" + packageName + '\'' +
                ", versionCode=" + versionCode +
                ", versionName='" + versionName + '\'' +
                ", forceUpdate=" + forceUpdate +
                ", preBaselineCode=" + preBaselineCode +
                ", downloadUrl='" + downloadUrl + '\'' +
                ", updateLog='" + updateLog + '\'' +
                ", size=" + size +
                ", fileSize='" + fileSize + '\'' +
                ", hasAffectCodes='" + hasAffectCodes + '\'' +
                ", updateTime='" + updateTime + '\'' +
                '}';
    }
}
