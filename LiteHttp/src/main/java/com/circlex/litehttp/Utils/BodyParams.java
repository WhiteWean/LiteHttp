package com.circlex.litehttp.Utils;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.MediaType;

/** class for request parameters **/
public class BodyParams implements Serializable {
    public LinkedHashMap<String, List<String>> stringParamsMap;
    public LinkedHashMap<String, List<FileWrapper>> fileParamsMap;
    public static final boolean IS_REPLACE = true;

    public BodyParams(String key, String value) {
        init();
        put(key, value, IS_REPLACE);
    }

    public BodyParams(String key, File file) {
        init();
        put(key, file);
    }

    private void init() {
        stringParamsMap = new LinkedHashMap<>();
        fileParamsMap = new LinkedHashMap<>();
    }

    public void put(BodyParams params) {
        if (params != null) {
            if (params.stringParamsMap != null && !params.stringParamsMap.isEmpty()) stringParamsMap.putAll(params.stringParamsMap);
            if (params.fileParamsMap != null && !params.fileParamsMap.isEmpty()) fileParamsMap.putAll(params.fileParamsMap);
        }
    }

    private void put(String key, String value, boolean isReplace) {
        if (key != null && value != null) {
            List<String> urlValues = stringParamsMap.get(key);
            if (urlValues == null) {
                urlValues = new ArrayList<>();
                stringParamsMap.put(key, urlValues);
            }
            if (isReplace) urlValues.clear();
            urlValues.add(value);
        }
    }
    public void put(Map<String, String> params, boolean... isReplace) {
        if (params == null || params.isEmpty()) return;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            put(entry.getKey(), entry.getValue(), isReplace);
        }
    }

    public void put(String key, String value, boolean... isReplace) {
        if (isReplace != null && isReplace.length > 0) {
            put(key, value, isReplace[0]);
        } else {
            put(key, value, IS_REPLACE);
        }
    }

    public void put(String key, int value, boolean... isReplace) {
        if (isReplace != null && isReplace.length > 0) {
            put(key, String.valueOf(value), isReplace[0]);
        } else {
            put(key, String.valueOf(value), IS_REPLACE);
        }
    }

    public void put(String key, long value, boolean... isReplace) {
        if (isReplace != null && isReplace.length > 0) {
            put(key, String.valueOf(value), isReplace[0]);
        } else {
            put(key, String.valueOf(value), IS_REPLACE);
        }
    }

    public void put(String key, float value, boolean... isReplace) {
        if (isReplace != null && isReplace.length > 0) {
            put(key, String.valueOf(value), isReplace[0]);
        } else {
            put(key, String.valueOf(value), IS_REPLACE);
        }
    }

    public void put(String key, double value, boolean... isReplace) {
        if (isReplace != null && isReplace.length > 0) {
            put(key, String.valueOf(value), isReplace[0]);
        } else {
            put(key, String.valueOf(value), IS_REPLACE);
        }
    }

    public void put(String key, char value, boolean... isReplace) {
        if (isReplace != null && isReplace.length > 0) {
            put(key, String.valueOf(value), isReplace[0]);
        } else {
            put(key, String.valueOf(value), IS_REPLACE);
        }
    }

    public void put(String key, boolean value, boolean... isReplace) {
        if (isReplace != null && isReplace.length > 0) {
            put(key, String.valueOf(value), isReplace[0]);
        } else {
            put(key, String.valueOf(value), IS_REPLACE);
        }
    }

    public void put(String key, File file) {
        put(key, file, file.getName());
    }

    public void put(String key, File file, String fileName) {
        put(key, file, fileName, HttpUtils.guessMimeType(fileName));
    }

    public void put(String key, File file, String fileName, MediaType mediaType) {
        if (key != null) {
            List<FileWrapper> fileWrappers = fileParamsMap.get(key);
            if (fileWrappers == null) {
                fileWrappers = new ArrayList<>();
                fileParamsMap.put(key, fileWrappers);
            }
            fileWrappers.add(new FileWrapper(file, fileName, mediaType));
        }
    }

    public void put(String key, FileWrapper fileWrapper) {
        if (key != null && fileWrapper != null) {
            put(key, fileWrapper.file, fileWrapper.fileName, fileWrapper.mediaType);
        }
    }

    public void removeString(String key) {
        stringParamsMap.remove(key);
    }

    public void removeFile(String key) {
        fileParamsMap.remove(key);
    }

    public void remove(String key) {
        removeString(key);
        removeFile(key);
    }

    public void clear() {
        stringParamsMap.clear();
        fileParamsMap.clear();
    }

    public static class FileWrapper implements Serializable{
        public File file;
        public String fileName;
        public transient MediaType mediaType;
        public long fileSize;

        public FileWrapper(File file, String fileName, MediaType mediaType) {
            this.file = file;
            this.fileName = fileName;
            this.mediaType = mediaType;
            this.fileSize = file.length();
        }

        private void writeObject(ObjectOutputStream out) throws IOException {
            out.defaultWriteObject();
            out.writeObject(mediaType.toString());
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            mediaType = MediaType.parse((String) in.readObject());
        }

        @NonNull
        public String toString() {
            return "FileWrapper{" + //
                    "file=" + file + //
                    ", fileName=" + fileName + //
                    ", mediaType=" + mediaType + //
                    ", fileSize=" + fileSize +//
                    "}";
        }
    }

    @NonNull
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (ConcurrentHashMap.Entry<String, List<String>> entry : stringParamsMap.entrySet()) {
            if (result.length() > 0) result.append("&");
            result.append(entry.getKey()).append("=").append(entry.getValue());
        }
        for (ConcurrentHashMap.Entry<String, List<FileWrapper>> entry : fileParamsMap.entrySet()) {
            if (result.length() > 0) result.append("&");
            result.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return result.toString();
    }
}
