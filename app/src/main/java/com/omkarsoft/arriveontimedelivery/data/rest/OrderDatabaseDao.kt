package com.omkarsoft.arriveontimedelivery.data.rest

import androidx.room.*
import com.omkarsoft.arriveontimedelivery.data.model.Order

@Dao
interface OrderDatabaseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrders(vararg orders: Order): List<Long>

    @Delete
    fun deleteOrders(vararg orders: Order)

    @Query("DELETE FROM orders")
    fun deleteOrders()

    @Query("DELETE FROM orders WHERE order_type = :orderType")
    fun deleteOrders(orderType: String)

    @Query("SELECT * FROM orders WHERE order_type = :orderType")
    fun getOrders(orderType: String): List<Order>

    @Query("SELECT * from orders WHERE id = :orderId")
    fun getOrder(orderId: String): Order?

    @Query("UPDATE orders SET serviceName = :serviceName, piece = :piece, weight = :weight, status = :status, roundTrip = :roundTrip WHERE id = :orderId")
    fun updateOrder(orderId: String, serviceName: String, piece: String, weight: String, status: String, roundTrip: Int)

    @Query("UPDATE orders SET accountNotes = :accountNotes, serviceName = :serviceName, status = :status, senderLatitude = :senderLatitude, senderLongitude = :senderLongitude, recipientLatitude = :recipientLatitude, recipientLongitude = :recipientLongitude, requestor = :requestor, piece = :piece, weight = :weight, adminNotes = :adminNotes WHERE id = :orderId")
    fun updateOrder(
        orderId: String,
        accountNotes: String, serviceName: String, status: String,
        senderLatitude: String, senderLongitude: String,
        recipientLatitude: String, recipientLongitude: String,
        requestor: String, piece: String, weight: String, adminNotes: String
    )
}