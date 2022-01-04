package com.omkarsoft.arriveontimedelivery.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Person(
    @SerializedName("company")
    var name: String = "",

    @SerializedName("address")
    var address: String = "",

    @SerializedName("suit")
    var suite: String = "",

    @SerializedName("city")
    var city: String = "",

    @SerializedName("state")
    var country: String = "",

    @SerializedName("zip")
    var postalCode: String = "",

    @SerializedName("cellPhone")
    var cecllPhone: String = "-",

    @SerializedName("homePhone")
    var homePhone: String = "-",

    @SerializedName("log")
    var longitude: String = "",

    @SerializedName("lat")
    var latitude: String = ""
): Parcelable