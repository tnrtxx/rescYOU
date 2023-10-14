package com.example.rescyou

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.rescyou.databinding.ActivitySendOtpBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import com.google.firebase.auth.PhoneAuthProvider.verifyPhoneNumber
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit


private lateinit var binding: ActivitySendOtpBinding

private lateinit var mobile: String

// [START declare_auth]
private lateinit var auth: FirebaseAuth
// [END declare_auth]

private var storedVerificationId: String? = ""
private var verificationId: String? = ""
private lateinit var resendToken: ForceResendingToken
private lateinit var callbacks: OnVerificationStateChangedCallbacks

private lateinit var code: String


class SendOtp : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySendOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        // [START initialize_auth]
//        // Initialize Firebase Auth
//        auth = FirebaseAuth.getInstance()
//        // [END initialize_auth]
//
//        binding.getOTPButton.setOnClickListener {
//
//            //Get the mobile number
//            mobile = binding.mobileTextInput.text.toString()
//
//
//
//            if (mobile.trim().length == 0) {
//                Toast.makeText(
//                    applicationContext,
//                    "Please enter your mobile number.",
//                    Toast.LENGTH_SHORT
//                ).show()
//            } else {
//
//                // if the text field is not empty we are calling our
//                // send OTP method for getting OTP from Firebase.
//                val phone = "+63" + mobile
//                Toast.makeText(applicationContext, phone, Toast.LENGTH_SHORT).show()
//
//                val options = PhoneAuthOptions.newBuilder(auth)
//                    .setPhoneNumber(phone) // Phone number to verify
//                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
//                    .setActivity(this) // Activity (for callback binding)
//                    .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
//                    .build()
//                verifyPhoneNumber(options)
//
//
//                // Initialize phone auth callbacks
//                // [START phone_auth_callbacks]
//                callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
//
//                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
//                        // This callback will be invoked in two situations:
//                        // 1 - Instant verification. In some cases the phone number can be instantly
//                        //     verified without needing to send or enter a verification code.
//                        // 2 - Auto-retrieval. On some devices Google Play services can automatically
//                        //     detect the incoming verification SMS and perform verification without
//                        //     user action.
//                        Log.d(TAG, "onVerificationCompleted:$credential")
//                        signInWithPhoneAuthCredential(credential)
//                        Toast.makeText(applicationContext, "Verification complete", Toast.LENGTH_SHORT).show()
//
//                    }
//
//                    override fun onVerificationFailed(e: FirebaseException) {
//                        // This callback is invoked in an invalid request for verification is made,
//                        // for instance if the the phone number format is not valid.
//                        Log.w(TAG, "onVerificationFailed", e)
//
//                        if (e is FirebaseAuthInvalidCredentialsException) {
//                            // Invalid request
//                        } else if (e is FirebaseTooManyRequestsException) {
//                            // The SMS quota for the project has been exceeded
//                        } else if (e is FirebaseAuthMissingActivityForRecaptchaException) {
//                            // reCAPTCHA verification attempted with null Activity
//                        }
//
//                        // Show a message and update the UI
//                    }
//
//                    override fun onCodeSent(
//                        verificationId: String,
//                        token: PhoneAuthProvider.ForceResendingToken,
//                    ) {
//                        // The SMS verification code has been sent to the provided phone number, we
//                        // now need to ask the user to enter the code and then construct a credential
//                        // by combining the code with a verification ID.
//                        Log.d(TAG, "onCodeSent:$verificationId")
//
//                        // Save verification ID and resending token so we can use them later
//                        storedVerificationId = verificationId
//                        resendToken = token
//
//                        Toast.makeText(applicationContext, "Code sent.", Toast.LENGTH_SHORT).show()
//                    }
//                }
//                // [END phone_auth_callbacks]
//
//
//
//
//            }
//        }
//
//
//    }
//
//    // [START sign_in_with_phone]
//    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
//        auth.signInWithCredential(credential)
//            .addOnCompleteListener(this) { task ->
//                if (task.isSuccessful) {
//                    // Sign in success, update UI with the signed-in user's information
//                    Log.d(TAG, "signInWithCredential:success")
//
//                    val user = task.result?.user
//                } else {
//                    // Sign in failed, display a message and update the UI
//                    Log.w(TAG, "signInWithCredential:failure", task.exception)
//                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
//                        // The verification code entered was invalid
//                    }
//                    // Update UI
//                }
//            }
//    }
//    // [END sign_in_with_phone]
//
//    private fun updateUI(user: FirebaseUser? = auth.currentUser) {
//    }
//
//    companion object {
//        private const val TAG = "PhoneAuthActivity"
//    }
    }
}









