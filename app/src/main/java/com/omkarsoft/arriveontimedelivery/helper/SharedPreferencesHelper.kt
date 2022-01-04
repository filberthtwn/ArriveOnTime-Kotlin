package com.omkarsoft.arriveontimedelivery.helper

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.omkarsoft.arriveontimedelivery.constant.Destination
import com.omkarsoft.arriveontimedelivery.constant.OrderType
import com.omkarsoft.arriveontimedelivery.data.model.Order
import com.omkarsoft.arriveontimedelivery.data.model.User
import com.google.gson.reflect.TypeToken




class SharedPreferencesHelper {
    companion object {
        //user
        private const val user = "current-user"
        private const val userUsername = "user-username"
        private const val userPassword = "user-password"
        private const val fcmTokenIdentifier = "fcm-token"

        //navigation
        private const val currentFragment = "currentFragment"

        //Location
        private const val locationLatitude = "location-latitude"
        private const val locationLongitude = "location-longitude"

        //Cache Time
        private const val deliveryTime = "time-delivery"
        private const val presentTime = "time-present"
        private const val dispatchTime = "time-dispatch"
        private const val openTime = "time-next"
        private const val futureTime = "time-future"
        private const val partialTime = "time-partial-picku"

        internal var prefs: SharedPreferences? = null

        @Volatile
        internal var instance: SharedPreferencesHelper? = null
        private var LOCK = Any()

        operator fun invoke(context: Context): SharedPreferencesHelper =
            instance ?: synchronized(LOCK) {
                instance ?: buildHelper(context).also {
                    instance = it
                }
            }

        private fun buildHelper(context: Context): SharedPreferencesHelper {
            prefs = PreferenceManager.getDefaultSharedPreferences(context)
            return SharedPreferencesHelper()
        }
    }

    fun saveCurrentUser(user: User){
        prefs?.edit(true){
            putString("current_user", Gson().toJson(user))
            apply()
        }
    }
    fun getCurrentUser(): User? = Gson().fromJson(prefs?.getString("current_user", ""), User::class.java)
    fun removeCurrentUser(){
        prefs?.edit(true){
            remove("current_user")
            apply()
        }
    }

    fun saveUserLogin(username: String, password: String){
        prefs?.edit(true) {
            putString(userUsername, username)
            putString(userPassword, password)
            apply()
        }
    }
    fun getUserUsername(): String = prefs?.getString(userUsername, "") ?: ""
    fun getUserPassword(): String = prefs?.getString(userPassword, "") ?: ""
    fun isRememberLogin() = getUserUsername().isNotEmpty()

    fun saveFCMToken(fcmToken:String){
        prefs?.edit(true) {
            putString(fcmTokenIdentifier, fcmToken)
            apply()
        }
    }
    fun getFCMToken(): String = prefs?.getString(fcmTokenIdentifier, "") ?: ""

    /// Partial Pickup
//    fun savePartialPickupOrder(pickupOrder: Order){
//        var tempOrder =  pickupOrder
//        val arrayList = getPartialPickupOrder()
//        if(arrayList.contains(pickupOrder)){
//            if(tempOrder.partialPiece.toInt() < tempOrder.piece.toInt()){
//                arrayList.removeAll { it.id == pickupOrder.id }
//            }
//        }
//        tempOrder.partialPiece = (tempOrder.partialPiece.toInt() + 1).toString()
//        arrayList.add(tempOrder)
//        prefs?.edit(true) {
//            val gson = Gson()
//            val json = gson.toJson(arrayList)
//            putString("partial_pickup_orders", json)
//            apply()
//        }
//    }
//    fun removePartialPickupOrder(username: String, password: String){
//        prefs?.edit(true) {
//            putString(userUsername, username)
//            putString(userPassword, password)
//            apply()
//        }
//    }
//    fun getPartialPickupOrder(): ArrayList<Order>{
//        prefs?.getString("partial_pickup_orders", null)?.let {
//            val gson = Gson()
//            val type = object : TypeToken<ArrayList<Order>>() {}.type
//            return gson.fromJson(it, type)
//        }.run {
//            return arrayListOf()
//        }
//    }

    //Navigation
    //----------------------------------------------------------------------------------------------
    fun setLastFragment(fragment_name: String){
        prefs?.edit(true){
            putString(currentFragment, fragment_name)
        }
    }
    fun getLastFragment() = prefs?.getString(currentFragment, Destination.Main.PRESENT) ?: Destination.Main.PRESENT

    //Location
    //----------------------------------------------------------------------------------------------
    fun saveLocation(longitude: Double, latitude: Double){
        prefs?.edit(true){
            putFloat(locationLatitude, latitude.toFloat())
            putFloat(locationLongitude, longitude.toFloat())
        }
    }
    fun getLatitude() = prefs?.getFloat(locationLatitude, 0f)?.toDouble() ?: 0.0
    fun getLongitude() = prefs?.getFloat(locationLongitude, 0f)?.toDouble() ?: 0.0

    //Cache Time
    //----------------------------------------------------------------------------------------------
    fun saveUpdateTime(type: OrderType, time: Long){
        prefs?.edit(commit = true){
            when (type){
                OrderType.DELIVER -> putLong(deliveryTime, time)
                OrderType.PRESENT -> putLong(presentTime, time)
                OrderType.DISPATCH -> putLong(dispatchTime, time)
                OrderType.OPEN -> putLong(openTime, time)
                OrderType.FUTURE -> putLong(futureTime, time)
            }
        }
    }
    fun getCacheTime(type: OrderType): Long =
        when (type){
            OrderType.DELIVER -> prefs?.getLong(deliveryTime, 0) ?: 0
            OrderType.PRESENT -> prefs?.getLong(presentTime, 0) ?: 0
            OrderType.DISPATCH -> prefs?.getLong(dispatchTime, 0) ?: 0
            OrderType.OPEN -> prefs?.getLong(openTime, 0) ?: 0
            OrderType.FUTURE -> prefs?.getLong(futureTime, 0) ?: 0
            OrderType.PARTIAL_PICKUP -> prefs?.getLong(partialTime, 0) ?: 0
        }
}