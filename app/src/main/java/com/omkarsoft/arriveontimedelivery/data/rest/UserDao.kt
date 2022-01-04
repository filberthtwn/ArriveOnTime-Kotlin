package com.omkarsoft.arriveontimedelivery.data.rest

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface UserDao {
    @FormUrlEncoded
    @POST("updateDriverToken.php")
    fun updateFCMToken(
        @Field("driver_id") driverId:String,
        @Field("device_fcm_token") fcmToken:String,
    ): Call<ResponseBody>
}