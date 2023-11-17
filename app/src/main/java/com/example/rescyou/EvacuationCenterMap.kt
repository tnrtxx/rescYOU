package com.example.rescyou

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.rescyou.databinding.ActivityEvacuationCenterMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class EvacuationCenterMap : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityEvacuationCenterMapBinding
    private lateinit var mMap: GoogleMap
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEvacuationCenterMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get latitude and longitude from intent
        latitude = intent.getDoubleExtra("latitude", 0.0)
        longitude = intent.getDoubleExtra("longitude", 0.0)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker and move the camera to the specified location
        val location = LatLng(latitude, longitude)
        mMap.addMarker(MarkerOptions().position(location).title("Evacuation Center"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, Constants.USER_LOCATION_MAP_LEVEL))

    }
}