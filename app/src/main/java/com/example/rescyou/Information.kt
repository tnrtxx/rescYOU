package com.example.rescyou

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.rescyou.databinding.ActivityCompassBinding
import com.example.rescyou.databinding.ActivityInformationBinding
import com.google.android.material.bottomnavigation.BottomNavigationView


class Information : AppCompatActivity() {

    private lateinit var binding: ActivityInformationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //PREPAREDNESS TIPS
        binding.tipsButton.setOnClickListener {
            val intent = Intent(this, PreparednessTips::class.java)
            startActivity(intent)
        }

        //HOTLINES
        binding.hotlinesButton.setOnClickListener {
            val intent = Intent(this, Hotlines::class.java)
            startActivity(intent)
        }


        //EVACUATION CENTERS
        binding.centersButton.setOnClickListener {
            val intent = Intent(this, EvacuationCenters::class.java)
            startActivity(intent)
        }

        //BOTTOM NAV VIEW
        // Initialize and assign variable
        var bottomNavigationView = binding.bottomNavView
        binding.bottomNavView.selectedItemId = R.id.info

        // Initialize and assign variable
        val selectedItem = bottomNavigationView.selectedItemId
        // Toast.makeText(applicationContext, selectedItem.toString(), Toast.LENGTH_SHORT).show()

        bottomNavigationView.setOnNavigationItemSelectedListener(navBarWhenClicked)
    }

    //NAV BAR
    private val navBarWhenClicked = BottomNavigationView.OnNavigationItemSelectedListener { item ->

        when (item.itemId) {
            R.id.home -> {
                val intent = Intent(this, Home::class.java)
                startActivity(intent)
                return@OnNavigationItemSelectedListener true
            }

            R.id.tools -> {
                Toast.makeText(applicationContext, "tools", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, Tools::class.java)
                startActivity(intent)
//                binding.bottomNavView.isSelected= true
                return@OnNavigationItemSelectedListener true
            }

            R.id.info -> {
                Toast.makeText(applicationContext, "information", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, Information::class.java)
                startActivity(intent)
                return@OnNavigationItemSelectedListener true
            }

            R.id.profile -> {
                Toast.makeText(applicationContext, "profile", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, Profile::class.java)
                startActivity(intent)
                return@OnNavigationItemSelectedListener true


            }
        }
        return@OnNavigationItemSelectedListener false
    }

}