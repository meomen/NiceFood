package com.vuducminh.nicefood.Remote;

import com.vuducminh.nicefood.Model.BraintreeToken;
import com.vuducminh.nicefood.Model.BraintreeTransaction;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ICloudFunction {
    @GET("token")
    Observable<BraintreeToken> getToken();

    @POST("checkout")
    @FormUrlEncoded
    Observable<BraintreeTransaction> submitPayment(@Field("amount") double amount,
                                                   @Field("payment_method_nonce") String nonce);
}
