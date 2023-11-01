package com.example.rescyou

import android.content.ContentValues
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rescyou.databinding.ActivityPinMyLocationBinding
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
    }

    private lateinit var binding: ActivityPinMyLocationBinding


    // FOR RATING YOUR SITUATION
    // for selected and unselected drawable
    private var selectedDrawable: Drawable? = null // Drawable for selected state
    private var unselectedDrawable: Drawable? = null // Drawable for unselected state


    // radio group
    private var radioGroup: RadioGroup? = null

    // radio button
    private var mildRadioButton: RadioButton? = null
    private var moderateRadioButton: RadioButton? = null
    private var severeRadioButton: RadioButton? = null
    private var criticalRadioButton: RadioButton? = null
    private var catastrophicRadioButton: RadioButton? = null

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

        //for recyclerview
        adapter = MainAdapter(uri)
        binding.photoRecyclerView.layoutManager = LinearLayoutManager(this)
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
            .setMaxCount(4) //optional
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == GALLERY_IMAGE_PICK_REQUEST_CODE) {
                // Get the selected photos and update the arrayList
                val selectedPhotos =
                    data.getParcelableArrayListExtra<Uri>(FilePickerConst.KEY_SELECTED_MEDIA)
                if (selectedPhotos != null) {
                    uri.addAll(selectedPhotos)
                    adapter.notifyDataSetChanged() // Notify the adapter that data has changed
                }
            }
        }
    }

}










