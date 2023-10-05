package com.example.rescyou

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.rescyou.databinding.ActivityHomeBinding
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog


class Home : AppCompatActivity(), OnMapReadyCallback, EasyPermissions.PermissionCallbacks {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient


    companion object {
        const val MAP_LEVEL = 16f
        const val PERMISSION_REQUEST_CODE_ACCESS_FINE_LOCATION = 900
        const val PERMISSION_REQUEST_CODE_ENABLE_GPS = 901
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)




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

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true

    }

    private fun hasLocationPermission(): Boolean =
        EasyPermissions.hasPermissions(this, android.Manifest.permission.ACCESS_FINE_LOCATION)

    private fun requestLocationPermission() {
        EasyPermissions.requestPermissions(
            this,
            "This application requires location permission to work properly.",
            PERMISSION_REQUEST_CODE_ACCESS_FINE_LOCATION,
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
        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    private fun pinLocation() {

    }


    private fun getCurrentLocation() {

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








