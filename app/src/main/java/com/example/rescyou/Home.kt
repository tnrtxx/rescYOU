package com.example.rescyou

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.rescyou.databinding.ActivityHomeBinding
import com.example.rescyou.utils.ConnectionLiveData
import com.example.rescyou.utils.FirebaseUtil
import com.example.rescyou.utils.FirebaseUtil.currentUserId
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
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException


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

        //FOR THE NOTIFICATIONS
        private const val FCM_API_KEY = "AAAA6jJJnJg:APA91bG-D1uEV29YYYCsxUtGQPoNpMUVWTt9V1Nq8q5mGibbF45F7ukPYkKpqgZ34zbW5wcav3GtXN_9zLwydF7U6-i956Sz9aWyBU5MAQYLaYe4MP6TYsvWXcjMKa2T1pqmeOgEtfiD"
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

    private var otherUser: UserModel? = null



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

        // Initialize Firebase
        FirebaseUtil.initializeFirebase(this)


        //PIN MY LOCATION BUTTON
        binding.pinMyLocationButton.setOnClickListener {
            // ----> Create a condition na di pwede mag-pin ng location kapag di pa resolved yung current pin
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

        googleMap.setOnMarkerClickListener { marker ->
            googleMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        marker.position.latitude,
                        marker.position.longitude
                    ), 14F
                )
            )

            val pins = pinList.find { it.pinId == marker.title }
            pins?.let { showDialog(it)

//                fetchOtherUserData(it.pinUserId)
            }



//            // Assuming you associate user data with the marker (e.g., marker.tag)
//            val otherUserId = marker.tag as? String
//            if (otherUserId != null) {
//                // Fetch otherUser data
//                fetchOtherUserData(otherUserId)
//            } else {
//                Log.e(TAG, "Failed to obtain otherUserId from marker.")
//            }

            true
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

//                for (postSnapshot in snapshot.children) {
//                    val pin = postSnapshot.getValue(Pins::class.java)
//                    pin?.let {
//                        pinList.add(it)
//                        val markerOptions = MarkerOptions().position(
//                            LatLng(
//                                it.latitude.toDouble(),
//                                it.longitude.toDouble()
//                            )
//                        ).title(it.pinId)
//
//                      googleMap.addMarker(markerOptions)?.let { markerList.add(it) }
//                          Marker.tag = it.pinUserId
//                    }
//
//                }
                for (postSnapshot in snapshot.children) {
                    val pin = postSnapshot.getValue(Pins::class.java)
                    pin?.let {
                        pinList.add(it)
                        val markerOptions = MarkerOptions().position(
                            LatLng(
                                it.latitude.toDouble(),
                                it.longitude.toDouble()
                            )
                        ).title(it.pinId)

                        val marker = googleMap.addMarker(markerOptions)
                        markerList.add(marker!!)
                        marker.tag = it.pinUserId

                        // Fetch otherUser data here

                        fetchOtherUserData(it.pinUserId)
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
                }
            }
        }
        fusedLocationProviderClient = LocationServices
            .getFusedLocationProviderClient(this)
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest, locationCallback, Looper.myLooper()
        )
    }

    //FOR THE DIALOGS

    private fun addNewImageview(data: Uri?, container: LinearLayout, counter: Int) {
        val imageView = ShapeableImageView(this)

        val layoutParams = LinearLayout.LayoutParams(
            resources.getDimensionPixelSize(R.dimen.new_img_view_large_size),
            resources.getDimensionPixelSize(R.dimen.new_img_view_large_size)
        )
        layoutParams.marginEnd = 8.dpToPx()

        imageView.layoutParams = layoutParams
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP

        Glide.with(this)
            .load(data)
            .into(imageView)

        container.addView(imageView, counter)
    }

    private fun Int.dpToPx(): Int {
        val density = resources.displayMetrics.density
        return (this * density).toInt()
    }

    private fun showDialog(pins: Pins) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.activity_view_pin)

        if (!isFinishing) {
            dialog.show()
        }
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.rgb(241, 242, 242)))
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)

        val pinnedByName = dialog.findViewById<TextView>(R.id.viewPin_pinnedByName)
        val ratingsSituation = dialog.findViewById<TextView>(R.id.viewPin_ratingsCurrentSituation)
        val disasterType = dialog.findViewById<TextView>(R.id.viewPin_disasterType)
        val currentSitio = dialog.findViewById<TextView>(R.id.viewPin_currentSitio)
        val currentSituation = dialog.findViewById<TextView>(R.id.viewPin_currentSituation)

        otherUser?.pinUserId =pins.pinUserId

        pinnedByName.text = pins.pinName
        ratingsSituation.text = pins.rate
        disasterType.text = pins.disasterType
        currentSitio.text = pins.sitio
        currentSituation.text = pins.description


        var attachmentCouner = 0
        pins.attachmentList.forEach {
            addNewImageview(
                it.toUri(),
                dialog.findViewById<LinearLayout>(R.id.attachmentContainer),
                attachmentCouner
            )
            attachmentCouner += 1
        }

        //BACK BUTTON
        val backButtonLayout = dialog.findViewById<RelativeLayout>(R.id.back_layout)
        backButtonLayout.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
        }
        //CANCEL BUTTON
        val cancelButton = dialog.findViewById<Button>(R.id.cancelPinButton)
        cancelButton.setOnClickListener {
            val alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder.setTitle("Cancel Confirmation")
            alertDialogBuilder.setMessage("Are you sure you want to cancel?")
            alertDialogBuilder.setPositiveButton("Yes") { dialogInterface, _ ->
                // Handle "Yes" button click, for example, navigate back or finish the activity
                dialogInterface.dismiss()
                finish()

                val intent = Intent(this, Home::class.java)
                startActivity(intent)
            }
            alertDialogBuilder.setNegativeButton("No") { dialogInterface, _ ->
                // Handle "No" button click, dismiss the dialog
                dialogInterface.dismiss()
            }

            val alertDialog: AlertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }
        val sendHelpButton = dialog.findViewById<Button>(R.id.sendHelpButton)
        sendHelpButton.setOnClickListener {
            val alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder.setTitle("Confirm Pinning")
            alertDialogBuilder.setMessage("Are you sure you want to pin this location?")
            alertDialogBuilder.setPositiveButton("Yes") { _, _ ->
                val senderUserId = currentUserId()
                if (senderUserId != null) {
                    sendHelpButtonClicked(pins.pinUserId, senderUserId, pins.pinId)
                    Toast.makeText(this, "receiver" + pins.pinUserId, Toast.LENGTH_SHORT).show()
                    Toast.makeText(this, "sender" + senderUserId, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to get sender user ID.", Toast.LENGTH_SHORT).show()
                }
            }
            alertDialogBuilder.setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }

            val alertDialog: AlertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }
    }

    //SENDING HELP BUTTON
    private fun fetchOtherUserData(pinUserId: String) {
        val userRef = database.reference.child("Users").child(pinUserId)
//        Toast.makeText(this, pinUserId, Toast.LENGTH_SHORT).show()

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Assuming UserModel has a constructor that takes a DataSnapshot
                otherUser = snapshot.getValue(UserModel::class.java)
                // Assign pinUserId directly to otherUser
                otherUser?.pinUserId = pinUserId

                // Fetch FCM token of the receiver
                otherUser?.fcmToken = snapshot.child("fcmToken").getValue(String::class.java).toString()

//                // Save the displayName from Firebase to otherUser's pinRescuerName
//                otherUser?.pinRescuerName = snapshot.child("displayName").getValue(String::class.java).toString()





                // Check if otherUser is not null
                if (otherUser != null) {
                    // Now, otherUser is initialized with the fetched data
                    Log.d(TAG, "otherUser data: $otherUser")
                } else {
                    Log.e(TAG, "Failed to fetch otherUser data.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error
                Log.e(TAG, "Error fetching otherUser data: ${error.message}")
            }
        })
    }


    private fun sendHelpNotification(
        senderUserId: String,
        senderDisplayName: String,
        pinId: String
    ){
                Toast.makeText(this, "pls" + senderDisplayName, Toast.LENGTH_SHORT).show()

        try {
            val jsonObject = JSONObject().apply {
                put("to", otherUser?.fcmToken ?: "")
                put("notification", JSONObject().apply {
                    put("title", "Help is on the way.")
                    put("body", "Someone wants to send you a help request.")
                })
                put("data", JSONObject().apply {
                    put("userId", senderUserId)
                    put("rescuerName", senderDisplayName)
                    put("pinId", pinId)// Include the rescuerName in the data payload
                })
            }

            if (otherUser?.fcmToken.isNullOrBlank()) {
                Toast.makeText(this, otherUser?.fcmToken, Toast.LENGTH_SHORT).show()

                Log.e(TAG, "Receiver FCM token is null or empty.")
                Toast.makeText(this, "Failed to send help notification: Receiver FCM token is null or empty.", Toast.LENGTH_SHORT).show()
                return
            }
            Toast.makeText(this, otherUser?.fcmToken, Toast.LENGTH_SHORT).show()

            callApi(jsonObject)
        } catch (e: Exception) {
            Log.e(TAG, "Error building notification payload: ${e.message}")
            Toast.makeText(this, "Failed to send help notification: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun callApi(jsonObject: JSONObject) {
        val JSON = MediaType.get("application/json; charset=utf-8")
        val client = OkHttpClient()
        val url = "https://fcm.googleapis.com/fcm/send"
        val body: RequestBody = RequestBody.create(JSON, jsonObject.toString()) // Swap the parameters
        val request = Request.Builder()
            .url(url)
            .post(body)
            .header("Authorization", "key=AAAA6jJJnJg:APA91bG-D1uEV29YYYCsxUtGQPoNpMUVWTt9V1Nq8q5mGibbF45F7ukPYkKpqgZ34zbW5wcav3GtXN_9zLwydF7U6-i956Sz9aWyBU5MAQYLaYe4MP6TYsvWXcjMKa2T1pqmeOgEtfiD")
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
            }
        })
    }


    // Inside sendHelpButton.setOnClickListener
    private fun sendHelpButtonClicked(
        receiverUserId: String,
        senderUserId: String,
        pinId: String
    ) {
        fetchOtherUserData(receiverUserId)
        fetchSenderUserData(senderUserId,receiverUserId, pinId)
    }


    private fun fetchSenderUserData(senderUserId: String, receiverUserId: String, pinId: String) {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(senderUserId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val senderUser = snapshot.getValue(UserModel::class.java)

                // Save the displayName from Firebase to senderUser's displayName
                val senderDisplayName = snapshot.child("displayName").getValue(String::class.java).toString()
                senderUser?.pinRescuerName = senderDisplayName

                // After fetching the otherUser data
                val sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("rescuerName", senderDisplayName)
                editor.putString("pinId", pinId)
                editor.apply()


                // Now, senderDisplayName contains the display name of the sender
                Log.d(TAG, "Sender display name: $senderDisplayName")
                sendHelpNotification(receiverUserId, senderDisplayName, pinId)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error
                Log.e(TAG, "Error fetching sender user data: ${error.message}")
            }
        })
    }

    //PERMISSIONS

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