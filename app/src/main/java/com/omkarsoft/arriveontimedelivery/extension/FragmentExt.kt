package com.omkarsoft.arriveontimedelivery.extension

import android.content.Context
import android.content.pm.PackageManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.omkarsoft.arriveontimedelivery.constant.BASE_FOLDER
import com.omkarsoft.arriveontimedelivery.constant.CACHE_DURATION
import com.omkarsoft.arriveontimedelivery.constant.OrderType
import com.omkarsoft.arriveontimedelivery.helper.SharedPreferencesHelper
import pl.aprilapps.easyphotopicker.EasyImage
import java.util.*

fun Fragment.hideKeyboard() {
    view?.let{activity?.hideKeyboard(it)}
}

fun Context.hideKeyboard(view: View){
    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun View.visible(){
    this.visibility = View.VISIBLE
}

fun View.gone(){
    this.visibility = View.GONE
}

fun Context.toast(message: String, duration: Int = Toast.LENGTH_LONG){
    Toast.makeText(this, message, duration).show()
}

fun Fragment.toast(message: String){
    this.requireContext().toast(message)
}

fun Int.toDoubleDigit(): String {
    return if (this < 10){
        "0$this"
    } else {
        this.toString()
    }
}

fun getBaseImageFolder(): String {
    val calendar = Calendar.getInstance()
    calendar.time = Date()

    val currentYear = calendar.get(Calendar.YEAR)
    val currentMonth = (calendar.get(Calendar.MONTH) + 1).toDoubleDigit()

    return "$BASE_FOLDER/$currentYear/$currentMonth/images"
}

fun getBaseSignFolder(): String {
    val calendar = Calendar.getInstance()
    calendar.time = Date()

    val currentYear = calendar.get(Calendar.YEAR)
    val currentMonth = (calendar.get(Calendar.MONTH) + 1).toDoubleDigit()

    return "$BASE_FOLDER/$currentYear/$currentMonth/sign"
}

fun Context.needLoadFromRemote(orderType: OrderType): Boolean {
    val updateTime = SharedPreferencesHelper().getCacheTime(orderType)
    return !(updateTime != 0L && System.nanoTime() - updateTime < CACHE_DURATION)
}

fun Fragment.showChooseImageDialog(){
    val storageAccess = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    val cameraAccess = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    if (storageAccess && cameraAccess){
        EasyImage.openChooserWithGallery(this, "Choose Image", 0)
    } else if (storageAccess) {
        EasyImage.openGallery(this, 0)
    } else if (cameraAccess){
        EasyImage.openCameraForImage(this, 0)
    } else {
        toast("Permission denied to read your external storage or using camera")
    }
}