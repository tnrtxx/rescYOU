package com.example.rescyou

import android.app.Dialog
import android.content.ContentValues
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.example.rescyou.PinMyLocation.Companion.TAG
import com.example.rescyou.databinding.ActivityPinMyLocationBinding
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
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

class DialogActivity : AppCompatActivity() {

    private lateinit var pinId:String
    private lateinit var otherUserID:String
    private lateinit var status:String

    private var myPinModel: MyPinModel ? = null


    private val database = FirebaseDatabase.getInstance("https://rescyou-57570-default-rtdb.asia-southeast1.firebasedatabase.app/")



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dialog)

        // Initialize myPinModel
        myPinModel = MyPinModel()

        // Retrieve the helper's name from the Intent
        val rescuerName = intent.getStringExtra("rescuerName")
        otherUserID = intent.getStringExtra("otherUserID")!!
        pinId = intent.getStringExtra("pinId")!!

//        Toast.makeText(this, otherUserID, Toast.LENGTH_SHORT).show()

        // Create a dialog
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Help Request")
        alertDialogBuilder.setMessage("Do you want to accept help from $rescuerName?")
        alertDialogBuilder.setPositiveButton("Yes") { _, _ ->
            status = "Accepted"
            fetchFCMToken(otherUserID)



            showDialog(pinId, rescuerName.toString())


            Log.d(TAG, "Rescuer name in DialogActivity: $rescuerName")
        }
        alertDialogBuilder.setNegativeButton("No") { dialogInterface, _ ->
            status = "Declined"
            Toast.makeText(this, "sendDeclineNotification(fcmToken: String", Toast.LENGTH_SHORT).show()

            fetchFCMToken(otherUserID)
//            sendDeclineNotification(myPinModel.fcmToken)
            dialogInterface.dismiss()
        }

        alertDialogBuilder.setCancelable(false)  // This will make the dialog unclickable outside the prompt

        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()

    }


    private fun fetchFCMToken(userId: String) {
        val myRef = FirebaseDatabase.getInstance().reference.child("Users").child(userId)

        Toast.makeText(this, "user id:" + userId, Toast.LENGTH_SHORT).show()


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
                    Toast.makeText(this@DialogActivity, "null", Toast.LENGTH_SHORT).show()

                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })



    }

    private fun sendAcceptNotification(fcmToken: String) {
//        Toast.makeText(this, "sendDeclineNotification(fcmToken: String yo" + fcmToken, Toast.LENGTH_SHORT).show()


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
                Toast.makeText(this, "yo " + fcmToken, Toast.LENGTH_SHORT).show()

                Log.e(ContentValues.TAG, "Receiver FCM token is null or empty.")
                Toast.makeText(this, "Failed to send help notification: Receiver FCM token is null or empty.", Toast.LENGTH_SHORT).show()
                return
            }
            Toast.makeText(this, fcmToken, Toast.LENGTH_SHORT).show()

            callApi(jsonObject)
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "Error building notification payload: ${e.message}")
            Toast.makeText(this, "Failed to send help notification: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendDeclineNotification(fcmToken: String) {
//        Toast.makeText(this, "sendDeclineNotification(fcmToken: String yo" + fcmToken, Toast.LENGTH_SHORT).show()


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
                Toast.makeText(this, "yo " + fcmToken, Toast.LENGTH_SHORT).show()

                Log.e(ContentValues.TAG, "Receiver FCM token is null or empty.")
                Toast.makeText(this, "Failed to send help notification: Receiver FCM token is null or empty.", Toast.LENGTH_SHORT).show()
                return
            }
            Toast.makeText(this, fcmToken, Toast.LENGTH_SHORT).show()

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

private fun showDialog(pinId: String, rescuerName: String) {



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
                    dialog.setContentView(R.layout.activity_dialog)

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



                    val message = SpannableString("$rescuerName is sending a help.")
                    message.setSpan(StyleSpan(Typeface.BOLD), 0, rescuerName!!.length, 0)
                    message.setSpan(ForegroundColorSpan(Color.parseColor("#072A4D")), 0, rescuerName.length, 0) // Navy blue color

                    helperNameTextView.text = message
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
}