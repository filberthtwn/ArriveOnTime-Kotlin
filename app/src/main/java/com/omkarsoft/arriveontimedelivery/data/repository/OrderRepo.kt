package com.omkarsoft.arriveontimedelivery.data.repository

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.omkarsoft.arriveontimedelivery.constant.OrderType
import com.omkarsoft.arriveontimedelivery.constant.Status
import com.omkarsoft.arriveontimedelivery.data.model.CancelReasonType
import com.omkarsoft.arriveontimedelivery.data.model.CancelReasonTypeResp
import com.omkarsoft.arriveontimedelivery.data.model.Order
import com.omkarsoft.arriveontimedelivery.data.utils.RetrofitService
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OrderRepo: MainRepo() {

    fun getOrders(orderType: OrderType, onResult: (status: Boolean, message: String, List<Order>) -> Unit) {
        when (orderType){
            OrderType.DELIVER -> {
                getAllOrderFromServer("past"){ status, message, data ->
                    val filteredOrder = data.filter { it.status == Status.DELIVERED }
                    if (status){
                        onResult(true, "", filteredOrder)
                    } else {
                        onResult(false, message, arrayListOf())
                    }
                }
            }
            OrderType.PRESENT -> { getAllOrderFromServer("current", onResult) }
            OrderType.DISPATCH -> { getAllDispatchFromServer(onResult) }
            OrderType.FUTURE -> { getAllOrderFromServer("future", onResult) }
            OrderType.OPEN, OrderType.PARTIAL_PICKUP -> { getOpenOrder(onResult) }
        }
    }

    fun getCancelReasonType(
        onResult: (status: Boolean, message: String, data: List<CancelReasonType>?) -> Unit
    ){
        val call = RetrofitService().orderDao.getCancelOrderType()
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, resp: Response<ResponseBody>) {
                if (resp.isSuccessful) {
                    try {
                        val gson = GsonBuilder().create()
                        val cancelReasonTypes = gson.fromJson<CancelReasonTypeResp>(
                            resp.body()!!.string(),
                            object : TypeToken<CancelReasonTypeResp>(){}.type
                        )

                        onResult(true, "Get Cancel Reason Types", cancelReasonTypes.data)
                    } catch(err: JSONException){
                        println("(DEBUG) ERROR")
                        println(err.localizedMessage!!)

                        onResult(false, err.localizedMessage!!, null)
                    }
                }
            }
            override fun onFailure(call: Call<ResponseBody>, err: Throwable) {
                println(err)
                onResult(false, err.localizedMessage!!, null)
            }
        })
    }

    fun cancelDeliveryOrder(
        orderId:String,
        reasonType:String,
        comment:String,
        onResult: (status: Boolean, message: String, data: Order?) -> Unit){

        val call = RetrofitService().orderDao.cancelDeliveryOrder(
            orderId = orderId,
            reasonType = reasonType,
            comment = if (comment.isNotEmpty()) comment else "NOT"
        )

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, resp: Response<ResponseBody>) {
                if (resp.isSuccessful) {
                    try {
                        val jsonObj = JSONObject(resp.body()!!.string())
                        if (!jsonObj.getBoolean("status")){
                            onResult(false, jsonObj.getString("message"), null)
                            return
                        }
                        onResult(true, jsonObj.getString("message"), null)
                    } catch(err: JSONException){
                        onResult(false, err.localizedMessage!!, null)
                    }
                }
            }
            override fun onFailure(call: Call<ResponseBody>, err: Throwable) {
                println(err)
                onResult(false, err.localizedMessage!!, null)
            }
        })
    }

    companion object {
        private var INSTANCE: OrderRepo? = null
        fun shared() = INSTANCE
            ?: OrderRepo().also {
                INSTANCE = it
            }
    }
}