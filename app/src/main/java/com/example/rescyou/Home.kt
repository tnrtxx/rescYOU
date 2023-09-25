package com.example.rescyou

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.rescyou.databinding.ActivityHomeBinding
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth



private lateinit var binding: ActivityHomeBinding

private lateinit var auth: FirebaseAuth
private lateinit var googleSignInClient: GoogleSignInClient




class Home : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize and assign variable
        var bottomNavigationView: BottomNavigationView = binding.bottomNavView

        // Initialize and assign variable
       val selectedItem = bottomNavigationView.selectedItemId
        Toast.makeText(applicationContext, selectedItem.toString(), Toast.LENGTH_SHORT).show()

        bottomNavigationView.setOnNavigationItemSelectedListener(navBarWhenClicked)

    }

        private val navBarWhenClicked= BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    return@OnNavigationItemSelectedListener false
                }
                R.id.tools -> {
                    Toast.makeText(applicationContext, "tools", Toast.LENGTH_SHORT).show()
                    return@OnNavigationItemSelectedListener true
                }
                R.id.info -> {
                    Toast.makeText(applicationContext, "info", Toast.LENGTH_SHORT).show()

                    return@OnNavigationItemSelectedListener true
                }
                R.id.profile -> {
                    Toast.makeText(applicationContext, "profile", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, Profile::class.java)
                    startActivity(intent)

                    return@OnNavigationItemSelectedListener true
                }
            }
            false

        }


    }








