package com.omkarsoft.arriveontimedelivery.view.main

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.omkarsoft.arriveontimedelivery.R
import com.omkarsoft.arriveontimedelivery.databinding.FragmentMainBinding
import com.omkarsoft.arriveontimedelivery.extension.gone
import com.omkarsoft.arriveontimedelivery.extension.toast
import com.omkarsoft.arriveontimedelivery.extension.visible
import com.omkarsoft.arriveontimedelivery.helper.SharedPreferencesHelper
import com.omkarsoft.arriveontimedelivery.view.deliver.DeliverFragment
import com.omkarsoft.arriveontimedelivery.view.dispatch.DispatchFragment
import com.omkarsoft.arriveontimedelivery.view.more.MoreFragment
import com.omkarsoft.arriveontimedelivery.view.nextday.NextDayFragment
import com.omkarsoft.arriveontimedelivery.view.present.PresentFragment
import com.omkarsoft.arriveontimedelivery.viewInterface.AutoMoveFragmentInterface
import com.omkarsoft.arriveontimedelivery.viewInterface.QROrderDetailInterface
import com.omkarsoft.arriveontimedelivery.viewModel.OrderViewModel
import kotlinx.android.synthetic.main.fragment_main.*
import android.content.Intent

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.findNavController
import com.omkarsoft.arriveontimedelivery.constant.*
import com.omkarsoft.arriveontimedelivery.data.model.Order
import com.omkarsoft.arriveontimedelivery.view.auth.AuthActivity
import com.omkarsoft.arriveontimedelivery.view.detail.OrderDetailFragmentDirections
import com.omkarsoft.arriveontimedelivery.view.partialPickup.PartialPickupFragment
import com.omkarsoft.arriveontimedelivery.view.template.LoadingDialog
import com.omkarsoft.arriveontimedelivery.viewInterface.DispatchInterface
import com.omkarsoft.arriveontimedelivery.viewInterface.MainInterface
import kotlinx.android.synthetic.main.toolbar_main.*
import java.util.*

class MainFragment : Fragment(), QROrderDetailInterface, AutoMoveFragmentInterface,
    DispatchInterface, MainInterface {
    private var currentFragment: Fragment? = null

    private lateinit var loadingDialog: LoadingDialog
    private lateinit var prefHelper: SharedPreferencesHelper
    private lateinit var orderVM: OrderViewModel
    private var isShown = true // show error message
    private var currentOrderType = OrderType.DELIVER
    private var orderTypes = arrayOf(OrderType.DELIVER, OrderType.PRESENT, OrderType.DISPATCH, OrderType.OPEN, OrderType.PARTIAL_PICKUP)
    private var scannedOrder: Order? = null

    private val mBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            if(bottom_navigation_view != null){
                val orderType = intent.getSerializableExtra("orderType") as OrderType
                setupBadge(orderType)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentMainBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingDialog = LoadingDialog(childFragmentManager)
        prefHelper = SharedPreferencesHelper(requireContext())

        if(Global.isNeedMoveToPresent) {
            prefHelper.setLastFragment(Destination.Main.PRESENT)
            Global.isNeedMoveToPresent = false
        }

        /// Register Notification Center
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(mBroadcastReceiver,
            IntentFilter(EventName.REFRESH_BADGE)
        );

        orderVM = ViewModelProvider(this).get(OrderViewModel::class.java)

        if(prefHelper.getFCMToken().isEmpty()){
            SharedPreferencesHelper().removeCurrentUser()
            startActivity(Intent(requireContext(), AuthActivity::class.java))
            activity?.finish()

            return
        }

        updateFragment()
        setupBottomNavigationBar()
        loadFragment()

        btn_scan_qr.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                    QRScannerFragment(childFragmentManager, this).startLoading()
                }
                shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA) -> {
                    toast("Permission Denied to use your camera")
                }
                else -> {
                    requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 1001)
                }
            }
        }

        observeViewModel()

        btn_confirm.setOnClickListener {
            when (currentFragment){
                is PresentFragment -> {
                    (currentFragment as PresentFragment).confirmPressed()
                }
                is DispatchFragment -> {
                    (currentFragment as DispatchFragment).confirmPressed()
                }
                is NextDayFragment -> {
                    (currentFragment as NextDayFragment).confirmPressed()
                }
            }
        }
        setupData()

        resetBadge()
        bottom_navigation_view.getOrCreateBadge(bottom_navigation_view.selectedItemId).apply {
            backgroundColor = ContextCompat.getColor(requireContext(), R.color.primary)
        }

        for (orderType in orderTypes){
            setupBadge(orderType)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        QRScannerFragment(childFragmentManager, this).startLoading()
    }

    private fun updateFragment(){
        currentFragment = when (prefHelper.getLastFragment()){
            Destination.Main.DELIVERY    -> {
                bottom_navigation_view.selectedItemId = R.id.bnv_deliver
                DeliverFragment()
            }
            Destination.Main.PRESENT     -> {
                bottom_navigation_view.selectedItemId = R.id.bnv_present
                PresentFragment()
            }
            Destination.Main.DISPATCH    -> {
                bottom_navigation_view.selectedItemId = R.id.bnv_dispatch
                DispatchFragment(this)
            }
            Destination.Main.NEXT        -> {
                bottom_navigation_view.selectedItemId = R.id.bnv_open
                NextDayFragment(this)
            }
            Destination.Main.MORE        -> {
                bottom_navigation_view.selectedItemId = R.id.bnv_partial_pickup
                PartialPickupFragment(this)
            }
            else -> {
                bottom_navigation_view.selectedItemId = R.id.bnv_deliver
                DeliverFragment()
            }
        }
    }

    private fun setupBottomNavigationBar(){
        bottom_navigation_view.setOnItemSelectedListener {
            currentFragment = when (it.itemId){
                R.id.bnv_deliver -> {
                    prefHelper.setLastFragment(Destination.Main.DELIVERY)
                    DeliverFragment()
                }
                R.id.bnv_present -> {
                    prefHelper.setLastFragment(Destination.Main.PRESENT)
                    PresentFragment()
                }
                R.id.bnv_dispatch -> {
                    prefHelper.setLastFragment(Destination.Main.DISPATCH)
                    DispatchFragment(this)
                }
                R.id.bnv_open -> {
                    prefHelper.setLastFragment(Destination.Main.NEXT)
                    NextDayFragment(this)
                }
                R.id.bnv_partial_pickup -> {
                    prefHelper.setLastFragment(Destination.Main.MORE)
                    PartialPickupFragment(this)
                }
                else -> DeliverFragment()
            }
            loadFragment()

            btn_confirm.gone()
            btn_scan_qr.visible()

            resetBadge()

            bottom_navigation_view.getOrCreateBadge(it.itemId).apply {
                backgroundColor = ContextCompat.getColor(requireContext(), R.color.primary)
            }

            true
        }
    }

    private fun loadFragment(){
        btn_confirm_all.visible()

        if(currentFragment!! !is DispatchFragment){
            btn_confirm_all.gone()

            /// Show QR Code Scanner Button
            cl_scan_qr.visible()
        }
        childFragmentManager.beginTransaction().replace(R.id.fl_main, currentFragment!!).commit()

    }

    override fun onQRScanned(jsonString: String) {
        loadingDialog.startLoading()
        orderVM.getOrderDetail(jsonString, false)
    }

    private fun setupData(){
        orderVM.getOrders(currentOrderType, true)
    }

    private fun observeViewModel(){
        orderVM.orders.observe(viewLifecycleOwner, { orders ->
            setupBadge(currentOrderType)

            when(currentOrderType) {
                OrderType.DELIVER -> {
                    Global.deliverOrders = orders
                    setupBadge(currentOrderType)

                    currentOrderType = OrderType.PRESENT
                    setupData()
                }
                OrderType.PRESENT -> {
                    Global.presentOrders = orders
                    setupBadge(currentOrderType)

                    currentOrderType = OrderType.DISPATCH
                    setupData()
                }
                OrderType.DISPATCH -> {
                    Global.dispatchOrders = orders
                    setupBadge(currentOrderType)

                    currentOrderType = OrderType.OPEN
                    setupData()
                }
                OrderType.OPEN -> {
                    Global.openOrders = orders.filter { it.orderColor != "" && it.status != "" }
                    setupBadge(currentOrderType)

                    currentOrderType = OrderType.PARTIAL_PICKUP
                    setupBadge(currentOrderType)
                    currentOrderType = OrderType.DELIVER
                }
            }
        })

        orderVM.order.observe(viewLifecycleOwner, { order ->
            scannedOrder = order
            loadingDialog.stopLoading()

            if(order.status == Status.DISPATCH || order.status == Status.DISPATCHED){
                orderVM.confirmDispatchOrder(order.id)
                return@observe
            }

            if(order.partialPiece.toInt() < order.piece.toInt()){
                val partialPiece = if(order.piece.toInt() > 1) order.partialPiece.toInt() + 1 else order.piece.toInt()
                orderVM.updateOpenOrderToPickedUp(order.id, partialPiece, true)
                return@observe
            }

            if(order.isPartialDeliver()){
                val action = MainFragmentDirections.actionMainFragmentToCompleteOrderFragment(order)
                findNavController().navigate(action)
                return@observe
            }

            val action = MainFragmentDirections.actionMainFragmentToOrderDetailFragment("", order.id)
            findNavController().navigate(action)
        })

        orderVM.success.observe(viewLifecycleOwner, {
            loadingDialog.stopLoading()
            val tempOrder = scannedOrder!!
            scannedOrder = null

            when(tempOrder.status){
                Status.DISPATCH, Status.DISPATCHED -> {
                    Toast.makeText(requireContext(), "Order Confirmed", Toast.LENGTH_SHORT).show()
                    moveToPresent()
                    return@observe
                }

                Status.OPEN_ORDER, Status.PICKED_UP -> {
                    Toast.makeText(requireContext(), "Order Picked Up", Toast.LENGTH_SHORT).show()
                    tempOrder.partialPiece = (tempOrder.partialPiece.toInt() + 1).toString()
                }
            }

            currentOrderType = OrderType.DELIVER
            setupData()

            /// Move to Collection Tab, When Scanned Order is Partial Piece
            if(tempOrder.piece.toInt() > 1 && (tempOrder.partialPiece.toInt() < tempOrder.piece.toInt())){
                moveToCollection()
                return@observe
            }

            /// Move to Next, When All Partial Piece Picked Up
            if((tempOrder.piece.toInt() == tempOrder.partialPiece.toInt())){
                moveToPresent()
                return@observe
            }

            if(currentFragment is PresentFragment){
                (currentFragment as PresentFragment).setupData()
                return@observe
            }
        })

        orderVM.errMsg.observe(viewLifecycleOwner, {
            loadingDialog.stopLoading()
            if (!isShown){
                toast(it)
            }
        })
    }

     fun setupBadge(orderType: OrderType){
        var totalData = 0
        var menuItem = R.id.bnv_deliver

        when(orderType) {
            OrderType.DELIVER -> {
                Global.deliverOrders?.let {
                    totalData = it.size
                }
                menuItem = R.id.bnv_deliver
            }
            OrderType.PRESENT -> {
                Global.presentOrders?.let {
                    totalData = it.size
                }
                menuItem = R.id.bnv_present
            }
            OrderType.DISPATCH -> {
                Global.dispatchOrders?.let {
                    totalData = it.size
                }
                menuItem = R.id.bnv_dispatch
            }
            OrderType.OPEN -> {
                Global.openOrders?.let {
                    totalData = it.size
                }
                menuItem = R.id.bnv_open
            }
            OrderType.PARTIAL_PICKUP -> {
                Global.presentOrders?.let { orders ->
                    totalData = orders.filter {
                        it.partialPiece.toInt() > 0 && (it.partialPiece.toInt() < it.piece.toInt())
                        || it.partialDeliver.toInt() > 0 && (it.partialDeliver.toInt() < it.piece.toInt())
                    }.size
                }
                menuItem = R.id.bnv_partial_pickup
            }
        }

         bottom_navigation_view.getOrCreateBadge(menuItem).apply {
             number = totalData
             maxCharacterCount = 3
         }
    }

    private fun resetBadge(){
        val itemIds = arrayListOf(R.id.bnv_deliver, R.id.bnv_present, R.id.bnv_dispatch, R.id.bnv_open, R.id.bnv_partial_pickup)
        for (itemId in itemIds){
            bottom_navigation_view.getOrCreateBadge(itemId).apply {
                backgroundColor = ContextCompat.getColor(requireContext(), R.color.muted)
            }
        }
    }

    private fun moveToCollection(){
        currentFragment = PartialPickupFragment(this)
        prefHelper.setLastFragment(Destination.Main.PARTIAL_PICKUP)
        loadFragment()

        /// Move tab bar to present
        bottom_navigation_view.selectedItemId = R.id.bnv_partial_pickup
    }

    private fun moveToNext(){
        currentFragment = NextDayFragment(this)
        prefHelper.setLastFragment(Destination.Main.PRESENT)
        loadFragment()

        /// Move tab bar to present
        bottom_navigation_view.selectedItemId = R.id.bnv_open
    }

    override fun moveToPresent(){
        currentFragment = PresentFragment()
        prefHelper.setLastFragment(Destination.Main.PRESENT)
        prefHelper.saveUpdateTime(OrderType.PRESENT, 0L)
        loadFragment()

        /// Move tab bar to present
        bottom_navigation_view.selectedItemId = R.id.bnv_present
    }

    override fun didOrderAccepted() {
        prefHelper.setLastFragment(Destination.Main.PRESENT)
        updateFragment()
    }

    override fun toggleFloatingBtn(isShowQR: Boolean) {
        if (isShowQR){
            requireActivity().btn_confirm.gone()
            requireActivity().btn_scan_qr.visible()
        } else {
            requireActivity().btn_scan_qr.gone()
            requireActivity().btn_confirm.text = "Pickup"
            requireActivity().btn_confirm.visible()
        }
    }
}