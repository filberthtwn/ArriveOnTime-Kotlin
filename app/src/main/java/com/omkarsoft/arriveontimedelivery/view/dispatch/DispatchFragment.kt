package com.omkarsoft.arriveontimedelivery.view.dispatch

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.omkarsoft.arriveontimedelivery.R
import com.omkarsoft.arriveontimedelivery.adapter.OrderAdapter
import com.omkarsoft.arriveontimedelivery.adapter.ShimmerAdapter
import com.omkarsoft.arriveontimedelivery.constant.*
import com.omkarsoft.arriveontimedelivery.data.model.Order
import com.omkarsoft.arriveontimedelivery.databinding.FragmentDispatchBinding
import com.omkarsoft.arriveontimedelivery.extension.gone
import com.omkarsoft.arriveontimedelivery.extension.toast
import com.omkarsoft.arriveontimedelivery.extension.visible
import com.omkarsoft.arriveontimedelivery.view.template.LoadingDialog
import com.omkarsoft.arriveontimedelivery.viewInterface.AutoMoveFragmentInterface
import com.omkarsoft.arriveontimedelivery.viewInterface.OrderConfirmInterface
import com.omkarsoft.arriveontimedelivery.viewModel.OrderViewModel
import kotlinx.android.synthetic.main.fragment_dispatch.*
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.fragment_present.*
import kotlinx.android.synthetic.main.toolbar_main.*

class DispatchFragment(
    private val moveListener: AutoMoveFragmentInterface
) : Fragment(), OrderConfirmInterface {
    private lateinit var orderVM: OrderViewModel
    private var selectedOrders: List<Order> = arrayListOf()

    private val shimmerAdapter = ShimmerAdapter(this, R.layout.item_shimmer_order, 10)
    private val orderAdapter = OrderAdapter(
        arrayListOf(),
        this,
        Destination.Main.DISPATCH,
        this
    )
    private lateinit var loadingDialog: LoadingDialog
    private var isConfirmLoading = false
    private var totalData = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentDispatchBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        orderVM = ViewModelProvider(this).get(OrderViewModel::class.java)
        loadingDialog = LoadingDialog(childFragmentManager)

        setupViews()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()

        setupData()

        requireParentFragment().btn_confirm_all.setOnClickListener {
            if(selectedOrders.isEmpty()){
                Global.dispatchOrders?.let {
                    if(it.isEmpty()){
                        showErrorToast()
                        return@setOnClickListener
                    }
                    selectedOrders = it
                } ?: run {
                    showErrorToast()
                    return@setOnClickListener
                }
            }
            confirmPressed()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupViews(){
        tv_toolbar_title.text = "Dispatch (0)"

        tv_toolbar_select_all.setOnClickListener {
            if (totalData > 0){
                if (selectedOrders.size == totalData)
                    orderAdapter.unselectAllOrder()
                else
                    orderAdapter.selectAllOrder()
            }
        }

        rv_dispatch.apply {
            adapter = orderAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }

        rv_shimmer_dispatch.apply {
            adapter = shimmerAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            hasFixedSize()
        }

        swipe_dispatch.setOnRefreshListener {
            startLoading()
            orderVM.getOrders(OrderType.DISPATCH, true)
            swipe_dispatch.isRefreshing = false
        }

        /// Setup More Button
        iv_more.setOnClickListener{
            findNavController().navigate(R.id.action_mainFragment_to_moreFragment)
        }
    }

    fun setupData(){
        startLoading()
        orderVM.getOrders(OrderType.DISPATCH, true)
    }

    private fun observeViewModel(){
        orderVM.orders.observe(viewLifecycleOwner, { orders ->
            stopLoading()
            val sortOrders = orders.sortedByDescending { it.pickupDate }

            orderAdapter.updateData(sortOrders)
            totalData = sortOrders.size

            tv_toolbar_title.text = "Dispatch (${sortOrders.size})"

            Global.dispatchOrders = sortOrders

            /// Send Broadcast for refresh badge
            sendBroadcast()

            toggleEmptyState(sortOrders)

            if(isConfirmLoading){
                isConfirmLoading = false
                moveListener.moveToPresent()
            }
        })

        orderVM.success.observe(viewLifecycleOwner, {
            loadingDialog.stopLoading()
            setupData()
            moveListener.moveToPresent()
        })

        orderVM.errMsg.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()){
                toast(it)
            }
        })
    }

    private fun sendBroadcast(){
        val intent = Intent(EventName.REFRESH_BADGE)
        intent.putExtra("orderType", OrderType.DISPATCH)
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent);
    }

    fun confirmPressed(){
        val orderIds = selectedOrders.map { it.id }
        orderVM.confirmDispatchOrder(orderIds)
        loadingDialog.startLoading()
        isConfirmLoading = true
    }

    private fun toggleConfirmButton(){
        if (selectedOrders.isNotEmpty()){
            requireActivity().btn_scan_qr.gone()
            requireActivity().btn_confirm.text = "Accept"
            requireActivity().btn_confirm.visible()
        } else {
            requireActivity().btn_confirm.gone()
            requireActivity().btn_scan_qr.visible()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun toggleSelectAllButton(){
        if (selectedOrders.size == totalData){
            tv_toolbar_select_all.text = "Unselect All"
        } else {
            tv_toolbar_select_all.text = "Select All"
        }
    }

    private fun startLoading(){
        shimmer_dispatch.visible()
        shimmer_dispatch.startShimmer()
        rv_dispatch.gone()
        tv_dispatch_no_order.gone()
    }

    private fun stopLoading(){
        shimmer_dispatch.gone()
        shimmer_dispatch.stopShimmer()
        rv_dispatch.visible()
    }

    private fun toggleEmptyState(orders: List<Order>){
        if (orders.isEmpty()){
            tv_dispatch_no_order.visible()
        }
    }

    override fun onSelected(orderList: List<Order>) {
        selectedOrders = orderList
        toggleSelectAllButton()
//        toggleConfirmButton()
    }

    private fun showErrorToast(){
        Toast.makeText(requireContext(), "No Dispatch Available", Toast.LENGTH_SHORT).show()
    }
}