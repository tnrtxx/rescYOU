package com.example.rescyou

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
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.rescyou.databinding.ActivityPinMyLocationBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.StorageReference
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import droidninja.filepicker.FilePickerBuilder
import droidninja.filepicker.FilePickerConst
import java.io.File


class PinMyLocation : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

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
    private lateinit var storageReference: StorageReference
    private lateinit var database: DatabaseReference

    //for pins
    private lateinit var pin: Pins

    //checking if empty
    private var isEmpty: String = "false"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPinMyLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

                    //uploadImages()
                    getSelectedRatings()
                    getTypeOfDisaster(spinnerCategory)
                    getSitio(spinnerSitio)



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

    private fun checkIfEmpty(){
        //to check baka may null na selected

//        when(selectedRateName == "" || selectedItemValue =="Select an item" || selectedSitioValue =="Select an item" || selectedSitioValue =="" || binding.describeTextInput.text?.isEmpty() == true){
//            true ->{
//                Toast.makeText(applicationContext, "Please fill all the required field." , Toast.LENGTH_SHORT).show()
//
//            }
//            false ->{
//
//            }
//        }
//        if (selectedRateName == ""){
//            Toast.makeText(applicationContext, "Please rate your current situation." , Toast.LENGTH_SHORT).show()
//            true
//
//        }
//        else if(selectedItemValue =="Select an item"){
//            Toast.makeText(applicationContext, "Please select the type of disaster." , Toast.LENGTH_SHORT).show()
//            true
//
//        }
//
//        else if(selectedSitioValue =="Select an item" || selectedSitioValue ==""){
//            Toast.makeText(applicationContext, "Please the Sitio you are in." , Toast.LENGTH_SHORT).show()
//            true
//
//        }
//
//        else if(binding.describeTextInput.text?.isEmpty() == true){
//            Toast.makeText(applicationContext, "Describe your situation" , Toast.LENGTH_SHORT).show()
//            true
//
//        }
//


        if(selectedRateName == "" || selectedItemValue =="Select an item" || selectedSitioValue =="" || binding.describeTextInput.text?.isEmpty() == true || uri.size == 0){
            Toast.makeText(applicationContext, "Please fill all the required field." , Toast.LENGTH_SHORT).show()
            isEmpty="true"

        }

//
//

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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            SettingsDialog.Builder(this).build().show()
        } else {
            if (requestCode == CAMERA_PERMISSION_CODE) {
                requestCameraPermission()
            } else if (requestCode == GALLERY_PERMISSION_REQUEST_CODE) {
                requestGalleryPermission()
            }
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        // Handle when permissions are granted
        if (requestCode == CAMERA_PERMISSION_CODE) {
            openCamera()
        } else if (requestCode == GALLERY_PERMISSION_REQUEST_CODE) {
            openGallery()
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

    //get the sitio
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

                Toast.makeText(applicationContext, selectedSitioValue, Toast.LENGTH_SHORT).show()

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle the case where nothing is selected if needed
            }
        }

//        Toast.makeText(applicationContext, selectedSitioValue, Toast.LENGTH_SHORT).show()

    }


    //UPLOADING THE PIN DETAILS
//    private fun uploadImages(){
//
//    }




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

}










