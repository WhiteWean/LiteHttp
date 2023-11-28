package com.circlex.litehttp.cookie.store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import okhttp3.Cookie;
import okhttp3.HttpUrl;

public class MemoryCookieStore implements CookieStore{

    private final Map<String, List<Cookie>> memoryCookies = new HashMap<>();

    @Override
    public synchronized void saveCookie(HttpUrl url, List<Cookie> cookies) {
        List<Cookie> savedCookies = memoryCookies.get(url.host());
        List<Cookie> needRemove = new ArrayList<>();
        for (Cookie cookie : cookies){
            for (Cookie oldCookie : savedCookies){
                if (cookie.name().equals(oldCookie.name())) {
                    needRemove.add(oldCookie);
                }
            }
        }
        savedCookies.removeAll(needRemove);
        savedCookies.addAll(cookies);
    }

    @Override
    public synchronized void saveCookie(HttpUrl url, Cookie cookie) {
        List<Cookie> savedCookies = memoryCookies.get(url.host());
        List<Cookie> needRemove = new ArrayList<>();
        for (Cookie item : savedCookies) {
            if (cookie.name().equals(item.name())) {
                needRemove.add(item);
            }
        }
        savedCookies.removeAll(needRemove);
        savedCookies.add(cookie);
    }

    @Override
    public synchronized List<Cookie> loadCookie(HttpUrl url) {
        List<Cookie> savedCookies = memoryCookies.get(url.host());
        if (savedCookies == null){
            savedCookies = new ArrayList<>();
            memoryCookies.put(url.host(), savedCookies);
        }
        return savedCookies;
    }

    @Override
    public synchronized List<Cookie> getAllCookie() {
        List<Cookie> allCookies = new ArrayList<>();
        Set<String> urls = memoryCookies.keySet();
        for (String url:urls){
            allCookies.addAll(Objects.requireNonNull(memoryCookies.get(url)));
        }
        return allCookies;
    }

    @Override
    public synchronized List<Cookie> getCookie(HttpUrl url) {
        List<Cookie> cookies = new ArrayList<>();
        List<Cookie> urlCookies = memoryCookies.get(url.host());
        if (urlCookies != null) cookies.addAll(urlCookies);
        return cookies;
    }

    @Override
    public synchronized boolean removeCookie(HttpUrl url, Cookie cookie) {
        List<Cookie> cookies = memoryCookies.get(url.host());
        return (cookie != null) && cookies.remove(cookie);
    }

    @Override
    public synchronized boolean removeCookie(HttpUrl url) {
        return memoryCookies.remove(url.host()) != null;
    }

    @Override
    public synchronized boolean removeAllCookie() {
        memoryCookies.clear();
        return true;
    }
}
