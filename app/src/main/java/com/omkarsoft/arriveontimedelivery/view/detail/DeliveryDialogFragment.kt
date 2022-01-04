package com.omkarsoft.arriveontimedelivery.view.detail

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.omkarsoft.arriveontimedelivery.R
import com.omkarsoft.arriveontimedelivery.constant.Global
import com.omkarsoft.arriveontimedelivery.constant.Status
import com.omkarsoft.arriveontimedelivery.data.model.CancelReasonType
import com.omkarsoft.arriveontimedelivery.data.model.Order
import com.omkarsoft.arriveontimedelivery.databinding.FragmentDeliveryDialogBinding
import com.omkarsoft.arriveontimedelivery.extension.*
import com.omkarsoft.arriveontimedelivery.view.complete.CompleteOrderSignatureFragment
import com.omkarsoft.arriveontimedelivery.view.deliver.DeliverFragment
import com.omkarsoft.arriveontimedelivery.view.template.LoadingDialog
import com.omkarsoft.arriveontimedelivery.viewModel.OrderViewModel
import kotlinx.android.synthetic.main.fragment_cancel_order_dialog.*
import kotlinx.android.synthetic.main.fragment_delivery_dialog.*
import com.google.android.material.bottomsheet.BottomSheetBehavior

class DeliveryDialogFragment(
    private val order: Order,
    private val orderVM: OrderViewModel,
    private val relationship: String,
    private val isUserLivedHere: String,
    private val lastName: String,
    private val fragment: Fragment
) : BottomSheetDialogFragment() {
    private val isRoundTripList = arrayListOf( "No", "Yes")
    private val transportationList = arrayListOf("Car", "Truck")
    private var selectedReasonType: CancelReasonType? = null

    private lateinit var loadingDialog: LoadingDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentDeliveryDialogBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingDialog = LoadingDialog(childFragmentManager)

        setupViews()
        setupData()
        observeViewModel()
    }

    private fun setupViews(){
        val bottomSheet: View = dialog!!.findViewById(R.id.design_bottom_sheet)

        val behavior = BottomSheetBehavior.from(bottomSheet)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.peekHeight = 0

        behavior.addBottomSheetCallback(object: BottomSheetBehavior.BottomSheetCallback(){
            override fun onStateChanged(bottomSheet: View, newState: Int) {}

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if(slideOffset <= 0){
                    this@DeliveryDialogFragment.dismiss()
                }
            }
        })

        if(order.status == Status.DELIVERED && order.isRoundTrip()){
            ll_isRoundTrip.gone()
        }

        btn_delivery_finished.setOnClickListener {
            if (!validate()){
                toast("Please fill in the blanks")
            } else {
                val totalPiece = edt_delivery_number_boxes.text.toString().toInt()
                /// Handle Partial Deliver
                if(totalPiece != order.piece.toInt() &&
                    order.partialDeliver.toInt() < order.piece.toInt()){
                    if (totalPiece == 0 || totalPiece > order.piece.toInt()){
                        toast("Order Package Mismatch")
                        return@setOnClickListener
                    }
                    showConfirmationDialog()
                    return@setOnClickListener
                }

                if (totalPiece != order.piece.toInt()){
                    toast("Order Package Mismatch")
                    return@setOnClickListener
                }

                when(order.status){
                    Status.PICKED_UP -> {
                        val isRoundTrip = spinner_delivery_isRoundtTrip.selectedItem.toString().lowercase()
                        (fragment as CompleteOrderSignatureFragment).finishAction(
                            waitTime = edt_delivery_wait_time.text.toString(),
                            numOfBoxes = edt_delivery_number_boxes.text.toString(),
                            transportation = spinner_delivery_transportation.selectedItem.toString().lowercase(),
                            isRoundTrip = spinner_delivery_isRoundtTrip.selectedItem.toString().lowercase(),
                            reasonType = if(isRoundTrip == "yes") selectedReasonType!!.reason else "",
                            partialDeliver = order.piece
                        )
                    }

                    Status.DELIVERED -> {
                        if (order.isPartialDeliver()){
                            updateToDeliver()
                            return@setOnClickListener
                        }

                        if (order.isRoundTrip()){
                            (fragment as CompleteOrderSignatureFragment).finishAction(
                                waitTime = edt_delivery_wait_time.text.toString(),
                                numOfBoxes = edt_delivery_number_boxes.text.toString(),
                                transportation = spinner_delivery_transportation.selectedItem.toString().lowercase(),
                                isRoundTrip = spinner_delivery_isRoundtTrip.selectedItem.toString().lowercase(),
                                partialDeliver = order.piece
                            )
                            return@setOnClickListener
                        }

                        if(fragment is OrderDetailFragment){
                            fragment.roundTripAction(
                                waitTime = edt_delivery_wait_time.text.toString(),
                                transportation = spinner_delivery_transportation.selectedItem.toString().lowercase(),
                                reasonType = selectedReasonType!!.reason
                            )
                        }

                        if(fragment is DeliverFragment){
                            if(edt_delivery_number_boxes.text.toString().toInt() > order.piece.toInt()){
                                toast("Order Package Mismatch")
                                return@setOnClickListener
                            }

                            fragment.roundTripAction(
                                order = order,
                                waitTime = edt_delivery_wait_time.text.toString(),
                                transportation = spinner_delivery_transportation.selectedItem.toString().lowercase(),
                                reasonType = selectedReasonType!!.reason
                            )
                        }
                    }
                }
            }
        }

        dialog_delivery.setOnClickListener {
            hideKeyboard()
        }

        /// Show/ Hide isRoundTrip Section
        if(order.status == Status.DELIVERED
            && !order.isRoundTrip()
            && !order.isPartialDeliver()){
            ll_isRoundTrip.gone()
        }
    }

    private fun setupData(){
        tv_delivery_order_id.text = order.id

        orderVM.getCancelReasonType()

        setupTransportationSpinner()
        setupIsRoundTripSpinner()
    }

    /*
     * Author: Filbert Hartawan
     *
     * Setup Transportation Dropdown
     */
    private fun setupTransportationSpinner(){
        val transportAdapter = object : ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_dropdown_item, transportationList){
            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                return view
            }
        }

        transportAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        spinner_delivery_transportation.adapter = transportAdapter
        spinner_delivery_transportation.setSelection(0)
        transportAdapter.notifyDataSetChanged()
    }

    /*
     * Author: Filbert Hartawan
     *
     * Setup Is Round Trip Dropdown
     */
    private fun setupIsRoundTripSpinner(){
        val isRoundTripAdapter = object : ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            isRoundTripList){
            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                return view
            }
        }
        isRoundTripAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        spinner_delivery_isRoundtTrip.adapter = isRoundTripAdapter
        spinner_delivery_isRoundtTrip.setSelection(0)
        spinner_delivery_isRoundtTrip.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateReasonType()
            }
        }
        isRoundTripAdapter.notifyDataSetChanged()
    }

    private fun observeViewModel(){
        orderVM.cancelReasonTypes.observe(viewLifecycleOwner, { data ->
            setupReasonTypeDropdown(data)

            if(data.isNotEmpty()){
                selectedReasonType = data[0]
                btn_delivery_finished.isEnabled = true
                btn_delivery_finished.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.primary, null))
            }
        })

        orderVM.success.observe(viewLifecycleOwner, {
            loadingDialog.stopLoading()
            if (it){
                toast("Order change to delivery")
                dismiss()

                Global.isNeedLoading = true
                findNavController().navigate(R.id.action_orderDetailFragment_to_mainFragment)
            }
        })

        orderVM.errMsg.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()){
                toast(it)
            }
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

        sp_round_trip_reason_type.apply {
            this.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    selectedReasonType = cancelReasonTypes[position]
                }
            }
        }

        cancelReasonTypeAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        sp_round_trip_reason_type.adapter = cancelReasonTypeAdapter
        sp_round_trip_reason_type.setSelection(0)
        cancelReasonTypeAdapter.notifyDataSetChanged()
    }

    private fun validate(): Boolean {
        if (edt_delivery_wait_time.text.toString().isEmpty()) return false
        if (edt_delivery_number_boxes.text.toString().isEmpty()) return false
        return true
    }

    private fun updateReasonType(){
        val isRoundTrip = spinner_delivery_isRoundtTrip.selectedItem.toString().lowercase()
        ll_reason_type.visibility = if(isRoundTrip == "no") View.GONE else View.VISIBLE
    }

    private fun showConfirmationDialog(){
        val dialog = AlertDialog.Builder(requireContext())
        dialog.setTitle("Confirmation")
        dialog.setMessage("You are delivering ${edt_delivery_number_boxes.text} out of ${order.piece} packages. Are you sure?")
        dialog.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        dialog.setPositiveButton("Yes"){ _, _ ->
            updateToDeliver()
        }
        dialog.setOnDismissListener {
            it.dismiss()
        }
        dialog.show()
    }

    private fun updateToDeliver(){
        val isRoundTrip = spinner_delivery_isRoundtTrip.selectedItem.toString().lowercase()

        (fragment as CompleteOrderSignatureFragment).finishAction(
            waitTime = edt_delivery_wait_time.text.toString(),
            numOfBoxes = order.piece,
            transportation = spinner_delivery_transportation.selectedItem.toString().lowercase(),
            isRoundTrip = spinner_delivery_isRoundtTrip.selectedItem.toString().lowercase(),
            reasonType = if(isRoundTrip == "yes") selectedReasonType!!.reason else "",
            partialDeliver = edt_delivery_number_boxes.text.toString()
        )
    }
}