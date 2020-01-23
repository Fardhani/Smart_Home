package com.fardhani.smarthome.Repository

import android.content.Context
import android.location.Location
import android.os.CountDownTimer
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LiveData
import com.fardhani.smarthome.Model.LocationModel
import com.fardhani.smarthome.Model.RecentActivityModel
import com.fardhani.smarthome.R
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.*
import com.mapbox.mapboxsdk.geometry.LatLng
import java.util.*

class LocationRepository(context: Context) : LiveData<LocationModel>() {
    private var fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private lateinit var databaseReference: DatabaseReference
    var homeLocation: LatLng? = null
    var distance: Double = 0.0
    var homeLocked: Boolean = false
    var timerStarted = false
    var homeClosed: Boolean? = null
    var securityMode: Boolean? = null
    val context = context
    var notify = false
    var uid_name: String? = ""

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
        databaseReference.child("key").limitToFirst(1)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {
                    p0.children.forEach {
                        uid_name = it.child("name").value.toString()
                    }
                }
            })
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
                    //add activity lock or unlock the door
                    if (homeLocked != (p0.child("locked").value as Boolean?)!!) {
                        if (homeLocked != false) {
                            //add input to recent activity
                            inputRecentActivity(
                                "UNLOCK THE DOOR",
                                Date().toString(),
                                uid_name
                            )
                        } else {
                            //add input to recent activity
                            inputRecentActivity(
                                "LOCK THE DOOR",
                                Date().toString(),
                                uid_name
                            )
                        }
                    }
                    //add activity open or close the door
                    if (homeClosed != (p0.child("closed").value as Boolean?)!!) {
                        if (homeClosed != false) {
                            //add input to recent activity
                            inputRecentActivity(
                                "CLOSE THE DOOR",
                                Date().toString(),
                                uid_name
                            )
                        } else {
                            //add input to recent activity
                            inputRecentActivity(
                                "OPEN THE DOOR",
                                Date().toString(),
                                uid_name
                            )
                        }
                    }
                    if (securityMode != (p0.child("securityMode").value as Boolean?)!!) {
                        if (securityMode != false) {
                            //add input to recent activity
                            inputRecentActivity(
                                "ENABLE SECURITY MODE",
                                Date().toString(),
                                uid_name
                            )
                        } else {
                            //add input to recent activity
                            inputRecentActivity(
                                "DISABLE SECURITY MODE",
                                Date().toString(),
                                uid_name
                            )
                        }
                    }
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
                            notify = false
                            timerStarted = true
                            timer.start()
                        }
                    } else
                        timer.cancel()
                } else if (homeClosed == false) {
                    if (timerStarted)
                        timer.cancel()
                    if (notify == false) {
                        notify = true
                        //make notification, because door unclosed
                        sendNotification("Close the Door", "Door still open, close it first")
                    }
                }
            } else {
                timer.cancel()
            }

            //door opened but door still locked
            if (homeClosed != null && homeLocked != null && homeLocked == true && homeClosed == false) {
                sendNotification("Danger!", "Someone may open the door without key")
            }
        }
    }

    //function to input recent activity in firebase
    private fun inputRecentActivity(activity: String?, time: String?, uid_name: String?) {
        val recentActivity = RecentActivityModel(activity, time, uid_name)
        databaseReference.child("activity").push().setValue(recentActivity)
    }

    fun sendNotification(title: String, message: String) {
        val notificationBuilder = NotificationCompat.Builder(context)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.mipmap.logo_app)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        with(NotificationManagerCompat.from(context)) {
            notify(1001, notificationBuilder.build())
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