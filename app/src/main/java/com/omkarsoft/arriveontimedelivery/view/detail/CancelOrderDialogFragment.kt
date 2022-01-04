package com.omkarsoft.arriveontimedelivery.view.detail

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.omkarsoft.arriveontimedelivery.R
import com.omkarsoft.arriveontimedelivery.data.model.CancelReasonType
import com.omkarsoft.arriveontimedelivery.extension.toast
import com.omkarsoft.arriveontimedelivery.view.template.LoadingDialog
import com.omkarsoft.arriveontimedelivery.viewModel.OrderViewModel
import kotlinx.android.synthetic.main.fragment_cancel_order_dialog.*

class CancelOrderDialogFragment(
    var orderId:String
) : BottomSheetDialogFragment() {
    lateinit var orderVM:OrderViewModel
    private var selectedReasonType: CancelReasonType? = null

    private lateinit var loadingDialog: LoadingDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cancel_order_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        orderVM = ViewModelProvider(this).get(OrderViewModel::class.java)
        setupData()
        setupViews()
        observeViewModel()
    }

    private fun setupViews(){
        loadingDialog = LoadingDialog(childFragmentManager)
        btn_cancel_done.isEnabled = false
        btn_cancel_done.apply {
            setOnClickListener {
                loadingDialog.startLoading()
                selectedReasonType?.let {
                    val reasonType = it.reason
                    val comment = edt_cancel_comment.text!!.toString()

                    orderVM.cancelDeliveryOrder(
                        orderId = orderId,
                        reasonType = reasonType,
                        comment = comment
                    )
                } ?: run {
                    loadingDialog.stopLoading()
                    Toast.makeText(context, "Reason Type Field to Load", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupData(){
        orderVM.getCancelReasonType()
    }

    private fun observeViewModel(){
        orderVM.cancelReasonTypes.observe(viewLifecycleOwner, { data ->
            setupReasonTypeDropdown(data)

            if(data.isNotEmpty()){
                selectedReasonType = data[0]
                btn_cancel_done.isEnabled = true
                btn_cancel_done.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.primary, null))
            }
        })

        orderVM.success.observe(viewLifecycleOwner, {
            loadingDialog.stopLoading()
            toast("Cancel order successful")
            dismiss()
        })

        orderVM.errMsg.observe(viewLifecycleOwner, {
            loadingDialog.stopLoading()
            toast(it)
        })
    }

    private fun setupReasonTypeDropdown(cancelReasonTypes: List<CancelReasonType>){
        val cancelReasonTypeAdapter = object : ArrayAdapter<CancelReasonType>(requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            cancelReasonTypes){
            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                view.text = cancelReasonTypes[position].reason

                return view
            }
        }

        sp_cancel_reason_type.apply {
            this.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    selectedReasonType = cancelReasonTypes[position]
                }
            }
        }

        cancelReasonTypeAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        sp_cancel_reason_type.adapter = cancelReasonTypeAdapter
        sp_cancel_reason_type.setSelection(0)
        cancelReasonTypeAdapter.notifyDataSetChanged()
    }


}