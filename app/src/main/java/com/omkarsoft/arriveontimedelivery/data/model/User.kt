package com.omkarsoft.arriveontimedelivery.data.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("user_id")
    var id: String = "",

    @SerializedName("Name")
    var name: String = "",

    @SerializedName("roleName")
    var role: String = "",

    @SerializedName("locationName")
    var location: String = ""
)

