package com.omkarsoft.arriveontimedelivery.viewInterface

import com.omkarsoft.arriveontimedelivery.data.model.Order

interface OrderListener {
    fun onButtonClicked(order: Order)
    fun onOrderClicked(order: Order)
}

interface OrderConfirmInterface {
    fun onSelected(orderList: List<Order>)
    fun onDeliveredOrderSelected(order: Order) {}
}

interface OrderDeliverInterface {
    fun onDeliveredOrderSelected(order: Order)
}

interface DispatchInterface {
    fun didOrderAccepted()
}