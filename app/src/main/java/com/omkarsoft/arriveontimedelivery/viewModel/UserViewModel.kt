package com.omkarsoft.arriveontimedelivery.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.omkarsoft.arriveontimedelivery.data.repository.UserRepo

class UserViewModel(application: Application): AndroidViewModel(application) {
    var isSuccess = MutableLiveData<Boolean>()
    var errMsg = MutableLiveData<String>()

    fun updateFCMToken(fcmToken:String){
        UserRepo.shared().updateFCMToken(fcmToken) { success, message ->
            if (!success){
                errMsg.postValue(message)
                return@updateFCMToken
            }
            isSuccess.postValue(true)
        }
    }
}