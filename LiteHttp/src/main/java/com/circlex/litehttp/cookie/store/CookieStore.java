package com.circlex.litehttp.cookie.store;

import java.util.List;

import okhttp3.Cookie;
import okhttp3.HttpUrl;

public interface CookieStore {

    /** Save all cookies corresponding to the URL */
    void saveCookie(HttpUrl url, List<Cookie> cookie);

    /** Save all cookies corresponding to the URL */
    void saveCookie(HttpUrl url, Cookie cookie);

    /** Load all cookies in the URL */
    List<Cookie> loadCookie(HttpUrl url);

    /** Get all currently saved cookies */
    List<Cookie> getAllCookie();

    /** Obtain all cookies corresponding to the current URL */
    List<Cookie> getCookie(HttpUrl url);

    /** Remove corresponding cookies based on URLs and cookies */
    boolean removeCookie(HttpUrl url, Cookie cookie);

    /** Remove all cookies based on the URL */
    boolean removeCookie(HttpUrl url);

    /** Remove all cookies */
    boolean removeAllCookie();
}
