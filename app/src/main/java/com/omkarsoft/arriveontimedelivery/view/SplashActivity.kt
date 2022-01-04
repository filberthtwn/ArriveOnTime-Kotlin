package com.omkarsoft.arriveontimedelivery.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.omkarsoft.arriveontimedelivery.constant.Destination
import com.omkarsoft.arriveontimedelivery.databinding.ActivitySplashBinding
import com.omkarsoft.arriveontimedelivery.helper.SharedPreferencesHelper
import com.omkarsoft.arriveontimedelivery.view.auth.AuthActivity
import com.omkarsoft.arriveontimedelivery.view.main.MainActivity

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefHelper = SharedPreferencesHelper(this)
        prefHelper.setLastFragment(Destination.Main.DISPATCH)

        if (SharedPreferencesHelper(this).getCurrentUser() == null){
            startActivity(Intent(this, AuthActivity::class.java))
            return
        }
        startActivity(Intent(this, MainActivity::class.java))
    }
}