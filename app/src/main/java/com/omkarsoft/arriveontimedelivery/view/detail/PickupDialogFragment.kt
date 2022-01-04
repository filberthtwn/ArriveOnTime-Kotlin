package com.omkarsoft.arriveontimedelivery.view.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.omkarsoft.arriveontimedelivery.constant.Status
import com.omkarsoft.arriveontimedelivery.data.model.Order
import com.omkarsoft.arriveontimedelivery.databinding.FragmentPickupDialogBinding
import com.omkarsoft.arriveontimedelivery.extension.toast
import com.omkarsoft.arriveontimedelivery.view.template.LoadingDialog
import kotlinx.android.synthetic.main.fragment_pickup_dialog.*

class PickupDialogFragment(
    private val order: Order,
    private val fm: FragmentManager,
    private val onDonePressed: (piece: Int) -> Unit
) : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPickupDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    private fun setupViews(){
        /// Setup Button for Dispatch to Pickup
        if(order.status == Status.DISPATCH || order.status == Status.DISPATCHED){
            dismiss()
            onDonePressed(order.piece.toInt())
            return
        }

        /// Setup Button for Open Order to Picked Up && Partial Pickup
//        if(order.partialPiece.toInt() < order.piece.toInt()){
//            tv_popup_title.text = "No. of picked up package (${order.partialPiece.toInt()}/${order.piece.toInt()})"
//            btn_pickup_done.setOnClickListener {
//                if (edt_num_delivering.text.toString().trim() != ""){
//                    val numberOfPackage = edt_num_delivering.text.toString().toInt()
//                    if(numberOfPackage > 0 && (order.partialPiece.toInt() + numberOfPackage) <= order.piece.toInt()){
//                        dismiss()
//                        onDonePressed(numberOfPackage)
//                        return@setOnClickListener
//                    }else{
//                        toast("Order Package Mismatch")
//                    }
//                }
//            }
//            return
//        }

        /// Setup Button for Picked Up to Deliver
        btn_pickup_done.setOnClickListener {
            if (edt_num_delivering.text.toString().trim() != "") {
                val numberOfPackage = edt_num_delivering.text.toString().toInt()
                if (numberOfPackage == order.piece.toInt()) {
                    dismiss()
                    onDonePressed(numberOfPackage)
                    return@setOnClickListener
                } else {
                    toast("Order Package Mismatch")
                }
            }
        }
    }

    fun startLoading(){
        show(fm, "Warning Dialog")
    }

    fun stopLoading(){
        dismiss()
    }
}