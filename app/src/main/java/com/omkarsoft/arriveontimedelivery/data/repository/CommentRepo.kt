package com.omkarsoft.arriveontimedelivery.data.repository

import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.omkarsoft.arriveontimedelivery.data.model.Notification
import com.omkarsoft.arriveontimedelivery.data.utils.RetrofitService
import com.omkarsoft.arriveontimedelivery.helper.SharedPreferencesHelper
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.XML
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CommentRepo {
    //region Comments
    fun getAllComments(
        onResult: (success: Boolean, message: String, data: ArrayList<Notification>) -> Unit
    ){
        val user = SharedPreferencesHelper().getCurrentUser()!!
        val call = RetrofitService().commentDao.getAllComments(
            driverId = user.id
        )

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful){
                    try {
                        val jsonObject = XML.toJSONObject(response.body()!!.string())
                        if (jsonObject.has("error")){
                            onResult(false, jsonObject["error"].toString(), arrayListOf())
                            return
                        }

                        var notifications = arrayListOf<Notification>()
                        val gson = GsonBuilder().create()

                        try {
                            val notif = gson.fromJson<Notification>(
                                jsonObject.getJSONObject("data").getJSONObject("notification").toString(),
                                object : TypeToken<Notification>(){}.type
                            )
                            notifications.add(notif)
                            onResult(true, "", notifications)
                            return
                        } catch (e: Exception){
                            Log.d("getAllComments", "No Notification: $e")
                        }

                        try {
                            notifications = gson.fromJson<ArrayList<Notification>>(
                                jsonObject.getJSONObject("data").getJSONObject("notification").toString(),
                                object : TypeToken<ArrayList<Notification>>(){}.type
                            ).toList() as ArrayList<Notification>
                            onResult(true, "", notifications)
                            return
                        } catch (e: Exception){
                            Log.d("getAllComments", "No Notification List: $e")
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

    companion object {
        private var INSTANCE: CommentRepo? = null
        fun shared() = INSTANCE
            ?: CommentRepo().also {
                INSTANCE = it
            }
    }
}