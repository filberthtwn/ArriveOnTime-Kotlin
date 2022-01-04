package com.omkarsoft.arriveontimedelivery.view.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.messaging.FirebaseMessaging
import com.omkarsoft.arriveontimedelivery.R
import com.omkarsoft.arriveontimedelivery.constant.Destination
import com.omkarsoft.arriveontimedelivery.databinding.FragmentSignInBinding
import com.omkarsoft.arriveontimedelivery.extension.hideKeyboard
import com.omkarsoft.arriveontimedelivery.helper.SharedPreferencesHelper
import com.omkarsoft.arriveontimedelivery.view.main.MainActivity
import com.omkarsoft.arriveontimedelivery.view.template.LoadingDialog
import com.omkarsoft.arriveontimedelivery.viewModel.AuthViewModel
import com.omkarsoft.arriveontimedelivery.viewModel.UserViewModel
import kotlinx.android.synthetic.main.fragment_sign_in.*

class SignInFragment : Fragment(), View.OnClickListener {

    private lateinit var authVM: AuthViewModel
    private lateinit var userVM: UserViewModel
    private lateinit var prefHelper: SharedPreferencesHelper
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentSignInBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authVM = ViewModelProvider(this).get(AuthViewModel::class.java)
        userVM = ViewModelProvider(this).get(UserViewModel::class.java)
        prefHelper = SharedPreferencesHelper(requireContext())
        prefHelper.setLastFragment(Destination.Main.DISPATCH)
        loadingDialog = LoadingDialog(childFragmentManager)

        setupViews()
        observeViewModel()
    }

    private fun setupViews(){
        btn_login.setOnClickListener(this)

        ll_main_sign_in.setOnClickListener {
            hideKeyboard()
        }

        prefHelper.apply {
            if (isRememberLogin()){
                edt_login_email.setText(getUserUsername())
                edt_login_password.setText(getUserPassword())
                cb_remember_password.isChecked = true
            }
        }
    }

    private fun observeViewModel(){
        this.authVM.user.observe(viewLifecycleOwner, { user ->
            if (cb_remember_password.isChecked){
                prefHelper.saveUserLogin(edt_login_email.text.toString(), edt_login_password.text.toString())
            }

            SharedPreferencesHelper(requireContext()).saveCurrentUser(user)
            FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                prefHelper.saveFCMToken(token)
                userVM.updateFCMToken(token)
            }
        })

        this.authVM.errMsg.observe(viewLifecycleOwner, { errMsg ->
            loadingDialog.stopLoading()
            Toast.makeText(requireContext(), errMsg, Toast.LENGTH_LONG).show()
            edt_login_password.setText("")
        })

        this.userVM.isSuccess.observe(viewLifecycleOwner, {
            loadingDialog.stopLoading()
            startActivity(Intent(requireContext(), MainActivity::class.java))
        })

        this.userVM.errMsg.observe(viewLifecycleOwner, { errMsg ->
            loadingDialog.stopLoading()
            Toast.makeText(requireContext(), errMsg, Toast.LENGTH_LONG).show()
            edt_login_password.setText("")
        })
    }

    override fun onClick(view: View?) {
        when(view?.id){
            R.id.btn_login -> {
                if (edt_login_email.text.isEmpty() || edt_login_password.text.isEmpty() ){
                    Toast.makeText(requireContext(), "Please fill the blanks",Toast.LENGTH_LONG).show()
                    return
                }
                authVM.login(edt_login_email.text.toString(), edt_login_password.text.toString())
                loadingDialog.startLoading()
            }
        }
    }
}