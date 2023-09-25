package com.example.rescyou

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.rescyou.databinding.ActivityPinMyLocationBinding
import com.example.rescyou.databinding.ActivityProfileBinding

private lateinit var binding: ActivityPinMyLocationBinding

class PinMyLocation : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPinMyLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}