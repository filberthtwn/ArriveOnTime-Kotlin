package com.omkarsoft.arriveontimedelivery.data.utils

import com.omkarsoft.arriveontimedelivery.constant.API_URL
import com.omkarsoft.arriveontimedelivery.data.rest.AuthDao
import com.omkarsoft.arriveontimedelivery.data.rest.CommentDao
import com.omkarsoft.arriveontimedelivery.data.rest.OrderDao
import com.omkarsoft.arriveontimedelivery.data.rest.UserDao
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitService {
    private val interceptor: HttpLoggingInterceptor = HttpLoggingInterceptor(ApiLogger())
        .setLevel(HttpLoggingInterceptor.Level.BODY)

    private val client = OkHttpClient.Builder()
        .readTimeout(60, TimeUnit.SECONDS)
        .connectTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(interceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(API_URL)
        .addConverterFactory(SimpleXmlConverterFactory.create())
        .client(client)
        .build()

    val authDao: AuthDao = retrofit.create(AuthDao::class.java)
    val userDao: UserDao = retrofit.create(UserDao::class.java)
    val orderDao: OrderDao = retrofit.create(OrderDao::class.java)
    val commentDao: CommentDao = retrofit.create(CommentDao::class.java)
}