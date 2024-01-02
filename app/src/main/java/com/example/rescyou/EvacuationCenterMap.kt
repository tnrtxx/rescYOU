package com.example.rescyou

import android.annotation.SuppressLint
import android.content.ContentValues
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.rescyou.databinding.ActivityEvacuationCenterMapBinding
import com.example.rescyou.utils.GpsStatusListener
import com.example.rescyou.utils.TurnOnGps
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import com.vmadalin.easypermissions.EasyPermissions
import org.json.JSONObject

private const val TAG = "EvacuationCenterMap"

class EvacuationCenterMap : AppCompatActivity(), OnMapReadyCallback,
    EasyPermissions.PermissionCallbacks {

    private lateinit var binding: ActivityEvacuationCenterMapBinding
    private lateinit var turnOnGps: TurnOnGps
    private lateinit var mapFragment: SupportMapFragment
    private var isGpsStatusChanged: Boolean? = null
    private var isGpsTurnedOn: Boolean = false
    private lateinit var evacuationCenterMap: GoogleMap
    private var marker: Marker? = null

    private var name: String? = null
    private var status: String? = null
    private var occupants: String? = null
    private var address: String? = null
    private var latitude: Double? = 0.0
    private var longitude: Double? = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =
            ActivityEvacuationCenterMapBinding.inflate(layoutInflater)

        // Get the data from the intent
        name = intent.getStringExtra("name")
        status = intent.getStringExtra("status")
        occupants = intent.getStringExtra("occupants")
        address = intent.getStringExtra("address")
        latitude = intent.getDoubleExtra("latitude", 0.0)
        longitude = intent.getDoubleExtra("longitude", 0.0)

        binding.backButton.setOnClickListener {
            finish()
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment =
            supportFragmentManager.findFragmentById(R.id.viewInMapEvacuationCenterFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
        setContentView(binding.root)

        @SuppressLint("MissingPermission")
        if (hasLocationPermission()) {
            turnOnGps = TurnOnGps(this)
            val gpsStatusListener = GpsStatusListener(this)
            gpsStatusListener.observe(this) { isGpsOn ->
                if (isGpsStatusChanged == null) {
                    isGpsStatusChanged = isGpsOn
                    handleGpsStatus(isGpsOn)
                } else if (isGpsStatusChanged != isGpsOn) {
                    isGpsStatusChanged = isGpsOn
                    handleGpsStatus(isGpsOn)
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        evacuationCenterMap = googleMap
        evacuationCenterMap.uiSettings.isZoomControlsEnabled = true
        setupMap()
    }

    @SuppressLint("MissingPermission")
    private fun setupMap() {
        if (hasLocationPermission() && Home.currentLocation != null && isGpsTurnedOn) {
            evacuationCenterMap.isMyLocationEnabled = true
            displayEvacuationCenterWithDirection()
        } else {
            displayEvacuationCenterWithoutDirection()
        }
    }

    private fun handleGpsStatus(isGpsOn: Boolean) {
        if (!isGpsOn) {
            turnOnGps.startGps(resultLauncher)
        } else {
            isGpsTurnedOn = true
            setupMap()
        }
    }

    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { activityResult ->
        if (activityResult.resultCode == RESULT_OK) {
            Log.d(ContentValues.TAG, "Connected")
        } else if (activityResult.resultCode == RESULT_CANCELED) {
            Log.d(ContentValues.TAG, "Request is cancelled")
        }
    }

    private fun hasLocationPermission(): Boolean =
        EasyPermissions.hasPermissions(this, android.Manifest.permission.ACCESS_FINE_LOCATION)

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
    }

    private fun displayEvacuationCenterWithDirection() {
        evacuationCenterMap.uiSettings.isMapToolbarEnabled = false
        evacuationCenterMap.uiSettings.isMyLocationButtonEnabled = true

        // Find views within the MapView based on their tags
        val buttonLocation: View =
            mapFragment.view?.findViewWithTag("GoogleMapMyLocationButton") as View
        val buttonZoomIn: View =
            mapFragment.view?.findViewWithTag("GoogleMapZoomInButton") as View
        val layoutZoom = buttonZoomIn.parent as View

        // adjust location button layout params above the zoom layout
        val locationLayout = buttonLocation.layoutParams as RelativeLayout.LayoutParams
        locationLayout.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
        locationLayout.addRule(RelativeLayout.ABOVE, layoutZoom.id)


        val latLngOrigin =
            LatLng(Home.currentLocation!!.latitude, Home.currentLocation!!.longitude)
        val latLngDestination = LatLng(latitude!!, longitude!!)

        evacuationCenterMap.addMarker(
            MarkerOptions().position(latLngDestination).title(name)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .snippet(
                    "Status: $status"
                )
        )

        evacuationCenterMap.setOnMapLoadedCallback {
            val boundsBuilder = LatLngBounds.builder()
                .include(latLngOrigin)
                .include(latLngDestination)
            val bounds = boundsBuilder.build()
            evacuationCenterMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 300))
        }

        val path: MutableList<List<LatLng>> = ArrayList()
        val urlDirections =
            "https://maps.googleapis.com/maps/api/directions/json?origin=${latLngOrigin.latitude},${latLngOrigin.longitude}&destination=${latLngDestination.latitude},${latLngDestination.longitude}&key=${Constants.GOOGLE_MAPS_API_KEY}"
        val directionsRequest = object :
            StringRequest(
                Method.GET,
                urlDirections,
                Response.Listener { response ->
                    val jsonResponse = JSONObject(response)
                    val routes = jsonResponse.getJSONArray("routes")
                    val legs = routes.getJSONObject(0).getJSONArray("legs")
                    val steps = legs.getJSONObject(0).getJSONArray("steps")
                    for (i in 0 until steps.length()) {
                        val points =
                            steps.getJSONObject(i).getJSONObject("polyline")
                                .getString("points")
                        path.add(PolyUtil.decode(points))
                    }
                    for (i in 0 until path.size) {
                        this.evacuationCenterMap.addPolyline(
                            PolylineOptions().addAll(path[i]).color(Color.BLUE)
                        )
                    }
                },
                Response.ErrorListener { _ ->
                    Log.d(TAG, Response.ErrorListener::class.java.toString())
                }) {}

        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(directionsRequest)
    }

    private fun displayEvacuationCenterWithoutDirection() {
        marker = evacuationCenterMap.addMarker(
            MarkerOptions()
                .position(LatLng(latitude!!, longitude!!))
                .title(name)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .snippet(
                    "Status: $status"
                )
        )

        val cameraPosition = CameraPosition.Builder()
            .target(LatLng(latitude!!, longitude!!))
            .zoom(Constants.INIT_MAP_LEVEL)
            .build()

        evacuationCenterMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }


}