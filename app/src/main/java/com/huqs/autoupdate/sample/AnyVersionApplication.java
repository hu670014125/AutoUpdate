package com.huqs.autoupdate.sample;
import android.app.Application;

import com.huqs.autoupdate.AnyVersion;
import com.huqs.autoupdate.IVersion;
import com.huqs.autoupdate.VersionParser;
import com.huqs.autoupdate.json.JSONHelper;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by Yoojia.Chen
 * yoojia.chen@gmail.com
 * 2015-01-06
 */
public class AnyVersionApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AnyVersion.init(this, new VersionParser() {
            @Override
            public IVersion onParse(String response) {
                try {
                    JSONObject object=new JSONObject(response);
                    object=object.getJSONObject("data");
                    return JSONHelper.parseObject(object, UserVersion.class);//反序列化
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
    }
}
