package com.omkarsoft.arriveontimedelivery.view.more

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.omkarsoft.arriveontimedelivery.R
import com.omkarsoft.arriveontimedelivery.databinding.FragmentMoreBinding
import com.omkarsoft.arriveontimedelivery.extension.gone
import com.omkarsoft.arriveontimedelivery.helper.SharedPreferencesHelper
import com.omkarsoft.arriveontimedelivery.view.auth.AuthActivity
import kotlinx.android.synthetic.main.fragment_more.*
import kotlinx.android.synthetic.main.toolbar_main.*
import kotlinx.android.synthetic.main.toolbar_main.toolbar_main
import kotlinx.android.synthetic.main.toolbar_main.tv_toolbar_title
import kotlinx.android.synthetic.main.toolbar_nav.*

class MoreFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentMoreBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    @SuppressLint("SetTextI18n")
    private fun setupViews(){
        tv_toolbar_title.text = "More"
        toolbar_main.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        val user = SharedPreferencesHelper().getCurrentUser()
        this.tv_more_name.text = if (user?.name != null) user.name else "-"
        this.tv_more_location.text = "Location: " + if (user?.location != null) user.location else "-"

        btn_more_future.setOnClickListener {
            findNavController().navigate(R.id.action_moreFragment_to_futureFragment)
        }

        btn_more_comments.setOnClickListener {
            findNavController().navigate(R.id.action_moreFragment_to_commentFragment)
        }

        btn_logout.setOnClickListener {
            SharedPreferencesHelper().removeCurrentUser()
            startActivity(Intent(requireContext(), AuthActivity::class.java))
            activity?.finish()
        }
    }
}