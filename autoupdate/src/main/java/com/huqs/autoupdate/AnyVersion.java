package com.huqs.autoupdate;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.huqs.autoupdate.utlis.NetWorkUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
/**
 * Created by Yoojia.Chen
 * yoojia.chen@gmail.com
 * 2015-01-04
 * AnyVersion - 自动更新 APK
 */
public class AnyVersion {

    private static final String SHARED_FILE_NAME = "ignoreList";
    private static final String TAG = "AnyVersion";
    private static final Lock LOCK = new ReentrantLock();
    private static AnyVersion ANY_VERSION = null;
    Context mContext;
    final VersionParser parser;
    private Future<?> workingTask;
    private Callback callback;
    private String url;
    private RemoteHandler remoteHandler;
    private final Version mCurrentVersion;
    private final Handler mainHandler;
    private final ExecutorService threads;
    private final Installations installations;
    private final Downloads downloads;
    private boolean isAutoCheck = false;

    public static AnyVersion getInstance(Context context) {
        try {
            LOCK.lock();
            if (ANY_VERSION == null) {
                throw new IllegalStateException("AnyVersion NOT init !");
            }
            ANY_VERSION.mContext=context;
            return ANY_VERSION;
        } finally {
            LOCK.unlock();
        }
    }

    private AnyVersion(final Context context, VersionParser parser) {
        Log.d(TAG, "AnyVersion init...");
        this.mContext = context;
        this.parser = parser;
        this.threads = Executors.newSingleThreadExecutor();
        this.installations = new Installations();
        this.downloads = new Downloads();
        this.mainHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                Version version = (Version) msg.obj;
                //如果服务器的版本高于本地的版本就提示更新
                if (version.getVersionCode() > mCurrentVersion.getVersionCode()) {
                    //有更新
                    String[] hasAffectCodes = version.getHasAffectCodes().split("\\|");
                    if (Arrays.asList(hasAffectCodes).contains(mCurrentVersion.getVersionCode()))
                        version.setForceUpdate(true); //被列入强制更新 不可忽略此版本,即强制更新
                    List<String> listCodes = loadArray();
                    if (!isAutoCheck) {//如果是手动更新，只要是版本高于本地就可以了
                        //如果手动检测更新，就强制更新
                        version.setForceUpdate(true);
                        //显示更新提示
                        showUploadDialog( version);
                        return;
                    }
                    if (!listCodes.contains(version.getVersionName())) {
                        //显示更新提示
                        showUploadDialog( version);
                    } else {
                        Log.d(TAG, "该版本已经忽略：" + version.getVersionName());

                    }

                }else if (!isAutoCheck){//如果是手动更新，没有最新的版本
                    Toast.makeText(context, "当前已经是最新版！", Toast.LENGTH_SHORT).show();
                }
            }
        };
        Version version = new Version();
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version.setPackageName(pi.packageName);
            version.setVersionName(pi.versionName);
            version.setVersionCode(pi.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }
        mCurrentVersion = version;
    }
    /**
     * 显示更新对话框
     *
     *
     * @param version
     */
    private void showUploadDialog(final Version version) {
        if (!(mContext instanceof Activity))throw  new RuntimeException("In the call AnyVersion.getInstance (Context Context), the Context must be a subclass of Activity");
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        AlertDialog alertDialog = builder.create();
        String updateLog = version.getUpdateLog();
        if (TextUtils.isEmpty(updateLog))
            updateLog = "新版本，欢迎更新";
        String versionName = version.getVersionName();
        if (TextUtils.isEmpty(versionName)) {
            versionName = "1.1.0";
        }
        alertDialog.setTitle("发现新版本：V " + versionName);
        alertDialog.setMessage(updateLog);
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String currentNetType = NetWorkUtils.getCurrentNetType(mContext);
                if (!currentNetType.equals("wifi")){
                    showAlertDialog(version);
                }else{
                    downloads.submit(mContext,version);
                }
            }
        });
        if (!version.isForceUpdate()) {
            final String finalVersionName = versionName;
            alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "忽略此版本", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //忽略此版本
                    List listCodes = loadArray();
                    if (listCodes != null) {
                        listCodes.add(finalVersionName);
                    } else {
                        listCodes = new ArrayList();
                        listCodes.add(finalVersionName);
                    }
                    saveArray(listCodes);
                    Toast.makeText(mContext, "此版本已忽略", Toast.LENGTH_SHORT).show();
                }
            });
        }
        if (version.isForceUpdate()) alertDialog.setCancelable(false);
        alertDialog.show();
        ((TextView) alertDialog.findViewById(android.R.id.message)).setLineSpacing(5, 1);
        Button btnPositive = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        Button btnNegative = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        btnNegative.setTextColor(Color.parseColor("#16b2f5"));
        Button btnNeutral = alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL);
        btnNeutral.setTextColor(Color.parseColor("#16b2f5"));
        btnPositive.setTextColor(Color.parseColor("#16b2f5"));
    }
    private void showAlertDialog(final Version version){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("温馨提示！");
        builder.setMessage("当前正在使用手机流量，是否继续？");
        AlertDialog alertDialog = builder.create();
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "继续", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                downloads.submit(mContext,version);
            }
        });
        alertDialog.show();
        Button btnPositive = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        Button btnNegative = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        btnNegative.setTextColor(Color.parseColor("#16b2f5"));
        btnPositive.setTextColor(Color.parseColor("#16b2f5"));
    }

    /**
     * 保存忽略的版本名称
     * @param list
     * @return
     */
    public boolean saveArray(List<String> list) {
        SharedPreferences sp = mContext.getSharedPreferences(SHARED_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor mEdit1 = sp.edit();
        mEdit1.putInt("Status_size", list.size());
        for (int i = 0; i < list.size(); i++) {
            mEdit1.remove("Status_" + i);
            mEdit1.putString("Status_" + i, list.get(i));
        }
        return mEdit1.commit();
    }

    /**
     * 读取忽略的版本名称
     * @return
     */
    public List<String> loadArray() {
        List<String> list = new ArrayList<>();
        SharedPreferences sharedPreference = mContext.getSharedPreferences(SHARED_FILE_NAME, Context.MODE_PRIVATE);
        list.clear();
        int size = sharedPreference.getInt("Status_size", 0);
        for (int i = 0; i < size; i++) {
            list.add(sharedPreference.getString("Status_" + i, null));
        }
        return list;
    }

    /**
     * 初始化 AnyVersion。
     *
     * @param context 必须是 Application
     * @param parser  服务端响应数据解析接口
     */
    public static void init(Context context, VersionParser parser) {
        Preconditions.requiredMainUIThread();
        ANY_VERSION = new AnyVersion(context, parser);
        ANY_VERSION.installations.register(context);
        try {
            LOCK.lock();
            if (ANY_VERSION != null) {
                Log.e(TAG, "");
                System.out.println("----Duplicate init AnyVersion ! This VersionParser  will be discard !");
                Log.e(TAG, "AnyVersion recommend init on YOUR-Application.onCreate(...) .");
                System.out.println("----AnyVersion recommend init on YOUR-Application.onCreate(...) .");
                return;
            }
        } finally {
            LOCK.unlock();
        }
        if (context == null) {
            throw new NullPointerException("Application Context CANNOT be null !");
        }
        if (parser == null) {
            throw new NullPointerException("Parser CANNOT be null !");
        }

    }

    /**
     * 注册接收新版本通知的 Receiver。
     */
    public static void registerReceiver(Context context, VersionReceiver receiver) {
        Broadcasts.register(context, receiver);
    }

    /**
     * 反注册接收新版本通知的 Receiver
     */
    public static void unregisterReceiver(Context context, VersionReceiver receiver) {
        Broadcasts.unregister(context, receiver);
    }

    /**
     * 设置发现新版本时的回调接口。当 check(NotifyStyle.Callback) 时，此接口参数生效。
     */
    public void setCallback(Callback callback) {
        if (this.mContext == null){
            throw new IllegalStateException("AnyVersion instance NOT init !");
        }
        if (callback == null) {
            throw new NullPointerException("Callback CANNOT be null !");
        }
        this.callback = callback;
    }

    /**
     * 设置检测远程版本的 URL。在使用内置 RemoteHandler 时，URL 是必须的。
     */
    public void setURL(String url) {
        if (this.mContext == null){
            throw new IllegalStateException("AnyVersion instance NOT init !");
        }
        checkRequiredURL(url);
        this.url = url;
    }

    /**
     * 设置自定义检测远程版本数据的接口
     */
    public void setCustomRemote(RemoteHandler remoteHandler) {
        if (this.mContext == null){
            throw new IllegalStateException("AnyVersion instance NOT init !");
        }
        if (remoteHandler == null) {
            throw new NullPointerException("RemoteHandler CANNOT be null !");
        }
        this.remoteHandler = remoteHandler;
    }

    /**
     * 检测新版本，并指定发现新版本的处理方式
     */
    public void check(NotifyStyle style) {
        isAutoCheck = false;
        createRemoteRequestIfNeed();
        check(this.url, this.remoteHandler, style);
    }

    /**
     * 手动检测新版本，并指定发现新版本的处理方式
     */
    public void autoCheck(NotifyStyle style) {
        isAutoCheck = true;
        createRemoteRequestIfNeed();
        check(this.url, this.remoteHandler, style);
    }

    /**
     * 按指定的 URL，检测新版本，并指定发现新版本的处理方式
     */
    public void check(String url, NotifyStyle style) {
        isAutoCheck = false;
        createRemoteRequestIfNeed();
        check(url, this.remoteHandler, style);
    }
    private void check(String url, final RemoteHandler remote, final NotifyStyle style) {
        if (this.mContext == null){
            throw new IllegalStateException("AnyVersion instance NOT init !");
        }
        if (NotifyStyle.Callback.equals(style) && callback == null) {
            throw new NullPointerException("If reply by callback, callback CANNOT be null ! " +
                    "Call 'setCallback(...) to setup !'");
        }
        final Callback core = new Callback() {
            @Override
            public void onVersion(Version remoteVersion) {
                // 检查是否为新版本
//                if (remoteVersion.isEmpty()) remoteVersion=new Version(remoteVersion);
//                if (mCurrentVersion.getVersionCode() >= remoteVersion.getVersionCode()) return;
                switch (style) {
                    case Callback:
                        //使用Handler回调到主线程中
                        Handler handler = new Handler(Looper.getMainLooper()) {
                            @Override
                            public void handleMessage(Message msg) {
                                super.handleMessage(msg);
                                callback.onVersion((Version) msg.obj);
                            }
                        };
                        Message message = handler.obtainMessage();
                        message.arg1=200;
                        message.obj=remoteVersion;
                        handler.sendMessage(message);
                        break;
                    case Broadcast:
                        Broadcasts.send(mContext, remoteVersion);
                        break;
                    case Dialog:
                        final Message msg = Message.obtain(ANY_VERSION.mainHandler, 0, remoteVersion);
                        msg.sendToTarget();
                        break;
                }
            }
        };
        remote.setOptions(url, parser, core);
        workingTask = threads.submit(remote);
    }

    /**
     * 取消当前正在检测的工作线程
     */
    public void cancelCheck() {
        if (this.mContext == null){
            throw new IllegalStateException("AnyVersion instance NOT init !");
        }
        if (workingTask != null && !workingTask.isDone()) {
            workingTask.cancel(true); // force interrupt
        }
    }

    private void createRemoteRequestIfNeed() {
        if (remoteHandler == null) {
            // 使用内置请求时，URL 地址是必须的。
            checkRequiredURL(this.url);
            remoteHandler = new SimpleRemoteHandler(mContext);
        }
    }

    private void checkRequiredURL(String url) {
        if (TextUtils.isEmpty(url)) {
            throw new NullPointerException("URL CANNOT be null or empty !");
        }
    }

}
