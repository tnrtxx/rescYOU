package com.example.rescyou

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.rescyou.databinding.ActivitySendOtpBinding
import com.example.rescyou.databinding.ActivityVerifyOtpBinding

private lateinit var binding: ActivityVerifyOtpBinding

class VerifyOtp : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}