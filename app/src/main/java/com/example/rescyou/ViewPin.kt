package com.example.rescyou

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.rescyou.databinding.ActivityHomeBinding
import com.example.rescyou.databinding.ActivityInformationBinding
import com.example.rescyou.databinding.ActivityViewPinBinding

private lateinit var binding: ActivityViewPinBinding


class ViewPin : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewPinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //BACK BUTTON
        binding.backButton.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
        }

    }


}