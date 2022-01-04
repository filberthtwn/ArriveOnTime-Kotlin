package com.omkarsoft.arriveontimedelivery.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.omkarsoft.arriveontimedelivery.constant.CACHE_DURATION
import com.omkarsoft.arriveontimedelivery.constant.OrderType
import com.omkarsoft.arriveontimedelivery.data.database.AppDatabase
import com.omkarsoft.arriveontimedelivery.data.model.CancelReasonType
import com.omkarsoft.arriveontimedelivery.data.model.Order
import com.omkarsoft.arriveontimedelivery.data.repository.OrderRepo
import com.omkarsoft.arriveontimedelivery.helper.SharedPreferencesHelper

class OrderViewModel(application: Application): AndroidViewModel(application) {
    private val prefHelper = SharedPreferencesHelper(application)

    private val _order = MutableLiveData<Order>()
    val order: LiveData<Order> = _order

    private val _orders = MutableLiveData<List<Order>>()
    val orders: LiveData<List<Order>> = _orders

    private val _cancelReasonTypes = MutableLiveData<List<CancelReasonType>>()
    val cancelReasonTypes: LiveData<List<CancelReasonType>> = _cancelReasonTypes

    private val _errMsg = MutableLiveData<String>()
    val errMsg: LiveData<String> = _errMsg

    private val _success = MutableLiveData<Boolean>()
    val success: LiveData<Boolean> = _success

    fun getOrders(orderType: OrderType, bypassCache: Boolean){
        if (bypassCache){
            getOrderFromRemote(orderType)
        } else {
            val updateTime = prefHelper.getCacheTime(orderType)
            Log.d("CacheTimeLeft", "${(CACHE_DURATION - (System.nanoTime() - updateTime)) / 1000000000} seconds, orderType: $orderType")
            if (updateTime != 0L && System.nanoTime() - updateTime < CACHE_DURATION){
                getOrderFromDatabase(orderType)
            } else {
                getOrderFromRemote(orderType)
            }
        }
    }

    private fun getOrderFromRemote(orderType: OrderType){
        OrderRepo.shared().getOrders(orderType){ status, message, list ->
            if (!status){
                _errMsg.value = message
            }
            storeDataLocally(orderType, list)
            _orders.value = list
        }
    }

    private fun getOrderFromDatabase(orderType: OrderType){
        val orders = AppDatabase(getApplication()).orderDao().getOrders(orderType.toString())
        _orders.value = orders
    }

    private fun storeDataLocally(orderType: OrderType, orderList: List<Order>){
        val dao = AppDatabase(getApplication()).orderDao()
        dao.deleteOrders(orderType = orderType.toString())

        orderList.map {
            it.order_type = orderType.toString()
            it.convertObjectToVariables()
        }
        dao.insertOrders(*orderList.toTypedArray())

        prefHelper.saveUpdateTime(orderType, System.nanoTime())
    }

    //region Dispatch Order
    fun confirmDispatchOrder(orderIds: List<String>){
        OrderRepo.shared().confirmDispatchOrder(orderIds) authRepo@ { status, message, data ->
            if (!status){
                _errMsg.value = message
            }
            _success.value = status
        }
    }

    fun confirmDispatchOrder(orderId: String){
        OrderRepo.shared().confirmDispatchOrder(listOf(orderId)) authRepo@ { status, message, _ ->
            if (!status){
                _errMsg.value = message
            } else {
                _success.value = status
            }
        }
    }
    //endregion
    //region Round Trip Order
    fun updateOpenOrderToPickedUp(
        orderId: String, partialPiece:Int, isRefreshList: Boolean = false
    ){
        OrderRepo.shared().updateOpenOrderToPickedUp(orderId, partialPiece){ status, message, data ->
            if (!status){
                _errMsg.value = message
            }
            data?.let {
                _order.value = data
            } ?: run {
                _success.value = status

                if (isRefreshList){
                    getOrders(OrderType.OPEN, true)
                    return@updateOpenOrderToPickedUp
                }
            }
        }
        prefHelper.saveUpdateTime(OrderType.OPEN, 0L)
    }

    fun updateOpenOrderToPickedUp(orderIds: List<String>){
        updateOpenOrderToPickedUp(orderIds.joinToString(separator = ","), 1,true)
    }

    fun updateDeliveredToRoundTrip(
        orderId: String,
        waitTime: String,
        transportation: String,
        boxes: String,
        reasonType: String
    ){
        OrderRepo.shared().updateDeliveredToRoundTrip(orderId, waitTime, transportation, boxes, reasonType){ status, message, data ->
            if (!status){
                _errMsg.value = message
            }
            data?.let {
                _order.value = data
            } ?: run {
                _success.value = status
            }
        }
    }

    fun updateOrderToComplete(
        orderId: String, notes: String, lastName: String, fileName: String
    ){
        OrderRepo.shared().updateOrderToComplete(orderId, notes, lastName, fileName){ status, message, data ->
            if (!status){
                _errMsg.value = message
            }
            data?.let {
                _order.value = data
            } ?: run {
                _success.value = status
            }
        }
    }

    fun updateRoundTripToDelivery(
        orderId: String,
        lastName: String,
        notes: String,
        fileName: String
    ){
        OrderRepo.shared().updateRoundTripToDelivery(orderId, lastName, fileName, notes){ status, message, data ->
            if (!status){
                _errMsg.value = message
            }
            data?.let {
                _order.value = data
            } ?: run {
                _success.value = status
            }
        }
    }
    //endregion
    //region Order
    fun getOrderDetail(orderId: String, isDispatch: Boolean = false){
        OrderRepo.shared().getOrderDetail(orderId) { status, message, data ->
            if (status && data != null){
                data.convertObjectToVariables()
                _order.value = data
                return@getOrderDetail
            }
            _errMsg.value = message
        }
    }

    fun getOrderIdFromQRCode(vendorOrderId: String){
        OrderRepo.shared().getOrderIdFromQRCode(vendorOrderId) orderRepo@ { status, message, data ->
            if (!status){
                _errMsg.value = message
                return@orderRepo
            }
            _order.value = data
        }
    }
    //endregion
    //region Pickup
    fun updateOrderDelivery(
        lastName: String,
        notes: String,
        orderId: String,
        waitTime: String,
        transportation: String,
        boxes: String,
        fileName: String,
        isRoundTrip: String,
        reasonType: String,
        partialDeliver: String
        ){
        OrderRepo.shared().updateOrderDelivery(
            orderId = orderId,
            waitTime = waitTime,
            transportation = transportation,
            boxes = boxes,
            isRoundTrip = isRoundTrip,
            fileName = fileName,
            lastName = lastName,
            notes = notes,
            reasonType = reasonType,
            partialDeliver = partialDeliver
        ) orderRepo@ { status, message, data ->
            if (!status){
                _errMsg.value = message
            }
            data?.let {
                _order.value = data
            } ?: run {
                _success.value = status
            }
        }
    }

    //endregion

    fun getCancelReasonType(){
        OrderRepo.shared().getCancelReasonType{ status, message, data ->
            if (!status){
                _errMsg.value = message
                return@getCancelReasonType
            }
            _cancelReasonTypes.value = data
        }
    }

    fun cancelDeliveryOrder(orderId:String, reasonType:String, comment:String){
        OrderRepo.shared().cancelDeliveryOrder(
            orderId = orderId,
            reasonType = reasonType,
            comment = comment,
        ){ status, message, data ->
            if (!status){
                _errMsg.value = message
                return@cancelDeliveryOrder
            }
            _success.value = true
        }
    }
}