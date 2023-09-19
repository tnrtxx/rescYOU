package com.example.rescyou

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.rescyou.databinding.ActivityMainBinding


private lateinit var binding: ActivityMainBinding

class MainActivity : AppCompatActivity()  {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signUpButton.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }

        binding.signInEmailButton.setOnClickListener {
            Toast.makeText(applicationContext,"Email",Toast.LENGTH_SHORT).show()
        }

        binding.signInGmailButton.setOnClickListener {
            Toast.makeText(applicationContext,"Gmail",Toast.LENGTH_SHORT).show()
        }
    }
}