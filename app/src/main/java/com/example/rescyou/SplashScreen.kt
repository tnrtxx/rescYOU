package com.example.rescyou

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


@Suppress("DEPRECATION")
class SplashScreen : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)


        auth = FirebaseAuth.getInstance()

        val firebaseUser: FirebaseUser? = auth.getCurrentUser()
        if (firebaseUser != null) {
            // set the new task and clear flags
            val intent = Intent(this, Home::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        } else {
            // This is used to hide the status bar and make
            // the splash screen as a full screen activity.
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )

            // we used the postDelayed(Runnable, time) method
            // to send a message with a delayed time.
            //Normal Handler is deprecated , so we have to change the code little bit

            // Handler().postDelayed({
            Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }, 1000) // it is the delayed time in milliseconds.
        }


    }

    private fun checkFirebaseUser() {
        val firebaseUser: FirebaseUser? = auth.getCurrentUser()
        if (firebaseUser != null) {
            //User is logged in already. You can proceed with your next screen
        } else {
            //User not logged in. So show email and password edit text for user to enter credentials
        }
    }
}