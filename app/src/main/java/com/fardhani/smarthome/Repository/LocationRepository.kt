package com.fardhani.smarthome.Repository

import android.content.Context
import android.location.Location
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.LiveData
import com.fardhani.smarthome.Model.LocationModel
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.*
import com.mapbox.mapboxsdk.geometry.LatLng

class LocationRepository(context: Context) : LiveData<LocationModel>() {
    private var fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private lateinit var databaseReference: DatabaseReference
    var homeLocation: LatLng? = null
    var distance: Double = 0.0
    var homeLocked: Boolean = false
    var timerStarted = false
    var homeClosed: Boolean? = null
    var securityMode: Boolean? = null

    override fun onActive() {
        super.onActive()
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.also {
                    setLocationData(it)
                }
            }
        startLocationUpdates()
    }

    private fun setLocationData(location: Location) {
        databaseReference = FirebaseDatabase.getInstance().reference
        databaseReference.child("home_profile").addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                homeLocation =
                    LatLng(p0.child("lat").value as Double, p0.child("lng").value as Double)
            }

        })
        databaseReference.child("security_status")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {
                    homeLocked = (p0.child("locked").value as Boolean?)!!
                    homeClosed = (p0.child("closed").value as Boolean?)!!
                    securityMode = (p0.child("securityMode").value as Boolean?)!!
                }

            })
        if (homeLocation != null) {
            distance = getDistance(
                location.latitude,
                homeLocation!!.latitude,
                location.longitude,
                homeLocation!!.longitude
            )
        }
        value = LocationModel(
            latitude = location.latitude,
            longitude = location.longitude,
            distance = distance
        )
        //set time countdown : 5000 mean 5 seconds
        val timer = object : CountDownTimer(20000, 1000) {
            override fun onFinish() {
                if (homeClosed != null && homeClosed == true) {
                    homeLocked = true
                    databaseReference.child("security_status").child("locked").setValue(true)
                }
                timerStarted = false
            }

            override fun onTick(millisUntilFinished: Long) {
                Log.e("timer", millisUntilFinished.toString())
            }

        }
        if (securityMode != null && securityMode == true) {
            //threshold of distance
            if (distance > 1.0) {
                //if home closed
                if (homeClosed != null && homeClosed == true) {
                    //if home not locked
                    if (!homeLocked!!) {
                        //if timer did not started
                        if (timerStarted == false) {
                            timerStarted = true
                            timer.start()
                        }
                    } else
                        timer.cancel()
                } else if (homeClosed == false) {
                    if (timerStarted)
                        timer.cancel()
                    //make notification, because door unclosed
                }
            } else {
                timer.cancel()
            }
        }
    }

    companion object {
        val locationRequest: LocationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult?) {
            p0 ?: return
            for (location in p0.locations) {
                setLocationData(location)
            }
        }
    }

    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null
        )
    }

    //rumus menghitung jarak antar dua titik
    fun getDistance(
        lat1: Double,
        lat2: Double,
        lon1: Double,
        lon2: Double
    ): Double {
        val R = 6371 // Radius of the earth
        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)
        val a =
            (Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                    + (Math.cos(Math.toRadians(lat1)) * Math.cos(
                Math.toRadians(
                    lat2
                )
            )
                    * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2)))
        val c =
            2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        var distance = R * c * 1000 // convert to meters
        distance = Math.pow(distance, 2.0)
        return Math.sqrt(distance)
    }
}