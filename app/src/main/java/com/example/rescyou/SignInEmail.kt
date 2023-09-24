package com.example.rescyou

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.rescyou.databinding.ActivitySignInEmailBinding
import com.example.rescyou.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

private lateinit var binding: ActivitySignInEmailBinding

//Account Information
private lateinit var email: String
private lateinit var password: String


private lateinit var auth: FirebaseAuth
class SignInEmail : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = Firebase.auth

        //FORGOT PASSWORD
        binding.forgotPasswordTextView.setOnClickListener{
            val intent = Intent(this, ForgotPassword::class.java)
            startActivity(intent)

        }


        //SIGN IN BUTTON
        binding.signInButton.setOnClickListener {

            //ACCOUNT INFORMATION
            email = binding.emailTextInput.text.toString()
            password = binding.passwordTextInput.text.toString()

            if(email.trim().length==0 || password.trim().length==0) {
                Toast.makeText(applicationContext, "Please fill up the requred field/s.", Toast.LENGTH_SHORT).show()
            }else {

                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success")
                            val user = auth.currentUser
                            updateUI(user)

                            val intent = Intent(this, Home::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            Toast.makeText(
                                applicationContext,
                                "sign in success",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.exception)
                            Toast.makeText(
                                baseContext,
                                "Authentication failed.",
                                Toast.LENGTH_SHORT,
                            ).show()
                            updateUI(null)
                        }
                    }

            }

        }
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


}