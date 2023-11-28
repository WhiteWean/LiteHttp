package com.circlex.litehttp;

import android.os.Handler;
import android.os.Looper;

import com.circlex.litehttp.Utils.SSLUtils;
import com.circlex.litehttp.interceptor.LoggerInterceptor;
import com.circlex.litehttp.request.DeleteRequest;
import com.circlex.litehttp.request.GetRequest;
import com.circlex.litehttp.request.HeadRequest;
import com.circlex.litehttp.request.OptionsRequest;
import com.circlex.litehttp.request.PatchRequest;
import com.circlex.litehttp.request.PostRequest;
import com.circlex.litehttp.request.PutRequest;
import com.circlex.litehttp.request.TraceRequest;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import okhttp3.Call;
import okhttp3.OkHttpClient;

public class OkHttpManager  {

    public static final long DEFAULT_TIMEOUT = 60;     //Default timeout time
    private static volatile OkHttpManager mInstance;
    private OkHttpClient okHttpClient;
    private final android.os.Handler handler;

    private OkHttpManager (OkHttpClient okHttpClient){
        handler = new Handler(Looper.getMainLooper());
        if (okHttpClient == null){
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            LoggerInterceptor loggerInterceptor = new LoggerInterceptor("OkHttpManager", LoggerInterceptor.Level.BODY, Level.INFO);
            SSLUtils.SSLParams sslParams = SSLUtils.getSSLSocketFactory(null, null, null);
            //Set timeout
            builder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                    .addInterceptor(loggerInterceptor)
                    .sslSocketFactory(sslParams.sslSocketFactory, sslParams.trustManager);
            this.okHttpClient = builder.build();
        } else {
          this.okHttpClient = okHttpClient;
        }

    }

    /** Construct client instance */
    public static OkHttpManager initClient(OkHttpClient... okHttpClient){
        if (mInstance == null)
        {
            synchronized (OkHttpManager.class) {
                if (mInstance == null) {
                    if (okHttpClient == null || okHttpClient.length == 0){
                        mInstance = new OkHttpManager(null);
                    } else {
                    mInstance = new OkHttpManager(okHttpClient[0]);
                    }
                }
            }
        }
        return mInstance;
    }

    public OkHttpManager setOkHttpClient(OkHttpClient okHttpClient){
        if (okHttpClient == null) {
            throw new NullPointerException("okHttpClient == null");
        }
        this.okHttpClient = okHttpClient;
        return this;
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public Handler getHandler() {
        return handler;
    }

    /** get request */
    public static GetRequest get(String url){
        return new GetRequest(url);
    }

    /** post request */
    public static PostRequest post(String url){
        return new PostRequest(url);
    }

    /** put request */
    public static PutRequest put(String url){
        return new PutRequest(url);
    }

    /** head request */
    public static HeadRequest head(String url){
        return new HeadRequest(url);
    }

    /** delete request */
    public static DeleteRequest delete(String url){
        return new DeleteRequest(url);
    }

    /** options request */
    public static OptionsRequest options(String url){
        return new OptionsRequest(url);
    }

    /** patch request */
    public static PatchRequest patch(String url){
        return new PatchRequest(url);
    }

    /** trace request */
    public static TraceRequest trace(String url){
        return new TraceRequest(url);
    }

    /** Cancel request based on Tag **/
    public void cancelTag(Object tag){
        if (tag == null) return;
        for (Call call : okHttpClient.dispatcher().queuedCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
        for (Call call : okHttpClient.dispatcher().runningCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
    }

    /** Cancel request based on Tag **/
    public static void cancelTag(OkHttpClient client, Object tag) {
        if (client == null || tag == null) return;
        for (Call call : client.dispatcher().queuedCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
        for (Call call : client.dispatcher().runningCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
    }

    /** Cancel all requests **/
    public void cancelAll() {
        for (Call call : getOkHttpClient().dispatcher().queuedCalls()) {
            call.cancel();
        }
        for (Call call : getOkHttpClient().dispatcher().runningCalls()) {
            call.cancel();
        }
    }

    /** Cancel all requests */
    public static void cancelAll(OkHttpClient client) {
        if (client == null) return;
        for (Call call : client.dispatcher().queuedCalls()) {
            call.cancel();
        }
        for (Call call : client.dispatcher().runningCalls()) {
            call.cancel();
        }
    }
}
