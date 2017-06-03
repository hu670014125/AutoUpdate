package com.huqs.autoupdate;
import com.huqs.autoupdate.Callback;
import com.huqs.autoupdate.IVersion;
import com.huqs.autoupdate.Version;
import com.huqs.autoupdate.VersionParser;

import java.io.IOException;

/**
 * Created by Yoojia.Chen
 * yoojia.chen@gmail.com
 * 2015-01-05
 * 远程请求处理接口
 */
public abstract class RemoteHandler implements Runnable {
    private String url;
    private VersionParser parser;
    private Callback callback;
    final void setOptions(String url, VersionParser parser, Callback callback){
        this.url =  url;
        this.parser = parser;
        this.callback = callback;
    }

    @Override
    final public void run() {
        Version version = null;
        try{
            IVersion iVersion = parser.onParse(request(this.url));
            version=new Version(iVersion);
        }catch (Exception ex){ /* Nothing */ }
        callback.onVersion(version);
    }

    /**
     * 处理连接服务器的请求，并返回内容
     * @param url 服务器地址
     * @return 服务器返回的内容
     */
    public abstract String request(String url) throws IOException;

}
