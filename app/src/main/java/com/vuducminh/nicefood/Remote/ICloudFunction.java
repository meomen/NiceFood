package com.vuducminh.nicefood.Remote;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ICloudFunction {
    @GET("")
    Observable<ResponseBody> getCustomToken(@Query("access_token") String accessToken);
}
