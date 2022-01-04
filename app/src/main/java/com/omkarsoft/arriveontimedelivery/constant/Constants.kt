package com.omkarsoft.arriveontimedelivery.constant

const val BASE_URL = "https://www.aotdelivery.com/mobile2021/"
const val API_URL = BASE_URL
const val BASE_FOLDER = "aotfiles"
const val BASE_MAP_URL = "https://www.google.com/maps/dir"
const val CACHE_DURATION = 5 * 60 * 1000 * 1000 * 1000L //5 Minutes

class NotificationName {
    companion object {
        const val DISPATCH_SELECTED  = "dispatch-selected"
        const val CONFIRM_DISPATCH = "confirm-dispatch"
        const val ORDER_TAPPED = "order-tapped"
    }
}

enum class OrderType {
    DELIVER { override fun toString(): String = "deliver" },
    PRESENT { override fun toString(): String = "current" },
    DISPATCH { override fun toString(): String = "dispatch" },
    OPEN { override fun toString(): String = "open" },
    FUTURE  { override fun toString(): String = "future" },
    PARTIAL_PICKUP  { override fun toString(): String = "partial_pickup" }
}

class Status {
    companion object {
        const val DELIVERED = "Delivered"
        const val PICKED_UP = "Picked up"
        const val DISPATCH = "Dispatch"
        const val DISPATCHED = "Dispatched"
        const val OPEN_ORDER = "Open Order"
        const val ROUND_TRIP = "Round Trip"
        const val CANCELLED = "Cancelled"
    }
}

class Destination {
    class Main {
        companion object {
            const val DELIVERY = "delivery"
            const val PRESENT = "present"
            const val DISPATCH = "dispatch"
            const val NEXT = "next-day"
            const val PARTIAL_PICKUP = "partial-pickup"
            const val MORE = "more"
        }
    }
}