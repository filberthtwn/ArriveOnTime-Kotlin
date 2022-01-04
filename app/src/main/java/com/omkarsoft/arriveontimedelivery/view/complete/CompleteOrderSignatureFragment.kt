package com.omkarsoft.arriveontimedelivery.view.complete

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.amplifyframework.core.Amplify
import com.omkarsoft.arriveontimedelivery.constant.Status
import com.omkarsoft.arriveontimedelivery.data.model.Order
import com.omkarsoft.arriveontimedelivery.databinding.FragmentCompleteOrderSignatureBinding
import com.omkarsoft.arriveontimedelivery.extension.getBaseSignFolder
import com.omkarsoft.arriveontimedelivery.extension.toast
import com.omkarsoft.arriveontimedelivery.view.detail.DeliveryDialogFragment
import com.omkarsoft.arriveontimedelivery.view.template.LoadingDialog
import com.omkarsoft.arriveontimedelivery.viewModel.OrderViewModel
import kotlinx.android.synthetic.main.fragment_complete_order_signature.*
import kotlinx.android.synthetic.main.toolbar_nav.*
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.math.log

class CompleteOrderSignatureFragment : Fragment() {
    private lateinit var order: Order

    private var lastName = ""
    private var recipient = ""
    private var relationship = ""

    private lateinit var orderVM: OrderViewModel
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentCompleteOrderSignatureBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        orderVM = ViewModelProvider(this).get(OrderViewModel::class.java)

        setupPreliminaryData()
        setupViews()
        observeViewModel()
    }

    private fun setupPreliminaryData(){
        arguments?.let {
            order = CompleteOrderSignatureFragmentArgs.fromBundle(it).order
            lastName = CompleteOrderSignatureFragmentArgs.fromBundle(it).lastName
            recipient = CompleteOrderSignatureFragmentArgs.fromBundle(it).userLivesHere
            relationship = CompleteOrderSignatureFragmentArgs.fromBundle(it).relationship
        }
    }

    private fun setupViews(){
        tv_toolbar_title.text = "Complete Order"
        toolbar_main.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        btn_complete_order_signature_rewrite.setOnClickListener {
            signature_complete_order.clear()
        }

        btn_complete_order_finish.setOnClickListener {
            val dialog = AlertDialog.Builder(requireContext())
            dialog.setTitle("Confirmation")
            dialog.setMessage("Are you sure to confirm the order?")
            dialog.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            dialog.setPositiveButton("Yes"){ _, _ ->
                submitData()
            }
            dialog.setOnDismissListener {
                it.dismiss()
            }
            dialog.show()
        }
    }

    private fun submitData(){
        if (checkPermission()){
            DeliveryDialogFragment(
                order = order,
                orderVM = orderVM,
                relationship = relationship,
                isUserLivedHere = recipient,
                lastName = lastName,
                fragment = this,
            ).show(
                childFragmentManager,
                DeliveryDialogFragment::class.qualifiedName
            )
        } else {
            requestPermission()
        }
    }

    private fun observeViewModel(){
        orderVM.errMsg.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()){
                toast(it)
            }
        })

        orderVM.success.observe(viewLifecycleOwner, {
            loadingDialog.stopLoading()
            if (it){
                toast("Order status changed to completed")

                val action = CompleteOrderSignatureFragmentDirections.actionCompleteOrderSignatureFragmentToOrderDetailFragment("", order.id)
                findNavController().navigate(action)
            }
        })
    }

    private fun createSignature(
        notes:String,
        waitTime: String,
        numOfBoxes: String,
        transportation: String,
        isRoundTrip: String,
        reasonType: String,
        partialDeliver: String
    ){
        loadingDialog = LoadingDialog(childFragmentManager)
        loadingDialog.startLoading()

        try {
            val signature = signature_complete_order.signatureBitmap
            val filePath = Environment.getExternalStorageDirectory().absolutePath + "/PhysicsSketchpad"

            val dir = File(filePath)
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "sign-${order.id}.jpg")
            val fOut = FileOutputStream(file)

            signature.compress(Bitmap.CompressFormat.JPEG, 85, fOut)
            fOut.flush()
            fOut.close()

            Amplify.Storage.uploadFile("${getBaseSignFolder()}/${order.id}.jpg", file,

                {
                    Log.i("Amplify", "Successfully uploaded: ${it.key}")

                    when(order.status){
                        Status.PICKED_UP -> {
                            orderVM.updateOrderDelivery(
                                lastName = lastName,
                                notes = notes,
                                orderId = order.id,
                                transportation = transportation,
                                waitTime = waitTime,
                                boxes = numOfBoxes,
                                fileName = "${getBaseSignFolder()}/${order.id}.jpg",
                                isRoundTrip = isRoundTrip,
                                reasonType = reasonType,
                                partialDeliver = partialDeliver
                            )
                        }
                        Status.DELIVERED -> {
                            if(order.isRoundTrip()){
                                orderVM.updateOrderToComplete(
                                    orderId = order.id,
                                    notes = createSummaryNotes(),
                                    lastName = lastName,
                                    fileName = "${getBaseSignFolder()}/${order.id}.jpg"
                                )
                            }

                            if(order.isPartialDeliver()){
                                orderVM.updateOrderDelivery(
                                    lastName = lastName,
                                    notes = notes,
                                    orderId = order.id,
                                    transportation = transportation,
                                    waitTime = waitTime,
                                    boxes = numOfBoxes,
                                    fileName = "${getBaseSignFolder()}/${order.id}.jpg",
                                    isRoundTrip = isRoundTrip,
                                    reasonType = reasonType,
                                    partialDeliver = partialDeliver
                                )
                            }
                        }
                    }
                },
                {
                    Log.e("Amplify", "Upload failed", it)
                }
            )
        } catch (e: Exception){
            loadingDialog.stopLoading()
            println(e)
            toast("Upload Signature Failed")
            e.printStackTrace()
        }
    }

    private fun createSummaryNotes(): String {
        return "Does ${order.recipient?.name} live here: ${recipient.uppercase(Locale.getDefault())} " +
                "What is your relationship to the patient: ${relationship.uppercase(Locale.getDefault())}"
    }

    private fun checkPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            Environment.isExternalStorageManager()
        } else {
            val readAccess = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE)
            val writeAccess = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            readAccess == PackageManager.PERMISSION_GRANTED && writeAccess == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = Uri.parse(String.format("package:%s", requireContext().packageName))
                startActivityForResult(intent, 2296)
            } catch (e: Exception){
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                startActivityForResult(intent, 2296)
            }
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ), 2297)
            submitData()
        }
    }

    /*
     * Author: Filbert Hartawan
     *
     * Finish Callback from Modal
     */
    fun finishAction(
        waitTime: String,
        numOfBoxes: String,
        transportation: String,
        isRoundTrip: String,
        reasonType: String = "",
        partialDeliver: String,
    ){
        createSignature(
            notes = createSummaryNotes(),
            waitTime = waitTime,
            numOfBoxes = numOfBoxes,
            transportation = transportation,
            isRoundTrip = isRoundTrip,
            reasonType = reasonType,
            partialDeliver = partialDeliver,
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode){
            2296 -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                    if (Environment.isExternalStorageManager()){
//                        createSignature()
                    } else {
                        toast("Permission needed for storage access")
                    }
                }
            }
            2297 -> {
                if (grantResults.isNotEmpty()){
                    val readAccess = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    val writeAccess = grantResults[0] == PackageManager.PERMISSION_GRANTED

                    if (readAccess && writeAccess){
//                        createSignature()
                    } else {
                        toast("Permission needed for storage access")
                    }
                }
            }
        }
    }
}