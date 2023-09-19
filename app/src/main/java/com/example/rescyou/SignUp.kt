package com.example.rescyou

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.TextView
import com.example.rescyou.databinding.ActivityMainBinding
import com.example.rescyou.databinding.ActivitySignUpBinding
import java.util.Calendar


private lateinit var binding: ActivitySignUpBinding
class SignUp : AppCompatActivity() {

    // on below line we are creating a variable.
    lateinit var pickDateBtn: Button
    lateinit var selectedDateTV: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.birthdayTextInput.setOnClickListener {
            DatePickerDialog(this).show()
        }

    }
}
