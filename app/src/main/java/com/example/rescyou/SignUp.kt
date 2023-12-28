package com.example.rescyou

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.CheckBox
import android.widget.DatePicker
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.rescyou.databinding.ActivitySignUpBinding
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.regex.Pattern




//Initialize DateSetListener
class SignUp : AppCompatActivity(), DatePickerDialog.OnDateSetListener{

    private lateinit var binding: ActivitySignUpBinding

    //Initialize variables
//Personal Information
    private lateinit var firstName: String
    private lateinit var middleName: String
    private lateinit var lastName: String
    private lateinit var displayName: String
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

    private lateinit var googleSignInClient : GoogleSignInClient

    private lateinit var userID: String


    //For DatePickerDialog
    private val calendar = Calendar.getInstance()
    private val formatter = SimpleDateFormat("MMMM d, yyyy", Locale.US)

    //Pssword matcher
    val passwordPattern = "^(?=.*[a-z])(?=.*[A-Z]).{6,}$"
    val passwordMatcher = Regex(passwordPattern)


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

            } else if (!passwordMatcher.matches(password.trim())) {
                Toast.makeText(
                    applicationContext,
                    "Password should be at least 6 characters, with at least 1 uppercase and 1 lowercase letter.",
                    Toast.LENGTH_SHORT
                ).show()

            } else if(checkEmail(email)==false && password.trim().length<6 ){
                Toast.makeText(applicationContext, "Invalid Email and password should be at least 6 characters.", Toast.LENGTH_SHORT).show()

            }else{

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            showTermsAndConditions()

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
        Log.i("Formatting", timestamp.toString())

    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            finish()
            overridePendingTransition(0, 0)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
    }

    private fun showTermsAndConditions() {
        val dialog = Dialog(this@SignUp)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.activity_terms_and_conditions)

        // Find the TextView in the dialog
        val termsTextView = dialog.findViewById<TextView>(R.id.TC_content)
        var termsAndConditions = getString(R.string.termsAncConditions_content)
        termsAndConditions = termsAndConditions.replace("\n", "<br/>").replace(" ", "&nbsp;")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            termsTextView.text = Html.fromHtml(termsAndConditions, Html.FROM_HTML_MODE_COMPACT)
        } else {
            termsTextView.text = Html.fromHtml(termsAndConditions)
        }

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



        //AGREE BUTTON
        val agreeButton = dialog.findViewById<Button>(R.id.agreeButton)
        agreeButton.setOnClickListener {
            val checkBox = dialog.findViewById<CheckBox>(R.id.acceptCheckbox)
            if (checkBox.isChecked) {

                // Sign in success, update UI with the signed-in user's information
                Log.d(TAG, "createUserWithEmail:success")
                val user = auth.currentUser
                userID= user?.uid.toString()
                displayName = firstName + " " + lastName


                //Set the display name of the user
                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()

                user?.updateProfile(profileUpdates)
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "User profile updated.")
                        }
                    }

                updateUI(user)

                storeData(userID, firstName, middleName, lastName, displayName, suffixName, birthday, age, email, password)




                val intent = Intent(this, Home::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                dialog.dismiss()

            } else {
                Toast.makeText(this, "Please agree to the terms and conditions.", Toast.LENGTH_SHORT).show()
            }
        }

        //DISAGREE BUTTON
        val declineButton = dialog.findViewById<Button>(R.id.declineButton)
        declineButton.setOnClickListener {
            // Get the current user
            val user = auth.currentUser
            user?.delete()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(MainActivity.TAG, "User account deleted.")
                }
            }

            // Sign out from Google
            googleSignInClient.signOut().addOnCompleteListener {
                // After sign out is completed, navigate back to MainActivity
                val intent = Intent(this@SignUp, MainActivity::class.java) // Create the Intent object
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent) // Use the Intent object to start the MainActivity
            }
        }

    }

    private fun updateUI(user: FirebaseUser?) {
    }

    fun storeData(userID:String, firstName: String, middleName: String, lastName: String, displayName:String, suffixName: String,  birthday: String, age: Int, email: String, password: String){
        //initialize database
        database = Firebase.database.reference

        data = FirebaseDatabase.getInstance("https://rescyou-57570-default-rtdb.asia-southeast1.firebasedatabase.app/")
        val myRef = data.reference

        var displayName: String = firstName + " " + lastName + suffixName

        //store data to the REALTIME DATABASE
        myRef.child("Users").child(userID).child("firstName").setValue(firstName)
        myRef.child("Users").child(userID).child("middleName").setValue(middleName)
        myRef.child("Users").child(userID).child("lastName").setValue(lastName)
        myRef.child("Users").child(userID).child("displayName").setValue(displayName)
        myRef.child("Users").child(userID).child("suffix").setValue(suffixName)
        myRef.child("Users").child(userID).child("birthday").setValue(birthday)
        myRef.child("Users").child(userID).child("age").setValue(age)
        myRef.child("Users").child(userID).child("email").setValue(email)

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            Log.d(TAG, token)
            // Save the FCM token to Firebase
            saveFcmTokenToFirebase(token)
        })



    }

    private fun saveFcmTokenToFirebase(token: String?) {

        val database = FirebaseDatabase.getInstance("https://rescyou-57570-default-rtdb.asia-southeast1.firebasedatabase.app/")
        val userRef = database.reference

        // Save the FCM token to the "fcmToken" field in the user's node
        userRef.child("Users").child(userID).child("fcmToken").setValue(token)

    }


}

