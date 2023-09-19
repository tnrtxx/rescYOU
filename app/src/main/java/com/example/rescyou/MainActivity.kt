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
import com.google.firebase.ktx.Firebase


private lateinit var binding: ActivityMainBinding

class MainActivity : AppCompatActivity() {


//    private lateinit var oneTapClient: SignInClient
//    private lateinit var signInRequest: BeginSignInRequest
//    private val REQ_ONE_TAP = 2  // Can be any integer unique to the Activity
//    private var showOneTapUI = true
//    private lateinit var auth: FirebaseAuth

//    private lateinit var auth : FirebaseAuth
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



//        auth = FirebaseAuth.getInstance()
//
//        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestIdToken("1005866032280-in6ljm34otj2k73u0ni85ers0gutv3up.apps.googleusercontent.com")
//            .requestEmail()
//            .build()
//
//        googleSignInClient = GoogleSignIn.getClient(this , gso)


////        AUTHENTICATIONS
//        auth = Firebase.auth
//
//
//        oneTapClient = Identity.getSignInClient(this)
//        signInRequest = BeginSignInRequest.builder()
//            .setPasswordRequestOptions(
//                BeginSignInRequest.PasswordRequestOptions.builder()
//                    .setSupported(true)
//                    .build()
//            )
//            .setGoogleIdTokenRequestOptions(
//                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
//                    .setSupported(true)
//                    // Your server's client ID, not your Android client ID.
//                    .setServerClientId("1005866032280-in6ljm34otj2k73u0ni85ers0gutv3up.apps.googleusercontent.com")
//                    // Only show accounts previously used to sign in.
//                    .setFilterByAuthorizedAccounts(true)
//                    .build()
//            )
////            // Automatically sign in when exactly one credential is retrieved.
////            .setAutoSelectEnabled(false)
//            .build()
////
////        // ...





        //SIGN UP
        binding.signUpButton.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }

        //SIGN IN VIA EMAL
        binding.signInEmailButton.setOnClickListener {
            val intent = Intent(this, SignInEmail::class.java)
            startActivity(intent)
        }


        //SIGN IN VIA GMAIL
        binding.signInGmailButton.setOnClickListener {
            signIn()
            Toast.makeText(applicationContext, "Gmail", Toast.LENGTH_SHORT).show()


        //            signInGoogle()



//            oneTapClient.beginSignIn(signInRequest)
//                .addOnSuccessListener(this) { result ->
//                    try {
//                        startIntentSenderForResult(
//                            result.pendingIntent.intentSender, REQ_ONE_TAP,
//                            null, 0, 0, 0, null
//                        )
//                    } catch (e: IntentSender.SendIntentException) {
//                        Log.e(TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
//                    }
//                }
//                .addOnFailureListener(this) { e ->
//                    // No saved credentials found. Launch the One Tap sign-up flow, or
//                    // do nothing and continue presenting the signed-out UI.
//                    Log.d(TAG, e.localizedMessage)
//                }


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


//    private fun signInGoogle(){
//        val signInIntent = googleSignInClient.signInIntent
//        launcher.launch(signInIntent)
//    }
//
//    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
//            result ->
//        if (result.resultCode == Activity.RESULT_OK){
//
//            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
//            handleResults(task)
//        }
//    }
//
//    private fun handleResults(task: Task<GoogleSignInAccount>) {
//        if (task.isSuccessful){
//            val account : GoogleSignInAccount? = task.result
//            if (account != null){
//                updateUI(account)
//                Toast.makeText(applicationContext, "yes", Toast.LENGTH_SHORT).show()
//            }
//        }else{
//            Toast.makeText(applicationContext, "no", Toast.LENGTH_SHORT).show()
//            Toast.makeText(this, task.exception.toString() , Toast.LENGTH_SHORT).show()
//        }
//    }

//    private fun updateUI(account: GoogleSignInAccount) {
//        val credential = GoogleAuthProvider.getCredential(account.idToken , null)
//        auth.signInWithCredential(credential).addOnCompleteListener {
//            if (it.isSuccessful){
//                Toast.makeText(applicationContext, "update ui", Toast.LENGTH_SHORT).show()
//                val intent : Intent = Intent(this , Home::class.java)
//                intent.putExtra("email" , account.email)
//                intent.putExtra("name" , account.displayName)
//                startActivity(intent)
//
//            }else{
//                Toast.makeText(this, it.exception.toString() , Toast.LENGTH_SHORT).show()
//                Toast.makeText(applicationContext, "update ui no", Toast.LENGTH_SHORT).show()
//
//            }
//        }
//    }


//    private fun updateUI(user: FirebaseUser?) {
//
//}
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        val googleCredential = oneTapClient.getSignInCredentialFromIntent(data)
//        val idToken = googleCredential.googleIdToken
//
//        when (requestCode) {
//            REQ_ONE_TAP -> {
//                try {
//                    val credential = oneTapClient.getSignInCredentialFromIntent(data)
//                    val idToken = credential.googleIdToken
//                    val username = credential.id
//                    val password = credential.password
//                    when {
//                        idToken != null -> {
//                            // Got an ID token from Google. Use it to authenticate
//                            // with your backend.
//                            Log.d(TAG, "Got ID token.")
//                        }
//
//                        password != null -> {
//                            // Got a saved username and password. Use them to authenticate
//                            // with your backend.
//                            Log.d(TAG, "Got password.")
//                        }
//
//                        else -> {
//                            // Shouldn't happen.
//                            Log.d(TAG, "No ID token or password!")
//                        }
//                    }
//                } catch (e: ApiException) {
//                    when (e.statusCode) {
//                        CommonStatusCodes.CANCELED -> {
//                            Log.d(TAG, "One-tap dialog was closed.")
//                            // Don't re-prompt the user.
//                            showOneTapUI = false
//                        }
//
//                        CommonStatusCodes.NETWORK_ERROR -> {
//                            Log.d(TAG, "One-tap encountered a network error.")
//                            // Try again or just ignore.
//                        }
//
//                        else -> {
//                            Log.d(
//                                TAG, "Couldn't get credential from result." +
//                                        " (${e.localizedMessage})"
//                            )
//                        }
//                    }
//                }
//            }
//        }
//
//        when {
//            idToken != null -> {
//                // Got an ID token from Google. Use it to authenticate
//                // with Firebase.
//                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
//                auth.signInWithCredential(firebaseCredential)
//                    .addOnCompleteListener(this) { task ->
//                        if (task.isSuccessful) {
//                            // Sign in success, update UI with the signed-in user's information
//                            Log.d(TAG, "signInWithCredential:success")
//                            val user = auth.currentUser
//                            updateUI(user)
////                            GoogleSignInClient.clearDefaultAccountAndReconnect();
//                        } else {
//                            // If sign in fails, display a message to the user.
//                            Log.w(TAG, "signInWithCredential:failure", task.exception)
//                            updateUI(null)
//                        }
//                    }
//            }
//            else -> {
//                // Shouldn't happen.
//                Log.d(TAG, "No ID token!")
//            }
//        }}
//
//
//        override fun onStart() {
//            super.onStart()
//            // Check if user is signed in (non-null) and update UI accordingly.
//            val currentUser = auth.currentUser
//            updateUI(currentUser)
//        }




    }
