package com.circlex.litehttp.cache;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CacheManager {
    private final ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
    private static final long DISK_CACHE_SIZE = 1024*1024*10;
    private static final String CACHE_DIR = "responses";
    private DiskLruCache diskLruCache;
    private volatile static CacheManager mCacheManager;


    public static CacheManager getInstance(Context context){
        if (mCacheManager == null){
            synchronized (CacheManager.class){
                if (mCacheManager == null){
                    mCacheManager = new CacheManager(context);
                }
            }
        }
        return mCacheManager;
    }

    @SuppressLint("UsableSpace")
    private CacheManager(Context context){
        File diskCacheDir = getDiskCacheDir(context);
        if (!diskCacheDir.exists()) diskCacheDir.mkdirs();
        if (diskCacheDir.getUsableSpace() > DISK_CACHE_SIZE){
            try {
                diskLruCache = com.jakewharton.disklrucache.DiskLruCache.open(diskCacheDir, getAPPVersion(context), 1, DISK_CACHE_SIZE);
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    /** Sync set cache */
     public void putCache(String key, String value){
        if (diskLruCache == null) return;
        OutputStream os = null;
        try {
            key = hashKeyForDisk(key);
            DiskLruCache.Editor editor = diskLruCache.edit(key);
            os = editor.newOutputStream(0);
            os.write(value.getBytes());
            os.flush();
            editor.commit();
            diskLruCache.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (os != null){
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /** Asynchronous set cache */
    public void setCache(final String key, final String value){
        cachedThreadPool.submit(() -> putCache(key, value));
    }

    /** Synchronize get cache */
    public String getCache(String key){
        if (diskLruCache == null) return null;
        InputStream is = null;
        ByteArrayOutputStream bos = null;
        try {
            DiskLruCache.Snapshot snapshot = diskLruCache.get(key);
            if (snapshot != null){
                is = snapshot.getInputStream(0);
                bos = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                int len;
                while ((len = is.read(buf)) != -1){
                    bos.write(buf, 0, len);
                }
                return bos.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null){
                try{
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bos != null){
                try{
                    bos.close();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /** Remove cache */
    public boolean removeCache(String key){
        if (diskLruCache != null){
            try {
                return diskLruCache.remove(key);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /** Delete all cache */
    public boolean deleteCache(){
        if (diskLruCache != null){
            try {
                diskLruCache.delete();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

     /** Get cache directory */
    private File getDiskCacheDir(Context context) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + CacheManager.CACHE_DIR);
    }

    /** Get APP version */
    private int getAPPVersion(Context context){
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** Encode strings with MD5 and use it as filename */
    public static String hashKeyForDisk(String key){
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            byte[] bytes = mDigest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte aByte : bytes) {
                String hex = Integer.toHexString(0XFF & aByte);
                if (hex.length() == 1) {
                    sb.append('0');
                }
                sb.append(hex);
            }
            cacheKey = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }
}
