package com.example.rescyou

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.DatePicker
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.rescyou.databinding.ActivityProfileBinding
import com.example.rescyou.utils.FirebaseUtil.currentUserId
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale


class Profile : AppCompatActivity(), DatePickerDialog.OnDateSetListener {
    private lateinit var binding: ActivityProfileBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private var birthday:String? = null
    private var age: Int = 0

    private lateinit var birthdayInputEditText: TextInputEditText

    //For DatePickerDialog
    private val calendar = Calendar.getInstance()
    private val formatter = SimpleDateFormat("MMMM d, yyyy", Locale.US)


    //for a progress dialog
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //GET THE PROFILE DETAILS
        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid.toString()


        val database = FirebaseDatabase.getInstance("https://rescyou-57570-default-rtdb.asia-southeast1.firebasedatabase.app/")
        val myRef = database.getReference("Users").child(userId)


        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val displayName = dataSnapshot.child("displayName").value.toString()
                val email = dataSnapshot.child("email").value.toString()
                birthday = dataSnapshot.child("birthday").value?.toString()
                age =  dataSnapshot.child("age").value?.toString()!!.toInt()

                binding.email.text = email

                if (displayName != null) {
                    binding.displayName.text = displayName
                }

                if (birthday == null  || birthday == "") {
                    binding.birthday.setTextColor(Color.rgb(182,182,182))
                    binding.birthday.text = "Enter your birthday"
                } else {
                    binding.birthday.setTextColor(Color.rgb(33,33,33)) // Change to the original color
                    binding.birthday.text = birthday
                }

                if (age == 0 || age == null) {
                    binding.age.setTextColor(Color.rgb(182,182,182))
                    binding.age.text = "Enter your birthday to calculate your age."
                } else {
                    binding.age.setTextColor(Color.rgb(33,33,33)) // Change to the original color
                    binding.age.text = age.toString()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
            }
        })


        binding.signOutButton.setOnClickListener {
            val alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder.setTitle("Sign Out Confirmation")
            alertDialogBuilder.setMessage("Are you sure you want to sign out?")
            alertDialogBuilder.setPositiveButton("Yes") { dialogInterface, _ ->
                // Handle "Yes" button click
                signOut()
                dialogInterface.dismiss()
            }
            alertDialogBuilder.setNegativeButton("No") { dialogInterface, _ ->
                // Handle "No" button click, dismiss the dialog
                dialogInterface.dismiss()
            }

            val alertDialog: AlertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }

        binding.editButton.setOnClickListener {
            var userId = currentUserId()
            showProfileEdit(userId)
        }

        //BOTTOM NAV VIEW
        // Initialize and assign variable
        var bottomNavigationView = binding.bottomNavView
        binding.bottomNavView.selectedItemId = R.id.profile

        // Initialize and assign variable
        val selectedItem = bottomNavigationView.selectedItemId

        bottomNavigationView.setOnNavigationItemSelectedListener(navBarWhenClicked)
    }

    //EDITING THE PROFILE
    private fun showProfileEdit(userId: String?) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.activity_profile_edit)

        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.rgb(241, 242, 242)))
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)

        //GET THE DISPLAY NAME TEXT INPUT AND BIRTHDAY TEXT INPUT
        val displayNameInputEditText = dialog.findViewById<TextInputEditText>(R.id.displayNameTextInput)
        birthdayInputEditText = dialog.findViewById<TextInputEditText>(R.id.birthdayTextInput)


        //GET THE VALUES FROM THE DATABASE AND DISPLAY IT
        val database = FirebaseDatabase.getInstance("https://rescyou-57570-default-rtdb.asia-southeast1.firebasedatabase.app/")
        val myRef = database.getReference("Users").child(userId!!)


        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val displayName = dataSnapshot.child("displayName").getValue(String::class.java).toString()
                birthday = dataSnapshot.child("birthday").value?.toString()

                displayNameInputEditText.setText(displayName)



                if (birthday.isNullOrEmpty()) {
                    birthdayInputEditText.setText("Enter your birthday")

                    birthdayInputEditText.setOnClickListener { view ->
                        // GET THE DATE PICKER FOR BIRTHDAY
                        val currentDate = Calendar.getInstance()

                        // Set the date range for the DatePickerDialog
                        val datePickerDialog = DatePickerDialog(
                            this@Profile,
                            this@Profile,
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        )

                        // Set the maximum date to the current date.
                        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()

                        // Set the minimum date to one year ago from the current date.
                        currentDate.add(Calendar.YEAR, -1)
                        datePickerDialog.datePicker.minDate = currentDate.timeInMillis

                        datePickerDialog.show()
                    }
                } else {
                    birthdayInputEditText.setText(birthday)

                    birthdayInputEditText.setOnClickListener { view ->
                        // GET THE DATE PICKER FOR BIRTHDAY
                        val datePickerDialog = DatePickerDialog(
                            this@Profile,
                            this@Profile,
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        )

                        // Set the maximum date to one year ago from the current date.
                        val currentDate = Calendar.getInstance()
                        currentDate.add(Calendar.YEAR, -1)
                        datePickerDialog.datePicker.maxDate = currentDate.timeInMillis

                        datePickerDialog.show()
                    }
                }



                if (age == 0) {
                    binding.age.setTextColor(Color.rgb(182,182,182))
                    binding.age.text = "Enter your birthday to calculate your age."
                } else {
                    binding.age.setTextColor(Color.rgb(33,33,33)) // Change to the original color
                    binding.age.text = age.toString()
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
            }
        })

        // When the "Save" button is clicked, update the values in the database
        val saveButton = dialog.findViewById<Button>(R.id.saveButton)
        saveButton.setOnClickListener {
            if (displayNameInputEditText.text.toString().trim().isEmpty() ||
                birthdayInputEditText.text.toString().trim().isEmpty()) {
                Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show()
            } else {
                val alertDialogBuilder = AlertDialog.Builder(this)
                alertDialogBuilder.setTitle("Save Confirmation")
                alertDialogBuilder.setMessage("Are you sure you want to save?")
                alertDialogBuilder.setPositiveButton("Yes") { dialogInterface, _ ->
                    // Handle "Yes" button click
                    dialogInterface.dismiss()


                    val dbRef = FirebaseDatabase.getInstance("https://rescyou-57570-default-rtdb.asia-southeast1.firebasedatabase.app/")
                        .reference

                    // Show loading dialog
                    showLoadingDialog()

                    if (userId != null){

                        val user = FirebaseAuth.getInstance().currentUser

                        //Set the display name of the user
                        val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                            .setDisplayName(displayNameInputEditText.text.toString())
                            .build()

                        user?.updateProfile(profileUpdates)
                            ?.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d(ContentValues.TAG, "User profile updated.")
                                }
                            }


                        dbRef.child("Users").child(userId).child("displayName").setValue(displayNameInputEditText.text.toString())
                        dbRef.child("Users").child(userId).child("birthday").setValue(birthdayInputEditText.text.toString())
                        dbRef.child("Users").child(userId).child("age").setValue(age)
                            .addOnCompleteListener { task ->
                                // Dismiss loading dialog
                                dismissLoadingDialog()
                                if (task.isSuccessful) {
                                    val intent = Intent(this@Profile, Profile::class.java)
                                    startActivity(intent)

                                    Toast.makeText(this, "Profile has been updated successfully.", Toast.LENGTH_SHORT).show()
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
        }

        //BACK BUTTON
        val backButtonLayout = dialog.findViewById<RelativeLayout>(R.id.back_layout)
        backButtonLayout.setOnClickListener {
            val intent = Intent(this@Profile, Profile::class.java)
            startActivity(intent)
        }

        //CANCEL BUTTON
        val cancelButton = dialog.findViewById<Button>(R.id.cancelButton)
        cancelButton.setOnClickListener {
            val alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder.setTitle("Cancel Confirmation")
            alertDialogBuilder.setMessage("Are you sure you want to cancel?")
            alertDialogBuilder.setPositiveButton("Yes") { dialogInterface, _ ->
                val intent = Intent(this, Profile::class.java)
                startActivity(intent)
                finish()
                dialogInterface.dismiss()

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

    //Get the Age using the current date
    private fun getAge(year: Int, month: Int, day: Int): Int {
        val dateOfBirth = Calendar.getInstance()
        val today = Calendar.getInstance()
        dateOfBirth[year, month] = day
        age = today[Calendar.YEAR] - dateOfBirth[Calendar.YEAR]
        if (today[Calendar.DAY_OF_YEAR] < dateOfBirth[Calendar.DAY_OF_YEAR]) {
            age--
        }
        return age
    }





    //Format the date

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        calendar.set(year, month, dayOfMonth)
        displayFormattedDate(calendar.timeInMillis)
        getAge(year,month, dayOfMonth)
    }

    //Display the date
    private fun displayFormattedDate(timestamp: Long) {
        birthdayInputEditText.setText(formatter.format(timestamp))
        birthday = birthdayInputEditText.text.toString()
        Log.i("Formatting", timestamp.toString())
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

    private fun signOut() {

        Toast.makeText(applicationContext,"Sign out successfully.", Toast.LENGTH_SHORT).show()


        FirebaseAuth.getInstance().signOut()
        val i  = Intent(this,MainActivity::class.java)
        startActivity(i)
    }

    //NAV BAR
    private val navBarWhenClicked = BottomNavigationView.OnNavigationItemSelectedListener { item ->

        when (item.itemId) {
            R.id.home -> {
                val intent = Intent(this, Home::class.java)
                startActivity(intent)
                return@OnNavigationItemSelectedListener true
            }

            R.id.tools -> {
                val intent = Intent(this, Tools::class.java)
                startActivity(intent)
//                binding.bottomNavView.isSelected= true
                return@OnNavigationItemSelectedListener true
            }

            R.id.info -> {
                val intent = Intent(this, Information::class.java)
                startActivity(intent)
                return@OnNavigationItemSelectedListener true
            }

            R.id.profile -> {
                // Check if the current activity is not Profile before starting it
                if (this !is Profile) {
                    val intent = Intent(this, Profile::class.java)
                    startActivity(intent)
                }
                return@OnNavigationItemSelectedListener true


            }
        }
        return@OnNavigationItemSelectedListener false
    }
}