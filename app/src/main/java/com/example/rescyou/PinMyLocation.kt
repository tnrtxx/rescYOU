package com.example.rescyou


import android.content.ContentValues
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rescyou.databinding.ActivityPinMyLocationBinding
import com.squareup.picasso.Picasso
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import droidninja.filepicker.FilePickerBuilder
import java.io.File


private lateinit var binding: ActivityPinMyLocationBinding


//FOR RATING YOUR SITUATION
//for selected and unselected drawable
private var selectedDrawable: Drawable? = null // Drawable for selected state
private var unselectedDrawable: Drawable? = null // Drawable for unselected state


//radio group
private var radioGroup: RadioGroup? = null

//radio button
private var mildRadioButton: RadioButton? = null
private var moderateRadioButton: RadioButton? = null
private var severeRadioButton: RadioButton? = null
private var criticalRadioButton: RadioButton? = null
private var catastrophicRadioButton: RadioButton? = null

//SAVING IMAGES AND OPENING CAMERA
var fileUri: Uri? = null
var CAPTURE_IMAGE = 1





class PinMyLocation : AppCompatActivity(), EasyPermissions.PermissionCallbacks {


    //ARRAY LIST FOR THE PHOTOS
//    var uri : ArrayList<Uri>? = null
    val uri: ArrayList<Uri> = ArrayList()

    lateinit var imageUri: Uri

    //FOR RECYCLERVIEW
//    var recyclerView: RecyclerView = findViewById(R.id.photo_recyclerView)
    lateinit var adapter: MainAdapter

    //permission request codes
    var PERMISSION_REQUEST_CODE = 1
    var GALLERY_PERMISSION_REQUEST_CODE = 12
    var CAMERA_PERMISSION_CODE = 123
    var PICK_IMG = 1
    var CUSTOM_REQUEST_CODE = 1

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


        //TAKE A PHOTO Button Listener
        binding.takePhotoButton.setOnClickListener {
            requestCameraPermission()


        }

        //ADD A PHOTO BUTTON
        binding.addPhotoButton.setOnClickListener {
            requestGalleryPermission()


        }

//        //for recyclerview
        adapter = MainAdapter(uri!!)
        binding.photoRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.photoRecyclerView.adapter = adapter
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

    private fun hasPermission(): Boolean =
        EasyPermissions.hasPermissions(
            this, android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )

    private fun requestGalleryPermission() {
        EasyPermissions.requestPermissions(
            this,
            "This application requires location permission to work properly.",
            GALLERY_PERMISSION_REQUEST_CODE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    private fun requestCameraPermission() {
        EasyPermissions.requestPermissions(
            this,
            "This application requires location permission to work properly.",
            CAMERA_PERMISSION_CODE,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    private fun requestPermission() {
        EasyPermissions.requestPermissions(
            this,
            "This application requires location permission to work properly.",
            PERMISSION_REQUEST_CODE,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            SettingsDialog.Builder(this).build().show()
        } else {
            requestPermission()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        //FOR CAMERA
        if (requestCode == CAMERA_PERMISSION_CODE) {

            try {
                val values = ContentValues()
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                fileUri =
                    contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)


                //opening camera
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                startActivityForResult(intent, CAPTURE_IMAGE)


            } catch (e: Exception) {
                e.printStackTrace()
            }


        }
        //FOR GALLERY
        else if (requestCode == GALLERY_PERMISSION_REQUEST_CODE) {
//             val mediaSelectionLimit = 2

//             Toast.makeText(this, "Please select maximum of 4 images", Toast.LENGTH_SHORT).show()
//             var intent = Intent()
//                 intent.type = "image/*"
//                 intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
//                 intent.action = Intent.ACTION_GET_CONTENT
//                 startActivityForResult(Intent.createChooser(intent, "Select 4 Pictures Maximum"),   CUSTOM_REQUEST_CODE)
//             ContentUriUtils.getFilePath(context, fileUri!!)

            FilePickerBuilder.instance
                .setActivityTitle("Select Image")
                .setMaxCount(4) //optional
//                .setSelectedFiles(uri!!) //optional
                .setActivityTheme(R.style.Theme_RescYOU) //optional
                .pickPhoto(this, CUSTOM_REQUEST_CODE)
            Toast.makeText(this, "pls", Toast.LENGTH_SHORT).show()

            //for recyclerview
//        adapter = MainAdapter(uri!!)
//        binding.photoRecyclerView.layoutManager = LinearLayoutManager(this)
//        binding.photoRecyclerView.adapter = adapter


        }
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

    //for the selected images
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Toast.makeText(this, "hi", Toast.LENGTH_SHORT).show()

        if (requestCode == CUSTOM_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.data != null) {
//        if (requestCode == CUSTOM_REQUEST_CODE && resultCode == RESULT_OK) {
            Toast.makeText(this, "hii", Toast.LENGTH_SHORT).show()
            imageUri= data.data!!


//            val uri= data?.data

//             if(data?.clipData != null){
//                 Toast.makeText(this, "huhu", Toast.LENGTH_SHORT).show()
////                 var  clipData: ClipData = data?.clipData!!
//
//                 //Initializ array list
////                 uri.addAll(data?.getParcelableArrayListExtra<Uri>(FilePickerConst.KEY_SELECTED_MEDIA)!!);
////                 uri= data.getParcelableArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA)!!
//
//                 var x= data.clipData?.itemCount
//                 for ( i in 0 until x!!){
//                     uri?.add(data.clipData!!.getItemAt(i)?.uri!!)
//                 }
//                 adapter?.notifyDataSetChanged()
////                  Toast.makeText(this, uri?.size, Toast.LENGTH_SHORT).show()
//
//             }}
//             else if(data?.data != null){
//                 var imageURL : String? = data.data?.path
//            uri?.add(Uri.parse(imageURL))
//             }

        }

    }
}









