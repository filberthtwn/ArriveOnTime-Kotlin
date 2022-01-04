package com.omkarsoft.arriveontimedelivery.view.detail

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.amplifyframework.core.Amplify
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.omkarsoft.arriveontimedelivery.R
import com.omkarsoft.arriveontimedelivery.data.model.Order
import com.omkarsoft.arriveontimedelivery.databinding.FragmentRoundTripDialogBinding
import com.omkarsoft.arriveontimedelivery.extension.*
import com.omkarsoft.arriveontimedelivery.view.template.LoadingDialog
import com.omkarsoft.arriveontimedelivery.viewModel.OrderViewModel
import kotlinx.android.synthetic.main.fragment_round_trip_dialog.*
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File


class RoundTripDialogFragment(
    private val order: Order,
    private val orderVM: OrderViewModel
) : BottomSheetDialogFragment() {
    private val transportationList = arrayListOf("Select Transportation", "Car", "Truck")

    private lateinit var loadingDialog: LoadingDialog

    private var isDoneLoading = true

    private var fileName: String = ""
    set(value) {
        if (value.isEmpty()){
            ll_round_trip_has_file.gone()
            btn_round_trip_upload_image.visible()
        } else {
            ll_round_trip_has_file.visible()
            btn_round_trip_upload_image.gone()
        }
        field = value
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentRoundTripDialogBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingDialog = LoadingDialog(childFragmentManager)

        setupViews()
        setupData()
        observeViewModel()
    }

    private fun setupViews(){
        btn_round_trip_finished.setOnClickListener {
            if (!validate()){
                toast("Please fill in the blanks")
            } else {
                loadingDialog.startLoading()
                isDoneLoading = false
                orderVM.updateDeliveredToRoundTrip(
                    orderId = order.id,
                    waitTime = edt_round_trip_wait_time.text.toString(),
                    transportation = spinner_transportation.selectedItem.toString(),
                    boxes = edt_round_trip_number_boxes.text.toString(),
                    reasonType = ""
                )
            }
        }

        dialog_round_trip.setOnClickListener {
            hideKeyboard()
        }

        btn_round_trip_upload_image.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED &&
                ContextCompat.checkSelfPermission(
                    requireContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {

                val permission = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA)
                ActivityCompat.requestPermissions(requireActivity(), permission, 1001)
            } else {
                showChooseImageDialog()
            }
        }

        img_round_trip_clear_file.setOnClickListener {
            ll_round_trip_has_file.gone()
            btn_round_trip_upload_image.visible()
        }
    }

    private fun setupData(){
        tv_round_trip_order_id.text = order.id

        val transportAdapter = object : ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_dropdown_item, transportationList){
            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                if (position == 0){
                    view.setTextColor(Color.GRAY)
                } else {
                    view.setTextColor(Color.BLACK)
                }
                return view
            }
        }
        transportAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        spinner_transportation.adapter = transportAdapter
        spinner_transportation.setSelection(0)
        transportAdapter.notifyDataSetChanged()
    }

    private fun observeViewModel(){
        orderVM.success.observe(viewLifecycleOwner, {
            loadingDialog.stopLoading()
            if (it){
                toast("Order changed to Round Trip")
                dismiss()
                findNavController().navigate(R.id.action_orderDetailFragment_to_mainFragment)
            }
        })

        orderVM.errMsg.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()){
                toast(it)
            }
        })
    }

    private fun validate(): Boolean {
        if (edt_round_trip_wait_time.text.isEmpty()) return false
        if (edt_round_trip_number_boxes.text.isEmpty()) return false
        if (spinner_transportation.selectedItem.toString() == transportationList[0]) return false
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        EasyImage.handleActivityResult(requestCode, resultCode, data, requireActivity(), object : EasyImage.Callbacks{
            @SuppressLint("SetTextI18n")
            override fun onImagesPicked(
                p0: MutableList<File>,
                p1: EasyImage.ImageSource?,
                p2: Int
            ) {
                loadingDialog.startLoading()

                Amplify.Storage.uploadFile("${getBaseImageFolder()}/${order.id}.png", p0[0],
                    {
                        ll_round_trip_has_file.visible()
                        btn_round_trip_upload_image.gone()
                        tv_round_trip_filename.text = "${order.id}.png"
                        fileName = "${getBaseImageFolder()}/${order.id}.png"
                        loadingDialog.stopLoading()
                    }, {
                        loadingDialog.stopLoading()
                        toast("File failed to upload")
                    }
                )
            }

            override fun onImagePickerError(p0: Exception?, p1: EasyImage.ImageSource?, p2: Int) {
                Log.d("ImagePicker", "ERROR")
            }

            override fun onCanceled(p0: EasyImage.ImageSource?, p1: Int) {
                Log.d("ImagePicker", "CANCELED")
            }
        })
    }
}