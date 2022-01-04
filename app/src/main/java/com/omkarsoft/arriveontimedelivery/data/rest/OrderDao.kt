package com.omkarsoft.arriveontimedelivery.data.rest

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface OrderDao {
    //region Dispatch Order
    @GET("getNewOfflineDispatches.php")
    fun getAllDispatch(
        @Query("user_id") userId:String,
        @Query("roleName") role:String,
    ): Call<ResponseBody>

    @POST("updateNewDispatches.php")
    fun confirmDispatchOrder(
        @Query("order_ids") orderIds:String,
        @Query("roleName") role:String,
        @Query("driver_id") driverId:String,
        @Query("datetime") datetime: String,
        ): Call<ResponseBody>
    //endregion
    //region Round Trip Order
    @GET("deliveredOrders_ios.php")
    fun getAllRoundTripDelivery(
        @Query("roleName") role: String,
        @Query("user_id") userId: String
    ): Call<ResponseBody>

    @GET("updateOrderPickup.php")
    fun updateOpenOrderToPickedUp(
        @Query("partial_piece") partialPiece: Int,
        @Query("roleName") roleName: String,
        @Query("order_ids") orderId: String,
        @Query("datetime", encoded = true) datetime: String,
        @Query("driver_Id") driverId: String
    ): Call<ResponseBody>

    @GET("updateRoundTripOrderPickup.php")
    fun updateDeliveredToRoundTrip(
        @Query("roleName") roleName: String,
        @Query("order_id") orderId: String,
        @Query("waittime") waitTime: String,
        @Query("transportation") transportation: String,
        @Query("boxes") boxes: String,
        @Query("driver_Id") driverId: String,
        @Query("isRoundTrip") isRoundTrip: String,
        @Query("roundTrip") roundTrip: String,
        @Query("datetime") datetime: String,
        @Query("reason_type") reasonType: String,
    ): Call<ResponseBody>

    @GET("updateRoundTripOrderDeliveryNew.php")
    fun updateOrderToComplete(
        @Query("roleName") roleName: String,
        @Query("order_id") orderId: String,
        @Query("notes") notes: String,
        @Query("lastname") lastname: String,
        @Query("driver_Id") driverId: String,
        @Query("fileName") filename: String,
        @Query("datetime") datetime: String,
    ): Call<ResponseBody>

    @GET("updateRoundTripOrderDeliveryMobile.php")
    fun updateRoundTripToDelivery(
        @Query("roleName") roleName: String,
        @Query("order_id") orderId: String,
        @Query("driver_id") driverId: String,
        @Query("lastname") lastname: String,
        @Query("filename") filename: String,
        @Query("notes") notes: String,
        @Query("datetime") datetime: String,
    ): Call<ResponseBody>
    //endregion
    //region Order
    @GET("getOrderDetails.php")
    fun getOrderDetail(
        @Query("order_id") orderId:String,
        @Query("roleName") role:String,
    ): Call<ResponseBody>

    @GET("getOrders.php")
    fun getAllOrder(
        @Query("user_id") userId:String,
        @Query("roleName") role:String,
        @Query("forDate") status:String,
        @Query("device") device:String,
    ): Call<ResponseBody>

    @GET("get_aot_vendor.php")
    fun getOrderIdFromQRCode(
        @Query("roleName") roleName: String,
        @Query("vendororderid") vendorOrderId: String
    ): Call<ResponseBody>

    @GET("openOrders.php")
    fun getOpenOrder(
        @Query("roleName") roleName: String,
        @Query("user_id") userId: String
    ): Call<ResponseBody>
    //endregion
    //region Pickup
    @GET("updateOrderDeliveryMobile.php")
    fun updateOrderDelivery(
        @Query("roleName") role: String,
        @Query("order_id") orderId: String,
        @Query("waittime") waitTime: String,
        @Query("transportation") transportation: String,
        @Query("boxes") boxes: String,
        @Query("driver_Id") driverId: String,
        @Query("lastname") lastname: String,
        @Query("isRoundTrip") isRoundTrip: String,
        @Query("roundTrip") roundTrip: String,
        @Query("notes") notes: String,
        @Query("datetime") datetime: String,
        @Query("filename") filename: String,
        @Query("reason_type") reasonType: String,
        @Query("partial_deliver") partialDeliver: String,
    ): Call<ResponseBody>
    //endregion

    /// Cancel Order
    @FormUrlEncoded
    @POST("create_order_cancel_activities.php")
    fun cancelDeliveryOrder(
        @Field("order_id") orderId:String,
        @Field("reason_type") reasonType:String,
        @Field("comment") comment:String,
    ): Call<ResponseBody>

    @GET("getReasonType.php")
    fun getCancelOrderType(): Call<ResponseBody>
}