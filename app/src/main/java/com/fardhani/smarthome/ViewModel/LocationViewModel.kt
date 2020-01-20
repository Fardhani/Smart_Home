package com.fardhani.smarthome.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.fardhani.smarthome.Repository.LocationRepository

class LocationViewModel(application: Application) : AndroidViewModel(application){
    private val locationData = LocationRepository(application)

    fun getLocationData() = locationData
}