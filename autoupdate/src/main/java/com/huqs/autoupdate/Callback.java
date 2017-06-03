package com.huqs.autoupdate;

/**
 * Created by Yoojia.Chen
 * yoojia.chen@gmail.com
 * 2015-01-04
 */
public interface Callback {

    /**
     * 新版本
     */
    void onVersion(Version version);
}
