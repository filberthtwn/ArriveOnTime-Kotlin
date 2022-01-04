package com.omkarsoft.arriveontimedelivery.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.omkarsoft.arriveontimedelivery.R
import com.omkarsoft.arriveontimedelivery.constant.Destination
import com.omkarsoft.arriveontimedelivery.constant.Status
import com.omkarsoft.arriveontimedelivery.data.model.Order
import com.omkarsoft.arriveontimedelivery.databinding.ItemOrderBinding
import com.omkarsoft.arriveontimedelivery.view.main.MainFragmentDirections
import com.omkarsoft.arriveontimedelivery.viewInterface.OrderConfirmInterface
import com.omkarsoft.arriveontimedelivery.viewInterface.OrderDeliverInterface
import com.omkarsoft.arriveontimedelivery.viewInterface.OrderListener
import kotlinx.android.synthetic.main.item_order.view.*

@SuppressLint("NotifyDataSetChanged")
class OrderAdapter(
    var data: List<Order>,
    val fragment: Fragment,
    val type: String,
    private val selectedListener: OrderConfirmInterface? = null,
    private val orderDeliver: OrderDeliverInterface? = null
): RecyclerView.Adapter<OrderAdapter.ViewHolder>(), OrderListener {
    private var selectedOrders: ArrayList<Order> = arrayListOf()

    fun updateData(data: List<Order>){
        this.data = data
        notifyDataSetChanged()
    }

    fun selectAllOrder(){
        selectedOrders.clear()

        when (type){
            Destination.Main.DISPATCH -> selectedOrders.addAll(data)
            Destination.Main.NEXT, Destination.Main.PRESENT -> selectedOrders.addAll(data.filter { it.status == Status.OPEN_ORDER })
        }

        selectedListener?.onSelected(selectedOrders)
        notifyDataSetChanged()
    }

    fun unselectAllOrder(){
        selectedOrders.clear()
        selectedListener?.onSelected(selectedOrders)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<ItemOrderBinding>(layoutInflater, R.layout.item_order, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.listener = this

        val item = data[position]
        holder.bind(item, type)
    }

    inner class ViewHolder(var binding: ItemOrderBinding): RecyclerView.ViewHolder(binding.root){
        @SuppressLint("SetTextI18n")
        fun bind(order: Order, type: String){
            binding.order = order
            binding.isShowExpectedTime = (order.senderName == "CORAM OF EL PASO")
            binding.executePendingBindings()

            binding.btnOrder.apply {
                when (type){
                    Destination.Main.DELIVERY -> {
                        setTextColor(Color.WHITE)
                        setBackgroundResource(R.drawable.rounded_button)

                        if (order.isRoundTrip()){
                            backgroundTintList = ContextCompat.getColorStateList(fragment.requireContext(), R.color.round_trip)
                            text = "Delivered"

                            if(order.signRoundtrip != ""){
                                setTextColor(ContextCompat.getColor(fragment.requireContext(), R.color.mutedButtonText))
                                val bgColor = ContextCompat.getColorStateList(fragment.requireContext(), R.color.mutedButtonBg)
                                backgroundTintList = bgColor
                                binding.clRoundtripStatus.backgroundTintList = bgColor

                                text = "Delivered"
                            }
                        } else {
                            backgroundTintList = ContextCompat.getColorStateList(fragment.requireContext(), R.color.delivered)
                            text = "Delivered"
                        }
                    }
                    Destination.Main.DISPATCH -> {
                        backgroundTintList = null

                        text = if (selectedOrders.contains(order)){
                            setTextColor(Color.WHITE)
                            setBackgroundResource(R.drawable.select_button_active)
                            "Confirmed"
                        } else {
                            setTextColor(ContextCompat.getColor(fragment.requireContext(), R.color.dispatch))
                            setBackgroundResource(R.drawable.select_button_inactiv)
                            "Confirm"
                        }
                    }
                    Destination.Main.MORE -> {
                        setTextColor(Color.WHITE)
                        setBackgroundResource(R.drawable.rounded_button)

                        val bgColor = ContextCompat.getColorStateList(fragment.requireContext(), R.color.primary)
                        backgroundTintList = bgColor
                        binding.clRoundtripStatus.backgroundTintList = bgColor
                        text = "Future"
                    }
                    else -> {
                        setTextColor(Color.WHITE)
                        setBackgroundResource(R.drawable.rounded_button)

                        if (selectedOrders.contains(order)){
                            backgroundTintList = ContextCompat.getColorStateList(fragment.requireContext(), R.color.success)
                            binding.clRoundtripStatus.backgroundTintList = ContextCompat.getColorStateList(fragment.requireContext(), R.color.primary)

                            if(order.status == Status.PICKED_UP){
                                /// Do when order is partial pickup
                                if(order.partialPiece.toInt() > 0){
                                    text = "Picked ${order.partialPiece}/${order.piece}"
                                }
                            }
                        } else {
                            when (order.status){
                                Status.OPEN_ORDER -> {
                                    text = "Open Order"

                                    val bgColor = ContextCompat.getColorStateList(fragment.requireContext(), R.color.primary)
                                    backgroundTintList = bgColor
                                    binding.clRoundtripStatus.backgroundTintList = bgColor
                                }
                                Status.PICKED_UP -> {
                                    text = "Picked Up"

                                    var bgColor = ContextCompat.getColorStateList(fragment.requireContext(), R.color.picked_up)

                                    /// Do when order is partial pickup
                                    if(order.partialPiece.toInt() > 0 && order.partialPiece.toInt() < order.piece.toInt()){
                                        text = "Picked ${order.partialPiece}/${order.piece}"
                                        bgColor = ContextCompat.getColorStateList(fragment.requireContext(), R.color.round_trip)
                                    }

                                    backgroundTintList = bgColor
                                    binding.clRoundtripStatus.backgroundTintList = bgColor
                                }
                                Status.CANCELLED -> {
                                    text = "Cancelled"
                                    backgroundTintList = ContextCompat.getColorStateList(fragment.requireContext(), R.color.cancelled)
                                }
                                Status.DELIVERED -> {
                                    text = "Delivered"


                                    if(order.partialDeliver.toInt() > 0 &&
                                        (order.partialDeliver.toInt() < order.piece.toInt())){
                                        text = "Delivered ${order.partialDeliver}/${order.piece}"
                                    }

                                    if (order.isRoundTrip()){
//                                        cl_roundtrip_status.visible()
                                        backgroundTintList = ContextCompat.getColorStateList(fragment.requireContext(), R.color.round_trip)
                                        if(order.signRoundtrip != ""){
                                            setTextColor(ContextCompat.getColor(fragment.requireContext(), R.color.mutedButtonText))
                                            backgroundTintList = ContextCompat.getColorStateList(fragment.requireContext(), R.color.mutedButtonBg)
                                        }
                                    } else {
                                        backgroundTintList = ContextCompat.getColorStateList(fragment.requireContext(), R.color.delivered)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onButtonClicked(order: Order) {
        when (type) {
            Destination.Main.DISPATCH -> {
                if (selectedOrders.contains(order)){
                    selectedOrders.remove(order)
                } else {
                    selectedOrders.add(order)
                }
            }
            Destination.Main.NEXT, Destination.Main.PRESENT -> {
                if (order.status == Status.OPEN_ORDER){
                    if (selectedOrders.contains(order)){
                        selectedOrders.remove(order)
                    } else {
                        selectedOrders.add(order)
                    }
                }
            }
        }

        if(order.status == Status.PICKED_UP){
            onOrderClicked(order)
            return
        }

        if(order.status == Status.DELIVERED && !order.isRoundTrip()){
            showRoundTripDialog(order)
            return
        }

        selectedListener?.onSelected(selectedOrders)
        notifyDataSetChanged()
    }

    private fun showRoundTripDialog(order: Order){
        val alertDialog = AlertDialog.Builder(fragment.requireContext())

        alertDialog.apply {
            setMessage("Order already delivered do you wish to choose RoundTrip manually.")
            setPositiveButton("Yes") { _, _ ->
                orderDeliver!!.onDeliveredOrderSelected(order)
            }
            setNegativeButton("No") { _, _ -> }
        }.create().show()
    }

    override fun onOrderClicked(order: Order) {
        val action = MainFragmentDirections.actionMainFragmentToOrderDetailFragment(status = type, orderId = order.id)
        fragment.findNavController().navigate(action)
    }
}