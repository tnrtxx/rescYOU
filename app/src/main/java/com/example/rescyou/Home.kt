package com.example.rescyou

// Android-related imports
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast

// Androidx-related imports
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible

// Third-party library imports
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

// Custom package imports
import com.example.rescyou.utils.GpsStatusListener
import com.example.rescyou.utils.NetworkManager
import com.example.rescyou.utils.TurnOnGps
import com.example.rescyou.databinding.ActivityHomeBinding
import com.google.android.gms.auth.api.signin.GoogleSignInClient


class Home : AppCompatActivity(), OnMapReadyCallback, EasyPermissions.PermissionCallbacks {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var turnOnGps: TurnOnGps

    private lateinit var dialog: AlertDialog

    private lateinit var googleMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment

    //Location
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var isGpsStatusChanged: Boolean? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient


    /** TODO:
     * UI RELATED
     * Map zoom controls change color, add shadow
     * Map current location button change color, add shadow
     *
     * LOGIC-RELATED
     * Oflline mode - nag aappear ket may internet naman
     * GPS - irequire kay user
     * Hindi makapag zoom kapag nagra route si user
     */
    override fun onDestroy() {
        super.onDestroy()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)

        dialog = MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogRounded)
            .setTitle("You are currently using the Map Feature in offline mode")
            .setMessage("Please connect to the internet to keep receiving real-time updates.")
            .setPositiveButton("Ok") { _, _ ->
                // Call showOfflineModeView() here
                showOfflineModeView()
            }
            .setCancelable(false)
            .create()

        val networkManager = NetworkManager(this)
        networkManager.observe(this) {
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

            // Modify the layout to adjust the location button's position
            val view = mapFragment.requireView().findViewById<View>(
                Constants.CURRENT_LOCATION_BUTTON_PARENT_ID).parent!! as View
            val locationButton = view.findViewById<View>(Constants.CURRENT_LOCATION_BUTTON_ID)
            val params = locationButton.layoutParams as RelativeLayout.LayoutParams
            params.addRule(RelativeLayout.ALIGN_BOTTOM, 0)
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
            params.bottomMargin = 50

        } else {
            requestLocationPermission()
        }
    }

    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { activityResult ->
        if (activityResult.resultCode == RESULT_OK) {

        } else if (activityResult.resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "Request is cancelled", Toast.LENGTH_SHORT).show()
        }
    }


    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setMinUpdateIntervalMillis(3000)
            .setMinUpdateDistanceMeters(5f)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)

                val newPos = p0.lastLocation?.let {
                    LatLng(
                        it.latitude,
                        it.longitude
                    )
                }
                Log.d("New Position", newPos.toString())
                newPos?.let { CameraUpdateFactory.newLatLngZoom(it, Constants.MAP_LEVEL) }
                    ?.let { googleMap.moveCamera(it) }
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

    // Inside onPermissionsGranted() method
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


    private fun pinLocation() {

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
}








