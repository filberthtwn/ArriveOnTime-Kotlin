package com.omkarsoft.arriveontimedelivery.view.deliver

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
import com.omkarsoft.arriveontimedelivery.constant.Destination
import com.omkarsoft.arriveontimedelivery.constant.EventName
import com.omkarsoft.arriveontimedelivery.constant.Global
import com.omkarsoft.arriveontimedelivery.constant.OrderType
import com.omkarsoft.arriveontimedelivery.data.model.Order
import com.omkarsoft.arriveontimedelivery.databinding.FragmentDeliverBinding
import com.omkarsoft.arriveontimedelivery.extension.gone
import com.omkarsoft.arriveontimedelivery.extension.toast
import com.omkarsoft.arriveontimedelivery.extension.visible
import com.omkarsoft.arriveontimedelivery.view.detail.DeliveryDialogFragment
import com.omkarsoft.arriveontimedelivery.view.template.LoadingDialog
import com.omkarsoft.arriveontimedelivery.viewInterface.OrderDeliverInterface
import com.omkarsoft.arriveontimedelivery.viewModel.OrderViewModel
import kotlinx.android.synthetic.main.fragment_deliver.*
import kotlinx.android.synthetic.main.toolbar_main.*

class DeliverFragment : Fragment(), OrderDeliverInterface {
    private val shimmerAdapter = ShimmerAdapter(this, R.layout.item_shimmer_order, 10)
    private val orderAdapter = OrderAdapter(arrayListOf(), this, Destination.Main.DELIVERY, orderDeliver = this)

    private lateinit var orderVM: OrderViewModel
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentDeliverBinding.inflate(inflater, container, false).root
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
    }

    @SuppressLint("SetTextI18n")
    private fun setupViews(){
        tv_toolbar_title.text = "Delivered (0)"
        tv_toolbar_select_all.gone()

        rv_shimmer_deliver_order.apply {
            adapter = shimmerAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            hasFixedSize()
        }

        rv_deliver_order.apply {
            adapter = orderAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }

        swipe_deliver.setOnRefreshListener {
            startLoading()
            orderVM.getOrders(OrderType.DELIVER, true)
            swipe_deliver.isRefreshing = false
        }

        /// Setup More Button
        iv_more.setOnClickListener{
            findNavController().navigate(R.id.action_mainFragment_to_moreFragment)
        }
    }

    private fun setupData(){
        startLoading()
        orderVM.getOrders(OrderType.DELIVER, true)
    }

    private fun observeViewModel(){
        orderVM.orders.observe(viewLifecycleOwner, { orders ->
            stopLoading()
            val sortOrders = orders.sortedByDescending { it.pickupDate }
            orderAdapter.updateData(sortOrders)

            tv_toolbar_title.text = "Delivered (${sortOrders.size})"

            Global.deliverOrders = orders

            /// Send Broadcast for refresh badge
            sendBroadcast()

            if (sortOrders.isEmpty()){
                tv_delivery_order_no_order.visible()
            }
        })

        orderVM.success.observe(viewLifecycleOwner, { success ->
            loadingDialog.stopLoading()
            if (success){
                setupData()
            }
        })

        orderVM.errMsg.observe(viewLifecycleOwner, {
            toast(it)
        })
    }

    private fun sendBroadcast(){
        val intent = Intent(EventName.REFRESH_BADGE)
        intent.putExtra("orderType", OrderType.DELIVER)
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent);
    }

    private fun startLoading(){
        shimmer_deliver_order.visible()
        shimmer_deliver_order.startShimmer()
        rv_deliver_order.gone()
        tv_delivery_order_no_order.gone()
    }

    private fun stopLoading(){
        shimmer_deliver_order.gone()
        shimmer_deliver_order.stopShimmer()
        rv_deliver_order.visible()
    }

    fun roundTripAction(
        order: Order,
        waitTime: String,
        transportation: String,
        reasonType: String
    ){
        loadingDialog.startLoading()
        orderVM.updateDeliveredToRoundTrip(
            orderId = order.id,
            waitTime = waitTime,
            transportation = transportation,
            boxes = order.piece,
            reasonType = reasonType
        )
    }

    override fun onDeliveredOrderSelected(order: Order) {
        DeliveryDialogFragment(
            order = order,
            orderVM = orderVM,
            relationship = "relationship",
            isUserLivedHere = "recipient",
            lastName = "lastName",
            fragment = this,
        ).show(
            childFragmentManager,
            DeliveryDialogFragment::class.qualifiedName
        )
    }
}