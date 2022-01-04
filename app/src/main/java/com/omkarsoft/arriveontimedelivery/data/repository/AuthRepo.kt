package com.omkarsoft.arriveontimedelivery.data.repository

import com.google.gson.Gson
import com.omkarsoft.arriveontimedelivery.data.model.User
import com.omkarsoft.arriveontimedelivery.data.utils.RetrofitService
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.XML
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AuthRepo {
    fun login(
        userId:String,
        password:String,
        onResult: (status: Boolean, message:String, data: User?) -> Unit,
    ){
        val loginCall =
            RetrofitService().authDao.login(userId, password)
        loginCall.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, resp: Response<ResponseBody>) {
                if (resp.isSuccessful) {
                    try {
                        val jsonObj = XML.toJSONObject(resp.body()!!.string())
                        if (jsonObj.has("error")){
                            onResult(false, jsonObj["error"].toString(), null)
                            return
                        }
                        val user = Gson().fromJson(jsonObj["user-info"].toString(), User::class.java)
                        println(user)
                        onResult(true, "", user)
                    }catch(err: JSONException){
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

    companion object {
        private var INSTANCE: AuthRepo? = null
        fun shared() = INSTANCE
            ?: AuthRepo().also {
                INSTANCE = it
            }
    }
}