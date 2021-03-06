package com.vuducminh.nicefood.remote;

import com.vuducminh.nicefood.common.CommonAgr;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitGoogleAPIClient {

    private static Retrofit instance;

    public static Retrofit getInstance() {
        return  instance == null ? new Retrofit.Builder()
                .baseUrl(CommonAgr.URL_MAP_GOOGLE)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build() : instance;
    }
}
