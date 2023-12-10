package com.example.rescyou

import android.app.Dialog
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

class DialogActivity : AppCompatActivity() {

    private lateinit var pinId:String

    private lateinit var myPinModel: MyPinModel


    private val database = FirebaseDatabase.getInstance("https://rescyou-57570-default-rtdb.asia-southeast1.firebasedatabase.app/")



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dialog)

        // Retrieve the helper's name from the Intent
        val rescuerName = intent.getStringExtra("rescuerName")
        pinId = intent.getStringExtra("pinId")!!

        Toast.makeText(this, pinId, Toast.LENGTH_SHORT).show()

        // Create a dialog
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Help Request")
        alertDialogBuilder.setMessage("Do you want to accept help from $rescuerName?")
        alertDialogBuilder.setPositiveButton("Yes") { _, _ ->


            showDialog(pinId, rescuerName.toString())


            Log.d(TAG, "Rescuer name in DialogActivity: $rescuerName")
        }
        alertDialogBuilder.setNegativeButton("No") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }

        alertDialogBuilder.setCancelable(false)  // This will make the dialog unclickable outside the prompt

        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()

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