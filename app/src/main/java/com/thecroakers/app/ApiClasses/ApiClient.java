package com.thecroakers.app.ApiClasses;

import android.content.Context;

import com.thecroakers.app.BuildConfig;
import com.thecroakers.app.Constants;
import com.thecroakers.app.SimpleClasses.Functions;
import com.thecroakers.app.SimpleClasses.Variables;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static Retrofit retrofit;
    public static Retrofit getRetrofitInstance(Context context) {

        if (retrofit == null) {

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

            httpClient.connectTimeout(1, TimeUnit.MINUTES)
                    .readTimeout(1, TimeUnit.MINUTES)
                    .writeTimeout(1, TimeUnit.MINUTES)
                    .build();
            httpClient.addInterceptor(chain -> {
                Request.Builder requestBuilder = chain.request().newBuilder();
                requestBuilder.header("Content-Type", "application/json");
                requestBuilder.header("Api-Key", Constants.API_KEY);
                requestBuilder.header("User-Id", Functions.getSharedPreference(context).getString(Variables.U_ID,"null"));
                requestBuilder.header("Auth-Token", Functions.getSharedPreference(context).getString(Variables.AUTH_TOKEN,"null"));
                requestBuilder.header("device", "android");
                requestBuilder.header("version", BuildConfig.VERSION_NAME);
                requestBuilder.header("ip", Functions.getSharedPreference(context).getString(Variables.DEVICE_IP, null));
                requestBuilder.header("device-token", Functions.getSharedPreference(context).getString(Variables.DEVICE_TOKEN, null));
                return chain.proceed(requestBuilder.build());
            });

            retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build();
        }
        return retrofit;
    }
}
