package com.omkarsoft.arriveontimedelivery.view.nextday

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import com.omkarsoft.arriveontimedelivery.databinding.FragmentNextDayBinding
import com.omkarsoft.arriveontimedelivery.extension.gone
import com.omkarsoft.arriveontimedelivery.extension.toast
import com.omkarsoft.arriveontimedelivery.extension.visible
import com.omkarsoft.arriveontimedelivery.view.template.LoadingDialog
import com.omkarsoft.arriveontimedelivery.viewInterface.AutoMoveFragmentInterface
import com.omkarsoft.arriveontimedelivery.viewInterface.OrderConfirmInterface
import com.omkarsoft.arriveontimedelivery.viewModel.OrderViewModel
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.fragment_next_day.*
import kotlinx.android.synthetic.main.toolbar_main.*

class NextDayFragment(
    private val moveListener: AutoMoveFragmentInterface
) : Fragment(), OrderConfirmInterface {
    private lateinit var orderVM: OrderViewModel
    private var selectedOrders: List<Order> = arrayListOf()

    private val shimmerAdapter = ShimmerAdapter(this, R.layout.item_shimmer_order, 10)
    private val orderAdapter = OrderAdapter(
        arrayListOf(),
        this,
        Destination.Main.NEXT,
        this
    )

    private lateinit var loadingDialog: LoadingDialog
    private var isConfirmLoading = false
    private var totalData = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentNextDayBinding.inflate(inflater, container, false).root
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
        tv_toolbar_title.text = "Open Orders (0)"

        tv_toolbar_select_all.setOnClickListener {
            if (totalData > 0){
                if (selectedOrders.size == totalData){
                    orderAdapter.unselectAllOrder()
                } else {
                    orderAdapter.selectAllOrder()
                }
            }
        }

        rv_shimmer_next_day.apply {
            adapter = shimmerAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            hasFixedSize()
        }

        rv_next_day.apply {
            adapter = orderAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }

        swipe_next.setOnRefreshListener {
            startLoading()
            orderVM.getOrders(OrderType.OPEN, true)
            swipe_next.isRefreshing = false
        }

        /// Setup More Button
        iv_more.setOnClickListener{
            findNavController().navigate(R.id.action_mainFragment_to_moreFragment)
        }
    }

    fun setupData(){
        startLoading()
        orderVM.getOrders(OrderType.OPEN, true)
    }

    private fun observeViewModel(){
        orderVM.orders.observe(viewLifecycleOwner, { orders ->
            if (isConfirmLoading){
                isConfirmLoading = false
                loadingDialog.stopLoading()
                moveListener.moveToPresent()
            } else {
                stopLoading()
                val filteredOrders = orders.filter {
                    (it.orderColor != "" && it.status != "") &&
                    (it.partialPiece.toInt() == 0 || it.partialPiece.toInt() == it.piece.toInt())
                }
                val sortOrders = filteredOrders.sortedByDescending { it.pickupDate }
                orderAdapter.updateData(sortOrders)
                totalData = sortOrders.filter{
                    it.status == Status.OPEN_ORDER
                }.size

                Global.openOrders = sortOrders

                /// Send Broadcast for refresh badge
                sendBroadcast()

                tv_toolbar_title.text = "Open Orders (${sortOrders.size})"

                if (sortOrders.isEmpty()){
                    tv_next_day_no_order.visible()
                }
            }
        })

        orderVM.errMsg.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()){
                toast(it)
                loadingDialog.stopLoading()
                selectedOrders = arrayListOf()
                toggleConfirmButton()
            }
        })

        orderVM.success.observe(viewLifecycleOwner, { success ->
            if (success){
                loadingDialog.stopLoading()
                selectedOrders = arrayListOf()
                toggleConfirmButton()
            }
        })
    }

    private fun sendBroadcast(){
        val intent = Intent(EventName.REFRESH_BADGE)
        intent.putExtra("orderType", OrderType.OPEN)
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent);
    }

    fun confirmPressed(){
        val orderIds = selectedOrders.map { it.id }
        orderVM.updateOpenOrderToPickedUp(orderIds)
        loadingDialog.startLoading()
        isConfirmLoading = true

        Global.openOrders = null
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
        shimmer_next_day.visible()
        shimmer_next_day.startShimmer()
        rv_next_day.gone()
        tv_next_day_no_order.gone()
    }

    private fun stopLoading(){
        shimmer_next_day.gone()
        shimmer_next_day.stopShimmer()
        rv_next_day.visible()
    }

    override fun onSelected(orderList: List<Order>) {
        selectedOrders = orderList
        toggleSelectAllButton()
        toggleConfirmButton()
    }
}