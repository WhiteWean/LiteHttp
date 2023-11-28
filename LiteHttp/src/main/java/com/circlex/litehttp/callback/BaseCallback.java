package com.circlex.litehttp.callback;

import okhttp3.Response;

public abstract class BaseCallback<T>{

    /** UI Thread */
    public void onBefore() {

    }

    /** UI Thread */
    public void onAfter() {

    }

    /** UI Thread */
    public void onProgress(float progress, long total){

    }

    public abstract void onFailure(Throwable e);

    public abstract void onSuccess(Response response);

    public abstract T parseNetworkResponse (Response response) throws Exception;
}
