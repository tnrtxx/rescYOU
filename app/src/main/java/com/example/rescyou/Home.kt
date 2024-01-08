package com.example.rescyou

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.Spinner
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
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.maps.android.clustering.ClusterManager
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.CountDownLatch


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
    private lateinit var dbRef: DatabaseReference

    private var otherUser: UserModel? = null

    var pinRescuer: String? = null
    var pinId: String? = null
    private var pinIds = mutableListOf<String>()
    var pinUserId: String? = null
    var resolved: String? = null


    // FOR RATING YOUR SITUATION
    // for selected and unselected drawable
    private var selectedDrawable: Drawable? = null // Drawable for selected state
    private var unselectedDrawable: Drawable? = null // Drawable for unselected state


    // radio group
    private var radioGroup: RadioGroup? = null

    // radio button
    private var selectedRateName: String = ""

    private var mildRadioButton: RadioButton? = null
    private var moderateRadioButton: RadioButton? = null
    private var severeRadioButton: RadioButton? = null
    private var criticalRadioButton: RadioButton? = null
    private var catastrophicRadioButton: RadioButton? = null

    //for type of disaster spinner
    private var selectedItemValue: String = ""
    private var selectedSitioValue: String = ""

    //for time
    private lateinit var formattedDate: String
    private lateinit var formattedTime: String

    //for pins
    private lateinit var pin: Pins

    //checking if empty
    private var isEmpty: String = "false"
    private lateinit var descriptionInputEditText: TextInputEditText

    //for a progress dialog
    private lateinit var progressDialog: ProgressDialog

    private var isDialogShowing = false  // Flag to check if any dialog is showing

    private var currentDialog: Dialog? = null



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

        if (!hasLocationPermission()) {
            requestLocationPermission()
        }

        if (!hasNotificationPermission()) {
            requestNotificationPermission()
        }



        //PIN MY LOCATION BUTTON

        binding.pinMyLocationButton.setOnClickListener {

            //DISABLE THE BUTTON IF NO INTERNET CONNECTION
            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo = connectivityManager.activeNetworkInfo


            // If the device is offline, disable the button and show an alert dialog
            if (activeNetworkInfo == null || !activeNetworkInfo.isConnected) {
                AlertDialog.Builder(this)
                    .setTitle("No Internet Connection")
                    .setMessage("Internet/WiFi is needed to access this feature.")
                    .setPositiveButton("OK", null)
                    .show()
            } else {

                if(pinIds.isEmpty()){
                    val intent = Intent(this, PinMyLocation::class.java)
                    startActivity(intent)
                }else{
                    val database = FirebaseDatabase.getInstance("https://rescyou-57570-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    val myRef = database.getReference("Pins")

                    var allPinsResolved = true
                    val latch = CountDownLatch(pinIds.size)

                    for (pinId in pinIds) {
                        myRef.child(pinId).addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                val isResolved = dataSnapshot.child("resolved").getValue(String::class.java)

                                if(isResolved == "false"){
                                    allPinsResolved = false
                                    Toast.makeText(this@Home, "You still have existing pin.", Toast.LENGTH_SHORT).show()
                                }

                                latch.countDown()
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                // Handle possible errors.
                                latch.countDown()
                            }
                        })
                    }

                    Thread {
                        latch.await()

                        runOnUiThread {
                            if (allPinsResolved) {
                                val intent = Intent(this@Home, PinMyLocation::class.java)
                                startActivity(intent)
                            }
                        }
                    }.start()
                }
            }

        }


        // Initialize and assign variable
        var bottomNavigationView: BottomNavigationView = binding.bottomNavView
        binding.bottomNavView.selectedItemId = R.id.home

        // Initialize and assign variable
        binding.bottomNavView.selectedItemId = R.id.home

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

        //GET THE PINID
        val database = FirebaseDatabase.getInstance("https://rescyou-57570-default-rtdb.asia-southeast1.firebasedatabase.app/")
        val myRef = database.getReference("Pins")

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (postSnapshot in dataSnapshot.children) {
                    val pinId = postSnapshot.child("pinId").getValue(String::class.java).toString()
                    val pinUserId = postSnapshot.child("pinUserId").getValue(String::class.java)

                    if(pinUserId == currentUserId()){
                        // Save pinId somewhere, e.g., in a list
                        pinIds.add(pinId)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
            }
        })




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
            pins?.let {
                val database = FirebaseDatabase.getInstance("https://rescyou-57570-default-rtdb.asia-southeast1.firebasedatabase.app/")
                val myRef = database.getReference("Pins").child(it.pinId)


                myRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        pinRescuer = dataSnapshot.child("pinRescuer").getValue(String::class.java).toString()
                        val pinUserId = dataSnapshot.child("pinUserId").getValue(String::class.java)

                        if(pinUserId == currentUserId()){
                            pinId= dataSnapshot.child("pinId").getValue(String::class.java).toString()
                            showMyDialog(pinId!!)
                        } else {
                            showDialog(it)
                        }

                        // Add the code that depends on pinId here
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle possible errors.
                    }
                })

//                fetchOtherUserData(it.pinUserId)
            }


            true
        }

    }


    override fun onBackPressed() {
        // Check if any dialog is showing
        if (isDialogShowing) {
            // Redirect to your desired activity
            val intent = Intent(this@Home, Home::class.java)
            startActivity(intent)
            isDialogShowing = false  // Reset the flag
        } else {
            super.onBackPressed()  // Default behavior if no dialog is showing
        }
    }


    private fun showMyDialog(pinId: String) {

        isDialogShowing = true  // Set the flag to true when the dialog is about to show

        // Dismiss the current dialog if it's showing
        currentDialog?.dismiss()


        // Get a reference to the "Pins" node in the database
        val pinsRef = database.getReference("Pins")

        // Attach a listener to read the data at the "Pins" reference
        pinsRef.child(pinId).addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("SuspiciousIndentation")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Pin data from snapshot
                val pinData = dataSnapshot.getValue(MyPinModel::class.java)
                if (pinData != null) {
                    val dialog = Dialog(this@Home)
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    dialog.setContentView(R.layout.activity_my_pin)


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


                    // Show the details of the pin
                    val helperNameTextView = dialog.findViewById<TextView>(R.id.viewPin_sendHelpName)
                    val pinnedByName = dialog.findViewById<TextView>(R.id.viewPin_pinnedByName)
                    val ratingsSituation = dialog.findViewById<TextView>(R.id.viewPin_ratingsCurrentSituation)
                    val disasterType = dialog.findViewById<TextView>(R.id.viewPin_disasterType)
                    val currentSitio = dialog.findViewById<TextView>(R.id.viewPin_currentSitio)
                    val currentSituation = dialog.findViewById<TextView>(R.id.viewPin_currentSituation)
                    val dateAndTime: TextView = dialog.findViewById(R.id.dateAndTime)


                    //for the display namme of the one who pinned
                    val usersRef = database.getReference("Users")

                    val database = FirebaseDatabase.getInstance("https://rescyou-57570-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    val myRef = database.getReference("Pins").child(pinId)


                    myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val pinUserId = dataSnapshot.child("pinUserId").getValue(String::class.java)

                            usersRef.child(pinUserId.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    val displayName = dataSnapshot.child("displayName").getValue(String::class.java)

                                    if (displayName != null) {
                                        val pinsRef = database.getReference("Pins").child(pinId)
                                        pinsRef.child("pinName").setValue(displayName)
                                    }
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    // Handle possible errors.
                                }
                            })



                            pinRescuer =
                                dataSnapshot.child("pinRescuer").getValue(String::class.java).toString()
                            val pinName = dataSnapshot.child("pinName").getValue(String::class.java)
                            val rescuerID = dataSnapshot.child("pinRescuerID").getValue(String::class.java)
                            val rate = dataSnapshot.child("rate").getValue(String::class.java)
                            val disaster = dataSnapshot.child("disasterType").getValue(String::class.java)
                            val sitio = dataSnapshot.child("sitio").getValue(String::class.java)
                            val description = dataSnapshot.child("description").getValue(String::class.java)
                            val date = dataSnapshot.child("date").getValue(String::class.java)
                            val time = dataSnapshot.child("time").getValue(String::class.java)
                            resolved = dataSnapshot.child("resolved").getValue(String::class.java)


                            if(pinRescuer==null || pinRescuer==""){
                                helperNameTextView.text = "No one is sending help yet."
                            }else if(resolved == "true"){
                                helperNameTextView.text = "This pin has been resolved."
                            }
                            else{
                                val message = SpannableString("$pinRescuer is sending a help.")
                                message.setSpan(StyleSpan(Typeface.BOLD), 0, pinRescuer!!.length, 0)
                                message.setSpan(ForegroundColorSpan(Color.parseColor("#072A4D")), 0, pinRescuer!!.length, 0) // Navy blue color

                                helperNameTextView.text = message

                            }

                            pinnedByName.text = pinName
                            ratingsSituation.text = rate
                            disasterType.text = disaster
                            currentSitio.text = sitio
                            currentSituation.text = description
                            dateAndTime.text = "Last updated: $time, $date"
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Handle possible errors.
                        }
                    })

                    pinnedByName.text = pinData.pinName
                    ratingsSituation.text = pinData.rate
                    disasterType.text = pinData.disasterType
                    currentSitio.text = pinData.sitio
                    currentSituation.text = pinData.description

                    var attachmentCounter = 0
                    pinData.attachmentList.forEach {
                        addNewImageview(
                            it.toUri(),
                            dialog.findViewById<LinearLayout>(R.id.attachmentContainer),
                            attachmentCounter
                        )
                        attachmentCounter += 1
                    }

                    //BACK BUTTON
                    val backButtonLayout = dialog.findViewById<RelativeLayout>(R.id.back_layout)
                    backButtonLayout.setOnClickListener {
                        val intent = Intent(this@Home, Home::class.java)
                        startActivity(intent)
                    }

                    //EDIT BUTTON
                    val editButtonLayout = dialog.findViewById<Button>(R.id.editPinButton)
                    editButtonLayout.setOnClickListener {
                        if(resolved == "true"){
                            Toast.makeText(this@Home, "This pin has been resolved.", Toast.LENGTH_SHORT).show()
                        } else{
                            showEditDialog(pinId)
                        }

                    }

                    //RESOLVED BUTTON
                    val resolvedButtonLayout = dialog.findViewById<Button>(R.id.resolvedButton)
                    resolvedButtonLayout.setOnClickListener {
                        if(resolved == "true"){
                            Toast.makeText(this@Home, "This pin has been resolved.", Toast.LENGTH_SHORT).show()
                        } else{
                            AlertDialog.Builder(this@Home)
                                .setTitle("Resolve Confirmation")
                                .setMessage("Are you sure you want to mark this as resolved? This action cannot be undone.")
                                .setPositiveButton("Yes") { _, _ ->
                                    val dbRef = FirebaseDatabase.getInstance("https://rescyou-57570-default-rtdb.asia-southeast1.firebasedatabase.app/")
                                        .reference


                                    dbRef.child("Pins").child(pinId).child("resolved").setValue("true")

                                    val intent = Intent(this@Home, Home::class.java)
                                    startActivity(intent)
                                }
                                .setNegativeButton("No", null)
                                .show()

                        }


                    }

                } else {
                    Log.d(PinMyLocation.TAG, "No Pin data found for pinId: $pinId")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Log error
                Log.d(PinMyLocation.TAG, "loadPin:onCancelled", databaseError.toException())
            }
        })

        // Keep a reference to the currently displayed dialog
        currentDialog = dialog


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
                    if (pin != null && postSnapshot.hasChild("latitude") && postSnapshot.child("resolved").getValue(String::class.java) != "true") {
                        pin.latitude = postSnapshot.child("latitude").getValue(String::class.java) ?: ""
                        pinList.add(pin)
                        val markerOptions = MarkerOptions().position(
                            LatLng(
                                pin.latitude.toDouble(),
                                pin.longitude.toDouble()
                            )
                        ).title(pin.pinId)

                        val marker = googleMap.addMarker(markerOptions)
                        markerList.add(marker!!)
                        marker.tag = pin.pinUserId

                        // Fetch otherUser data here
                        fetchOtherUserData(pin.pinUserId)
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



    //SENDING HELP BUTTON
    private fun fetchOtherUserData(pinUserId: String) {
        val userRef = database.reference.child("Users").child(pinUserId)

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
        otherUserID: String,
        senderDisplayName: String,
        pinId: String
    ){

        try {
            val jsonObject = JSONObject().apply {
                put("to", otherUser?.fcmToken ?: "")
                put("notification", JSONObject().apply {
                    put("title", "Help is on the way.")
                    put("body", "Someone wants to send you a help request.")
                })
                put("data", JSONObject().apply {
                    put("userId", senderUserId)
                    put("otherUserID", otherUserID)
                    put("rescuerName", senderDisplayName)
                    put("pinId", pinId)// Include the rescuerName in the data payload
                    put("type", "sendRequest") // Set the notification type here

                })
            }

            if (otherUser?.fcmToken.isNullOrBlank()) {

                Log.e(TAG, "Receiver FCM token is null or empty.")
                Toast.makeText(this, "Failed to send help notification: Receiver FCM token is null or empty.", Toast.LENGTH_SHORT).show()
                return
            }

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
                editor.putString("receiverUserId", receiverUserId)
                editor.putString("pinId", pinId)
                editor.apply()


                // Now, senderDisplayName contains the display name of the sender
                Log.d(TAG, "Sender display name: $senderDisplayName")
                sendHelpNotification(receiverUserId, senderUserId, senderDisplayName, pinId)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error
                Log.e(TAG, "Error fetching sender user data: ${error.message}")
            }
        })
    }

    //PERMISSIONS

    private fun hasNotificationPermission(): Boolean =
        EasyPermissions.hasPermissions(this, android.Manifest.permission.ACCESS_NOTIFICATION_POLICY)

    private fun requestNotificationPermission() {
        EasyPermissions.requestPermissions(
            this,
            "This application requires notification access to work properly.",
            Constants.PERMISSION_REQUEST_CODE_ACCESS_NOTIFICATION,
            android.Manifest.permission.ACCESS_NOTIFICATION_POLICY
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
            if (requestCode == Constants.PERMISSION_REQUEST_CODE_ACCESS_FINE_LOCATION) {
                requestLocationPermission()
            } else if (requestCode == Constants.PERMISSION_REQUEST_CODE_ACCESS_NOTIFICATION) {
                requestNotificationPermission()
            }
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        when (requestCode) {
            Constants.PERMISSION_REQUEST_CODE_ACCESS_FINE_LOCATION -> restartApp()
            Constants.PERMISSION_REQUEST_CODE_ACCESS_NOTIFICATION -> {
                // Handle the permission granted for notifications if needed
            }
        }
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


    //SHOW THE DIALOGS

    //SHOW DIALOG (for other pins)
    private fun showDialog(pins: Pins) {
        isDialogShowing = true  // Set the flag to true when the dialog is about to show
        // Dismiss the current dialog if it's showing
        currentDialog?.dismiss()



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




        val rescuerName = dialog.findViewById<TextView>(R.id.viewPin_sendHelpName)
        val pinnedByName = dialog.findViewById<TextView>(R.id.viewPin_pinnedByName)
        val ratingsSituation = dialog.findViewById<TextView>(R.id.viewPin_ratingsCurrentSituation)
        val disasterType = dialog.findViewById<TextView>(R.id.viewPin_disasterType)
        val currentSitio = dialog.findViewById<TextView>(R.id.viewPin_currentSitio)
        val currentSituation = dialog.findViewById<TextView>(R.id.viewPin_currentSituation)
        val dateAndTime: TextView = dialog.findViewById(R.id.dateAndTime)

        otherUser?.pinUserId =pins.pinUserId

        val database = FirebaseDatabase.getInstance("https://rescyou-57570-default-rtdb.asia-southeast1.firebasedatabase.app/")
        val myRef = database.getReference("Pins").child(pins.pinId)

        val usersRef = database.getReference("Users")

        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val pinUserId = dataSnapshot.child("pinUserId").getValue(String::class.java)

                usersRef.child(pinUserId.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val displayName = dataSnapshot.child("displayName").getValue(String::class.java)

                        if (displayName != null) {
                            val pinsRef = database.getReference("Pins").child(pins.pinId)
                            pinsRef.child("pinName").setValue(displayName)
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle possible errors.
                    }
                })


                pinRescuer = dataSnapshot.child("pinRescuer").getValue(String::class.java)
                val pinName = dataSnapshot.child("pinName").getValue(String::class.java)
                val rescuerID = dataSnapshot.child("pinRescuerID").getValue(String::class.java)
                val rate = dataSnapshot.child("rate").getValue(String::class.java)
                val disaster = dataSnapshot.child("disasterType").getValue(String::class.java)
                val sitio = dataSnapshot.child("sitio").getValue(String::class.java)
                val description = dataSnapshot.child("description").getValue(String::class.java)
                val date = dataSnapshot.child("date").getValue(String::class.java)
                val time = dataSnapshot.child("time").getValue(String::class.java)
                resolved = dataSnapshot.child("resolved").getValue(String::class.java)

                //to appear if the pin has been resolved
                if (resolved == "true") {
                    // Create an AlertDialog.Builder instance
                    val alertDialogBuilder = AlertDialog.Builder(this@Home)
                    alertDialogBuilder.setTitle("Pin Resolved")
                    alertDialogBuilder.setMessage("This pin has been resolved.")
                    alertDialogBuilder.setPositiveButton("OK") { dialogInterface, _ ->
                        val intent = Intent(this@Home, Home::class.java) // Use this@Home instead of this
                        startActivity(intent)
                        // Dismiss the dialog when the "OK" button is clicked
                        dialogInterface.dismiss()
                    }

                    // Create and show the AlertDialog
                    val alertDialog: AlertDialog = alertDialogBuilder.create()
                    alertDialog.show()
                }


                if(pinRescuer==null || pinRescuer==""){
                    rescuerName?.text = "No one is sending help yet."
                }
                else if(resolved == "true"){
                    rescuerName.text = "This pin has been resolved."
                }
                else{
                    if(currentUserId()==rescuerID){
                        rescuerName.text = "You are sending a help."

                        val sendHelpButton = dialog.findViewById<Button>(R.id.sendHelpButton)

                        // Create an AlertDialog.Builder instance
                        val alertDialogBuilder = AlertDialog.Builder(this@Home)
                        alertDialogBuilder.setTitle("Sending Help")
                        alertDialogBuilder.setMessage("You are currently sending help.")
                        alertDialogBuilder.setPositiveButton("OK") { dialogInterface, _ ->
                            // Dismiss the dialog when the "OK" button is clicked
                            dialogInterface.dismiss()
                        }

                        // Create and show the AlertDialog
                        val alertDialog: AlertDialog = alertDialogBuilder.create()
                        alertDialog.show()




                    }
                    else{
                        val message = SpannableString("$pinRescuer is sending a help.")
                        message.setSpan(StyleSpan(Typeface.BOLD), 0, pinRescuer!!.length, 0)
                        message.setSpan(ForegroundColorSpan(Color.parseColor("#072A4D")), 0, pinRescuer!!.length, 0) // Navy blue color

                        rescuerName.text = message
                    }

                }

                if(pinName==null){
                    val alertDialogBuilder = AlertDialog.Builder(this@Home) // Use this@Home instead of this
                    alertDialogBuilder.setTitle("Pin Deletion")
                    alertDialogBuilder.setMessage("The current pin has already been deleted.")
                    alertDialogBuilder.setPositiveButton("OK") { dialogInterface, _ ->
                        // Handle "OK" button click, dismiss the dialog
                        val intent = Intent(this@Home, Home::class.java) // Use this@Home instead of this
                        startActivity(intent)
                        dialogInterface.dismiss()
                    }

                    val alertDialog: AlertDialog = alertDialogBuilder.create()
                    alertDialog.show()
                }

                pinnedByName.text = pinName
                ratingsSituation.text = rate
                disasterType.text = disaster
                currentSitio.text = sitio
                currentSituation.text = description
                dateAndTime.text = "Last updated: $time, $date"
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
            }
        })


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

        //SEND HELP BUTTON


        val sendHelpButton = dialog.findViewById<Button>(R.id.sendHelpButton)
        sendHelpButton.setOnClickListener {
            if(pinRescuer!=null && pinRescuer!=""){
                Toast.makeText(this, "Help is already on the way.", Toast.LENGTH_SHORT).show()
            }else{
                if(resolved=="true"){
                    Toast.makeText(this, "This pin has already been resolved.", Toast.LENGTH_SHORT).show()
                }else{
                    val alertDialogBuilder = AlertDialog.Builder(this)
                    alertDialogBuilder.setTitle("Confirm Send Help")
                    alertDialogBuilder.setMessage("Are you sure you want to send help?")
                    alertDialogBuilder.setPositiveButton("Yes") { _, _ ->
                        val senderUserId = currentUserId()
                        if (senderUserId != null) {
                            sendHelpButtonClicked(pins.pinUserId, senderUserId, pins.pinId)
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


        }

        // Keep a reference to the currently displayed dialog
        currentDialog = dialog



    }


    //SHOW MY DIALOG (for my pin)
    private fun showEditDialog(pinId: String) {
        isDialogShowing = true  // Set the flag to true when the dialog is about to show

        // Dismiss the current dialog if it's showing
        currentDialog?.dismiss()

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.activity_edit_pin)

        // Show the dialog
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

//        //for the ratings of the situation
        setOnCheckedChangeListener(dialog)
        val spinnerCategory = dialog.findViewById<Spinner>(R.id.spinnerCategory)
        val spinnerSitio = dialog.findViewById<Spinner>(R.id.spinnerSitio)
        descriptionInputEditText = dialog.findViewById<TextInputEditText>(R.id.describeTextInput)

        getTypeOfDisaster(spinnerCategory, dialog)
        getSitio(spinnerSitio, dialog)
        retrieveSitioList(dialog)

        //GET THE VALUES FROM THE DATABASE AND DISPLAY IT

        val database = FirebaseDatabase.getInstance("https://rescyou-57570-default-rtdb.asia-southeast1.firebasedatabase.app/")
        val myRef = database.getReference("Pins").child(pinId)


        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val rate = dataSnapshot.child("rate").getValue(String::class.java).toString()
                val disasterType = dataSnapshot.child("disasterType").getValue(String::class.java).toString()
                val sitio = dataSnapshot.child("sitio").getValue(String::class.java).toString()
                val description = dataSnapshot.child("description").getValue(String::class.java).toString()

                // Move this code into onDataChange
                when (rate) {
                    "Mild" -> mildRadioButton!!.isChecked = true
                    "Moderate" -> moderateRadioButton!!.isChecked = true
                    "Severe" -> severeRadioButton!!.isChecked = true
                    "Critical" -> criticalRadioButton!!.isChecked = true
                    "Catastrophic" -> catastrophicRadioButton!!.isChecked = true
                    else -> {} // Add an else branch to make the when expression exhaustive
                }

                // Set the selected value of spinnerCategory
                val categoryAdapter = spinnerCategory.adapter as ArrayAdapter<String>
                val categoryPosition = categoryAdapter.getPosition(disasterType)
                spinnerCategory.setSelection(categoryPosition)

                // Set the selected value of spinnerSitio
                val sitioAdapter = spinnerSitio.adapter as ArrayAdapter<String>
                val sitioPosition = sitioAdapter.getPosition(sitio)
                spinnerSitio.setSelection(sitioPosition)

                descriptionInputEditText.setText(description)



                //uploadImages()
                getSelectedRatings()
                getTypeOfDisaster(spinnerCategory, dialog)
                getSitio(spinnerSitio, dialog)
                getCurrentTime()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
            }
        })

        // When the "Save" button is clicked, update the values in the database
        val saveButton = dialog.findViewById<Button>(R.id.saveButton)
        saveButton.setOnClickListener {
            val alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder.setTitle("Save Confirmation")
            alertDialogBuilder.setMessage("Are you sure you want to save?")
            alertDialogBuilder.setPositiveButton("Yes") { dialogInterface, _ ->
                // Handle "Yes" button click
                dialogInterface.dismiss()

                checkIfEmpty()

                val dbRef = FirebaseDatabase.getInstance("https://rescyou-57570-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .reference

                // Show loading dialog
                showLoadingDialog()

                if (pinId != null){
                    dbRef.child("Pins").child(pinId).child("rate").setValue(selectedRateName)
                    dbRef.child("Pins").child(pinId).child("disasterType").setValue(selectedItemValue)
                    dbRef.child("Pins").child(pinId).child("sitio").setValue(selectedSitioValue)
                    dbRef.child("Pins").child(pinId).child("description").setValue(descriptionInputEditText.text.toString())
                    dbRef.child("Pins").child(pinId).child("date").setValue(formattedDate)
                    dbRef.child("Pins").child(pinId).child("time").setValue(formattedTime)
                        .addOnCompleteListener { task ->
                            // Dismiss loading dialog
                            dismissLoadingDialog()
                            if (task.isSuccessful) {
                                showMyDialog(pinId)
                                Toast.makeText(this, "Pin details edited successfully.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, "Failed to save data", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
            alertDialogBuilder.setNegativeButton("No") { dialogInterface, _ ->
                // Handle "No" button click, dismiss the dialog
                dialogInterface.dismiss()
            }

            val alertDialog: AlertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }

        //BACK BUTTON
        val backButtonLayout = dialog.findViewById<RelativeLayout>(R.id.back_layout)
        backButtonLayout.setOnClickListener {
            val intent = Intent(this@Home, Home::class.java)
            startActivity(intent)
        }

        //DELETE BUTTON
        val deleteButton = dialog.findViewById<Button>(R.id.deletePinButton)
        deleteButton.setOnClickListener {
            val alertDialogBuilder = AlertDialog.Builder(this@Home)
            alertDialogBuilder.setTitle("Delete Confirmation")
            alertDialogBuilder.setMessage("Are you sure you want to permanently delete this?")
            alertDialogBuilder.setPositiveButton("Yes") { dialogInterface, _ ->
                // Handle "Yes" button click, for example, navigate back or finish the activity
                dialogInterface.dismiss()

                if (pinId != null) {
                    // Delete the pin from the database if the pinId is not null
                    val dbRef = FirebaseDatabase.getInstance("https://rescyou-57570-default-rtdb.asia-southeast1.firebasedatabase.app/")
                        .reference
                    dbRef.child("Pins").child(pinId).removeValue()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "Pin deleted successfully", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, "Failed to delete pin", Toast.LENGTH_SHORT).show()
                            }
                        }

                    finish()

                    val intent = Intent(this@Home, Home::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Pin ID is null", Toast.LENGTH_SHORT).show()
                }
            }
            alertDialogBuilder.setNegativeButton("No") { dialogInterface, _ ->
                // Handle "No" button click, dismiss the dialog
                dialogInterface.dismiss()
            }

            val alertDialog: AlertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }

        //CANCEL BUTTON
        val cancelButton = dialog.findViewById<Button>(R.id.cancelButton)
        cancelButton.setOnClickListener {
            val alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder.setTitle("Cancel Confirmation")
            alertDialogBuilder.setMessage("Are you sure you want to cancel?")
            alertDialogBuilder.setPositiveButton("Yes") { dialogInterface, _ ->
                // Handle "Yes" button click, for example, navigate back or finish the activity
                dialogInterface.dismiss()

                showMyDialog(pinId)
            }
            alertDialogBuilder.setNegativeButton("No") { dialogInterface, _ ->
                // Handle "No" button click, dismiss the dialog
                dialogInterface.dismiss()
            }

            val alertDialog: AlertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }

        dialog.show()

        // Keep a reference to the currently displayed dialog
        currentDialog = dialog
    }


    /////////// GET EACH SELECTED VALUES  ///////////


    //GET THE RATINGS
    private fun setOnCheckedChangeListener(dialog: Dialog) {
        // check current state of a radio button (true or false).
        // find the radiobutton by returned id
        mildRadioButton = dialog.findViewById(R.id.radio_mild)
        moderateRadioButton = dialog.findViewById(R.id.radio_moderate)
        severeRadioButton = dialog.findViewById(R.id.radio_severe)
        criticalRadioButton = dialog.findViewById(R.id.radio_critical)
        catastrophicRadioButton = dialog.findViewById(R.id.radio_catastrophic)

        radioGroup = dialog.findViewById(R.id.rate_radiobutton)
        radioGroup!!.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { group, checkedId ->

            when (mildRadioButton!!.isChecked) {
                true -> {
                    selectedRateName = "Mild"
                    selectedDrawable = resources.getDrawable(R.drawable.mild_clicked, null)
                    mildRadioButton!!.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        selectedDrawable,
                        null,
                        null
                    )

                    //change color for unchecked
                    moderateRadioButton!!.isChecked = false
                    severeRadioButton!!.isChecked = false
                    criticalRadioButton!!.isChecked = false
                    catastrophicRadioButton!!.isChecked = false
                }

                false -> {
                    unselectedDrawable = resources.getDrawable(R.drawable.mild_1, null)
                    mildRadioButton!!.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        unselectedDrawable,
                        null,
                        null
                    )

                }

            }

            when (moderateRadioButton!!.isChecked) {
                true -> {
                    selectedRateName = "Moderate"
                    selectedDrawable = resources.getDrawable(R.drawable.moderate_clicked, null)
                    moderateRadioButton!!.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        selectedDrawable,
                        null,
                        null
                    )

                    //change color for unchecked
                    mildRadioButton!!.isChecked = false
                    severeRadioButton!!.isChecked = false
                    criticalRadioButton!!.isChecked = false
                    catastrophicRadioButton!!.isChecked = false
                }

                false -> {
                    unselectedDrawable = resources.getDrawable(R.drawable.moderate, null)
                    moderateRadioButton!!.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        unselectedDrawable,
                        null,
                        null
                    )

                }

            }

            when (severeRadioButton!!.isChecked) {
                true -> {
                    selectedRateName = "Severe"
                    selectedDrawable = resources.getDrawable(R.drawable.severe_clicked, null)
                    severeRadioButton!!.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        selectedDrawable,
                        null,
                        null
                    )

                    //change color for unchecked
                    mildRadioButton!!.isChecked = false
                    moderateRadioButton!!.isChecked = false
                    criticalRadioButton!!.isChecked = false
                    catastrophicRadioButton!!.isChecked = false
                }

                false -> {
                    unselectedDrawable = resources.getDrawable(R.drawable.severe, null)
                    severeRadioButton!!.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        unselectedDrawable,
                        null,
                        null
                    )

                }

            }
            when (criticalRadioButton!!.isChecked) {
                true -> {
                    selectedRateName = "Critical"
                    selectedDrawable = resources.getDrawable(R.drawable.crtical_clicked, null)
                    criticalRadioButton!!.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        selectedDrawable,
                        null,
                        null
                    )

                    //change color for unchecked
                    mildRadioButton!!.isChecked = false
                    moderateRadioButton!!.isChecked = false
                    criticalRadioButton!!.isChecked = false
                    catastrophicRadioButton!!.isChecked = false
                }

                false -> {
                    unselectedDrawable = resources.getDrawable(R.drawable.crtical, null)
                    criticalRadioButton!!.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        unselectedDrawable,
                        null,
                        null
                    )

                }

            }
            when (catastrophicRadioButton!!.isChecked) {
                true -> {
                    selectedRateName = "Catastrophic"
                    selectedDrawable = resources.getDrawable(R.drawable.catastropic_4_clicked, null)
                    catastrophicRadioButton!!.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        selectedDrawable,
                        null,
                        null
                    )

                    //change color for unchecked
                    mildRadioButton!!.isChecked = false
                    moderateRadioButton!!.isChecked = false
                    severeRadioButton!!.isChecked = false
                    criticalRadioButton!!.isChecked = false
                }

                false -> {
                    unselectedDrawable = resources.getDrawable(R.drawable.catastropic_4, null)
                    catastrophicRadioButton!!.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        unselectedDrawable,
                        null,
                        null
                    )
                }
            }
        })
    }


    //RETRIEVE SITIO FROM FIREBASE
    private fun retrieveSitioList(dialog: Dialog) {
        // Initialize Firebase in your onCreate or onCreateView
        FirebaseApp.initializeApp(this)

        dbRef = FirebaseDatabase.getInstance("https://rescyou-57570-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Sitios")

        val sitioList = ArrayList<String>()
        sitioList.add("Select an item")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d(PinMyLocation.TAG, "onDataChange called")


                for (postSnapshot in dataSnapshot.children) {
                    val sitioName = postSnapshot.child("sitioName").getValue(String::class.java)

                    sitioList.add(sitioName!!)

                }
                val spinner = dialog.findViewById<Spinner>(R.id.spinnerSitio)
                val adapter = ArrayAdapter(this@Home, android.R.layout.simple_spinner_item, sitioList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(PinMyLocation.TAG, "loadPost:onCancelled", databaseError.toException())
                // ...
            }
        })
    }

    //GETTING THE SELECTED RATING OF THE SITUATION
    private fun getSelectedRatings(){
    }

    //TYPE OF DISASTER
    private fun getTypeOfDisaster(spinnerCategory: Spinner, dialog: Dialog) {

        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                // Get the selected item value
                selectedItemValue = parent?.getItemAtPosition(position).toString()

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle the case where nothing is selected if needed
            }
        }


    }

    //GET THE SELECTED SITIO
    private fun getSitio(spinnerSitio: Spinner, dialog: Dialog) {

        spinnerSitio.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                // Get the selected item value
                selectedSitioValue = parent?.getItemAtPosition(position).toString()

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle the case where nothing is selected if needed
            }
        }

    }

    private fun checkIfEmpty(){
        //to check baka may null na selected

        if(selectedRateName == "" || selectedItemValue =="Select an item" || selectedSitioValue =="Select an item" || descriptionInputEditText.length() == 0){
            Toast.makeText(applicationContext, "Please fill all the required field." , Toast.LENGTH_SHORT).show()
            isEmpty="true"

        }
    }

    //GET CURRENT TIME
    private fun getCurrentTime() {
        // Get the current date and time
        val calendar = Calendar.getInstance()
        val currentDate = calendar.time

        // Define date and time formatters
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormatter = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()) // Changed to 12-hour format

        // Format date and time using the formatters
        formattedDate = dateFormatter.format(currentDate)
        formattedTime = timeFormatter.format(currentDate)
    }

    //PROGRESS DIALOG
    private fun showLoadingDialog() {
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Updating...")
        progressDialog.setCancelable(false)
        progressDialog.show()
    }

    private fun dismissLoadingDialog() {
        if (progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }






    //NAV BAR
    private val navBarWhenClicked = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.home -> {
                // Check if the current activity is not Profile before starting it
                if (this !is Home) {
                    val intent = Intent(this, Home::class.java)
                    startActivity(intent)
                    finish()  // Finish the current activity
                }

                return@OnNavigationItemSelectedListener true
            }

            R.id.tools -> {
                val intent = Intent(this, Tools::class.java)
                startActivity(intent)
                finish()  // Finish the current activity
                return@OnNavigationItemSelectedListener true

            }

            R.id.info -> {
                val intent = Intent(this, Information::class.java)
                startActivity(intent)
                finish()  // Finish the current activity
                return@OnNavigationItemSelectedListener true
            }

            R.id.profile -> {
                val intent = Intent(this, Profile::class.java)
                startActivity(intent)
                finish()  // Finish the current activity
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