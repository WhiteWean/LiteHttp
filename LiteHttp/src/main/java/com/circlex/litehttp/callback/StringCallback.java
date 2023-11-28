package com.circlex.litehttp.callback;

import okhttp3.Response;

public abstract class StringCallback extends BaseCallback<String>{
    @Override
    public String parseNetworkResponse(Response response) throws Exception {
        return response.body().string();
    }
}
