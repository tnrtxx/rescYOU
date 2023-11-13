package com.example.rescyou

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.rescyou.databinding.ActivityHomeBinding
import com.example.rescyou.utils.ConnectionLiveData
import com.example.rescyou.utils.GpsStatusListener
import com.example.rescyou.utils.TurnOnGps
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog


private const val TAG = "Home"

class Home : AppCompatActivity(), OnMapReadyCallback, EasyPermissions.PermissionCallbacks {

    companion object {
        /* To access the currentLocation variable from other activities, you can use Home.currentLocation wherever you need it in those activities.
         * Example:
         *          val currentLocation = Home.currentLocation
         *          if (currentLocation != null) {
         *          // Use the currentLocation
         *          } else {
         *          // Handle the case where the currentLocation is null
         *          }
         */

        var currentLocation: LatLng? = null
        lateinit var googleMap: GoogleMap
    }

    private lateinit var binding: ActivityHomeBinding
    private lateinit var turnOnGps: TurnOnGps
    private lateinit var dialog: AlertDialog
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var isGpsStatusChanged: Boolean? = null
    private lateinit var connectionLiveData: ConnectionLiveData
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    //for the markers
    private lateinit var pinList: ArrayList<Pins>
    private lateinit var markerList: ArrayList<Marker>

    private val PIN_LOCATION_REQUEST_CODE = 123 // Use any unique request code

    private val database = FirebaseDatabase.getInstance("https://rescyou-57570-default-rtdb.asia-southeast1.firebasedatabase.app/")


    /** TODO:
     * LOGIC-RELATED
     * GPS - irequire kay user
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        connectionLiveData = ConnectionLiveData(this)

        val isNetworkAvailable = connectionLiveData.observe(this) {
            if (!it) {
                if (!dialog.isShowing) {
                    dialog.show()
                }
            } else {
                if (dialog.isShowing || binding.offlineModeTextView.isVisible) {
                    dialog.hide()
                    hideOfflineModeView()
                }
            }
        }

        dialog = MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogRounded)
            .setTitle("You are currently using the Map Feature in offline mode")
            .setMessage("Please connect to the internet to keep receiving real-time updates.")
            .setPositiveButton("Dismiss") { _, _ ->
                // Call showOfflineModeView() here
                showOfflineModeView()
            }
            .setCancelable(false)
            .create()

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

        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setContentView(binding.root)


        //PIN MY LOCATION BUTTON
        binding.pinMyLocationButton.setOnClickListener {
            val intent = Intent(this, PinMyLocation::class.java)
            startActivity(intent)
        }

        // Initialize and assign variable
        var bottomNavigationView: BottomNavigationView = binding.bottomNavView
        binding.bottomNavView.selectedItemId = R.id.home

        // Initialize and assign variable
        binding.bottomNavView.selectedItemId = R.id.home
//        Toast.makeText(applicationContext, selectedItem.toString(), Toast.LENGTH_SHORT).show()

        bottomNavigationView.setOnNavigationItemSelectedListener(navBarWhenClicked)

    }

    private fun handleGpsStatus(isGpsOn: Boolean) {
        if (!isGpsOn) {
            turnOnGps.startGps(resultLauncher)
        }
        getCurrentLocation()
    }

    private fun showOfflineModeView() {
        binding.offlineModeTextView.visibility = View.VISIBLE
        Log.d("DEBUG", "showOfflineModeView")
    }

    private fun hideOfflineModeView() {
        binding.offlineModeTextView.visibility = View.GONE
        Log.d("DEBUG", "hideOfflineModeView")
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        getPinLists()
        googleMap.uiSettings.isZoomControlsEnabled = true




        if (hasLocationPermission()) {
            googleMap.uiSettings.isMyLocationButtonEnabled = true
            googleMap.isMyLocationEnabled = true

            // Modify the layout to adjust the location button's position
            val mapView = mapFragment.requireView().findViewById<View>(
                Constants.CURRENT_LOCATION_BUTTON_PARENT_ID
            ).parent!! as View

            // Get map views
            val buttonLocation: View = mapView.findViewWithTag("GoogleMapMyLocationButton")
            val buttonZoomIn: View = mapView.findViewWithTag("GoogleMapZoomInButton")
            val layoutZoom = buttonZoomIn.parent as View

            // adjust location button layout params above the zoom layout
            val locationLayout = buttonLocation.layoutParams as RelativeLayout.LayoutParams
            locationLayout.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
            locationLayout.addRule(RelativeLayout.ABOVE, layoutZoom.id)

            addMarkerIfLocationAvailable()


            googleMap.setOnMyLocationButtonClickListener {
                // Handle the click event by obtaining the user's location and zooming the map
                fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val location = task.result
                        if (location != null) {
                            val userLatLng = LatLng(location.latitude, location.longitude)
                            googleMap.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    userLatLng,
                                    Constants.USER_LOCATION_MAP_LEVEL
                                )
                            )
                        } else {
                            Toast.makeText(
                                this,
                                "Location unavailable. Please enable your GPS location.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        if (task.exception != null) {
                            Toast.makeText(
                                this,
                                "Failed to get location: ${task.exception!!.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                true // Return true to indicate that you've handled the click event
            }

            googleMap.setOnMyLocationClickListener {
                // Handle the click event by obtaining the user's location and zooming the map
                fusedLocationProviderClient.lastLocation.addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to get location: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }.addOnSuccessListener { location ->
                    val userLatLng = LatLng(location.latitude, location.longitude)
                    val zoomLevel = Constants.USER_LOCATION_MAP_LEVEL
                    googleMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(userLatLng, zoomLevel)
                    )
                }
            }
        } else {
            requestLocationPermission()
        }
    }

    private fun addMarkerIfLocationAvailable() {
        // Check if currentLocation is not null and add a marker
        if (currentLocation != null) {
            val cameraUpdate =
                CameraUpdateFactory.newLatLngZoom(currentLocation!!, 18F)
            googleMap.moveCamera(cameraUpdate)
        }else{
            // Set the camera to the center of Canlubang
            val canlubangLatLng = LatLng(14.1856, 121.0536) // Coordinates for Canlubang
            val cameraUpdate =
                CameraUpdateFactory.newLatLngZoom(canlubangLatLng, Constants.INIT_MAP_LEVEL)
            googleMap.moveCamera(cameraUpdate)
        }
    }

    private fun removeAllMarkers() {
        for (marker in markerList) {
            marker.remove()
        }

        // Clear the list of markers after removing them
        markerList.clear()
    }


    private fun getPinLists() {
        // Initialize markerList here
        markerList = ArrayList()

        // Initialize pinList here
        pinList = ArrayList()

        database.reference.child("Pins").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                removeAllMarkers() // Now, this should work without an UninitializedPropertyAccessException

                // Clear the pinList before adding new data
                pinList.clear()

                for (postSnapshot in snapshot.children) {
                    val pin = postSnapshot.getValue(Pins::class.java)
                    pin?.let {
                        pinList.add(it)
                        val markerOptions = MarkerOptions().position(
                            LatLng(
                                it.latitude.toDouble(),
                                it.longitude.toDouble()
                            )
                        ).title(it.pinName + " needs help.")

                        googleMap.addMarker(markerOptions)?.let { markerList.add(it) }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error
                Log.e(TAG, "Error getting Pins from Firebase: ${error.message}")
            }
        })
    }





    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { activityResult ->
        if (activityResult.resultCode == RESULT_OK) {
            Log.d(TAG, "Connected")
        } else if (activityResult.resultCode == RESULT_CANCELED) {
            Log.d(TAG, "Request is cancelled")
        }
    }


    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setMinUpdateIntervalMillis(3000)
            .setMinUpdateDistanceMeters(5f)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)

                val lastLocation = locationResult.lastLocation
                if (lastLocation != null) {
                    val userLatLng = LatLng(lastLocation.latitude, lastLocation.longitude)

                    // Set the current location
                    currentLocation = userLatLng

                    // Log the new position
                    Log.d("New Position", userLatLng.toString())

//                    // Create a camera update to move to the new position with a specific zoom level
//                    val cameraUpdate =
//                        CameraUpdateFactory.newLatLngZoom(userLatLng, Constants.MAP_LEVEL)
//
//                    // Move the Google Map camera to the new position
//                    googleMap.moveCamera(cameraUpdate)
                }
            }
        }
        fusedLocationProviderClient = LocationServices
            .getFusedLocationProviderClient(this)
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest, locationCallback, Looper.myLooper()
        )
    }

    private fun hasLocationPermission(): Boolean =
        EasyPermissions.hasPermissions(this, android.Manifest.permission.ACCESS_FINE_LOCATION)

    private fun requestLocationPermission() {
        EasyPermissions.requestPermissions(
            this,
            "This application requires location permission to work properly.",
            Constants.PERMISSION_REQUEST_CODE_ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            SettingsDialog.Builder(this).build().show()
        } else {
            requestLocationPermission()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        restartApp()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    private fun restartApp() {
        val intent = Intent(applicationContext, Home::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }

    //NAV BAR
    private val navBarWhenClicked = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.home -> {
                val intent = Intent(this, Home::class.java)
                startActivity(intent)
                return@OnNavigationItemSelectedListener true
            }

            R.id.tools -> {
                Toast.makeText(applicationContext, "tools", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, Tools::class.java)
                startActivity(intent)
                return@OnNavigationItemSelectedListener true

            }

            R.id.info -> {
                Toast.makeText(applicationContext, "information", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, Information::class.java)
                startActivity(intent)
                return@OnNavigationItemSelectedListener true
            }

            R.id.profile -> {
                Toast.makeText(applicationContext, "profile", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, Profile::class.java)
                startActivity(intent)
                return@OnNavigationItemSelectedListener true


            }
        }
        return@OnNavigationItemSelectedListener false
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    // Handle the result from PinMyLocation activity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PIN_LOCATION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val latitude = data?.getDoubleExtra("latitude", 0.0)
            val longitude = data?.getDoubleExtra("longitude", 0.0)

            // Check if latitude and longitude are not null, then add a marker
            if (latitude != null && longitude != null) {
                val locationLatLng = LatLng(latitude, longitude)

                // Add a marker to the map
                googleMap.addMarker(MarkerOptions().position(locationLatLng).title("Marker Title"))

                // Move the camera to the new position
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationLatLng, 15F))
            }

        }
    }
}