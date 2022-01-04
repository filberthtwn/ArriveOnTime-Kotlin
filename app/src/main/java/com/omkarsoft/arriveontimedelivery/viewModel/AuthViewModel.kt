package com.omkarsoft.arriveontimedelivery.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.omkarsoft.arriveontimedelivery.data.model.User
import com.omkarsoft.arriveontimedelivery.data.repository.AuthRepo

class AuthViewModel(application: Application): AndroidViewModel(application) {
    val user = MutableLiveData<User>()
    val errMsg = MutableLiveData<String>()

    fun login(username:String, password:String) {
        AuthRepo.shared().login(username, password) authRepo@ { status, message, user ->
            if (!status){
                this.errMsg.postValue(message)
                return@authRepo
            }
            this.user.postValue(user)
        }
    }
}