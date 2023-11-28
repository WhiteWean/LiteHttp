package com.circlex.litehttp.Utils;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class HttpUtils {

     /** Splice the passed in parameters into urls **/
     public static String appendParams(String url, Map<String, String> urlParamsMap) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(url);
            if (url.indexOf('?') > 0) sb.append("&");
            else sb.append("?");
            for (Map.Entry<String, String> urlParams: urlParamsMap.entrySet()){
                String urlValue = URLEncoder.encode(urlParams.getValue(), "UTF-8");
                sb.append(urlParams.getKey()).append("=").append(urlValue).append("&");
            }
            sb.deleteCharAt(sb.length() - 1);
            return sb.toString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return url;
    }

    /** Universal splicing request header **/
    public static Headers appendHeaders(Map<String, String> headersMap){
        Headers headers = null;
        if (headersMap != null && headersMap.size() > 0) {
            Headers.Builder headersBuilder = new Headers.Builder();
            try {
                for (Map.Entry<String, String> entry: headersMap.entrySet()) {
                    headersBuilder.add(entry.getKey(), entry.getValue());
                    headers = headersBuilder.build();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return headers;
    }

    /** Obtain MIME type based on file name **/
    public static MediaType guessMimeType(String fileName){
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentType = fileNameMap.getContentTypeFor(fileName.replace("#", ""));
        if (contentType == null){
            return MediaType.parse("application/octet-stream");
        }
        return MediaType.parse(contentType);
    }

    /** Generate a request body similar to a form **/
    public static RequestBody generateMultipartRequestBody(BodyParams params, boolean isMultipart) {
        if (params.fileParamsMap.isEmpty() && !isMultipart){
            //Submit form without files
            FormBody.Builder bodyBuilder = new FormBody.Builder();
            for (String key : params.stringParamsMap.keySet()){
                List<String> stringValues = params.stringParamsMap.get(key);
                for (String value: stringValues){
                    bodyBuilder.addEncoded(key, value);
                }
            }
            return bodyBuilder.build();
        } else {
            //Submit form with files
            MultipartBody.Builder multipartBodybuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            if (!params.stringParamsMap.isEmpty()){
                for (Map.Entry<String, List<String >> entry: params.stringParamsMap.entrySet()){
                    List<String> stringValues = entry.getValue();
                    for (String value:stringValues){
                        multipartBodybuilder.addFormDataPart(entry.getKey(), value);
                    }
                }
                for (Map.Entry<String, List<BodyParams.FileWrapper>> entry : params.fileParamsMap.entrySet()) {
                    List<BodyParams.FileWrapper> fileValues = entry.getValue();
                    for (BodyParams.FileWrapper fileWrapper : fileValues) {
                        RequestBody fileBody = RequestBody.create(fileWrapper.mediaType, fileWrapper.file);
                        multipartBodybuilder.addFormDataPart(entry.getKey(), fileWrapper.fileName, fileBody);
                    }
                }
            }
            return multipartBodybuilder.build();
        }
    }

    /** Convert input stream to bytes*/
    public static byte[] ToByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int len;
        byte[] buffer = new byte[4096];
        while ((len = inputStream.read(buffer)) != -1) outputStream.write(buffer, 0, len);
        return outputStream.toByteArray();
    }
}
