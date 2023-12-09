package com.example.rescyou

import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.rescyou.PinMyLocation.Companion.TAG
import com.example.rescyou.databinding.ActivityPinMyLocationBinding
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
            // Show the helper's name
            val helperNameTextView = findViewById<TextView>(R.id.viewPin_sendHelpName)
            val message = SpannableString("$rescuerName is sending a help.")
            message.setSpan(StyleSpan(Typeface.BOLD), 0, rescuerName!!.length, 0)
            message.setSpan(ForegroundColorSpan(Color.parseColor("#072A4D")), 0, rescuerName.length, 0) // Navy blue color
            helperNameTextView.text = message
//            showDialog()
            Log.d(TAG, "Rescuer name in DialogActivity: $rescuerName")
        }
        alertDialogBuilder.setNegativeButton("No") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }

        alertDialogBuilder.setCancelable(false)  // This will make the dialog unclickable outside the prompt

        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()

    }



    private fun showDialog() {
        // Create a reference to the "Pins" node
        val pinsRef = database.reference.child("Pins").child(pinId)

        // Attach a listener to read the data at the "Pins" reference
        pinsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                myPinModel.rate = dataSnapshot.child("rate").getValue(String::class.java).toString()
                myPinModel.disasterType =
                    dataSnapshot.child("disasterType").getValue(String::class.java).toString()
                myPinModel.sitio = dataSnapshot.child("sitio").getValue(String::class.java).toString()
                myPinModel.description =
                    dataSnapshot.child("description").getValue(String::class.java).toString()


                //update the UI here
                val ratingsTextView = findViewById<TextView>(R.id.viewPin_ratingsCurrentSituation)
                val disasterTypeTextView = findViewById<TextView>(R.id.viewPin_disasterType)
                val sitioTextView = findViewById<TextView>(R.id.viewPin_currentSitio)
                val descriptionTextView = findViewById<TextView>(R.id.viewPin_currentSituation)


                ratingsTextView.text = myPinModel.rate
                disasterTypeTextView.text = myPinModel.disasterType
                sitioTextView.text = myPinModel.sitio
                descriptionTextView.text = myPinModel.description

                // Do something with the values
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })





    }
}