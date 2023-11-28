package com.circlex.litehttp.request;

import okhttp3.Request;
import okhttp3.RequestBody;

public class DeleteRequest extends BodyRequest{
    public DeleteRequest(String url) {
        super(url);
    }

    @Override
    public Request generateRequest(RequestBody requestBody) {
        Request.Builder requestBuilder = generateRequestBuilder(requestBody);
        return requestBuilder.delete(requestBody).build();
    }
}
