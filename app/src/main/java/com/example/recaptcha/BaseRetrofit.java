package com.example.recaptcha;

import android.util.Log;

import com.ihsanbal.logging.Level;
import com.ihsanbal.logging.LoggingInterceptor;


import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BaseRetrofit {


    public static String REQUEST = "REQUEST";
    public static String RESPONSE = "RESPONSE";

    public static String BASEURL = "https://www.google.com/recaptcha/api/";

    public static Retrofit retrofit = null;




    public static Retrofit getDefaultRetrofit() {


        LoggingInterceptor loggingInterceptor = new LoggingInterceptor.Builder()
                .loggable(BuildConfig.DEBUG)
                .setLevel(Level.BASIC)
                .request(REQUEST)
                .response(RESPONSE)
                .build();

        OkHttpClient okHttpClient = new OkHttpClient.Builder().addInterceptor(loggingInterceptor).build();

        retrofit = new Retrofit.Builder().addConverterFactory(GsonConverterFactory.create()).baseUrl(BASEURL).client(okHttpClient).build();

        return retrofit;


    }


}
