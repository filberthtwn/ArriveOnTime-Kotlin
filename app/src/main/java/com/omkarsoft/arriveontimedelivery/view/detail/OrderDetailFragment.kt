package com.omkarsoft.arriveontimedelivery.view.detail

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.amplifyframework.core.Amplify
import com.omkarsoft.arriveontimedelivery.R
import com.omkarsoft.arriveontimedelivery.constant.BASE_MAP_URL
import com.omkarsoft.arriveontimedelivery.constant.Destination
import com.omkarsoft.arriveontimedelivery.constant.Global
import com.omkarsoft.arriveontimedelivery.constant.Status
import com.omkarsoft.arriveontimedelivery.data.model.Order
import com.omkarsoft.arriveontimedelivery.databinding.FragmentOrderDetailBinding
import com.omkarsoft.arriveontimedelivery.extension.*
import com.omkarsoft.arriveontimedelivery.helper.SharedPreferencesHelper
import com.omkarsoft.arriveontimedelivery.view.template.LoadingDialog
import com.omkarsoft.arriveontimedelivery.viewModel.OrderViewModel
import kotlinx.android.synthetic.main.fragment_delivery_dialog.*
import kotlinx.android.synthetic.main.fragment_order_detail.*
import kotlinx.android.synthetic.main.toolbar_nav.*
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File

@SuppressLint("SetTextI18n")
class OrderDetailFragment : Fragment() {
    private val prefHelper = SharedPreferencesHelper()

    private var from = ""
    private var orderId = ""

    private var isStatusChanged = true

    private lateinit var orderVM: OrderViewModel
    private lateinit var loadingDialog: LoadingDialog

    private var uploadedImage = ""
    private var order = Order()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentOrderDetailBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        orderVM = ViewModelProvider(this).get(OrderViewModel::class.java)
        loadingDialog = LoadingDialog(childFragmentManager)

        setupPreliminaryData()
        setupViews()
        setupData()
        observeViewModel()
    }

    private fun setupPreliminaryData(){
        arguments?.let {
            orderId = OrderDetailFragmentArgs.fromBundle(it).orderId
            from = OrderDetailFragmentArgs.fromBundle(it).status
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupViews(){
        tv_toolbar_title.text = "Order Detail"

        toolbar_main.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_orderDetailFragment_to_mainFragment)
        }

        if(order.status == Status.CANCELLED){
            rl_order_detail_action.gone()
        }
    }

    private fun setupData(){
        orderVM.getOrderDetail(orderId)
    }

    private fun observeViewModel(){
        orderVM.order.observe(viewLifecycleOwner, { data ->
            if (!isStatusChanged){
                isStatusChanged = true
                findNavController().navigate(R.id.action_orderDetailFragment_to_mainFragment)
            } else {
                data?.let {
                    ll_order_detail_shimmer.gone()
                    sv_order_detail_content.visible()
                    rl_order_detail_action.visible()

                    order = data
                    reloadData()
                }
            }
        })

        orderVM.errMsg.observe(viewLifecycleOwner, {
            loadingDialog.stopLoading()
            toast(it)
        })

        orderVM.success.observe(viewLifecycleOwner, { success ->
            loadingDialog.stopLoading()
            if (success){
                if(order.status == Status.OPEN_ORDER || order.status == Status.DELIVERED){
                    Global.isNeedLoading = true
                }

                if(order.status == Status.DISPATCH || order.status == Status.DISPATCHED){
                    Global.isNeedMoveToPresent = true
                }

                findNavController().navigate(R.id.action_orderDetailFragment_to_mainFragment)
            }
        })
    }

    private fun uploadImage(){
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

    private fun reloadData(){
        order.apply {
            tv_detail_id.text = id
            tv_detail_status.text = status
            if(status == Status.DELIVERED && isRoundTrip()){
                tv_detail_status.text = Status.ROUND_TRIP
            }
            tv_detail_name.text = accountName
            tv_detail_notes.text = accountNotes

            tv_detail_sender_name.text = senderName
            tv_detail_sender_phone_number.text = if (senderCellPhone.isNotEmpty()) senderCellPhone else "-"
            tv_detail_sender_telephone.text = if (senderHomePhone.isNotEmpty()) senderHomePhone else "-"
            tv_detail_sender_location.text = "$senderAddress, $senderCity, $senderCountry, $senderPostalCode"
            tv_detail_sender_instruction.text = if (senderInstruction.isNotEmpty()) senderInstruction else "No Instruction"

            tv_detail_recipient_name.text = recipientName
            tv_detail_recipient_phone_number.text = if (recipientCellPhone.isNotEmpty()) recipientCellPhone else "-"
            tv_detail_recipient_telephone.text = if (recipientHomePhone.isNotEmpty()) recipientHomePhone else "-"
            tv_detail_recipient_location.text = "$recipientAddress, $recipientCity, $recipientCountry, $recipientPostalCode"
            tv_detail_recipient_instruction.text = if (recipientInstruction.isNotEmpty()) recipientInstruction else "No Instruction"

            tv_detail_service_type.text = order.serviceName
            tv_detail_pickup_ready.text = if (pickupDate.isNotEmpty()) pickupDate else "-"

            img_detail_shipper_release.setImageResource(if (isShipperRelease()) R.drawable.ic_check else R.drawable.ic_cross)
            img_detail_signature_required.setImageResource(if (isSignatureRequired()) R.drawable.ic_check else R.drawable.ic_cross)
            img_detail_round_trip.setImageResource(if (isRoundTrip()) R.drawable.ic_check else R.drawable.ic_cross)

            tv_detail_requestor.text = "Requested by $requestor"
            tv_detail_item_quantity.text = "$piece (${weight}g)"
            tv_detail_admin_notes.text = adminNotes

            cl_detail_maps.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                val link = "$BASE_MAP_URL/" +
                        "${prefHelper.getLatitude()},${prefHelper.getLongitude()}/" +
                        "$senderLatitude,$senderLongitude/$recipientLatitude,$recipientLongitude"
                Log.d("MAP_URL", link)

                intent.data = Uri.parse(link)
                startActivity(intent)
            }
        }

        when(order.status){
            Status.DISPATCH -> {
                dispatchToConfirmButton()
            }
            Status.PICKED_UP -> {
                pickupToDeliveryButton()
                if(order.partialPiece < order.piece) {
                    openToPickupButton()
                }
            }
            Status.OPEN_ORDER -> {
                openToPickupButton()
            }
            Status.ROUND_TRIP -> {
                roundTripToDelivery()
            }
            Status.DELIVERED -> {
                if (order.partialDeliver.toInt() > 0 &&
                    (order.partialDeliver.toInt() < order.piece.toInt())){
                    pickupToDeliveryButton()
                    return
                }

                if(order.signRoundtrip != ""){
                    rl_order_detail_action.gone()
                    return
                }

                if (order.hasRoundTrip()){
                    deliveryToCompleteButton()
                } else {
                    deliveryToRoundTripOrComplete()
                }
            }
        }
    }

    /** dispatch to confirm */
    private fun dispatchToConfirmButton(){
        showSingleButton()

        btn_order_detail_dispatch_confirm.apply {
            text = "Confirm"
            setOnClickListener {
                val dialog = PickupDialogFragment(order, childFragmentManager){
                    loadingDialog.startLoading()
                    orderVM.confirmDispatchOrder(orderId)
                    return@PickupDialogFragment
                }
                dialog.setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth)
                dialog.startLoading()
            }
        }
    }

    /** pickup to delivery */
    private fun pickupToDeliveryButton(){
        showDoubleButton()

        btn_order_detail_upload.apply {
            setOnClickListener { uploadImage() }
        }

        btn_order_detail_confirm.apply {
            text = "Deliver"
            setOnClickListener {
                val dialog = PickupDialogFragment(order, childFragmentManager){
                    val action = OrderDetailFragmentDirections.actionOrderDetailFragmentToCompleteOrderFragment(order)
                    findNavController().navigate(action)
                }
                dialog.setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth)
                dialog.startLoading()
            }
        }
    }

    /** delivery to round trip or complete */
    private fun deliveryToRoundTripOrComplete(){
        showDoubleButton()

        btn_order_detail_upload.apply {
            setOnClickListener { uploadImage() }
        }

        btn_order_detail_confirm.apply {
            text = "Round Trip"
            setOnClickListener {
                DeliveryDialogFragment(
                    order = order,
                    orderVM = orderVM,
                    relationship = "relationship",
                    isUserLivedHere = "recipient",
                    lastName = "lastName",
                    fragment = this@OrderDetailFragment,
                ).show(
                    childFragmentManager,
                    DeliveryDialogFragment::class.qualifiedName
                )
            }
        }
    }

    /** delivery to complete */
    private fun deliveryToCompleteButton(){
        showSingleButton()

        btn_order_detail_dispatch_confirm.apply {
            text = "Complete"

            setOnClickListener {
                val dialog = PickupDialogFragment(order, childFragmentManager){
                    val action = OrderDetailFragmentDirections.actionOrderDetailFragmentToCompleteOrderFragment(order)
                    findNavController().navigate(action)
                }
                dialog.setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth)
                dialog.startLoading()
            }
        }
    }

    /** open to pickup */
    private fun openToPickupButton(){
        showDoubleButton()

        btn_order_detail_upload.apply {
            setOnClickListener { uploadImage() }
        }

        btn_order_detail_confirm.apply {
            text = "Pick Up"
            setOnClickListener {
                /// UNUSED YET
                val dialog = PickupDialogFragment(order, childFragmentManager){ piece ->
                    orderVM.updateOpenOrderToPickedUp(orderId, (order.partialPiece.toInt() + piece), false)
                    loadingDialog.startLoading()
                }
                dialog.setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth)
                dialog.startLoading()
            }
        }
    }

    /** round trip to delivery */
    private fun roundTripToDelivery(){
        showDoubleButton()

        btn_order_detail_upload.apply {
            setOnClickListener { uploadImage() }
        }

        btn_order_detail_confirm.apply {
            text = "Delivered"
            setOnClickListener {
                val dialog = PickupDialogFragment(order, childFragmentManager){
                    orderVM.updateRoundTripToDelivery(
                        orderId = orderId,
                        lastName = "",
                        notes = order.accountNotes,
                        fileName = uploadedImage
                    )
                    loadingDialog.startLoading()
                }
                dialog.setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth)
                dialog.startLoading()
            }
        }
    }

    private fun showSingleButton(){
        btn_order_detail_dispatch_confirm.visible()
        ll_detail_order_action.gone()
    }

    private fun showDoubleButton(){
        btn_order_detail_dispatch_confirm.gone()
        ll_detail_order_action.visible()
    }

    fun roundTripAction(
        reasonType: String,
        waitTime: String,
        transportation: String
    ){
        loadingDialog.startLoading()

        orderVM.updateDeliveredToRoundTrip(
            orderId = orderId,
            waitTime = waitTime,
            transportation = transportation,
            boxes = order.piece,
            reasonType = reasonType
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        EasyImage.handleActivityResult(requestCode, resultCode, data, requireActivity(), object : EasyImage.Callbacks {
            override fun onImagesPicked(
                p0: MutableList<File>,
                p1: EasyImage.ImageSource?,
                p2: Int
            ) {
                loadingDialog.startLoading()

                Amplify.Storage.uploadFile("${getBaseImageFolder()}/$orderId.png", p0[0],
                    {
                        Log.i("Amplify", "Successfully uploaded: ${it.key}")
                        loadingDialog.stopLoading()
                        toast("File uploaded")
                    }, {
                        Log.e("Amplify", "Upload failed", it)
                        loadingDialog.stopLoading()
                        toast("File failed to upload")
                    }
                )
                uploadedImage = "${getBaseImageFolder()}/$orderId.png"
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