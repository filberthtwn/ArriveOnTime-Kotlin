package com.omkarsoft.arriveontimedelivery.data.model

import com.google.gson.annotations.SerializedName

data class Notification (
    @SerializedName("date")
    var date: String = "",

    @SerializedName("message")
    var message: String = ""
)