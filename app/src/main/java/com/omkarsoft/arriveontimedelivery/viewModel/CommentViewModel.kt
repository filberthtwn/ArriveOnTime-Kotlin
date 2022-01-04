package com.omkarsoft.arriveontimedelivery.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.omkarsoft.arriveontimedelivery.data.model.Notification
import com.omkarsoft.arriveontimedelivery.data.repository.CommentRepo

class CommentViewModel(application: Application): AndroidViewModel(application) {
    var comments = MutableLiveData<List<Notification>>()
    var errMsg = MutableLiveData<String>()

    fun getAllComments(){
        CommentRepo.shared().getAllComments { success, message, data ->
            if (!success){
                errMsg.postValue(message)
                return@getAllComments
            }
            comments.postValue(data)
        }
    }
}