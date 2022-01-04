package com.omkarsoft.arriveontimedelivery.data.model

import com.google.gson.annotations.SerializedName

class CancelReasonTypeResp {
    @SerializedName("success")
    var success: Boolean = false

    @SerializedName("data")
    var data: List<CancelReasonType> = listOf()
}

class CancelReasonType {
    @SerializedName("id")
    var id: String = ""

    @SerializedName("reason")
    var reason: String = ""

    override fun toString(): String {
        return reason
    }
}