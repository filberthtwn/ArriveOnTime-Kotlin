package com.omkarsoft.arriveontimedelivery.view.partialPickup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.omkarsoft.arriveontimedelivery.R
import com.omkarsoft.arriveontimedelivery.adapter.OrderAdapter
import com.omkarsoft.arriveontimedelivery.adapter.ShimmerAdapter
import com.omkarsoft.arriveontimedelivery.constant.Destination
import com.omkarsoft.arriveontimedelivery.constant.Global
import com.omkarsoft.arriveontimedelivery.constant.OrderType
import com.omkarsoft.arriveontimedelivery.constant.Status
import com.omkarsoft.arriveontimedelivery.data.model.Order
import com.omkarsoft.arriveontimedelivery.extension.gone
import com.omkarsoft.arriveontimedelivery.extension.visible
import com.omkarsoft.arriveontimedelivery.helper.SharedPreferencesHelper
import com.omkarsoft.arriveontimedelivery.viewInterface.AutoMoveFragmentInterface
import com.omkarsoft.arriveontimedelivery.viewInterface.MainInterface
import com.omkarsoft.arriveontimedelivery.viewInterface.OrderConfirmInterface
import com.omkarsoft.arriveontimedelivery.viewModel.OrderViewModel
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.fragment_next_day.*
import kotlinx.android.synthetic.main.fragment_partial_pickup.*
import kotlinx.android.synthetic.main.toolbar_main.*

class PartialPickupFragment(
    private val mainDelegate: MainInterface
) : Fragment(), OrderConfirmInterface {
    private val shimmerAdapter = ShimmerAdapter(this, R.layout.item_shimmer_order, 10)
    private val orderAdapter = OrderAdapter(
        arrayListOf(),
        this,
        Destination.Main.PARTIAL_PICKUP,
        this
    )
    private lateinit var orderVM:OrderViewModel
    private lateinit var prefHelper:SharedPreferencesHelper
    private var partialOrders: List<Order> = arrayListOf()
    private var selectedOrders: List<Order> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_partial_pickup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupData()
        observeViewModel()
    }

    private fun setupViews(){
        tv_toolbar_title.text = "Partial (0)"
        tv_toolbar_select_all.gone()
//        tv_toolbar_select_all.setOnClickListener {
//            if (selectedOrders.size == partialOrders.size){
//                selectedOrders = arrayListOf()
//                orderAdapter.unselectAllOrder()
//            } else{
//                selectedOrders = partialOrders
//                orderAdapter.selectAllOrder()
//            }
//        }

        rv_shimmer_partial_pickup.apply {
            adapter = shimmerAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            hasFixedSize()
        }

        rv_partial_pickup.apply {
            adapter = orderAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }

        swipe_partial_pickup.setOnRefreshListener {
            setupData()
            swipe_partial_pickup.isRefreshing = false
        }

        /// Setup More Button
        iv_more.setOnClickListener{
            findNavController().navigate(R.id.action_mainFragment_to_moreFragment)
        }

        orderVM = ViewModelProvider(this).get(OrderViewModel::class.java)
    }

    fun setupData(){
        startLoading()
        orderVM.getOrders(OrderType.PRESENT, true)
    }

    private fun observeViewModel(){
        orderVM.orders.observe(viewLifecycleOwner, { orders ->
            stopLoading()
            partialOrders = orders.filter {
                it.partialPiece.toInt() > 0 && (it.partialPiece.toInt() < it.piece.toInt()) ||
                it.partialDeliver.toInt() > 0 && (it.partialDeliver.toInt() < it.piece.toInt())
            }
            tv_toolbar_title.text = "Partial (${partialOrders.size})"
            orderAdapter.updateData(partialOrders.sortedByDescending { it.pickupDate })
        })
    }

    private fun startLoading(){
        shimmer_partial_pickup.visible()
        shimmer_partial_pickup.startShimmer()
        rv_partial_pickup.gone()
    }

    private fun stopLoading(){
        shimmer_partial_pickup.gone()
        shimmer_partial_pickup.stopShimmer()
        rv_partial_pickup.visible()
    }

    override fun onSelected(orderList: List<Order>) {
        selectedOrders = orderList
        tv_toolbar_select_all.text = if(selectedOrders.size == partialOrders.size) "Unselect All" else "Select All"
        mainDelegate.toggleFloatingBtn(selectedOrders.isEmpty())
    }
}