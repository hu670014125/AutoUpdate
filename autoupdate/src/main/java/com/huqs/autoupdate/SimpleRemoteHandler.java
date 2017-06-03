package com.huqs.autoupdate;

import android.content.Context;
import android.net.Uri;

import com.huqs.autoupdate.RemoteHandler;
import com.huqs.autoupdate.utlis.APKTools;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Yoojia.Chen
 * yoojia.chen@gmail.com
 * 2015-01-05
 */
class SimpleRemoteHandler extends RemoteHandler {

    static final int DEFAULT_HTTP_CONNECT_TIMEOUT = 15 * 1000;
    static final int DEFAULT_HTTP_READ_TIMEOUT = 15 * 1000;
    static final String ALLOWED_URI_CHARS = "@#&=*+-_.,:!?()/~'%";
    static final int MAX_REDIRECT_COUNT = 3;
    static final int BUFFER_SIZE = 32 * 1024; // 32 Kb
    private Context mContext;

    public SimpleRemoteHandler(Context context) {
        this.mContext = context;
    }

    @Override
    public String request(String url) throws IOException {
        InputStream is = getStreamFromNetwork(url);
        return toStringBuffer(is).toString();
    }

    private StringBuilder toStringBuffer(InputStream is) throws IOException{
        if( null == is) return null;
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        StringBuilder buffer = new StringBuilder();
        String line ;
        while ((line = in.readLine()) != null){
            buffer.append(line).append("\n");
        }
        is.close();
        return buffer;
    }

    private HttpURLConnection createConnection(String url) throws IOException {
        String encodedUrl = Uri.encode(url, ALLOWED_URI_CHARS);
        HttpURLConnection conn = (HttpURLConnection) new URL(encodedUrl).openConnection();
        conn.setConnectTimeout(DEFAULT_HTTP_CONNECT_TIMEOUT);
        conn.setReadTimeout(DEFAULT_HTTP_READ_TIMEOUT);
        return conn;
    }

    private InputStream getStreamFromNetwork(String url) throws IOException {
        HttpURLConnection conn = createConnection(url);
        conn.setRequestMethod("POST");
        // 获取URLConnection对象对应的输出流
        PrintWriter printWriter = new PrintWriter(conn.getOutputStream());
        // 发送请求参数
        printWriter.write(String.format("packageName=%s", APKTools.getPackgeName(mContext)));//post的参数 xx=xx&yy=yy
        // flush输出流的缓冲
        printWriter.flush();
        int redirectCount = 0;
        final int httpCode = conn.getResponseCode();
        while (httpCode / 100 == 3 && redirectCount < MAX_REDIRECT_COUNT) {
            conn = createConnection(conn.getHeaderField("Location"));
            redirectCount++;
        }
        InputStream stream;
        try {
            stream = conn.getInputStream();
        } catch (IOException e) {
            readAndCloseStream(conn.getErrorStream());
            throw e;
        }
        if (httpCode != 200) {
            closeSilently(stream);
            throw new IOException("URL request failed with response code " + httpCode);
        }
        return stream;
    }

    private void readAndCloseStream(InputStream is) {
        final byte[] bytes = new byte[BUFFER_SIZE];
        try {
            while (is.read(bytes, 0, BUFFER_SIZE) != -1) { /* nop */ }
        } catch (IOException ignored) {
        } finally {
            closeSilently(is);
        }
    }

    private void closeSilently(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {
            }
        }
    }
}
