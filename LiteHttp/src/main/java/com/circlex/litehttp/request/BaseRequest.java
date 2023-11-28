package com.circlex.litehttp.request;

import android.os.Handler;

import androidx.annotation.NonNull;

import com.circlex.litehttp.OkHttpManager;
import com.circlex.litehttp.callback.BaseCallback;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public abstract class BaseRequest<T extends BaseRequest> {

    protected String url;
    protected transient Object tag;
    protected Map<String, String> headersMap = new HashMap<>();  //Store headers
    protected Map<String, String> urlParamsMap = new HashMap<>();   //Store url params
    protected String baseUrl;
    protected transient OkHttpClient client;
    protected transient okhttp3.Request mRequest;
    protected transient BaseCallback callback;
    protected transient android.os.Handler handler;


    public BaseRequest(String url){
        this.url = url;
        baseUrl = url;
    }

    public T tag(Object tag) {
        this.tag = tag;
        return (T) this;
    }

    public T headers(Map<String, String> headersMap) {
        this.headersMap = headersMap;
        return (T) this;
    }

    public T headers(String key, String value) {
        headersMap.put(key, value);
        return (T) this;
    }

    public T params(Map<String, String> urlParamsMap) {
        this.urlParamsMap = urlParamsMap;
        return (T) this;
    }

    public T params(String key, String value) {
        urlParamsMap.put(key, value);
        return (T) this;
    }

    public T setCache(boolean ifCache){
        if (ifCache){
            headersMap.put("cache","true");
        }
        return (T) this;
    }

    public void setCallback(BaseCallback callback) {
        this.callback = callback;
    }

    /** Generate different RequestBody based on different request methods and parameters **/
    protected abstract RequestBody generateRequestBody();

    /** Convert RequestBody into a Request object based on different request methods **/
    public abstract okhttp3.Request generateRequest(RequestBody requestBody);

    /** Obtain the call of the request **/
    public okhttp3.Call getCall(){
       RequestBody requestBody = generateRequestBody();
       if (requestBody != null){
           mRequest = generateRequest(requestBody);
       }else {
           mRequest = generateRequest(null);
       }
        if (client == null) client = OkHttpManager.initClient().getOkHttpClient();
        return client.newCall(mRequest);
    }

    /** Normal call, blocking method, synchronous request execution **/
    public Response execute() throws IOException {
        return getCall().execute();
    }

    /** Non blocking method, asynchronous request, but callback executed in sub thread **/
    public void execute(BaseCallback callback){
        if (callback == null) throw new NullPointerException("callback == null");
        if (handler == null){
            handler = OkHttpManager.initClient().getHandler();
        }
        Call call = getCall();
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                failCallback(e, handler, callback);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try {
                    if (call.isCanceled()){
                        failCallback(new IOException("Canceled"), handler, callback);
                    }
                    successCallback(response, handler, callback);
                }catch (Exception e){
                    failCallback(e, handler, callback);
                }finally {
                    if (response.body() != null){
                        response.body().close();
                    }
                }
            }
        });
    }

    public void failCallback(final Exception e, final Handler handler, final BaseCallback callback){
        if (callback == null) return;
        callback.onFailure(e);
        handler.post(callback::onAfter);
    }

    public void successCallback(final Response response, final Handler handler, final BaseCallback callback){
        if (callback == null) return;
        callback.onSuccess(response);
        handler.post(callback::onAfter);

    }
}
