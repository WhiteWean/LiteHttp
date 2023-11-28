package com.circlex.litehttp.interceptor;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.circlex.litehttp.Utils.HttpUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import okhttp3.Connection;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.HttpHeaders;
import okio.Buffer;

public class LoggerInterceptor implements Interceptor {

    private static final Charset UTF8 = StandardCharsets.UTF_8;
    private volatile Level printLevel = Level.NONE;
    private java.util.logging.Level colorLevel = java.util.logging.Level.INFO;
    private Logger logger;

    public enum Level {
        NONE,       //Do not print log
        BASIC,      //Print only the first line of the request and the first line of the response
        HEADERS,    //Print all headers for requests and responses
        BODY        //Print all data
    }

    public LoggerInterceptor(String tag, LoggerInterceptor.Level printLevel, java.util.logging.Level colorLevel){
        if (printLevel != null) {
            this.printLevel = printLevel;
        }
        if (colorLevel != null) {
            this.colorLevel = colorLevel;
        }
        if (!TextUtils.isEmpty(tag)) {
            logger = Logger.getLogger("OkHttpManager");
        }else {
            logger = Logger.getLogger(tag);
        }

    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        if (printLevel == Level.NONE) {
            return chain.proceed(request);
        }
        //Request log interception
        logForRequest(request, chain.connection());
        //Record request time
        long startNs = System.nanoTime();
        Response response;
        try {
            response = chain.proceed(request);
        } catch (Exception e) {
            log("<-- HTTP FAILED: " + e);
            throw e;
        }
        long endNs = System.nanoTime();
        long tookMs = TimeUnit.NANOSECONDS.toMillis(endNs - startNs);
        return logForResponse(response, tookMs);
    }

    /** Request log interception */
    private void logForRequest(Request request, Connection connection){
        boolean logBody = (printLevel == Level.BODY);
        boolean logHeaders = (printLevel == Level.BODY || printLevel == Level.HEADERS);
        RequestBody requestBody = request.body();
        Protocol protocol = connection != null ? connection.protocol() : Protocol.HTTP_1_1;
        try {
            String requestStartMessage = "--> " + request.method() + ' ' + request.url() + ' ' + protocol;
            log(requestStartMessage);
            if (logHeaders){
                if (requestBody != null) {
                    if (requestBody.contentType() != null) {
                        log("\tContent-Type: " + requestBody.contentType());
                    }
                    if (requestBody.contentLength() != -1) {
                        log("\tContent-Length: " + requestBody.contentLength());
                    }
                }
                Headers headers = request.headers();
                for (int i = 0, count = headers.size(); i < count; i++) {
                    String name = headers.name(i);
                    // Skip headers from the request body as they are explicitly logged above.
                    if (!"Content-Type".equalsIgnoreCase(name) && !"Content-Length".equalsIgnoreCase(name)) {
                        log("\t" + name + ": " + headers.value(i));
                    }
                }
                log(" ");
                if (logBody && requestBody != null) {
                    if (isPlaintext(requestBody.contentType())) {
                        log("\tbody:" + RequestBodyToString(request));
                    } else {
                        log("\tOmitted! Body maybe not in text format.");
                    }
                }
            }

        } catch (Exception e)
        {
            e.printStackTrace();
        } finally {
            log("--> END " + request.method());
        }
    }


    /** Response log interception */
    private Response logForResponse(Response response, long tookMs){
        boolean logBody = (printLevel == Level.BODY);
        boolean logHeaders = (printLevel == Level.BODY || printLevel == Level.HEADERS);
        Response clone = response.newBuilder().build();
        ResponseBody responseBody = clone.body();
        try {
            String responseStartMessage = "<-- " + clone.code() + ' ' + clone.message() + ' ' + clone.request().url() + " (" + tookMs + "ms）";
            log(responseStartMessage);
            if (logHeaders){
                Headers headers = clone.headers();
                for (int i=0, count = headers.size(); i < count; i++){
                    log("\t" + headers.name(i) + ": " + headers.value(i));
                }
                log("");
                if (logBody && HttpHeaders.hasBody(clone)){
                    if (responseBody == null) return response;

                    MediaType contentType = responseBody.contentType();
                    if (contentType != null){
                        log("\tContent-Type: " + contentType);
                        if (isPlaintext(contentType)){
                            byte[] bytes = HttpUtils.ToByteArray(responseBody.byteStream());
                            log("\tbody:" + new String(bytes, getCharset(contentType)));
                            responseBody = ResponseBody.create(responseBody.contentType(), bytes);
                            return response.newBuilder().body(responseBody).build();
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        } finally {
            log("<-- END HTTP");
        }
        return response;
    }

    private void log(String message) {
        logger.log(colorLevel, message);
    }
    private String RequestBodyToString(Request request) {
        try {
            RequestBody body = request.newBuilder().build().body();
            if (body == null) return null;
            Buffer buffer = new Buffer();
            body.writeTo(buffer);
            return buffer.readString(getCharset(body.contentType()));
        } catch (Exception e) {
            return "Something error when show requestBody.";
        }
    }

    /**
     * Return true if the body in question probably contains readable text.
     */
    private static boolean isPlaintext(MediaType mediaType) {
        if (mediaType == null) return false;
        if (mediaType.type().equals("text")) {
            return true;
        }
        String subtype = mediaType.subtype();
        subtype = subtype.toLowerCase();
        return subtype.contains("x-www-form-urlencoded") || subtype.contains("json") || subtype.contains("xml") || subtype.contains("html");
    }

    private static Charset getCharset(MediaType contentType) {
        Charset charset = contentType != null ? contentType.charset(UTF8) : UTF8;
        if (charset == null) charset = UTF8;
        return charset;
    }

}
