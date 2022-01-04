package com.omkarsoft.arriveontimedelivery.data.rest

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthDao {
    @POST("Login.php")
    fun login(
        @Query("user_id") userId:String,
        @Query("password") password:String,
    ): Call<ResponseBody>
}