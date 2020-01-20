package com.fardhani.smarthome

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import kotlinx.android.synthetic.main.activity_change_home_location.*

class ChangeHomeLocationActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var databaseReference: DatabaseReference
    private var mapView: MapView? = null
    private lateinit var mapboxMap: MapboxMap
    lateinit var tempHomeLocation: LatLng

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_home_location)

        //initiate database reference
        databaseReference = FirebaseDatabase.getInstance().reference

        //show back button
        val actionBar = supportActionBar
        actionBar!!.title = "Change Home Location"
        actionBar.setDisplayHomeAsUpEnabled(true)

        //initiate mapbox
        Mapbox.getInstance(this, getString(R.string.token_mapbox))

        mapView = mapHomeLocation
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)

        btSaveLocation.setOnClickListener {
            databaseReference.child("home_profile").child("lat").setValue(tempHomeLocation.latitude)
            databaseReference.child("home_profile").child("lng").setValue(tempHomeLocation.longitude)
            finish()
        }
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        mapboxMap.setStyle(Style.Builder().fromUri("mapbox://styles/mapbox/dark-v10")) {
            // Create and customize the LocationComponent's options
            val customLocationComponentOptions =
                LocationComponentOptions.builder(this)
                    .trackingGesturesManagement(true)
                    .accuracyColor(
                        ContextCompat.getColor(
                            this,
                            R.color.blueLogo
                        )
                    )
                    .build()

            val locationComponentActivationOptions =
                LocationComponentActivationOptions.builder(
                    this,
                    it
                )
                    .locationComponentOptions(customLocationComponentOptions)
                    .build()

            mapboxMap.locationComponent.apply {

                // Activate the LocationComponent with options
                activateLocationComponent(locationComponentActivationOptions)

                // Enable to make the LocationComponent visible
                isLocationComponentEnabled = true
                if (lastKnownLocation != null) {
                    mapboxMap.cameraPosition = CameraPosition.Builder()
                        .target(LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude))
                        .zoom(14.0)
                        .tilt(20.0)
                        .build()
                }
            }

            mapboxMap!!.addOnMapClickListener {
                mapboxMap.clear()
                mapboxMap?.addMarker(MarkerOptions().position(it).title("Home Location"))
                tempHomeLocation = it
                btSaveLocation.visibility = View.VISIBLE
                true
            }

        }
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }

    //set action of back button
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
