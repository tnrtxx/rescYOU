package com.example.rescyou

import android.app.DatePickerDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.rescyou.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.regex.Pattern


private lateinit var binding: ActivitySignUpBinding

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


//Initialize DateSetListener
class SignUp : AppCompatActivity(), DatePickerDialog.OnDateSetListener{

    //For DatePickerDialog
    private val calendar = Calendar.getInstance()
    private val formatter = SimpleDateFormat("MMMM d, yyyy", Locale.US)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Initialize Firebase Auth
        auth = Firebase.auth

        // For Sign Up
        binding.signUpButton.setOnClickListener {
            //Get the values from TextInputEditText

            //PERSONAL INFORMATION
            firstName = binding.firstNameTextInput.text.toString()
            middleName = binding.middleNameTextInput.text.toString()
            lastName = binding.lastNameTextInput.text.toString()
            suffixName = binding.suffixNameTextInput.text.toString()


            //ACCOUNT INFORMATION
            email = binding.emailTextInput.text.toString()
            password = binding.passwordTextInput.text.toString()



//            Check for the required values
            if(firstName.trim().length==0 || lastName.trim().length==0 ||  birthday.trim().length==0 ||  email.trim().length==0) {
                Toast.makeText(applicationContext, "Please fill up the requred field/s.", Toast.LENGTH_SHORT).show()
            }else if(checkEmail(email)==false){
                Toast.makeText(applicationContext, "Invalid Email", Toast.LENGTH_SHORT).show()

            }else if(password.trim().length<6){
                Toast.makeText(applicationContext, "Password should be at least 6 characters.", Toast.LENGTH_SHORT).show()

            }else if(checkEmail(email)==false && password.trim().length<6 ){
                Toast.makeText(applicationContext, "Invalid Email and password should be at least 6 characters.", Toast.LENGTH_SHORT).show()

            }else{

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success")
                            val user = auth.currentUser
                            updateUI(user)
                            val userID= user?.uid.toString()

                            Toast.makeText(applicationContext, userID, Toast.LENGTH_SHORT).show()


                            storeData(userID, firstName, middleName, lastName, suffixName, birthday, age, email, password)




                            val intent = Intent(this, Home::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            Toast.makeText(applicationContext, userID, Toast.LENGTH_SHORT).show()
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.exception)
                            Toast.makeText(
                                baseContext,
                                "Email already exists.",
                                Toast.LENGTH_SHORT,
                            ).show()
                            updateUI(null)
                        }
                    }


            }
        }


        //For DatePickerDialog
        binding.birthdayTextInput.setOnClickListener {
            //Get the date from the DatePickerDialog
            DatePickerDialog(
                this,
                this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()

        }

    }

    //Email Address Pattern
    val EMAIL_ADDRESS_PATTERN: Pattern = Pattern.compile(
        "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                "\\@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(" +
                "\\." +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                ")+"
    )

    //Check Email Pattern
    private fun checkEmail(email: String): Boolean {
        return EMAIL_ADDRESS_PATTERN.matcher(email).matches()
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
        binding.birthdayTextInput.setText(formatter.format(timestamp))
        birthday=binding.birthdayTextInput.text.toString()
        Toast.makeText(applicationContext, birthday, Toast.LENGTH_SHORT).show()

        Log.i("Formatting", timestamp.toString())

    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            finish();
            overridePendingTransition(0, 0);
            startActivity(getIntent());
            overridePendingTransition(0, 0);
        }
    }

    private fun updateUI(user: FirebaseUser?) {
    }

    fun storeData(userID:String, firstName: String, middleName: String, lastName: String, suffixName: String,  birthday: String, age: Int, email: String, password: String){
        //initialize database
        database = Firebase.database.reference

        data = FirebaseDatabase.getInstance("https://rescyou-57570-default-rtdb.asia-southeast1.firebasedatabase.app/")
        val myRef = data.reference

        //store data to the REALTIME DATABASE
        myRef.child("Users").child(userID).child("firstName").setValue(firstName)
        myRef.child("Users").child(userID).child("middleName").setValue(middleName)
        myRef.child("Users").child(userID).child("lastName").setValue(lastName)
        myRef.child("Users").child(userID).child("suffix").setValue(suffixName)
        myRef.child("Users").child(userID).child("birthday").setValue(birthday)
        myRef.child("Users").child(userID).child("age").setValue(age)
        myRef.child("Users").child(userID).child("email").setValue(email)



    }


}

