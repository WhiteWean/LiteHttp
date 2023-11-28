package com.circlex.litehttp.cookie.store;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.circlex.litehttp.cookie.SerializableCookie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.Cookie;
import okhttp3.HttpUrl;

public class PrefsCookieStore implements CookieStore{

    private static final String COOKIE_PREFS = "CookiePrefsFile";
    private static final String COOKIE_NAME_PREFIX = "cookie_";
    private final Map<String, ConcurrentHashMap<String, Cookie>> cookies;
    private final SharedPreferences cookiePrefs;

    /** Construct a persistent cookie store */
    public PrefsCookieStore(Context context) {
        cookies = new HashMap<>();
        cookiePrefs = context.getSharedPreferences(COOKIE_PREFS, Context.MODE_PRIVATE);
        //Cache persistent cookies into memory
        //Data structure: Map<Url.host, Map<CookieToken, Cookie>>
        Map<String, ?> prefsMap = cookiePrefs.getAll();
        for (Map.Entry<String, ?> entry: prefsMap.entrySet()){
            if ((entry.getValue()) != null && !entry.getKey().startsWith(COOKIE_NAME_PREFIX)){
                String[] cookieTokens = TextUtils.split((String) entry.getValue(), ",");
                for (String cookieToken : cookieTokens){
                    String encodedCookie = cookiePrefs.getString(COOKIE_NAME_PREFIX + cookieToken, null);
                    if (encodedCookie != null){
                        Cookie decodedCookie = SerializableCookie.decodeCookie(encodedCookie);
                        if (decodedCookie != null){
                            if (!cookies.containsKey(entry.getKey())){
                                cookies.put(entry.getKey(), new ConcurrentHashMap<>());
                            }
                            cookies.get(entry.getKey()).put(cookieToken,decodedCookie);
                        }
                    }
                }
            }
        }
    }

    @Override
    public synchronized void saveCookie(HttpUrl url, List<Cookie> cookies) {
        for (Cookie cookie : cookies){
            saveCookie(url, cookie);
        }
    }

    @Override
    public synchronized void saveCookie(HttpUrl url, Cookie cookie) {
        if (!cookies.containsKey(url.host())){
            cookies.put(url.host(), new ConcurrentHashMap<>());
        }
        if (isCookieExpired(cookie)){
            removeCookie(url, cookie);
        }else {
            saveCookie(url, cookie, getCookieToken(cookie));
        }
    }

    /** Save cookies and persist them locally */
    private void saveCookie(HttpUrl url, Cookie cookie, String cookieToken) {
        //Save cookie to memory
        cookies.get(url.host()).put(cookieToken, cookie);
        //Save cookie to SharedPreferences
        SharedPreferences.Editor editor = cookiePrefs.edit();
        editor.putString(url.host(), TextUtils.join(",", cookies.get(url.host()).keySet()));
        editor.putString(COOKIE_NAME_PREFIX + cookieToken, SerializableCookie.encodeCookie(url.host(), cookie));
        editor.apply();
    }


    @Override
    public List<Cookie> loadCookie(HttpUrl url) {
        List<Cookie> cookieList = new ArrayList<>();
        if (!cookies.containsKey(url.host())) return cookieList;

        for (Cookie cookie : cookies.get(url.host()).values()){
            if (isCookieExpired(cookie)) {
                removeCookie(url, cookie);
            } else {
                cookieList.add(cookie);
            }

        }
        return cookieList;
    }

    @Override
    public List<Cookie> getAllCookie() {
        List<Cookie> cookieList = new ArrayList<>();
        for (String key : cookies.keySet()){
            cookieList.addAll(cookies.get(key).values());
        }
        return cookieList;
    }

    @Override
    public List<Cookie> getCookie(HttpUrl url) {
        List<Cookie> cookieList = new ArrayList<>();
        if (!cookies.containsKey(url.host())) return cookieList;
        Map<String, Cookie> mapCookie = cookies.get(url.host());
        if (mapCookie != null){
            cookieList.addAll(mapCookie.values());
        }
        return cookieList;
    }

    @Override
    public boolean removeCookie(HttpUrl url, Cookie cookie) {
        if (!cookies.containsKey(url.host())) return false;

        String cookieToken = getCookieToken(cookie);
        if (!cookies.get(url.host()).containsKey(cookieToken)) return false;

        //Remove from memory
        cookies.get(url.host()).remove(cookieToken);
        //Remove from file
        SharedPreferences.Editor editor = cookiePrefs.edit();
        if (cookiePrefs.contains(COOKIE_NAME_PREFIX + cookieToken)){
            editor.remove(COOKIE_NAME_PREFIX + cookieToken);
        }
        editor.putString(url.host(), TextUtils.join(",", cookies.get(url.host()).keySet()));
        editor.apply();
        return true;
    }

    @Override
    public boolean removeCookie(HttpUrl url) {
        if (!cookies.containsKey(url.host())) return false;

        //Remove from memory
        ConcurrentHashMap<String, Cookie> cookieMap = cookies.remove(url.host());
        //Remove from file
        Set<String> cookieTokens = cookieMap.keySet();
        SharedPreferences.Editor editor = cookiePrefs.edit();
        for (String cookieToken : cookieTokens){
            if (cookiePrefs.contains(COOKIE_NAME_PREFIX + cookieToken)){
                editor.remove(COOKIE_NAME_PREFIX + cookieToken);
            }
        }
        editor.remove(url.host());
        editor.apply();
        return true;
    }

    @Override
    public boolean removeAllCookie() {
        //Remove from memory
        cookies.clear();
        //Remove from file
        SharedPreferences.Editor editor = cookiePrefs.edit();
        editor.clear();
        editor.apply();
        return true;
    }

    /** Is the current cookie expired */
    private static boolean isCookieExpired(Cookie cookie) {
        return cookie.expiresAt() < System.currentTimeMillis();
    }

    private String getCookieToken(Cookie cookie) {
        return cookie.name() + "@" + cookie.domain();
    }
}
