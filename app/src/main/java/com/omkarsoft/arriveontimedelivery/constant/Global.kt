package com.omkarsoft.arriveontimedelivery.constant

import com.omkarsoft.arriveontimedelivery.data.model.Order

class Global {
    companion object {
        var isNeedLoading = false
        var isNeedMoveToPresent = false

        var deliverOrders: List<Order>? = null
        var presentOrders: List<Order>? = null
        var dispatchOrders: List<Order>? = null
        var openOrders: List<Order>? = null
    }
}