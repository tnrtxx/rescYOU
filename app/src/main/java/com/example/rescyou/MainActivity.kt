package com.example.rescyou

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.IntentSender
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.rescyou.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


private lateinit var binding: ActivityMainBinding

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

class MainActivity : AppCompatActivity() {


    private lateinit var googleSignInClient : GoogleSignInClient

    // [START declare_auth]
    private lateinit var auth: FirebaseAuth
    // [END declare_auth]
    override fun onCreate(savedInstanceState: Bundle?) {

        //VIEW BINDING
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // [START config_signin]
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1005866032280-6dnai3e3knn4ng744ogenbsf22i53o4j.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        // [END config_signin]

        // [START initialize_auth]
        // Initialize Firebase Auth
        auth = Firebase.auth
        // [END initialize_auth]




        //SIGN UP
        binding.signUpButton.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }

        //SIGN IN VIA EMAIL
        binding.signInEmailButton.setOnClickListener {
            val intent = Intent(this, SignInEmail::class.java)
            startActivity(intent)
        }


        //SIGN IN VIA GMAIL
        binding.signInGmailButton.setOnClickListener {
            signIn()
            Toast.makeText(applicationContext, "Gmail", Toast.LENGTH_SHORT).show()

        }
    }

    // [START on_start_check_user]
    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    // [END on_start_check_user]

    // [START onactivityresult]
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {

                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)




//                getProviderData()
                val user = auth.currentUser
                val userID= user?.uid.toString()
                Toast.makeText(applicationContext, userID, Toast.LENGTH_SHORT).show()


                val intent = Intent(this, Home::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)

            } catch (e: ApiException) {

                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }
    // [END onactivityresult]

    // [START auth_with_google]
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)



                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }
    // [END auth_with_google]

    // [START signin]
    private fun signIn() {
        // Initialize Firebase Auth
//        auth = Firebase.auth
//        val user = auth.currentUser
//        val userID= user?.uid.toString()
//        Toast.makeText(applicationContext, userID, Toast.LENGTH_SHORT).show()
//        getProviderData()
//        // Initialize Firebase Auth
//        auth = Firebase.auth
//        val user = auth.currentUser
//        val userID= user?.uid.toString()
//        Toast.makeText(applicationContext, userID, Toast.LENGTH_SHORT).show()
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }
    // [END signin]

    private fun updateUI(user: FirebaseUser?) {
    }

    companion object {
        private const val TAG = "GoogleActivity"
        private const val RC_SIGN_IN = 9001
    }
    fun storeData(userID:String, firstName: String, email: String){
        //initialize database
        database = Firebase.database.reference

        data = FirebaseDatabase.getInstance("https://rescyou-57570-default-rtdb.asia-southeast1.firebasedatabase.app/")
        val myRef = data.reference

        //store data to the REALTIME DATABASE
        myRef.child("Users").child(userID).child("firstName").setValue(firstName)
//        myRef.child("Users").child(userID).child("middleName").setValue(middleName)
//        myRef.child("Users").child(userID).child("lastName").setValue(lastName)
//        myRef.child("Users").child(userID).child("suffix").setValue(suffixName)
//        myRef.child("Users").child(userID).child("birthday").setValue(birthday)
//        myRef.child("Users").child(userID).child("age").setValue(age)
        myRef.child("Users").child(userID).child("email").setValue(email)



    }

    private fun getProviderData() {
        // [START get_provider_data]
        val user = Firebase.auth.currentUser
        user?.let {
            for (profile in it.providerData) {
                // Id of the provider (ex: google.com)
//                val providerId = profile.providerId

                // UID specific to the provider
                val uid = profile.uid
                val user = auth.currentUser

                // Name, email address, and profile photo Url
                firstName = profile.displayName.toString()
                email = profile.email.toString()
                val userID= user?.uid.toString()

                Toast.makeText(applicationContext, userID, Toast.LENGTH_SHORT).show()
                storeData(userID, firstName, email)

            }
        }
        // [END get_provider_data]
    }

    }
