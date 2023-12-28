package com.example.rescyou

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.rescyou.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.auth.User
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    //Initialize variables
//Personal Information
    var firstName: String?= null
    var middleName: String?= null
    var lastName: String?= null
    var suffixName: String?= null
    var birthday: String = ""
    var age: Int = 0

//Account Information
    private lateinit var email: String
    private lateinit var password: String

    //For Realtime Database
    private lateinit var database: DatabaseReference
    private lateinit var data: FirebaseDatabase

    //for FirebaseAuth (SIGN UP/REGISTRATION OF USER)

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

//                checkIfFirstTime()

//                //initialize database
//                database = Firebase.database.reference
//
//                //Check if user exists in database
//                //Get the User details
//                val user = auth.currentUser
//                updateUI(user)
//
//                //geting the userID
//                val userID= user?.uid.toString()
//
//                val rootRef = FirebaseDatabase.getInstance("https://rescyou-57570-default-rtdb.asia-southeast1.firebasedatabase.app/").reference.child("Users").child(userID)
//                rootRef.addListenerForSingleValueEvent(object : ValueEventListener {
//                    override fun onDataChange(dataSnapshot: DataSnapshot) {
//                        if (dataSnapshot.exists()) {
//                            // firebase user data is present in db, do appropiate action or take user to home screen
//                            return
//                        } else {
//                            val intent = Intent(this, TermsAndConditions::class.java)
////                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                            startActivity(intent)
//
//                        }
//                    }
//
//                    override fun onCancelled(databaseError: DatabaseError) {}
//                })
//

            } catch (e: ApiException) {

                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    private fun saveDisplayNameToFirebase(displayName: String) {
        // Assuming you have a reference to your Firebase Database
        val user = auth.currentUser ?: return
        val userID: String = auth.currentUser?.uid ?: return
        email = user.email ?: "No email" // Default value in case email is null
        val database = FirebaseDatabase.getInstance("https://rescyou-57570-default-rtdb.asia-southeast1.firebasedatabase.app/")
        val userRef = database.reference

        //store data to the REALTIME DATABASE
        // Save the personal details' field in the user's node
        userRef.child("Users").child(userID).child("displayName").setValue(displayName)
        userRef.child("Users").child(userID).child("birthday").setValue(birthday)
        userRef.child("Users").child(userID).child("age").setValue(age)
        userRef.child("Users").child(userID).child("email").setValue(email) // Save email

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            Log.d(TAG, token)
            Toast.makeText(baseContext, token, Toast.LENGTH_SHORT).show()

            // Save the FCM token to Firebase
            saveFcmTokenToFirebase(token)
        })
    }

    private fun saveFcmTokenToFirebase(token: String?) {
        // Assuming you have a reference to your Firebase Database
        val userID: String = auth.currentUser?.uid ?: return
        val database = FirebaseDatabase.getInstance("https://rescyou-57570-default-rtdb.asia-southeast1.firebasedatabase.app/")
        val userRef = database.reference

        // Save the FCM token to the "fcmToken" field in the user's node
        userRef.child("Users").child(userID).child("fcmToken").setValue(token)

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
                    // Now you can save the display name to Firebase
                    saveDisplayNameToFirebase(user?.displayName ?: "")
                    Toast.makeText(applicationContext, "Success", Toast.LENGTH_SHORT).show()

                    // Start Home activity here
                    val intent = Intent(this, Home::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }

//    fun storeData(userID:String, firstName: String, middleName: String, lastName: String, suffixName: String,  birthday: String, age: Int, email: String){
//        //initialize database
//        database = Firebase.database.reference
//
//        data = FirebaseDatabase.getInstance("https://rescyou-57570-default-rtdb.asia-southeast1.firebasedatabase.app/")
//        val myRef = data.reference
//
//        //store data to the REALTIME DATABASE
//        myRef.child("Users").child(userID).child("firstName").setValue(firstName)
//        myRef.child("Users").child(userID).child("middleName").setValue(middleName)
//        myRef.child("Users").child(userID).child("lastName").setValue(lastName)
//        myRef.child("Users").child(userID).child("suffix").setValue(suffixName)
//        myRef.child("Users").child(userID).child("birthday").setValue(birthday)
//        myRef.child("Users").child(userID).child("age").setValue(age)
//        myRef.child("Users").child(userID).child("email").setValue(email)
//
//
//
//    }
    // [END auth_with_google]

    // [START signin]
    private fun signIn() {
        // Initialize Firebase Auth
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

    //SAVING TO DATABASE
//    fun checkIfFirstTime(){
//        //initialize database
//        database = Firebase.database.reference
//
//        //Check if user exists in database
//                //Get the User details
//                val user = auth.currentUser
//                updateUI(user)
//
//                //geting the userID
//                val userID= user?.uid.toString()
//
//        val rootRef = FirebaseDatabase.getInstance("https://rescyou-57570-default-rtdb.asia-southeast1.firebasedatabase.app/").reference.child("Users").child(userID)
//                rootRef.addListenerForSingleValueEvent(object : ValueEventListener {
//                    override fun onDataChange(dataSnapshot: DataSnapshot) {
//                        if (dataSnapshot.exists()) {
//                            HomeActivity()
//                            return
//                        } else {
//                            TermsAndConditionsActivity()
//                        }
//                    }
//
//                    override fun onCancelled(databaseError: DatabaseError) {}
//                })
//
//    }

    //NEW INTENT
//    fun TermsAndConditionsActivity(){
//        val intent = Intent(this, TermsAndConditions::class.java)
////                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        startActivity(intent)
//    }
//
//    fun HomeActivity(){
//            val intent = Intent(this, Home::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            startActivity(intent)
//        }

}


