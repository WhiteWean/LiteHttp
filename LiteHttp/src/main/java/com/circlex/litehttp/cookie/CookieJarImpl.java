package com.circlex.litehttp.cookie;

import androidx.annotation.NonNull;

import com.circlex.litehttp.cookie.store.CookieStore;

import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

public class CookieJarImpl implements CookieJar {

    private com.circlex.litehttp.cookie.store.CookieStore cookieStore;

    public CookieJarImpl(CookieStore cookieStore){
        if (cookieStore == null) {
            throw new IllegalArgumentException("CookieStore can not be null!");
        }
        this.cookieStore = cookieStore;
    }
    @NonNull
    @Override
    public List<Cookie> loadForRequest(@NonNull HttpUrl httpUrl) {
        return cookieStore.loadCookie(httpUrl);
    }

    @Override
    public void saveFromResponse(@NonNull HttpUrl httpUrl, @NonNull List<Cookie> list) {
        cookieStore.saveCookie(httpUrl, list);
    }

    public CookieStore getCookieStore() {
        return cookieStore;
    }
}
