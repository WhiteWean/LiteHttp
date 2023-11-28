package com.circlex.litehttp.request;

import com.circlex.litehttp.Utils.BodyParams;
import com.circlex.litehttp.Utils.HttpUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

public abstract class BodyRequest extends BaseRequest<BodyRequest>{

    protected static MediaType MEDIA_TYPE_PLAIN = MediaType.parse("text/plain;charset=utf-8");
    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json;charset=utf-8");
    public static final MediaType MEDIA_TYPE_STREAM = MediaType.parse("application/octet-stream");
    protected BodyParams bodyParams;
    protected MediaType mediaType;
    protected String content;
    protected byte[] bs;
    protected transient File file;
    protected boolean isMultipart = false;  //Force the use of multipart/form data form upload or not
    protected RequestBody requestBody;

    public BodyRequest(String url) {
        super(url);
    }

    public BodyRequest isMultipart(boolean isMultipart) {
        this.isMultipart = isMultipart;
        return this;
    }

    public BodyRequest body(Map<String, String> params, boolean... isReplace){
        bodyParams.put(params, isReplace);
        return this;
    }

    public BodyRequest body(String key, String value, boolean... isReplace) {
        bodyParams.put(key, value, isReplace);
        return this;
    }

    public BodyRequest body(String key, int value, boolean... isReplace) {
        bodyParams.put(key, value, isReplace);
        return this;
    }

    public BodyRequest body(String key, float value, boolean... isReplace) {
        bodyParams.put(key, value, isReplace);
        return this;
    }

    public BodyRequest body(String key, double value, boolean... isReplace) {
        bodyParams.put(key, value, isReplace);
        return this;
    }

    public BodyRequest body(String key, long value, boolean... isReplace) {
        bodyParams.put(key, value, isReplace);
        return this;
    }

    public BodyRequest body(String key, char value, boolean... isReplace) {
        bodyParams.put(key, value, isReplace);
        return this;
    }

    public BodyRequest body(String key, boolean value, boolean... isReplace) {
        bodyParams.put(key, value, isReplace);
        return this;
    }

    public BodyRequest body(String key, File file){
        bodyParams.put(key, file);
        return this;
    }

    public BodyRequest body(String key, File file, String fileName){
        bodyParams.put(key, file, fileName);
        return this;
    }

    public BodyRequest body(String key, File file, String fileName, MediaType mediaType) {
        bodyParams.put(key, file, fileName, mediaType);
        return this;
    }

    public BodyRequest body(BodyParams params){
        this.bodyParams.put(params);
        return this;
    }

    public BodyRequest body(String key, List params){
        if (params != null && params.size() > 0){
            for (int i = 0; i<params.size(); i++){
                Object obj = params.get(i);
                if (obj instanceof String) bodyParams.put(key, (String) obj);
                if (obj instanceof Integer) bodyParams.put(key, (int) obj);
                if (obj instanceof Long) bodyParams.put(key, (long) obj);
                if (obj instanceof Float) bodyParams.put(key, (float) obj);
                if (obj instanceof Double) bodyParams.put(key, (double) obj);
                if (obj instanceof Character) bodyParams.put(key, (char) obj);
                if (obj instanceof Boolean) bodyParams.put(key, (boolean) obj);
                if (obj instanceof File) bodyParams.put(key, (File) obj);
                if (obj instanceof BodyParams.FileWrapper) bodyParams.put(key, (BodyParams.FileWrapper) obj);
                else throw new IllegalArgumentException("Illegal element");
            }
        }
        return this;
    }

    /**Note that using this method to upload a string will clear all other parameters in the entity,
     * and the header information will not be cleared**/
    public BodyRequest upString(String content){
        this.content = content;
        this.mediaType = MEDIA_TYPE_PLAIN;
        return this;
    }

    /**Note that using this method to upload a string will clear all other parameters in the entity,
     * and the header information will not be cleared**/
    public BodyRequest upString(String content, MediaType mediaType){
        this.content = content;
        this.mediaType = mediaType;
        return this;
    }

    /**Note that using this method to upload a json string will clear all other parameters in the entity,
     * and the header information will not be cleared**/
    public BodyRequest upJson(String json) {
        this.content = json;
        this.mediaType = MEDIA_TYPE_JSON;
        return this;
    }

    /**Note that using this method to upload a json string will clear all other parameters in the entity,
     * and the header information will not be cleared**/
    public BodyRequest upJson(JSONObject jsonObject) {
        this.content = jsonObject.toString();
        this.mediaType = MEDIA_TYPE_JSON;
        return this;
    }

    /**Note that using this method to upload a json string will clear all other parameters in the entity,
     * and the header information will not be cleared**/
    public BodyRequest upJson(JSONArray jsonArray) {
        this.content = jsonArray.toString();
        this.mediaType = MEDIA_TYPE_JSON;
        return this;
    }

    /**Note that using this method to upload a byte stream will clear all other parameters in the entity,
     * and the header information will not be cleared**/
    public BodyRequest upBytes(byte[] bs){
        this.bs = bs;
        this.mediaType = MEDIA_TYPE_STREAM;
        return this;
    }

    /**Note that using this method to upload a byte stream will clear all other parameters in the entity,
     * and the header information will not be cleared**/
    public BodyRequest upBytes(byte[] bs, MediaType mediaType){
        this.bs = bs;
        this.mediaType = mediaType;
        return this;
    }

    /**Note that using this method to upload a file will clear all other parameters in the entity,
     * and the header information will not be cleared**/
    public BodyRequest upFile(File file){
        this.file = file;
        this.mediaType = HttpUtils.guessMimeType(file.getName());
        return this;
    }

    /**Note that using this method to upload a file will clear all other parameters in the entity,
     * and the header information will not be cleared**/
    public BodyRequest upFile(File file, MediaType mediaType){
        this.file = file;
        this.mediaType = mediaType;
        return this;
    }

    /**Note that using this method to upload a request body will clear all other parameters in the entity,
     * and the header information will not be cleared**/
    public BodyRequest upRequestBody(RequestBody requestBody){
        this.requestBody = requestBody;
        return this;
    }

    @Override
    protected RequestBody generateRequestBody() {
        if (requestBody != null) return requestBody;                                                //Customized request body
        if (content != null && mediaType != null) return RequestBody.create(mediaType, content);    //Upload string data
        if (bs != null && mediaType != null) return RequestBody.create(mediaType, bs);              //Upload Byte Array
        if (file != null && mediaType != null) return RequestBody.create(mediaType, file);          //Upload file
        return HttpUtils.generateMultipartRequestBody(bodyParams, isMultipart);
    }

    protected okhttp3.Request.Builder generateRequestBuilder(RequestBody requestBody) {
        if (urlParamsMap != null) url = HttpUtils.appendParams(baseUrl, urlParamsMap);
        try {
            headersMap.put("Content-Length", String.valueOf(requestBody.contentLength()));
        } catch (IOException e){
            e.printStackTrace();
        }
        Headers headers = HttpUtils.appendHeaders(headersMap);
        Request.Builder requestBuilder = new Request.Builder();
        return requestBuilder.url(url).tag(tag).headers(headers);
    }
}
