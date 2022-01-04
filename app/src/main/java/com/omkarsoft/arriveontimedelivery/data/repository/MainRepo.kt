package com.omkarsoft.arriveontimedelivery.data.repository

import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.omkarsoft.arriveontimedelivery.data.model.Info
import com.omkarsoft.arriveontimedelivery.data.model.Order
import com.omkarsoft.arriveontimedelivery.data.utils.RetrofitService
import com.omkarsoft.arriveontimedelivery.helper.DateFormatterHelper
import com.omkarsoft.arriveontimedelivery.helper.SharedPreferencesHelper
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import org.json.XML
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

open class MainRepo {
    private val prefHelper = SharedPreferencesHelper()
    private val currentUser = prefHelper.getCurrentUser()!!
    private val userRole = "driver"

    //region Dispatch Order
    internal fun getAllDispatchFromServer(
        onResult: (status: Boolean, message: String, data: List<Order>) -> Unit
    ){
        val call = RetrofitService().orderDao.getAllDispatch(
            userId = currentUser.id,
            role = userRole
        )

        call.enqueue(object : Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful){
                    try {
                        val jsonObj = XML.toJSONObject(response.body()!!.string())
                        if (jsonObj.has("error")){
                            onResult(false, jsonObj["error"].toString(), arrayListOf())
                            return
                        }

                        val gson = GsonBuilder().create()

                        try {
                            val jsonOrder = jsonObj.getJSONObject("dispatches-info").getJSONObject("order").toString()
                            val order = gson.fromJson<Order>(jsonOrder, object : TypeToken<Order>(){}.type)
                            onResult(true, "", arrayListOf(order))
                            return
                        } catch (e: Exception){
                            Log.d("DispatchCall", "No Order: $e")
                        }

                        try {
                            val jsonArray = jsonObj.getJSONObject("dispatches-info").getJSONArray("order").toString()
                            val orders = gson.fromJson<ArrayList<Order>>(jsonArray, object : TypeToken<ArrayList<Order>>(){}.type)
                            onResult(true, "", orders)
                            return
                        } catch (e: Exception){
                            Log.d("DispatchCall", "No Orders: $e")
                        }

                        try {
                            val message = jsonObj.getString("info")
                            onResult(true, message, arrayListOf())
                            return
                        } catch (e: Exception){
                            Log.d("DispatchCall", "No Dispatch: $e")
                        }

                    } catch (e: JSONException){
                        onResult(false, e.localizedMessage ?: "", arrayListOf())
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                onResult(false, t.localizedMessage ?: "", arrayListOf())
            }
        })
    }

    fun confirmDispatchOrder(
        orderIds: List<String>,
        onResult: (status: Boolean, message:String, data: List<Order>) -> Unit
    ){
        val call = RetrofitService().orderDao.confirmDispatchOrder(
            driverId = currentUser.id,
            orderIds = orderIds.joinToString(separator = ","),
            role = userRole,
            datetime = DateFormatterHelper.format("yyyy-MM-dd", TimeZone.getTimeZone("America/Chicago"), Date()),
        )

        call.enqueue(object : Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful){
                    try {
                        val jsonObj = XML.toJSONObject(response.body()!!.string())
                        if (jsonObj.has("error")){
                            onResult(false, jsonObj["error"].toString(), arrayListOf())
                            return
                        }

                        val gson = GsonBuilder().create()

                        try {
                            val info = gson.fromJson<Info>(
                                jsonObj.toString(),
                                object : TypeToken<Info>(){}.type
                            )
                            onResult(true, info.info, arrayListOf())

                            return
                        } catch (e: Exception){
                            Log.d("DispatchCall", "No Info: $e")
                        }

                    } catch (e: JSONException){
                        onResult(false, e.localizedMessage ?: "", arrayListOf())
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                onResult(false, t.localizedMessage ?: "", arrayListOf())
            }
        })
    }
    //endregion
    //region Round Trip Order
    fun updateOpenOrderToPickedUp(
        orderId: String,
        partialPiece: Int,
        onResult: (status: Boolean, message: String, data: Order?) -> Unit
    ){
        val call = RetrofitService().orderDao.updateOpenOrderToPickedUp(
            partialPiece = partialPiece,
            roleName = userRole,
            orderId = orderId,
            datetime = DateFormatterHelper.format("yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("America/Chicago"), Date()),
            driverId = currentUser.id
        )

        call.enqueue(object : Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, resp: Response<ResponseBody>) {
                if (resp.isSuccessful) {
                    try {
                        val jsonObj = XML.toJSONObject(resp.body()!!.string())
                        if (jsonObj.has("error")){
                            onResult(false, jsonObj["error"].toString(), null)
                            return
                        }

                        try {
                            val info = jsonObj.getString("info")
                            onResult(true, info, null)
                            return
                        } catch (e: Exception){
                            Log.d("updateOpenOrderToPickedUp", "No Info: $e")
                        }
                    }catch(err: JSONException){
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

    fun updateDeliveredToRoundTrip(
        orderId: String, waitTime: String, transportation: String, boxes: String, reasonType: String,
        onResult: (status: Boolean, message: String, data: Order?) -> Unit
    ){
        val call = RetrofitService().orderDao.updateDeliveredToRoundTrip(
            roleName = userRole,
            orderId = orderId,
            waitTime = waitTime,
            transportation = transportation,
            boxes = boxes,
            driverId = currentUser.id,
            isRoundTrip = "yes",
            roundTrip = "yes",
            datetime = DateFormatterHelper.format("yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("America/Chicago"), Date()),
            reasonType = reasonType,
        )

        call.enqueue(object : Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, resp: Response<ResponseBody>) {
                if (resp.isSuccessful) {
                    try {
                        val jsonObj = XML.toJSONObject(resp.body()!!.string())
                        if (jsonObj.has("error")){
                            onResult(false, jsonObj["error"].toString(), null)
                            return
                        }

                        try {
                            val info = jsonObj.getString("info")
                            onResult(true, info, null)
                            return
                        } catch (e: Exception){
                            Log.d("updateDeliveredToRoundTrip", "No Info: $e")
                        }
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

    fun updateOrderToComplete(
        orderId: String, notes: String, lastName: String, fileName: String,
        onResult: (status: Boolean, message: String, data: Order?) -> Unit
    ){
        val call = RetrofitService().orderDao.updateOrderToComplete(
            roleName = userRole,
            orderId = orderId,
            notes = notes,
            lastname = lastName,
            driverId = currentUser.id,
            filename = fileName,
            datetime = DateFormatterHelper.format("yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("America/Chicago"), Date()),
        )

        call.enqueue(object : Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, resp: Response<ResponseBody>) {
                if (resp.isSuccessful) {
                    try {
                        val jsonObj = XML.toJSONObject(resp.body()!!.string())
                        if (jsonObj.has("error")){
                            onResult(false, jsonObj["error"].toString(), null)
                            return
                        }

                        try {
                            val info = jsonObj.getString("info")
                            onResult(true, info, null)
                            return
                        } catch (e: Exception){
                            Log.d("updateOrderToComplete", "No Info: $e")
                        }
                    }catch(err: JSONException){
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

    fun updateRoundTripToDelivery(
        orderId: String, lastName: String, fileName: String, notes: String,
        onResult: (status: Boolean, message: String, data: Order?) -> Unit
    ){
        val call = RetrofitService().orderDao.updateRoundTripToDelivery(
            roleName = userRole,
            orderId = orderId,
            driverId = currentUser.id,
            lastname = lastName,
            filename = fileName,
            notes = notes,
            datetime = DateFormatterHelper.format("yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("America/Chicago"), Date()),
        )

        call.enqueue(object : Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, resp: Response<ResponseBody>) {
                if (resp.isSuccessful) {
                    try {
                        val jsonObj = XML.toJSONObject(resp.body()!!.string())
                        if (jsonObj.has("error")){
                            onResult(false, jsonObj["error"].toString(), null)
                            return
                        }

                        try {
                            val info = jsonObj.getString("info")
                            onResult(true, info, null)
                            return
                        } catch (e: Exception){
                            Log.d("updateRoundTripToDelivery", "No Info: $e")
                        }
                    }catch(err: JSONException){
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
    //endregion
    //region Order
    internal fun getAllOrderFromServer(
        status: String,
        onResult: (status: Boolean, message: String, data: List<Order>) -> Unit
    ){
        val call = RetrofitService().orderDao.getAllOrder(
            userId = currentUser.id,
            role = userRole,
            status = status,
            device = "android"
        )

        call.enqueue(object : Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, resp: Response<ResponseBody>) {
                if (resp.isSuccessful) {
                    try {
                        val jsonObj = XML.toJSONObject(resp.body()!!.string())
                        if (jsonObj.has("error")){
                            onResult(false, jsonObj["error"].toString(), arrayListOf())
                            return
                        }

                        var orders = arrayListOf<Order>()
                        val gson = GsonBuilder().create()

                        if (jsonObj.get("dispatches-info") is String){
                            onResult(true, jsonObj.getString("dispatches-info"), orders)
                            return
                        }

                        if (jsonObj.getJSONObject("dispatches-info").get("order") is JSONObject){
                            val order = gson.fromJson<Order>(
                                jsonObj.getJSONObject("dispatches-info").getJSONObject("order").toString(),
                                object : TypeToken<Order>(){}.type
                            )
                            orders.add(order)
                            onResult(true, "", orders)
                            return
                        }

                        orders = gson.fromJson<ArrayList<Order>>(
                            jsonObj.getJSONObject("dispatches-info").getJSONArray("order").toString(),
                            object : TypeToken<ArrayList<Order>>(){}.type
                        ).toList() as ArrayList<Order>
                        onResult(true, "", orders)
                    }catch(err: JSONException){
                        println(err)
                        onResult(false, err.localizedMessage ?: "", arrayListOf())
                    }
                }
            }
            override fun onFailure(call: Call<ResponseBody>, err: Throwable) {
                println(err)
                onResult(false, err.localizedMessage ?: "", arrayListOf())
            }
        })
    }

    fun getOrderDetail(
        orderId: String,
        onResult: (status: Boolean, message:String, data: Order?) -> Unit,
    ){
        val dispatchCall = RetrofitService().orderDao.getOrderDetail(
            orderId = orderId,
            role = userRole,
        )

        dispatchCall.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, resp: Response<ResponseBody>) {
                if (resp.isSuccessful) {
                    try {
                        val jsonObj = XML.toJSONObject(resp.body()!!.string())
                        if (jsonObj.has("error")){
                            onResult(false, jsonObj["error"].toString(), null)
                            return
                        }

                        val orderDetail = jsonObj.getJSONObject("order-details")

                        if(orderDetail.getString("PUAddress") == ""){
                            orderDetail.putOpt("PUAddress", JSONObject.NULL)
                        }

                        if(orderDetail.getString("DLAddress") == ""){
                            orderDetail.putOpt("DLAddress", JSONObject.NULL)
                        }

                        val gson = GsonBuilder().create()
                        val order = gson.fromJson<Order>(
                            orderDetail.toString(),
                            object : TypeToken<Order>(){}.type
                        )
                        onResult(true, "", order)
                    }catch(err: JSONException){
                        println("(DEBUG) ERROR")
                        println(err)

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

    fun getOrderIdFromQRCode(
        vendorOrderId: String,
        onResult: (status: Boolean, message: String, data: Order?) -> Unit
    ){
        val call = RetrofitService().orderDao.getOrderIdFromQRCode(
            roleName = userRole,
            vendorOrderId = vendorOrderId
        )

        call.enqueue(object : Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, resp: Response<ResponseBody>) {
                if (resp.isSuccessful) {
                    try {
                        val jsonObj = XML.toJSONObject(resp.body()!!.string())
                        val gson = GsonBuilder().create()

                        try {
                            val order = gson.fromJson<Order>(
                                jsonObj.getJSONObject("dispatches-info").getJSONObject("order").toString(),
                                object : TypeToken<Order>(){}.type
                            )
                            onResult(true, "", order)
                            return
                        } catch (err: Exception){
                            Log.d("getOrderIdFromQRCode", "No Dispatch Info: $err")
                        }

                        onResult(false, "Order Not Found", null)
                    }catch(err: JSONException){
                        onResult(false, err.localizedMessage!!, null)
                    }
                }
            }
            override fun onFailure(call: Call<ResponseBody>, err: Throwable) {
                onResult(false, err.localizedMessage!!, null)
            }
        })
    }

    fun getOpenOrder(
        onResult: (status: Boolean, message: String, data: List<Order>) -> Unit
    ){
        val call = RetrofitService().orderDao.getOpenOrder(
            userId = currentUser.id,
            roleName = userRole
        )

        call.enqueue(object : Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful){
                    try {
                        val jsonObj = XML.toJSONObject(response.body()!!.string())
                        if (jsonObj.has("error")){
                            onResult(false, jsonObj["error"].toString(), arrayListOf())
                            return
                        }

                        var orders = arrayListOf<Order>()
                        val gson = GsonBuilder().create()

                        if (jsonObj.has("info")){
                            onResult(true, jsonObj.getString("info"), orders)
                            return
                        }

                        orders = gson.fromJson<ArrayList<Order>>(
                            jsonObj.getJSONObject("dispatches-info").getJSONArray("order").toString(),
                            object : TypeToken<ArrayList<Order>>(){}.type
                        ).toList() as ArrayList<Order>
                        onResult(true, "", orders)
                    } catch (e: JSONException){
                        onResult(false, e.localizedMessage ?: "", arrayListOf())
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                onResult(false, t.localizedMessage ?: "", arrayListOf())
            }
        })
    }
    //endregion
    //region Pickup
    fun updateOrderDelivery(
        orderId: String, waitTime: String, transportation: String, boxes: String, isRoundTrip: String,
        fileName: String, lastName: String, notes: String, reasonType: String, partialDeliver: String,
        onResult: (status: Boolean, message: String, data: Order?) -> Unit
    ){
        val call = RetrofitService().orderDao.updateOrderDelivery(
            role = userRole,
            orderId = orderId,
            waitTime = waitTime,
            transportation = transportation,
            boxes = boxes,
            driverId = currentUser.id,
            isRoundTrip = isRoundTrip,
            roundTrip = isRoundTrip,
            datetime = DateFormatterHelper.format("yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("America/Chicago"), Date()),
            filename = fileName,
            lastname = lastName,
            notes = notes,
            reasonType = reasonType,
            partialDeliver = partialDeliver
        )

        call.enqueue(object : Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, resp: Response<ResponseBody>) {
                if (resp.isSuccessful) {
                    try {
                        val jsonObj = XML.toJSONObject(resp.body()!!.string())
                        if (jsonObj.has("error")){
                            onResult(false, jsonObj["error"].toString(), null)
                            return
                        }

                        try {
                            val info = jsonObj.getString("info")
                            onResult(true, info, null)
                            return
                        } catch (e: Exception){
                            Log.d("updateOrderDelivery", "No Info: $e")
                        }
                    }catch(err: JSONException){
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
    //endregion
}