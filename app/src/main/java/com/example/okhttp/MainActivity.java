package com.example.okhttp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.circlex.litehttp.OkHttpManager;
import com.circlex.litehttp.Utils.SSLUtils;
import com.circlex.litehttp.cache.CacheInterceptor;
import com.circlex.litehttp.callback.StringCallback;
import com.circlex.litehttp.cookie.CookieJarImpl;
import com.circlex.litehttp.cookie.store.PrefsCookieStore;
import com.circlex.litehttp.interceptor.LoggerInterceptor;

import java.util.logging.Level;

import okhttp3.OkHttpClient;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    TextView responseText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        responseText = findViewById(R.id.show_text);
        Button button = findViewById(R.id.get_url);
        button.setOnClickListener(view -> sendRequestWithOKHttp());
    }


    private void sendRequestWithOKHttp(){
        String url = "https://www.google.com";

        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        //Set tag, printLevel and colorLevel for log.
        LoggerInterceptor loggerInterceptor = new LoggerInterceptor("LiteHttp", LoggerInterceptor.Level.BODY, Level.INFO);
        builder.addInterceptor(loggerInterceptor);

        //Set custom cache that can save post requests.
        //You can use the setCache() method we provide to enable custom cache.Web protocol 'Cache-Control' can also be used to control cache policy.
        builder.addInterceptor(new CacheInterceptor(this));

        //Automatically manage cookies
        //Use memory to keep cookies,.After exiting the app, cookies disappear.
        //builder.cookieJar(new CookieJarImpl(new MemoryCookieStore()));
        //Use prefs to keep cookies.If they do not expire,it will remain valid.
        builder.cookieJar(new CookieJarImpl(new PrefsCookieStore(this)));

        //Enable SSL and configure your own TrustManager, bskFile, passwd or certificates.
        SSLUtils.SSLParams sslParams = SSLUtils.getSSLSocketFactory(null, null, null);
        builder.sslSocketFactory(sslParams.sslSocketFactory, sslParams.trustManager);
        OkHttpManager
                .initClient(builder.build())                  //Set OkhttpClient, not setting will use the default
                .get(url)
                .setCache(true)                              //Set true to enable custom cache
                .execute(new StringCallback() {              //Custom string callback function, we also provide defined callback for file and Bitemap.
                    @Override
                    public void onFailure(Throwable e) {
                e.printStackTrace();
            }
                    @Override
                    public void onSuccess(Response response){
                        try {
                            String string = parseNetworkResponse(response);
                            runOnUiThread(() -> responseText.setText(string));
                        } catch (Exception e) {
                        e.printStackTrace();
                        }
                    }
                });
    }
}