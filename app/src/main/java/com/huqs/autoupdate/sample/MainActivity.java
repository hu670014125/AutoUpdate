package com.huqs.autoupdate.sample;
import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.huqs.autoupdate.AnyVersion;
import com.huqs.autoupdate.Callback;
import com.huqs.autoupdate.NotifyStyle;
import com.huqs.autoupdate.Version;
import com.huqs.autoupdate.VersionReceiver;
import com.huqs.autoupdate.utlis.NetWorkUtils;
import pub.devrel.easypermissions.EasyPermissions;
/**
 * Created by Yoojia.Chen
 * yoojia.chen@gmail.com
 * 2015-01-04
 */
public class MainActivity  extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });
        TextView textView = (TextView) findViewById(R.id.tv_app_info);
        try {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            StringBuffer buffer = new StringBuffer();
            buffer.append("本地信息：\n");
            buffer.append(String.format("包名：%s\n", pi.packageName));
            buffer.append(String.format("versionName：%s\n", pi.packageName));
            buffer.append(String.format("versionCode：%s\n", pi.versionCode));
            textView.setText(buffer);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String currentNetType = NetWorkUtils.getCurrentNetType(this);
        textView.append("\n网络状态："+currentNetType);
    }

     class NewVersionReceiver extends VersionReceiver {
        @Override
        protected void onVersion(Version newVersion) {
            Toast.makeText(MainActivity.this, "自动升级-广播通知", Toast.LENGTH_SHORT).show();
            System.out.println(">> Broadcast === \n" + newVersion);
        }
    }

    private NewVersionReceiver newVersionReceiver = new NewVersionReceiver();

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
    public  void autoUpdate(View v){
        AnyVersion version = AnyVersion.getInstance(this);
        version.setURL("http://192.168.2.250:8080/admin/appVersion/checkVersion.do");
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            version.autoCheck(NotifyStyle.Dialog);
        } else {
            EasyPermissions.requestPermissions(this, "访问内部存储设备", 1, perms);
        }
    }
    public  void broadcastUpdate(View v){
        AnyVersion version = AnyVersion.getInstance(this);
        version.check(NotifyStyle.Broadcast);
    }
    public  void callbackUpdate(View v){
        AnyVersion version = AnyVersion.getInstance(this);
        version.setCallback(new Callback() {
            @Override
            public void onVersion(Version version) {
                System.out.println(version);
                Toast.makeText(MainActivity.this, "自动升级-事件回调", Toast.LENGTH_SHORT).show();
            }
        });
        version.check(NotifyStyle.Callback);
    }
    public  void dialogUpdate(View v){
        AnyVersion version = AnyVersion.getInstance(this);
        version.check(NotifyStyle.Dialog);
    }
    public  void checkUpdate(View v){
        AnyVersion version = AnyVersion.getInstance(this);
        version.check(NotifyStyle.Dialog);
    }
    public  void clearCache(View v){
        SharedPreferences sp = this.getSharedPreferences("ignoreList", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.clear();
        edit.commit();
        Toast.makeText(MainActivity.this, "缓存清理完成", Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onStart() {
        super.onStart();
        AnyVersion.registerReceiver(this, newVersionReceiver);
    }
    @Override
    protected void onStop() {
        super.onStop();
        AnyVersion.unregisterReceiver(this, newVersionReceiver);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
