package com.circlex.litehttp.request;

import okhttp3.Request;
import okhttp3.RequestBody;

public class OptionsRequest extends BodyRequest{
    public OptionsRequest(String url) {
        super(url);
    }

    @Override
    public Request generateRequest(RequestBody requestBody) {
        Request.Builder requestBuilder = generateRequestBuilder(requestBody);
        return requestBuilder.method("OPTIONS", requestBody).build();
    }
}
