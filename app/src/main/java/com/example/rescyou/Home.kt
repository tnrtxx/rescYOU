package com.example.rescyou

import android.annotation.SuppressLint
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
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
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

    /** TODO:
     * LOGIC-RELATED
     * GPS - irequire kay user
     * Hindi makapag zoom kapag nagra route si user
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

        // Initialize and assign variable
        val selectedItem = bottomNavigationView.selectedItemId
        Toast.makeText(applicationContext, selectedItem.toString(), Toast.LENGTH_SHORT).show()

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

            // Handle click on the "My Location" button
            googleMap.setOnMyLocationClickListener {
                fusedLocationProviderClient.lastLocation.addOnFailureListener { e ->
                    Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                }.addOnSuccessListener { location ->
                    val userLatLng = LatLng(location.latitude, location.longitude)
                    googleMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            userLatLng,
                            Constants.MAP_LEVEL
                        )
                    )
                }
            }


        } else {
            requestLocationPermission()
        }
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

                    // Create a camera update to move to the new position with a specific zoom level
                    val cameraUpdate =
                        CameraUpdateFactory.newLatLngZoom(userLatLng, Constants.MAP_LEVEL)

                    // Move the Google Map camera to the new position
                    googleMap.moveCamera(cameraUpdate)
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

    private val navBarWhenClicked = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.home -> {
                return@OnNavigationItemSelectedListener false
            }

            R.id.tools -> {
                Toast.makeText(applicationContext, "tools", Toast.LENGTH_SHORT).show()
                return@OnNavigationItemSelectedListener true
            }

            R.id.info -> {
                Toast.makeText(applicationContext, "info", Toast.LENGTH_SHORT).show()

                return@OnNavigationItemSelectedListener true
            }

            R.id.profile -> {
                Toast.makeText(applicationContext, "profile", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, Profile::class.java)
                startActivity(intent)

                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }
}








