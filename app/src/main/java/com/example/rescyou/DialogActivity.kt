package com.example.rescyou

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.Gravity
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
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.example.rescyou.PinMyLocation.Companion.TAG
import com.example.rescyou.databinding.ActivityDialogBinding
import com.example.rescyou.utils.FirebaseUtil
import com.example.rescyou.utils.FirebaseUtil.currentUserId
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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


class DialogActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDialogBinding

    private lateinit var pinId:String
    private lateinit var otherUserID:String
    private lateinit var status:String
    private lateinit var pinRescuer:String

    private var myPinModel: MyPinModel ? = null

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


    private var database = FirebaseDatabase.getInstance("https://rescyou-57570-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private lateinit var dbRef: DatabaseReference

    //for a progress dialog
    private lateinit var progressDialog: ProgressDialog

    var pinUserId: String? = null
    var resolved: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize myPinModel
        myPinModel = MyPinModel()

        // Retrieve the helper's name from the Intent
        val rescuerName = intent.getStringExtra("rescuerName")
        otherUserID = intent.getStringExtra("otherUserID").toString()
        pinId = intent.getStringExtra("pinId").toString()



        // Create a dialog
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Help Request")
        alertDialogBuilder.setMessage("Do you want to accept help from $rescuerName?")
        alertDialogBuilder.setPositiveButton("Yes") { _, _ ->
            status = "Accepted"


            val dbRef = FirebaseDatabase.getInstance("https://rescyou-57570-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .reference

            if (pinId != null && rescuerName != null) {
                dbRef.child("Pins").child(pinId).child("pinRescuer").setValue(rescuerName)
                dbRef.child("Pins").child(pinId).child("pinRescuerID").setValue(otherUserID)
            } else {
                Log.d(TAG, "pinId or rescuerName is null")
            }

            if (otherUserID != null) {
                fetchFCMToken(otherUserID)
            } else {
                Log.d(TAG, "otherUserID is null")
            }

            if (rescuerName != null) {
                showDialog(pinId)
            } else {
                Log.d(TAG, "rescuerName is null")
            }

            Log.d(TAG, "Rescuer name in DialogActivity: $rescuerName")

        }
        alertDialogBuilder.setNegativeButton("No") { dialogInterface, _ ->
            status = "Declined"

            fetchFCMToken(otherUserID)
            showDeclineDialog(pinId)
//            sendDeclineNotification(myPinModel.fcmToken)
            dialogInterface.dismiss()
        }

        alertDialogBuilder.setCancelable(false)  // This will make the dialog unclickable outside the prompt

        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()

    }


    private fun fetchFCMToken(userId: String) {
        val myRef = FirebaseDatabase.getInstance().reference.child("Users").child(userId)



        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

//                myPinModel = dataSnapshot.getValue(MyPinModel::class.java)
//                myPinModel?.fcmToken = dataSnapshot.child("fcmToken").getValue(String::class.java).toString()
//                sendDeclineNotification("yooo" + myPinModel?.fcmToken.toString())

                myPinModel = dataSnapshot.getValue(MyPinModel::class.java)
                myPinModel?.fcmToken = dataSnapshot.child("fcmToken").getValue(String::class.java).toString()
                if (myPinModel?.fcmToken != null) {
                    if (status == "Accepted"){
                        sendAcceptNotification(myPinModel?.fcmToken.toString())}
                    else if (status == "Declined"){
                        sendDeclineNotification(myPinModel?.fcmToken.toString())
                    }

                } else {
                    Log.e(TAG, "FCM token is null.")


                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })



    }

    private fun sendAcceptNotification(fcmToken: String) {

        try {
            val jsonObject = JSONObject().apply {
                put("to", fcmToken)
                put("notification", JSONObject().apply {
                    put("title", "Help is on the way.")
                    put("body", "Someone wants to send you a help request.")
                })
                put("data", JSONObject().apply {
                    put("pinId", pinId)// Include the rescuerName in the data payload
                    put("type", "acceptRequest") // Set the notification type here
                })
            }

            if (fcmToken.isNullOrBlank()) {

                Log.e(ContentValues.TAG, "Receiver FCM token is null or empty.")
                Toast.makeText(this, "Failed to send help notification: Receiver FCM token is null or empty.", Toast.LENGTH_SHORT).show()
                return
            }

            callApi(jsonObject)
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error building notification payload: ${e.message}")
            Toast.makeText(this, "Failed to send help notification: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendDeclineNotification(fcmToken: String) {
        try {
            val jsonObject = JSONObject().apply {
                put("to", fcmToken)
                put("notification", JSONObject().apply {
                    put("title", "Help is on the way.")
                    put("body", "Someone wants to send you a help request.")
                })
                put("data", JSONObject().apply {
                    put("pinId", pinId)// Include the rescuerName in the data payload
                    put("type", "declineRequest") // Set the notification type here
                })
            }

            if (fcmToken.isNullOrBlank()) {
                Log.e(ContentValues.TAG, "Receiver FCM token is null or empty.")
                Toast.makeText(this, "Failed to send help notification: Receiver FCM token is null or empty.", Toast.LENGTH_SHORT).show()
                return
            }
            callApi(jsonObject)
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error building notification payload: ${e.message}")
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

private fun showDialog(pinId: String) {

        // Get a reference to the "Pins" node in the database
        val pinsRef = database.getReference("Pins")

        // Attach a listener to read the data at the "Pins" reference
        pinsRef.child(pinId).addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("SuspiciousIndentation")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Pin data from snapshot
                val pinData = dataSnapshot.getValue(MyPinModel::class.java)
                if (pinData != null) {
                    val dialog = Dialog(this@DialogActivity)
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

                    val database = FirebaseDatabase.getInstance("https://rescyou-57570-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    val myRef = database.getReference("Pins").child(pinId)

                    val usersRef = database.getReference("Users")

                    myRef.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val pinUserId = dataSnapshot.child("pinUserId").getValue(String::class.java)

                            usersRef.child(pinUserId.toString()).addValueEventListener(object : ValueEventListener {
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
                        val intent = Intent(this@DialogActivity, Home::class.java)
                        startActivity(intent)
                    }

                    //EDIT BUTTON
                    val editButtonLayout = dialog.findViewById<Button>(R.id.editPinButton)
                    editButtonLayout.setOnClickListener {
                        if (resolved == "true"){
                            Toast.makeText(this@DialogActivity, "This pin has been resolved.", Toast.LENGTH_SHORT).show()
                        } else{
                            showEditDialog(pinId)
                            Toast.makeText(this@DialogActivity, "Edit button clicked", Toast.LENGTH_SHORT).show()
                        }
                    }

                    //RESOLVED BUTTON
                    val resolvedButtonLayout = dialog.findViewById<Button>(R.id.resolvedButton)
                    resolvedButtonLayout.setOnClickListener {
                        AlertDialog.Builder(this@DialogActivity)
                            .setTitle("Resolve Confirmation")
                            .setMessage("Are you sure you want to mark this as resolved?")
                            .setPositiveButton("Yes") { _, _ ->
                                val dbRef = FirebaseDatabase.getInstance("https://rescyou-57570-default-rtdb.asia-southeast1.firebasedatabase.app/")
                                    .reference

                                dbRef.child("Pins").child(pinId).child("resolved").setValue("true")

                                val intent = Intent(this@DialogActivity, Home::class.java)
                                startActivity(intent)
                            }
                            .setNegativeButton("No", null)
                            .show()
                    }

                } else {
                    Log.d(TAG, "No Pin data found for pinId: $pinId")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Log error
                Log.d(TAG, "loadPin:onCancelled", databaseError.toException())
            }
        })

    }

    private fun showDeclineDialog(pinId: String) {

        // Get a reference to the "Pins" node in the database
        val pinsRef = database.getReference("Pins")

        // Attach a listener to read the data at the "Pins" reference
        pinsRef.child(pinId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Pin data from snapshot
                val pinData = dataSnapshot.getValue(MyPinModel::class.java)
                if (pinData != null) {
                    val dialog = Dialog(this@DialogActivity)
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

                    val database = FirebaseDatabase.getInstance("https://rescyou-57570-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    val myRef = database.getReference("Pins").child(pinId)

                    val usersRef = database.getReference("Users")


                    myRef.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val pinUserId = dataSnapshot.child("pinUserId").getValue(String::class.java)

                            usersRef.child(pinUserId.toString()).addValueEventListener(object : ValueEventListener {
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

                            if(resolved == "true"){
                                helperNameTextView.text = "This pin has been resolved."
                            }

                            helperNameTextView.text = "No one is sending help yet."
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
                        val intent = Intent(this@DialogActivity, Home::class.java)
                        startActivity(intent)
                    }

                    //EDIT BUTTON
                    val editButtonLayout = dialog.findViewById<Button>(R.id.editPinButton)
                    editButtonLayout.setOnClickListener {
                        if(resolved == "true"){
                            Toast.makeText(this@DialogActivity, "This pin has been resolved.", Toast.LENGTH_SHORT).show()
                        } else{
                            showEditDialog(pinId)
                            Toast.makeText(this@DialogActivity, "Edit button clicked", Toast.LENGTH_SHORT).show()
                        }

                    }

                    //RESOLVED BUTTON
                    val resolvedButtonLayout = dialog.findViewById<Button>(R.id.resolvedButton)
                    resolvedButtonLayout.setOnClickListener {
                        if(resolved == "true"){
                            Toast.makeText(this@DialogActivity, "This pin has been resolved.", Toast.LENGTH_SHORT).show()
                        } else{
                            AlertDialog.Builder(this@DialogActivity)
                                .setTitle("Resolve Confirmation")
                                .setMessage("Are you sure you want to mark this as resolved?")
                                .setPositiveButton("Yes") { _, _ ->
                                    val dbRef = FirebaseDatabase.getInstance("https://rescyou-57570-default-rtdb.asia-southeast1.firebasedatabase.app/")
                                        .reference


                                    dbRef.child("Pins").child(pinId).child("resolved").setValue("true")

                                    val intent = Intent(this@DialogActivity, Home::class.java)
                                    startActivity(intent)
                                }
                                .setNegativeButton("No", null)
                                .show()

                        }


                    }

                } else {
                    Log.d(TAG, "No Pin data found for pinId: $pinId")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Log error
                Log.d(TAG, "loadPin:onCancelled", databaseError.toException())
            }
        })
    }


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


    //EDIT
    private fun showEditDialog(pinId: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.activity_edit_pin)

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


        myRef.addValueEventListener(object : ValueEventListener {
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
                                showDialog(pinId)
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
            val intent = Intent(this@DialogActivity, Home::class.java)
            startActivity(intent)
        }

        //DELETE BUTTON
        val deleteButton = dialog.findViewById<Button>(R.id.deletePinButton)
        deleteButton.setOnClickListener {
            val alertDialogBuilder = AlertDialog.Builder(this@DialogActivity)
            alertDialogBuilder.setTitle("Delete Confirmation")
            alertDialogBuilder.setMessage("Are you sure you want to delete?")
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

                    val intent = Intent(this@DialogActivity, Home::class.java)
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

                showDialog(pinId)
            }
            alertDialogBuilder.setNegativeButton("No") { dialogInterface, _ ->
                // Handle "No" button click, dismiss the dialog
                dialogInterface.dismiss()
            }

            val alertDialog: AlertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }

        dialog.show()
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
                Log.d(TAG, "onDataChange called")


                for (postSnapshot in dataSnapshot.children) {
                    val sitioName = postSnapshot.child("sitioName").getValue(String::class.java)

                    sitioList.add(sitioName!!)

                }
                val spinner = dialog.findViewById<Spinner>(R.id.spinnerSitio)
                val adapter = ArrayAdapter(this@DialogActivity, android.R.layout.simple_spinner_item, sitioList)
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

}