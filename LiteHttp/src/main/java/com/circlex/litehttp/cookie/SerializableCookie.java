package com.circlex.litehttp.cookie;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Locale;

import okhttp3.Cookie;

public class SerializableCookie implements Serializable {
    private static final long serialVersionUID = -570542945053100900L;

    public String host;
    public String name;
    public String domain;
    private transient Cookie cookie;
    private transient Cookie clientCookie;

    public SerializableCookie(String host, Cookie cookie){
        this.cookie = cookie;
        this.host = host;
        this.name = cookie.name();
        this.domain = cookie.domain();
    }

    public Cookie getCookie() {
        if (clientCookie != null) {
            return clientCookie;
        }
        return cookie;
    }

    private void writeObject(ObjectOutputStream oos) throws IOException{
        oos.defaultWriteObject();
        oos.writeObject(cookie.name());
        oos.writeObject(cookie.value());
        oos.writeLong(cookie.expiresAt());
        oos.writeObject(cookie.domain());
        oos.writeObject(cookie.path());
        oos.writeBoolean(cookie.secure());
        oos.writeBoolean(cookie.httpOnly());
        oos.writeBoolean(cookie.hostOnly());
        oos.writeBoolean(cookie.persistent());
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        String name = (String) ois.readObject();
        String value = (String) ois.readObject();
        long expiresAt = ois.readLong();
        String domain = (String) ois.readObject();
        String path = (String) ois.readObject();
        boolean secure = ois.readBoolean();
        boolean httpOnly = ois.readBoolean();
        boolean hostOnly = ois.readBoolean();
        boolean persistent = ois.readBoolean();
        Cookie.Builder builder = new Cookie.Builder();
        builder.name(name);
        builder.value(value);
        builder.expiresAt(expiresAt);
        builder = hostOnly ? builder.hostOnlyDomain(domain) : builder.domain(domain);
        builder.path(path);
        if (secure) {
            builder.secure();
        }
        if (httpOnly) {
            builder.httpOnly();
        }
        clientCookie = builder.build();
    }

    /** Sequencing cookies into strings */
    public static String encodeCookie(String host, Cookie cookie){
        if (cookie == null) return null;
        byte[] cookieBytes = cookieToBytes(host, cookie);
        return bytesToHex(cookieBytes);
    }

    /** Converting cookies to binary arrays */
    public static byte[] cookieToBytes(String host, Cookie cookie) {
        SerializableCookie serializableCookie = new SerializableCookie(host, cookie);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(os);
            outputStream.writeObject(serializableCookie);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return os.toByteArray();
    }

    /** Convert binary array to hexadecimal string */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes){
            sb.append(String.format("%02X", b));
        }
        return sb.toString().toUpperCase(Locale.US);
    }

    /** Deserialize strings into cookies */
    public static Cookie decodeCookie(String cookieString){
        byte[] bytes = hexToBytes(cookieString);
        return bytesToCookie(bytes);
    }

    /** Convert Hexadecimal string to binary array */
    private static byte[] hexToBytes(String cookieString) {
        byte[] bytes = cookieString.getBytes();
        int length = bytes.length;
        byte[] outs = new byte[length / 2];
        for (int i = 0; i < length; i = i + 2) {
            String tmp = new String(bytes, i, 2);
            outs[i / 2] = (byte) Integer.parseInt(tmp, 16);
        }
        return outs;
    }

    private static Cookie bytesToCookie(byte[] bytes) {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        Cookie cookie = null;
        try {
            ObjectInputStream ois = new ObjectInputStream(bis);
            cookie = ((SerializableCookie) ois.readObject()).getCookie();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return cookie;
    }
}
