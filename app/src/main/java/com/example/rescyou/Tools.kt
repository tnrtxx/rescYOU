package com.example.rescyou

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.media.AudioManager
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.rescyou.databinding.ActivityTermsAndConditionsBinding
import com.example.rescyou.databinding.ActivityToolsBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class Tools : AppCompatActivity() {

    private lateinit var binding: ActivityToolsBinding
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var audioManager: AudioManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityToolsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // Whistle
        binding.whistleButton.setOnClickListener {
            if (mediaPlayer?.isPlaying == true) {
                // If the whistle is playing, stop it and change the button text to "PLAY".
                mediaPlayer?.stop()
                mediaPlayer?.prepare() // prepare for next start
                binding.whistleButton.text = "PLAY"
            } else {
                // If the whistle is not playing, start it and change the button text to "STOP".
                mediaPlayer?.start()
                binding.whistleButton.text = "STOP"
            }
        }

        // Compass
        binding.openCompassButton.setOnClickListener {
            val intent = Intent(this, Compass::class.java)
            startActivity(intent)
        }

        //BOTTOM NAV VIEW
        // Initialize and assign variable
        var bottomNavigationView = binding.bottomNavView
        binding.bottomNavView.selectedItemId = R.id.tools

        // Initialize and assign variable
        val selectedItem = bottomNavigationView.selectedItemId
        // Toast.makeText(applicationContext, selectedItem.toString(), Toast.LENGTH_SHORT).show()

        bottomNavigationView.setOnNavigationItemSelectedListener(navBarWhenClicked)
    }

    override fun onStart() {
        super.onStart()
        setupMediaPlayer()  // Set up the media player.
        setVolumeToMax()  // Set the volume to maximum.
        binding.whistleButton.text = "PLAY"
    }

    override fun onStop() {
        super.onStop()
        // If the siren is playing, stop it.
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.stop()
        }
        // Release resources used by the media player.
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun setupMediaPlayer() {
        mediaPlayer = MediaPlayer.create(this, R.raw.whistle)
        mediaPlayer?.isLooping = true  // Set the media player to loop the sound.
    }

    private fun setVolumeToMax() {
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0)
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
        return@OnNavigationItemSelectedListener false
    }

}