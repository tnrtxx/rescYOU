package com.example.rescyou

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.rescyou.databinding.ActivityMainBinding
import com.example.rescyou.databinding.ActivityTermsAndConditionsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

private lateinit var binding: ActivityTermsAndConditionsBinding

//Initialize variables
//Personal Information
private lateinit var firstName: String
private lateinit var middleName: String
private lateinit var lastName: String
private lateinit var suffixName: String
private lateinit var birthday: String
private var age: Int = 0

//Account Information
private lateinit var email: String
private lateinit var password: String

//For Realtime Database
private lateinit var database: DatabaseReference
private lateinit var data: FirebaseDatabase

//for FirebaseAuth (SIGN UP/REGISTRATION OF USER)
private lateinit var auth: FirebaseAuth



class TermsAndConditions : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTermsAndConditionsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.agreeButton.setOnClickListener {

            //initialize database
            database = Firebase.database.reference

            data = FirebaseDatabase.getInstance("https://rescyou-57570-default-rtdb.asia-southeast1.firebasedatabase.app/")
            val myRef = data.reference

            val user = auth.currentUser
            val userID= user?.uid.toString()


            //getting the displayName and saving it as First Name
            val displayName= user?.displayName
            firstName= displayName!!

            //store data to the REALTIME DATABASE
            myRef.child("Users").child(userID).child("firstName").setValue(firstName)


            Toast.makeText(applicationContext, "Saved successfully", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, Home::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}