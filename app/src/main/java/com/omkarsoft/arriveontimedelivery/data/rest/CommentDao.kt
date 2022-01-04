package com.omkarsoft.arriveontimedelivery.data.rest

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface CommentDao {
    //region Comments
    @GET("notification.php")
    fun getAllComments(
        @Query("driver") driverId: String
    ): Call<ResponseBody>
    //endregion
}