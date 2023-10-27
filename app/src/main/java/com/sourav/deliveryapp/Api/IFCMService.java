package com.sourav.deliveryapp.Api;


import com.sourav.deliveryapp.Model.DataMessage;
import com.sourav.deliveryapp.Model.MyResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {

    @Headers({

            "Content-Type:application/json",
            "Authorization:key=AAAA22UlciA:APA91bGZHTFvnk9JxNx40K87Hzxizf_mOKJ2g0vDVxjDB-Z6ucAYb7ZxeZYQnBj79QloANtkeiVd4KHbb9rHKfPwnw9T6F7px7sGohdTnMjBTe8Rmmy5QLpIckQAQUxz4WV0oQvIaTLB"

    })
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body DataMessage body);
}