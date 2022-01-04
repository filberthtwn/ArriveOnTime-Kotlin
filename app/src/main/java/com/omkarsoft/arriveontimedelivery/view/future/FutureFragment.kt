package com.omkarsoft.arriveontimedelivery.view.future

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.omkarsoft.arriveontimedelivery.R
import com.omkarsoft.arriveontimedelivery.adapter.OrderAdapter
import com.omkarsoft.arriveontimedelivery.adapter.ShimmerAdapter
import com.omkarsoft.arriveontimedelivery.constant.Destination
import com.omkarsoft.arriveontimedelivery.constant.OrderType
import com.omkarsoft.arriveontimedelivery.databinding.FragmentFutureBinding
import com.omkarsoft.arriveontimedelivery.extension.gone
import com.omkarsoft.arriveontimedelivery.extension.needLoadFromRemote
import com.omkarsoft.arriveontimedelivery.extension.toast
import com.omkarsoft.arriveontimedelivery.extension.visible
import com.omkarsoft.arriveontimedelivery.viewModel.OrderViewModel
import kotlinx.android.synthetic.main.fragment_future.*
import kotlinx.android.synthetic.main.toolbar_nav.*

class FutureFragment : Fragment() {
    private var shimmerAdapter = ShimmerAdapter(this, R.layout.item_shimmer_order, 10)
    private var orderAdapter = OrderAdapter(arrayListOf(), this, Destination.Main.DELIVERY)

    private lateinit var orderVM: OrderViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentFutureBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        orderVM = ViewModelProvider(this).get(OrderViewModel::class.java)

        setupViews()
        setupData()
        observeViewModel()
    }

    @SuppressLint("SetTextI18n")
    private fun setupViews(){
        tv_toolbar_title.text = "Future Order"

        toolbar_main.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        rv_shimmer_future_order.apply {
            adapter = shimmerAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            hasFixedSize()
        }

        rv_future_order.apply {
            adapter = orderAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }

        swipe_future.setOnRefreshListener {
            stopLoading()
            orderVM.getOrders(OrderType.FUTURE, false)
            swipe_future.isRefreshing = false
        }
    }

    private fun setupData(){
        if (requireContext().needLoadFromRemote(OrderType.FUTURE)){
            startLoading()
        }
        orderVM.getOrders(OrderType.FUTURE, false)
    }

    private fun observeViewModel(){
        orderVM.orders.observe(viewLifecycleOwner, { orders ->
            stopLoading()
            orderAdapter.updateData(orders)

            if (orders.isEmpty()){
                tv_future_order_no_order.visible()
            }
        })

        orderVM.errMsg.observe(viewLifecycleOwner, {
            toast(it)
        })
    }

    private fun startLoading(){
        shimmer_future_order.visible()
        shimmer_future_order.startShimmer()
        rv_future_order.gone()
        tv_future_order_no_order.gone()
    }

    private fun stopLoading(){
        shimmer_future_order.gone()
        shimmer_future_order.stopShimmer()
        rv_future_order.visible()
    }
}