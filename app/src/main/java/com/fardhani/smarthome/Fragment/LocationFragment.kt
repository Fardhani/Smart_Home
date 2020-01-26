package com.fardhani.smarthome.Fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.fardhani.smarthome.R
import com.fardhani.smarthome.ViewModel.LocationViewModel
import com.google.firebase.database.*
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.core.constants.Constants.PRECISION_6
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlinx.android.synthetic.main.location.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LocationFragment : Fragment(), OnMapReadyCallback, PermissionsListener {
    private var mapView: MapView? = null
    private var mapboxMap: MapboxMap? = null
    private var myLocation: LatLng? = null
    private var homeLocation: LatLng? = null
    private var locationEngine: LocationEngine? = null
    private lateinit var databaseReference: DatabaseReference
    private var dashedLineDirectionsFeatureCollection: FeatureCollection? = null
    private var style: Style? = null
    private val ROUTE_LAYER_ID = "route-layer-id"
    private val ROUTE_SOURCE_ID = "route-source-id"
    private lateinit var locationViewModel: LocationViewModel

    private var permissionsManager: PermissionsManager = PermissionsManager(this)

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //initiate mapbox
        Mapbox.getInstance(activity?.baseContext!!, getString(R.string.token_mapbox))
        var view: View = inflater.inflate(R.layout.location, container, false)

        mapView = view.findViewById(R.id.map)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)

        //initiate database reference
        databaseReference = FirebaseDatabase.getInstance().reference

        databaseReference.child("home_profile").addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(
                    activity?.applicationContext!!,
                    "Error : $p0",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onDataChange(p0: DataSnapshot) {
                homeLocation =
                    LatLng(p0.child("lat").value as Double, p0.child("lng").value as Double)
                mapboxMap?.clear()
                mapboxMap?.addMarker(MarkerOptions().position(homeLocation).title("Home"))
            }

        })

        //get my location from viewmodel
        locationViewModel = ViewModelProviders.of(this).get(LocationViewModel::class.java)
        locationViewModel.getLocationData().observe(this, Observer {
            myLocation = LatLng(it.latitude, it.longitude)
        })

        //zoom between home and mylocation
        view.btAdjustLocation.setOnClickListener {
            if (homeLocation != null && myLocation != null) {

                val latLngBounds = LatLngBounds.Builder().include(
                    myLocation!!
                ).include(homeLocation!!).build()
                this.mapboxMap?.easeCamera(
                    CameraUpdateFactory.newLatLngBounds(
                        latLngBounds
                        , 200
                    ), 3000
                )
                getRoute(myLocation!!, homeLocation!!)
            }
        }

        return view
    }

    //getroute from device location to home
    private fun getRoute(myLocation: LatLng, homeLocation: LatLng) {
        val client = MapboxDirections.builder()
            .origin(Point.fromLngLat(myLocation.longitude, myLocation.latitude))
            .destination(Point.fromLngLat(homeLocation.longitude, homeLocation.latitude))
            .overview(DirectionsCriteria.OVERVIEW_FULL)
            .profile(DirectionsCriteria.PROFILE_DRIVING)
            .accessToken(getString(R.string.token_mapbox))
            .build()

        client?.enqueueCall(object : Callback<DirectionsResponse> {
            override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                Log.e("Error: ", t.message)
            }

            override fun onResponse(
                call: Call<DirectionsResponse>,
                response: Response<DirectionsResponse>
            ) {
                if (response.body() == null) {
                    Log.e(
                        "error",
                        "No routes found, make sure you set the right user and access token."
                    )
                    return
                } else if (response.body()!!.routes().size < 1) {
                    Log.e("error", "No routes found")
                    return
                }

                // Get the directions route
                val response = response

                val currentRoute = response.body()!!.routes()[0]

                drawNavigationPolylineRoute(currentRoute)
            }

        })
    }

    //draw route
    private fun drawNavigationPolylineRoute(route: DirectionsRoute) {
        mapboxMap!!.getStyle {
            var directionRouteFeatureList: ArrayList<Feature> = ArrayList<Feature>()
            var lineString: LineString =
                LineString.fromPolyline(route.geometry().toString(), PRECISION_6)
            var coordinates: List<Point> = lineString.coordinates()
            var i = 0
            while (i < coordinates.size) {
                directionRouteFeatureList.add(
                    Feature.fromGeometry(
                        LineString.fromLngLats(
                            coordinates
                        )
                    )
                )
                i++
            }
            dashedLineDirectionsFeatureCollection =
                FeatureCollection.fromFeatures(directionRouteFeatureList)
            var source: GeoJsonSource = style?.getSourceAs(ROUTE_SOURCE_ID)!!
            if (source != null) {
                source.setGeoJson(dashedLineDirectionsFeatureCollection)
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

    override fun onDestroyView() {
        super.onDestroyView()
        mapView?.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        mapboxMap.setStyle(Style.Builder().fromUri("mapbox://styles/mapbox/dark-v10")) {
            enableLocationComponent(it)
            initSource(it)
            initLayers(it)
            style = it
        }
    }


    /**
     * Add the route and marker sources to the map
     */
    private fun initSource(loadedMapStyle: Style) {
        loadedMapStyle.addSource(
            GeoJsonSource(
                ROUTE_SOURCE_ID,
                FeatureCollection.fromFeatures(arrayOf())
            )
        )
    }

    /**
     * Add the route and maker icon layers to the map
     */
    private fun initLayers(loadedMapStyle: Style) {
        val routeLayer = LineLayer(
            ROUTE_LAYER_ID,
            ROUTE_SOURCE_ID
        )
        // Add the LineLayer to the map. This layer will display the directions route.
        routeLayer.setProperties(
            PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
            PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
            PropertyFactory.lineWidth(5f),
            lineColor(resources.getColor(R.color.blueLogo))
        )
        loadedMapStyle.addLayer(routeLayer)
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(loadedMapStyle: Style) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(activity?.applicationContext!!)) {

            // Create and customize the LocationComponent's options
            val customLocationComponentOptions =
                LocationComponentOptions.builder(activity?.applicationContext!!)
                    .trackingGesturesManagement(true)
                    .accuracyColor(
                        ContextCompat.getColor(
                            activity?.applicationContext!!,
                            R.color.blueLogo
                        )
                    )
                    .build()

            val locationComponentActivationOptions =
                LocationComponentActivationOptions.builder(
                    activity?.applicationContext!!,
                    loadedMapStyle
                ).useDefaultLocationEngine(true)
                    .locationComponentOptions(customLocationComponentOptions)
                    .build()

            // Get an instance of the LocationComponent and then adjust its settings
            mapboxMap?.locationComponent?.apply {

                // Activate the LocationComponent with options
                activateLocationComponent(locationComponentActivationOptions)

                // Enable to make the LocationComponent visible
                isLocationComponentEnabled = true

                // Set the LocationComponent's camera mode
                cameraMode = CameraMode.TRACKING

                // Set the LocationComponent's render mode
                renderMode = RenderMode.COMPASS

//                myLocation = LatLng(lastKnownLocation?.latitude!!, lastKnownLocation?.longitude!!)

                mapboxMap!!.cameraPosition = CameraPosition.Builder()
                    .target(myLocation)
                    .zoom(14.0)
                    .tilt(20.0)
                    .build()
            }
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(activity)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onExplanationNeeded(permissionsToExplain: List<String>) {
        Toast.makeText(activity?.applicationContext!!, "", Toast.LENGTH_LONG)
            .show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            enableLocationComponent(mapboxMap?.style!!)
        } else {
            Toast.makeText(activity?.applicationContext!!, "", Toast.LENGTH_LONG)
                .show()
        }
    }
}