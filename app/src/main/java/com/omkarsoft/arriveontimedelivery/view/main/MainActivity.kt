package com.omkarsoft.arriveontimedelivery.view.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.amplifyframework.core.Amplify
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.omkarsoft.arriveontimedelivery.R
import com.omkarsoft.arriveontimedelivery.databinding.ActivityMainBinding
import com.omkarsoft.arriveontimedelivery.extension.toast
import com.omkarsoft.arriveontimedelivery.helper.DateFormatterHelper
import com.omkarsoft.arriveontimedelivery.helper.GPSHelper
import com.omkarsoft.arriveontimedelivery.helper.SharedPreferencesHelper
import com.omkarsoft.arriveontimedelivery.view.more.MoreFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar_main.*
import pl.aprilapps.easyphotopicker.EasyImage
import java.util.*


class MainActivity : AppCompatActivity() {
    private var doubleBackToExitPressedOnce = false

    /// Firebase Database
    private val firebaseDB = Firebase.database
    private val reference = firebaseDB.getReference("coordinates")

    /// Android Location
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        signInToAWS()
        checkPermission()
        getLocation()
    }

    override fun onBackPressed() {
        val navHost = NavHostFragment.findNavController(nav_host_fragment).currentDestination!!.id
        if (navHost != R.id.mainFragment){
            findNavController(R.id.nav_host_fragment).popBackStack()
        } else {
            if (doubleBackToExitPressedOnce){
                finish()
            }

            doubleBackToExitPressedOnce = true
            toast("Please click BACK gain to exit")

            Handler(Looper.myLooper()!!).postDelayed({
                doubleBackToExitPressedOnce = false
            }, 2000)
        }
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState.clear()
        outPersistentState.clear()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.clear()
    }

    private fun signInToAWS(){
        Amplify.Auth.signIn("kelinci", "KelinciBerkelana",
            { result ->
                if (result.isSignInComplete) {
                    Log.i("Amplify", "Sign in succeeded")
                } else {
                    Log.i("Amplify", "Sign in not complete")
                }
            },
            { Log.e("Amplify", "Failed to sign in", it) }
        )
    }

    private fun checkPermission(){
        try {
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ), 101
                )
            }
            locationListener = LocationListener { location ->
                val longitude = location.longitude
                val latitude = location.latitude

                SharedPreferencesHelper().getCurrentUser()?.let { currentUser ->
                    val currentDate = DateFormatterHelper.format("YYYY-MM-DD HH:mm:ss", TimeZone.getTimeZone("America/Chicago"), Date())

                    reference.child(currentUser.id).child("longitude").setValue(longitude)
                    reference.child(currentUser.id).child("latitude").setValue(latitude)
                    reference.child(currentUser.id).child("timestamp").setValue(currentDate)
                }
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0F, locationListener)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getLocation(){
        val gpsHelper = GPSHelper(this)
        if (gpsHelper.canGetLocation){
            gpsHelper.getLocation()
        } else {
            gpsHelper.showSettingsAlert()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode){
            1001 -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){ // storage + camera
                    EasyImage.openChooserWithGallery(this, "Choose Image", 0)
                } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED){ // only storage
                    EasyImage.openGallery(this, 0)
                } else if (grantResults[1] == PackageManager.PERMISSION_GRANTED){ // only camera
                    EasyImage.openCameraForImage(this, 0)
                } else { // none of them
                    toast("Permission denied to read your external storage or using camera")
                }
            }
        }
    }
}