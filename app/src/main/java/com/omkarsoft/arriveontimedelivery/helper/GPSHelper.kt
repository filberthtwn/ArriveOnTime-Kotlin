package com.omkarsoft.arriveontimedelivery.helper

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import androidx.core.app.ActivityCompat


class GPSHelper(private val context: Context): Service(), LocationListener {
    companion object {
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Float = 10f
        private const val MIN_TIME_BW_UPDATES: Long = 1000 * 60 * 1
    }

    init {
        getLocation()
    }

    private var isGPSEnabled = false
    private var isNetworkEnabled = false
    var canGetLocation = false

    private val prefHelper = SharedPreferencesHelper()

    private lateinit var locationManager: LocationManager

    private var location: Location? = null

    fun getLocation() {
        try {
            locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (isGPSEnabled || isNetworkEnabled) {
                canGetLocation = true

                if (isNetworkEnabled){
                    checkPermission()

                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES,
                        this
                    )

                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    location?.let {
                        prefHelper.saveLocation(it.longitude, it.latitude)
                    }
                }

                if (isGPSEnabled){
                    if (location == null){
                        checkPermission()

                        locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            this
                        )

                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        location?.let {
                            prefHelper.saveLocation(it.longitude, it.latitude)
                        }
                    }
                }
            }
        } catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun showSettingsAlert(){
        val alertDialog = AlertDialog.Builder(context)
        alertDialog.setTitle("GPS Settings")
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?")

        alertDialog.setPositiveButton("Settings") { _, _ ->
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            context.startActivity(intent)
        }

        alertDialog.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        alertDialog.show()
    }

    override fun onLocationChanged(location: Location) {
        prefHelper.saveLocation(location.longitude, location.latitude)
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

    override fun onBind(arg0: Intent?): IBinder? {
        return null
    }

    private fun checkPermission(){
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(context as Activity, arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ), 101)
        }
    }
}