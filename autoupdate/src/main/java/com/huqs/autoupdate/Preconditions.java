package com.huqs.autoupdate;

import android.os.Looper;

/**
 * Created by Yoojia.Chen
 * yoojia.chen@gmail.com
 * 2015-01-05
 */
class Preconditions {

    private Preconditions(){}

    public static void requiredMainUIThread(){
        if (Looper.getMainLooper() != Looper.myLooper()){
            throw new IllegalStateException("Should run on main UI thread !");
        }
    }

//    public static void requireInited(){
//        if (AnyVersion.getInstance().mContextcontext == null){
//            throw new IllegalStateException("AnyVersion instance NOT init !");
//        }
//    }
}
