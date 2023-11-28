package com.circlex.litehttp.request;

import com.circlex.litehttp.Utils.HttpUtils;

import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.RequestBody;

public class GetRequest extends BaseRequest<GetRequest> {

    public GetRequest(String url) {
        super(url);
    }

    @Override
    protected RequestBody generateRequestBody() {
        return null;
    }

    @Override
    public Request generateRequest(RequestBody requestBody) {
        Request.Builder requestBuilder = new Request.Builder();
        if (urlParamsMap != null) url = HttpUtils.appendParams(baseUrl, urlParamsMap);
        if (headersMap != null) {
            Headers headers = HttpUtils.appendHeaders(headersMap);
            requestBuilder.headers(headers);
        }
        return requestBuilder.get().url(url).tag(tag).build();
    }
}
