package com.omkarsoft.arriveontimedelivery.viewInterface

interface QROrderDetailInterface {
    fun onQRScanned(jsonString: String)
}

interface AutoMoveFragmentInterface {
    fun moveToPresent()
}

