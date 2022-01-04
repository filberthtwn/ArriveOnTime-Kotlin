package com.omkarsoft.arriveontimedelivery.view.main

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.zxing.Result
import com.omkarsoft.arriveontimedelivery.databinding.FragmentQRScannerBinding
import com.omkarsoft.arriveontimedelivery.viewInterface.QROrderDetailInterface
import kotlinx.android.synthetic.main.fragment_q_r_scanner.*
import me.dm7.barcodescanner.zxing.ZXingScannerView

class QRScannerFragment(
    private val fm: FragmentManager,
    private val listener: QROrderDetailInterface
): BottomSheetDialogFragment(), ZXingScannerView.ResultHandler {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentQRScannerBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        qr_scanner.startCamera()
        qr_scanner.setResultHandler(this)
    }

    override fun handleResult(result: Result) {
        val text = result.text
        dismiss()
        listener.onQRScanned(text)
    }

    override fun onResume() {
        super.onResume()
        qr_scanner.setResultHandler(this)
        qr_scanner.startCamera()
    }

    override fun onPause() {
        super.onPause()
        qr_scanner.stopCamera()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        dismiss()
    }

    fun startLoading(){
        isCancelable = true
        show(fm, "Bottom Dialog")
    }
}