package com.omkarsoft.arriveontimedelivery.data.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "orders")
@Parcelize
class Order: Parcelable {
    @PrimaryKey
    @SerializedName("order_id")
    var id = ""

    @SerializedName("driver_id")
    var driverId = ""

    @SerializedName("accountName")
    var accountName = ""

    @SerializedName("notes")
    var accountNotes = ""

    @SerializedName("company")
    var senderName = ""

    @SerializedName("address")
    var senderAddress = ""

    @SerializedName("suit")
    var senderSuite = ""

    @SerializedName("city")
    var senderCity = ""

    @SerializedName("state")
    var senderCountry = ""

    @SerializedName("zip")
    var senderPostalCode = ""

    @SerializedName("cellPhone")
    var senderCellPhone = ""

    @SerializedName("homePhone")
    var senderHomePhone = ""

    @SerializedName("PUInstruction")
    var senderInstruction = ""

    @SerializedName("pulat")
    var senderLatitude = ""

    @SerializedName("pulog")
    var senderLongitude = ""

    @SerializedName("dlcompany")
    var recipientName = ""

    @SerializedName("dladdress")
    var recipientAddress = ""

    @SerializedName("dlsuit")
    var recipientSuite = ""

    @SerializedName("dlcity")
    var recipientCity = ""

    @SerializedName("dlstate")
    var recipientCountry = ""

    @SerializedName("dlzip")
    var recipientPostalCode = ""

    @SerializedName("dl_cellPhone")
    var recipientCellPhone = ""

    @SerializedName("dl_homePhone")
    var recipientHomePhone = ""

    @SerializedName("DLInstruction")
    var recipientInstruction = ""

    @SerializedName("dllat")
    var recipientLatitude = ""

    @SerializedName("dllog")
    var recipientLongitude = ""

    @SerializedName("serviceName")
    var serviceName = ""

    @SerializedName("RDDate")
    var pickupDate = ""

    @SerializedName("orderStatus")
    var status = ""

    @Ignore
    @SerializedName("shipper")
    private val shipperRelease: Int = 0

    @Ignore
    @SerializedName("signature")
    private val signature: Int = 0

    @SerializedName("isRoundTrip")
    var roundTrip: Int = 0

    @Ignore
    @SerializedName("isroundtrip")
    var roundtrip: Int = 0

    @SerializedName("PCRoundTrip")
    var hasRoundTrip: Int = 0

    @SerializedName("requestor")
    var requestor = ""

    @SerializedName(value="piece", alternate= ["Piece"])
    var piece = ""

    @SerializedName("weight")
    var weight = ""

    @SerializedName("adminNotes")
    var adminNotes = ""

    @SerializedName("SignRoundTrip")
    var signRoundtrip = ""

    @Ignore
    @SerializedName("RDDateFormat")
    var expectedPickupTime:String = ""

    @Ignore
    @SerializedName("EDDateFormat")
    var expectedDeliveryTime:String = ""

    @Ignore
    @SerializedName("PUAddress")
    var sender: Person? = null

    @Ignore
    @SerializedName("DLAddress")
    var recipient: Person? = null

    @Ignore
    @SerializedName("aotorderid")
    var aotOrderId = ""

    @Ignore
    @SerializedName("orderColor")
    var orderColor = ""

    @Ignore
    @SerializedName("vendororderid")
    var vendorOrderId = ""

    @ColumnInfo(name = "order_type")
    var order_type = ""

    @Ignore
    @SerializedName("partial_piece")
    var partialPiece: String = "0"

    @Ignore
    @SerializedName("partial_deliver")
    var partialDeliver: String = "0"

    fun isShipperRelease(): Boolean = shipperRelease == 0
    fun isSignatureRequired(): Boolean = signature == 0
    fun isRoundTrip(): Boolean = roundTrip == 1 || roundtrip == 1
    fun hasRoundTrip(): Boolean = hasRoundTrip == 1

    fun isPartialDeliver():Boolean{
        return (partialDeliver.toInt() > 0 && partialDeliver.toInt() < piece.toInt())
    }

    fun convertObjectToVariables() {
        sender?.let {
            senderName = it.name
            senderCellPhone = it.cecllPhone
            senderHomePhone = it.homePhone
            senderAddress = it.address
            senderSuite = it.suite
            senderCity = it.city
            senderCountry = it.country
            senderPostalCode = it.postalCode
        }

        recipient?.let {
            recipientName = it.name
            recipientCellPhone = it.cecllPhone
            recipientHomePhone = it.homePhone
            recipientAddress = it.address
            recipientSuite = it.suite
            recipientCity = it.city
            recipientCountry = it.country
            recipientPostalCode = it.postalCode
        }
    }
}



