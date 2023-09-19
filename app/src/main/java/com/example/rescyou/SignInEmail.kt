package com.example.rescyou

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.rescyou.databinding.ActivitySignInEmailBinding
import com.example.rescyou.databinding.ActivitySignUpBinding

private lateinit var binding: ActivitySignInEmailBinding
class SignInEmail : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}