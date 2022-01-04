package com.omkarsoft.arriveontimedelivery.view.present

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.omkarsoft.arriveontimedelivery.databinding.FragmentPresentBinding
import com.omkarsoft.arriveontimedelivery.extension.gone
import com.omkarsoft.arriveontimedelivery.extension.toast
import com.omkarsoft.arriveontimedelivery.extension.visible
import com.omkarsoft.arriveontimedelivery.view.template.LoadingDialog
import com.omkarsoft.arriveontimedelivery.viewInterface.OrderConfirmInterface
import com.omkarsoft.arriveontimedelivery.viewModel.OrderViewModel
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.fragment_present.*
import kotlinx.android.synthetic.main.toolbar_main.*

class PresentFragment : Fragment(), OrderConfirmInterface {
    private lateinit var orderVM: OrderViewModel
    private var selectedOrders: List<Order> = arrayListOf()

    private val shimmerAdapter = ShimmerAdapter(this, R.layout.item_shimmer_order, 10)
    private val orderAdapter = OrderAdapter(
        arrayListOf(),
        this,
        Destination.Main.PRESENT,
        this
    )

    private lateinit var loadingDialog: LoadingDialog
    private var isConfirmLoading = false
    private var totalData = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentPresentBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        orderVM = ViewModelProvider(this).get(OrderViewModel::class.java)
        loadingDialog = LoadingDialog(childFragmentManager)

        setupViews()
        setupData()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        setupData()
    }

    @SuppressLint("SetTextI18n")
    private fun setupViews(){
        tv_toolbar_title.text = "Present (0)"

        tv_toolbar_select_all.setOnClickListener {
            if (totalData > 0){
                if (selectedOrders.size == totalData)
                    orderAdapter.unselectAllOrder()
                else
                    orderAdapter.selectAllOrder()
            }
        }

        rv_present.apply {
            adapter = orderAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }

        rv_shimmer_present.apply {
            adapter = shimmerAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            hasFixedSize()
        }

        swipe_present.setOnRefreshListener {
            startLoading()
            orderVM.getOrders(OrderType.PRESENT, true)
            swipe_present.isRefreshing = false
        }

        /// Setup More Button
        iv_more.setOnClickListener{
            findNavController().navigate(R.id.action_mainFragment_to_moreFragment)
        }
    }

    fun setupData(){
        startLoading()
        orderVM.getOrders(OrderType.PRESENT, true)
    }

    private fun observeViewModel(){
        this.orderVM.orders.observe(viewLifecycleOwner, { orders ->
            if (isConfirmLoading){
                isConfirmLoading = false
                loadingDialog.stopLoading()

                selectedOrders = listOf()
                toggleConfirmButton()

                startLoading()
                orderVM.getOrders(OrderType.PRESENT, true)
            } else {
                stopLoading()
                val sortOrders = orders.sortedByDescending { it.pickupDate }
                orderAdapter.updateData(sortOrders)
                totalData = sortOrders.filter { it.status == Status.OPEN_ORDER }.size

                tv_toolbar_title.text = "Present (${sortOrders.size})"

                Global.presentOrders = sortOrders

                /// Send Broadcast for refresh badge
                sendBroadcast()

                toggleEmptyState(sortOrders)
            }
        })

        orderVM.errMsg.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()){
                toast(it)
            }
        })
    }

    private fun sendBroadcast(){
        val intent = Intent(EventName.REFRESH_BADGE)
        intent.putExtra("orderType", OrderType.PRESENT)
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent);
    }

    fun confirmPressed(){
        val orderIds = selectedOrders.map { it.id }
        orderVM.updateOpenOrderToPickedUp(orderIds)
        loadingDialog.startLoading()
        isConfirmLoading = true
    }

    private fun toggleConfirmButton(){
        if (selectedOrders.isNotEmpty()){
            requireActivity().btn_scan_qr.gone()
            requireActivity().btn_confirm.text = "Pickup"
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
        shimmer_present.visible()
        shimmer_present.startShimmer()
        rv_present.gone()
        tv_present_no_order.gone()
    }

    private fun stopLoading(){
        shimmer_present.gone()
        shimmer_present.stopShimmer()
        rv_present.visible()
    }

    private fun toggleEmptyState(orders: List<Order>){
        if (orders.isEmpty()){
            tv_present_no_order.visible()
        }
    }

    override fun onSelected(orderList: List<Order>) {
        selectedOrders = orderList
        toggleSelectAllButton()
        toggleConfirmButton()
    }
}