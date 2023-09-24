package com.example.rescyou

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.rescyou.databinding.ActivityForgotPasswordBinding
import com.example.rescyou.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

private lateinit var binding: ActivityForgotPasswordBinding


private lateinit var auth: FirebaseAuth

private lateinit var email: String

class ForgotPassword : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Initialize Firebase Auth
        auth = Firebase.auth

        binding.resetPasswordButton.setOnClickListener {

            //Get the email
            email = binding.emailTextInput.text.toString()

            if( email.trim().length==0) {
                Toast.makeText(applicationContext, "Please enter your email.", Toast.LENGTH_SHORT).show()
            }else{
                sendPasswordReset()
            }

        }



    }

    private fun sendPasswordReset() {
        // [START send_password_reset]

        Firebase.auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Email sent.")

                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                else{
                    Log.w(TAG, "resetPassword:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Error",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        // [END send_password_reset]
    }
}