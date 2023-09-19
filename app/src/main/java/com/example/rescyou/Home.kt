package com.example.rescyou

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.rescyou.databinding.ActivityHomeBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

private lateinit var binding: ActivityHomeBinding
private lateinit var auth: FirebaseAuth
private lateinit var googleSignInClient: GoogleSignInClient
class Home : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.signOutButton.setOnClickListener {
            signOut()



        }
    }

    private fun signOut() {

        Toast.makeText(applicationContext,"signout", Toast.LENGTH_SHORT).show()


        FirebaseAuth.getInstance().signOut()
        val i  = Intent(this,MainActivity::class.java)
        startActivity(i)
    }



}