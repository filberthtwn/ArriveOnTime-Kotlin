package com.omkarsoft.arriveontimedelivery.view.template

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.omkarsoft.arriveontimedelivery.databinding.LoadingDialogBinding

class LoadingDialog(private val fm: FragmentManager): DialogFragment() {
    private var shown = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return LoadingDialogBinding.inflate(inflater, container, false).root
    }

    fun startLoading(){
        isCancelable = false
        shown = true
        show(fm, "LoadingDialog")
    }

    fun stopLoading(){
        if (shown)
            dismiss()
    }
}