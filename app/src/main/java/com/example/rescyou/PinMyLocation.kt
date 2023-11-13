package com.example.rescyou

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.rescyou.Home.Companion.currentLocation
import com.example.rescyou.Home.Companion.googleMap
import com.example.rescyou.databinding.ActivityHomeBinding
import com.example.rescyou.databinding.ActivityPinMyLocationBinding
import com.example.rescyou.utils.ConnectionLiveData
import com.example.rescyou.utils.TurnOnGps
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import droidninja.filepicker.FilePickerBuilder
import droidninja.filepicker.FilePickerConst
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class PinMyLocation : AppCompatActivity(), EasyPermissions.PermissionCallbacks, OnMapReadyCallback {

    // Pakilagay nito sa Constants obj pagkatapos para malinis
    companion object {
        const val GALLERY_PERMISSION_REQUEST_CODE = 1676
        const val CAMERA_PERMISSION_CODE = 2676

        // for camera
        const val CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034
        const val GALLERY_IMAGE_PICK_REQUEST_CODE = 3423

        const val TAG = "PinMyLocation"
    }

    private lateinit var binding: ActivityPinMyLocationBinding


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

    // SAVING IMAGES AND OPENING CAMERA
    var fileUri: Uri? = null
    var CAPTURE_IMAGE = 1

    // ARRAY LIST FOR THE PHOTOS
    var uri = ArrayList<Uri>()

    // FOR RECYCLERVIEW
    // var recyclerView: RecyclerView = findViewById(R.id.photo_recyclerView)
    lateinit var adapter: MainAdapter


    //for camera
    val APP_TAG = "rescYOU"
    val CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034
    val photoFileName = "photo.jpg"
    var photoFile: File? = null

    //for databases
    private lateinit var database: DatabaseReference

    private lateinit var storageRef: StorageReference
    private lateinit var firestore: FirebaseFirestore

    private lateinit var imageUrl:String

    //for pins
    private lateinit var pin: Pins

    //checking if empty
    private var isEmpty: String = "false"


    //for the map
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


    val currentLocation = Home.currentLocation
    val latitude = currentLocation?.latitude
    val longitude = currentLocation?.longitude

    //for time
    private lateinit var formattedDate: String
    private lateinit var formattedTime: String

    //for a progress dialog
    private lateinit var progressDialog: ProgressDialog




    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPinMyLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()


        storageRef = FirebaseStorage.getInstance("gs://rescyou-57570.appspot.com").reference.child("Images")
        firestore = FirebaseFirestore.getInstance()
        // find the radiobutton by returned id
        mildRadioButton = binding.radioMild
        moderateRadioButton = binding.radioModerate
        severeRadioButton = binding.radioSevere
        criticalRadioButton = binding.radioCritical
        catastrophicRadioButton = binding.radioCatastrophic

        //for the ratings of the situation
        setOnCheckedChangeListener()

        //for the Type of Disaster Spinner
        val spinnerCategory = findViewById<Spinner>(R.id.spinnerCategory)
        val spinnerSitio = findViewById<Spinner>(R.id.spinnerSitio)

        getTypeOfDisaster(spinnerCategory)
        getSitio(spinnerSitio)


        //initialize the spinner for sitio
        retrieveSitioList()

        //geting the Pin details
        pin= Pins()



        //FOR THE NUMBER OF PHOTOS
        var selectPhotosLimit = binding.totalPhotosTextView

        //for recyclerview
        adapter = MainAdapter(uri, object : MainAdapter.CountOfImagesWhenRemoved {
            override fun clicked(getSize: Int) {
                // Update your UI with the new count
                selectPhotosLimit.text = "Selected Photos ($getSize/4)"
            }
        })


        binding.photoRecyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.photoRecyclerView.adapter = adapter

        //TAKE A PHOTO Button Listener
        binding.takePhotoButton.setOnClickListener {
            // Check and request camera permission
            if (hasCameraPermission()) {
                Log.d("CAMERAy", "Has Camera permission")
                // Permission already granted, open camera here
                openCamera()
            } else {
                Log.d("CAMERAy", "Requesting Camera permission")
                requestCameraPermission()
            }
        }

        //ADD A PHOTO BUTTON
        binding.addPhotoButton.setOnClickListener {
            // Check and request gallery permission
            if (hasGalleryPermission()) {
                // Permission already granted, open gallery here
                openGallery()
            } else {
                requestGalleryPermission()
            }
        }

        //BUTTONS

        //CANCEL BUTTON
        binding.cancelPinButton.setOnClickListener {

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

        val currentLocation = Home.currentLocation
        if (currentLocation != null) {
            val latitude = currentLocation.latitude
            val longitude = currentLocation.longitude
            val locationString = "Latitude: $latitude, Longitude: $longitude"
            Toast.makeText(this@PinMyLocation, locationString, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this@PinMyLocation, "huhu", Toast.LENGTH_SHORT).show()
        }

        //PIN MY LOCATION BUTTON
        binding.pinMyLocationButton.setOnClickListener {


            checkIfEmpty()

            if (isEmpty == "true"){

            } else if (isEmpty == "false" || uri.size != 0){
                val alertDialogBuilder = AlertDialog.Builder(this)
                alertDialogBuilder.setTitle("Confirm Pinning")
                alertDialogBuilder.setMessage("Are you sure you want to pin this location?")
                alertDialogBuilder.setPositiveButton("Yes") { dialogInterface, _ ->
                    // Handle "Yes" button click, for example, proceed with pinning the location
                    dialogInterface.dismiss()
                    // Call the function to proceed with pinning the location
                    showLoadingDialog()

                    //uploadImages()
                    getSelectedRatings()
                    getTypeOfDisaster(spinnerCategory)
                    getSitio(spinnerSitio)
                    getCurrentTime()
                    uploadImages()
                    getPinDetails()

                    // Add a marker for the selected location
//                    val currentLocation = Home.currentLocation
//                    pin.latitude= currentLocation?.latitude.toString()
//                    pin.latitude= currentLocation?.longitude.toString()
//
//                    val intent = Intent(this, Home::class.java)
//                    startActivity(intent)

//



                }
                alertDialogBuilder.setNegativeButton("No") { dialogInterface, _ ->
                    // Handle "No" button click, dismiss the dialog
                    dialogInterface.dismiss()
                }

                val alertDialog: AlertDialog = alertDialogBuilder.create()
                alertDialog.show()
            }


        }

    }
    //PROGRESS DIALOG

    private fun showLoadingDialog() {
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Uploading...")
        progressDialog.setCancelable(false)
        progressDialog.show()
    }

    private fun dismissLoadingDialog() {
        if (progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }

    private fun getCurrentTime() {

        // Get the current date and time
        val calendar = Calendar.getInstance()
        val currentDate = calendar.time

        // Define date and time formatters
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        // Format date and time using the formatters
        formattedDate = dateFormatter.format(currentDate)
        formattedTime = timeFormatter.format(currentDate)



    }

    private fun getPinDetails() {
        val currentLocation = Home.currentLocation
        pin.pinUserId=auth.currentUser?.uid.toString()
        pin.pinName= auth.currentUser?.displayName.toString()
        pin.date= formattedDate
        pin.time= formattedTime
        pin.rate=selectedRateName
        pin.disasterType=selectedItemValue
        pin.sitio=selectedSitioValue
        pin.description= binding.describeTextInput.text.toString()
        pin.latitude= currentLocation?.latitude.toString()
        pin.longitude= currentLocation?.longitude.toString()
        pin.isResolved="false"
        pin.pinRescuer=""

    }

    private fun checkIfEmpty(){
        //to check baka may null na selected

        if(selectedRateName == "" || selectedItemValue =="Select an item" || selectedSitioValue =="" || binding.describeTextInput.text?.isEmpty() == true || uri.size == 0){
            Toast.makeText(applicationContext, "Please fill all the required field." , Toast.LENGTH_SHORT).show()
            isEmpty="true"

        }
    }


    private fun setOnCheckedChangeListener() {
        // check current state of a radio button (true or false).
        // find the radiobutton by returned id
        mildRadioButton = binding.radioMild
        moderateRadioButton = binding.radioModerate
        severeRadioButton = binding.radioSevere
        criticalRadioButton = binding.radioCritical
        catastrophicRadioButton = binding.radioCatastrophic

        radioGroup = binding.rateRadiobutton
        radioGroup!!.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { group, checkedId ->

            when (mildRadioButton!!.isChecked) {
                true -> {
                    selectedRateName = "Mild"
                    Toast.makeText(applicationContext, "Mild", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(applicationContext, "Moderate", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(applicationContext, "Severe", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(applicationContext, "Critical", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(applicationContext, "Moderate", Toast.LENGTH_SHORT).show()
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

    // FOR EASY PERMISSION
    private fun hasCameraPermission(): Boolean {
        return EasyPermissions.hasPermissions(this, android.Manifest.permission.CAMERA)
    }

    private fun hasGalleryPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            EasyPermissions.hasPermissions(
                this,
                android.Manifest.permission.READ_MEDIA_IMAGES
            )
        } else {
            EasyPermissions.hasPermissions(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    private fun requestCameraPermission() {
        EasyPermissions.requestPermissions(
            this,
            "Camera permission is required for taking photos via camera",
            CAMERA_PERMISSION_CODE,
            android.Manifest.permission.CAMERA
        )
    }

    private fun requestGalleryPermission() {
        val rationale = "Storage permission is required for taking photos via gallery."
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            EasyPermissions.requestPermissions(
                this,
                rationale,
                GALLERY_PERMISSION_REQUEST_CODE,
                android.Manifest.permission.READ_MEDIA_IMAGES
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                rationale,
                GALLERY_PERMISSION_REQUEST_CODE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            SettingsDialog.Builder(this).build().show()
        } else {
            if (requestCode == CAMERA_PERMISSION_CODE) {
                requestCameraPermission()
            } else if (requestCode == GALLERY_PERMISSION_REQUEST_CODE) {
                requestGalleryPermission()
            }else if(requestCode == Constants.PERMISSION_REQUEST_CODE_ACCESS_FINE_LOCATION){
                requestLocationPermission()
            }
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        // Handle when permissions are granted
        if (requestCode == CAMERA_PERMISSION_CODE) {
            openCamera()
        } else if (requestCode == GALLERY_PERMISSION_REQUEST_CODE) {
            openGallery()
        }else if(requestCode == Constants.PERMISSION_REQUEST_CODE_ACCESS_FINE_LOCATION){
            val intent = intent
            finish()
            startActivity(intent)
        }
    }

    private fun openGallery() {
        FilePickerBuilder.instance
            .setActivityTitle("Select Image")
            .setMaxCount(1) //optional
            .setSelectedFiles(uri) //optional
            .pickPhoto(this, GALLERY_IMAGE_PICK_REQUEST_CODE)


    }

    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        fileUri =
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        //opening camera
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        startActivityForResult(intent, CAPTURE_IMAGE)
    }



    /////////// GET EACH SELECTED VALUES  ///////////

    //RETRIEVE SITIO FROM FIREBASE
    private fun retrieveSitioList(){
        // Initialize Firebase in your onCreate or onCreateView
        FirebaseApp.initializeApp(this)

        database = FirebaseDatabase.getInstance("https://rescyou-57570-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Sitios")

        val sitioList = ArrayList<String>()
        sitioList.add("Select an item")

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d(TAG, "onDataChange called")


                for (postSnapshot in dataSnapshot.children) {
                    val sitioName = postSnapshot.child("sitioName").getValue(String::class.java)

                    sitioList.add(sitioName!!)

                }
                val spinner = findViewById<Spinner>(R.id.spinnerSitio)
                val adapter = ArrayAdapter(this@PinMyLocation, android.R.layout.simple_spinner_item, sitioList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                // ...
            }
        })
    }

    //GETTING THE SELECTED RATING OF THE SITUATION
    private fun getSelectedRatings(){
//        Toast.makeText(applicationContext, selectedRateName , Toast.LENGTH_SHORT).show()
    }

    //TYPE OF DISASTER
    private fun getTypeOfDisaster(spinnerCategory: Spinner) {

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

//        Toast.makeText(applicationContext, selectedItemValue, Toast.LENGTH_SHORT).show()

    }

    //GET THE SELECTED SITIO
    private fun getSitio(spinnerSitio: Spinner) {

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



    //Permission for the map
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


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }








    /////////// UPLOADING THE PIN DETAILS  ///////////
    private suspend fun uploadImage(imageUri: Uri): String {
        return suspendCoroutine { continuation ->
            val storageRef = storageRef.child("${System.currentTimeMillis()}_${UUID.randomUUID()}")
            storageRef.putFile(imageUri).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        continuation.resume(downloadUri.toString())
                    }
                } else {
                    continuation.resumeWithException(task.exception ?: Exception("Image upload failed"))
                }
            }
        }
    }

    private fun uploadImages() {
        showLoadingDialog()
        val totalAttachments = uri.size
        var uploadedCounter = 0

        val latch = CountDownLatch(totalAttachments)

        lifecycleScope.launch {
            uri.forEach { imageUri ->
                try {
                    val imageUrl = uploadImage(imageUri)
                    imageUrl.let {
                        pin?.attachmentList?.add(it)
                        saveImageUrlToDatabase(it)
                    }
                    uploadedCounter++
                } catch (e: Exception) {
                    Log.e("Attachment List", "Failed to upload image: ${e.message}")
                } finally {
                    latch.countDown()
                }
            }

            latch.await() // Wait for all image uploads to complete before updating the database

            // All images have been uploaded and URLs stored in the database
            updateDatabase()
            dismissLoadingDialog()
        }
    }

    private fun saveImageUrlToDatabase(imageUrl: String) {
        val databaseReference = FirebaseDatabase.getInstance("https://rescyou-57570-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("Images")
        val imageId = databaseReference.push().key

        if (imageId != null) {
            databaseReference.child(imageId).setValue(imageUrl)
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.e("Attachment List", "Failed to save image URL to database: ${task.exception?.message}")
                    }
                }
        }
    }

    private fun updateDatabase() {
        showLoadingDialog()
        val dbRef = FirebaseDatabase.getInstance("https://rescyou-57570-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .reference.child("Pins").push()
        pin?.pinId = dbRef.key.toString()
        dbRef.setValue(pin)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val intent = Intent(this, Home::class.java)
                    startActivity(intent)
                    finishAffinity()
                    finish()
                } else {
                    Toast.makeText(this, "Something went wrong.", Toast.LENGTH_SHORT).show()
                }
                dismissLoadingDialog() // Dismiss the dialog inside the callback
            }
    }




    //ONACTIVITY RESULT
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == GALLERY_IMAGE_PICK_REQUEST_CODE) {
                // Get the selected photos and update the arrayList
                val selectedPhotos =
                    data.getParcelableArrayListExtra<Uri>(FilePickerConst.KEY_SELECTED_MEDIA)

                if (selectedPhotos != null) {
                    for (imageUri in selectedPhotos) {
                        if (uri.size < 4) {
                            uri.add(imageUri)
                        } else {
                            Toast.makeText(this, "Not allowed to pick more than 4 images", Toast.LENGTH_SHORT).show()
                            break
                        }
                    }

                    adapter.notifyDataSetChanged()
                    adapter.updateItemCount()
                }
                else{
                    if (uri.size < 4) {
                        //this part is to get the single images
                        val imageUri = data.data
                        if (imageUri != null) {
                            uri.add(imageUri)
                        }
                    } else {
                        Toast.makeText(this, "Not allowed to pick more than 4 images", Toast.LENGTH_SHORT).show()
                    }

                }
            }
        }
    }

    /////////// GET CURRENT LOCATION  ///////////

    @SuppressLint("MissingPermission")
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true

            googleMap.uiSettings.isMyLocationButtonEnabled = true
            googleMap.isMyLocationEnabled = true

        // Add a marker for the selected location
        val currentLocation = Home.currentLocation
        googleMap.addMarker(MarkerOptions().position(currentLocation!!).title("Selected Location"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation!!, 15F))

            }

    }












