package com.vuducminh.nicefood.Remote;

import com.vuducminh.nicefood.Model.FCMService.FCMResponse;
import com.vuducminh.nicefood.Model.FCMService.FCMSendData;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
    @Headers({
            "Conten-Type:application/json",
            "Authorization:key=AAAAHRWAyVk:APA91bG4bu0WSl0GAHTUHs3opxriv5loR1i53yHWYnYa5OLI9hE48H8EuJDmkq7NhJRNoM-HY97bP2FKqTWmohcuTBX1yTLfBJWf43XyltjpG35i33gbWTr39p7f2P2JlALUgepNJsoj"
    })
    @POST("fom/send")
    Observable<FCMResponse> sendNotification(@Body FCMSendData body);
}
