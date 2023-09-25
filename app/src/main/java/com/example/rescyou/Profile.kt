package com.example.rescyou

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.rescyou.databinding.ActivityHomeBinding
import com.example.rescyou.databinding.ActivityMainBinding
import com.example.rescyou.databinding.ActivityProfileBinding
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth

private lateinit var binding: ActivityProfileBinding

private lateinit var auth: FirebaseAuth
private lateinit var googleSignInClient: GoogleSignInClient

class Profile : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
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