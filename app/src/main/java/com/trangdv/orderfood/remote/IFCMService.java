package com.trangdv.orderfood.remote;


import com.trangdv.orderfood.model.FCMSendData;
import com.trangdv.orderfood.model.FCMResponse;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {

    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAADXD-qcQ:APA91bFGJEu8uSNhP9CdMbm_SqsC7WZyf8WSw65OWfD1KZZFwgrV2t9GVfDnx7hWjSCn-FwyZtFoNrf3BYe-Gq6DkBJXLM_EtHCn8e61xhU8d0iLorykhNoodUjr7dTMqxj71VOYGuck"
            }

    )

    @POST("fcm/send")
    Observable<FCMResponse> sendNotification(@Body FCMSendData body);
}
