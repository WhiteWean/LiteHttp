package com.circlex.litehttp.cache;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class CacheInterceptor implements Interceptor {

    private Context  context;

    public void setContext(Context context){
        this.context = context;
    }

    public CacheInterceptor(Context context){
        this.context = context;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        String cacheHead = request.header("cache");
        String cache_control = request.header("Cache-Control");

        if ("true".equals(cacheHead) || (cache_control != null && !cache_control.isEmpty())){
            String url = request.url().url().toString();
            String reqBodyStr = getPostParas(request);
            String responseStr = null;
            try {
                Response response = chain.proceed(request);
                if (response.isSuccessful()) {
                    ResponseBody responseBody = response.body();
                    if (responseBody != null){
                        responseStr = responseBody.string();
                        if (reqBodyStr == null){
                            responseStr = "";
                        }
                        CacheManager.getInstance(context).setCache(url + reqBodyStr, responseStr);
                    }
                    return getOnlineResponse(response, responseStr);
                } else {
                    return chain.proceed(request);
                }
            } catch (Exception e){
                e.printStackTrace();
                Response response = getCacheResponse(request);
                if (response == null){
                    return chain.proceed(request);
                }else {
                    return response;
                }
            }
        }else {
            return chain.proceed(request);
        }
    }

    /** Get request results from cache */
    private Response getCacheResponse(Request request){
        String url = request.url().toString();
        String reqBodyStr = getPostParas(request);
        String cacheStr = CacheManager.getInstance(context).getCache(url + reqBodyStr);
        if (cacheStr == null){
            Log.i("CacheManager", "*** Get Cache Failure ***");
            return null;
        }
        return new Response.Builder()
                .code(200)
                .request(request)
                .body(ResponseBody.create(null, cacheStr))
                .message("from disk cache")
                .protocol(Protocol.HTTP_1_0)
                .build();
    }

    /** Get request results from the network */
     private Response getOnlineResponse(Response response, String body){
        ResponseBody responseBody = response.body();
        return new Response.Builder()
                .code(response.code())
                .body(ResponseBody.create(responseBody == null? null : responseBody.contentType(), body))
                .request(response.request())
                .message(response.message())
                .protocol(response.protocol())
                .build();
    }

    /** Obtain parameters sent to the server in Post mode */
     private String getPostParas(Request request) {
        String reqBodyPara = "";
        String method = request.method();
        if ("POST".equals(method)){
            StringBuilder sb = new StringBuilder();
            if (request.body() instanceof FormBody){
                FormBody formBody = (FormBody) request.body();
                for (int i = 0; i < formBody.size(); i++) {
                    sb.append(formBody.encodedName(i)).append("=").append(formBody.encodedValue(i)).append(",");
                }
                sb.delete(sb.length() - 1, sb.length());
                reqBodyPara = sb.toString();
                sb.delete(0, sb.length());
            }
        }
        return reqBodyPara;
    }
}
