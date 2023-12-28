package com.example.rescyou

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.rescyou.databinding.ActivityDialogBinding
import com.example.rescyou.databinding.ActivityEditPinBinding

class EditPin : AppCompatActivity() {
    private lateinit var binding: ActivityEditPinBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditPinBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}