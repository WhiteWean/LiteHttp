package com.circlex.litehttp.request;

import com.circlex.litehttp.Utils.HttpUtils;

import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.RequestBody;

public class PostRequest extends BodyRequest {
    public PostRequest(String url) {
        super(url);
    }

    @Override
    public Request generateRequest(RequestBody requestBody) {
        Request.Builder requestBuilder = generateRequestBuilder(requestBody);
        return requestBuilder.post(requestBody).build();
    }
}
