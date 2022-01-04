package com.omkarsoft.arriveontimedelivery.view.comments

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
import com.omkarsoft.arriveontimedelivery.adapter.CommentAdapter
import com.omkarsoft.arriveontimedelivery.adapter.ShimmerAdapter
import com.omkarsoft.arriveontimedelivery.databinding.FragmentCommentBinding
import com.omkarsoft.arriveontimedelivery.extension.gone
import com.omkarsoft.arriveontimedelivery.extension.toast
import com.omkarsoft.arriveontimedelivery.extension.visible
import com.omkarsoft.arriveontimedelivery.viewModel.CommentViewModel
import kotlinx.android.synthetic.main.fragment_comment.*
import kotlinx.android.synthetic.main.toolbar_nav.*

class CommentFragment : Fragment() {
    private val shimmerAdapter = ShimmerAdapter(this, R.layout.item_shimmer_comment, 10)
    private val commentAdapter = CommentAdapter(arrayListOf())

    private lateinit var commentVM: CommentViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentCommentBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        commentVM = ViewModelProvider(this).get(CommentViewModel::class.java)

        setupViews()
        setupData()
        observeViewModel()
    }

    @SuppressLint("SetTextI18n")
    private fun setupViews(){
        tv_toolbar_title.text = "Comments"
        toolbar_main.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        rv_shimmer_comment.apply {
            adapter = shimmerAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            hasFixedSize()
        }

        rv_comments.apply {
            adapter = commentAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }

        swipe_comments.setOnRefreshListener {
            startLoading()
            commentVM.getAllComments()
        }
    }

    private fun setupData(){
        startLoading()
        commentVM.getAllComments()
    }

    private fun observeViewModel(){
        commentVM.comments.observe(viewLifecycleOwner, {
            stopLoading()

            if (it.first().date.isEmpty() || it.isEmpty()){
                tv_comment_no_data.visible()
                rv_comments.gone()
            } else {
                commentAdapter.updateData(it)
            }
        })

        commentVM.errMsg.observe(viewLifecycleOwner, {
            toast(it)
        })
    }

    private fun startLoading(){
        shimmer_comment.visible()
        shimmer_comment.startShimmer()
        rv_comments.gone()
        tv_comment_no_data.gone()
    }

    private fun stopLoading(){
        shimmer_comment.gone()
        shimmer_comment.stopShimmer()
        rv_comments.visible()
    }
}