package com.fardhani.smarthome.Service

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer
import com.fardhani.smarthome.ViewModel.LocationViewModel

class LocationService: LifecycleService() {
    private lateinit var locationViewModel: LocationViewModel

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //get my location from viewmodel
        locationViewModel = LocationViewModel(applicationContext as Application)
        locationViewModel.getLocationData().observe(this, Observer {
            Log.e("distance", it.distance.toString())
        })
        return super.onStartCommand(intent, flags, startId)
    }
}