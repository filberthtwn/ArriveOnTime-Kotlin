package com.omkarsoft.arriveontimedelivery.view.complete

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.omkarsoft.arriveontimedelivery.R
import com.omkarsoft.arriveontimedelivery.data.model.Order
import com.omkarsoft.arriveontimedelivery.databinding.FragmentCompleteOrderBinding
import kotlinx.android.synthetic.main.fragment_complete_order.*
import kotlinx.android.synthetic.main.toolbar_nav.*

class CompleteOrderFragment : Fragment() {
    private val recipientArray = arrayListOf("Yes", "No")
    private val relationshipArray = arrayListOf("Self", "Nurse", "Receptionist", "RN", "Husband", "Wife", "Care Driver", "Other")

    private var recipientPosition = 0
    private var relationshipPosition = 0

    private lateinit var order: Order

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentCompleteOrderBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPreliminaryData()
        setupViews()
        setupData()
    }

    private fun setupPreliminaryData(){
        arguments?.let {
            order = CompleteOrderFragmentArgs.fromBundle(it).order
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupViews(){
        tv_toolbar_title.text = "Complete Order"
        toolbar_main.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        tv_complete_order_id.text = order.id
        tv_complete_order_recipient_question.text = "Does ${order.recipient?.name} live here?"

        spinner_complete_order_recipient.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                recipientPosition = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinner_complete_order_recipient_relationship.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                relationshipPosition = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        btn_complete_order_next.setOnClickListener {
            if (validate()){
                val action = CompleteOrderFragmentDirections.actionCompleteOrderFragmentToCompleteOrderSignatureFragment(
                    order = order,
                    lastName = edt_complete_order_last_name.text.toString(),
                    userLivesHere = recipientArray[recipientPosition],
                    relationship = relationshipArray[relationshipPosition]
                )
                findNavController().navigate(action)
            }
        }
    }

    private fun setupData(){
        val recipientAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, recipientArray)
        recipientAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        spinner_complete_order_recipient.adapter = recipientAdapter
        recipientAdapter.notifyDataSetChanged()

        val relationshipAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, relationshipArray)
        relationshipAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        spinner_complete_order_recipient_relationship.adapter = relationshipAdapter
        relationshipAdapter.notifyDataSetChanged()
    }

    private fun validate(): Boolean {
        if (edt_complete_order_last_name.text.isNullOrEmpty()) return false
        return true
    }
}