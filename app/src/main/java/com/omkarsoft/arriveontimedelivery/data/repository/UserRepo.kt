package com.omkarsoft.arriveontimedelivery.data.repository

import com.omkarsoft.arriveontimedelivery.data.utils.RetrofitService
import com.omkarsoft.arriveontimedelivery.helper.SharedPreferencesHelper
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.XML
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserRepo {
    private val prefHelper = SharedPreferencesHelper()
    private val currentUser = prefHelper.getCurrentUser()!!

    fun updateFCMToken(
        fcmToken:String,
        onResult: (success: Boolean, message: String) -> Unit){
        val loginCall = RetrofitService().userDao.updateFCMToken(
            driverId = currentUser.id,
            fcmToken = fcmToken
        )
        loginCall.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, resp: Response<ResponseBody>) {
                if (resp.isSuccessful) {
                    try {
                        val jsonObj = XML.toJSONObject(resp.body()!!.string())
                        if (jsonObj.has("error")){
                            onResult(false, jsonObj["error"].toString())
                            return
                        }
                        onResult(true, jsonObj["token-updated"].toString())
                    }catch(err: JSONException){
                        println(err)
                        onResult(false, err.localizedMessage!!)
                    }
                }
            }
            override fun onFailure(call: Call<ResponseBody>, err: Throwable) {
                onResult(false, err.localizedMessage!!)
            }
        })
    }

    companion object {
        private var INSTANCE: UserRepo? = null
        fun shared() = INSTANCE
            ?: UserRepo().also {
                INSTANCE = it
            }
    }
}