package com.example.rescyou

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Switch
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.rescyou.databinding.ActivityToolsBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class Tools : AppCompatActivity() {


    private lateinit var binding: ActivityToolsBinding
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var audioManager: AudioManager
    private lateinit var cameraManager: CameraManager
    private var isFlash = false
    private lateinit var flashlightSwitch: Switch

    private var torchCallback: CameraManager.TorchCallback = object : CameraManager.TorchCallback() {
        override fun onTorchModeUnavailable(cameraId: String) {
            super.onTorchModeUnavailable(cameraId)
            // Handle the case where the flashlight is unavailable.
            isFlash = false
            updateFlashlightButton()
        }

        override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
            super.onTorchModeChanged(cameraId, enabled)
            // Update the flashlight status and the flashlight button when the flashlight status changes.
            isFlash = enabled
            updateFlashlightButton()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityToolsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        //Flashlight
        flashlightSwitch = findViewById(R.id.switchFlashlight_button)
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        flashlightSwitch.setOnClickListener { flashLightOnOrOff(it) }

        // Register the torch callback to get updates about the flashlight status.
        cameraManager.registerTorchCallback(torchCallback, null)


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

        bottomNavigationView.setOnNavigationItemSelectedListener(navBarWhenClicked)
    }

    private fun updateFlashlightButton() {
        // Update the flashlight button based on the flashlight status.
        if (isFlash) {
            flashlightSwitch.text = getString(R.string.switch_on)
            flashlightSwitch.isChecked = true
        } else {
            flashlightSwitch.text = getString(R.string.switch_off)
            flashlightSwitch.isChecked = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the torch callback when the activity is destroyed to avoid memory leaks.
        cameraManager.unregisterTorchCallback(torchCallback)
    }

    private fun flashLightOnOrOff(v: View?) {
        val switchFlashlight = findViewById<Switch>(R.id.switchFlashlight_button)

        try {
            val cameraListId = cameraManager.cameraIdList[0]

            if (!isFlash) {
                cameraManager.setTorchMode(cameraListId, true)
                isFlash = true
                switchFlashlight.text = getString(R.string.switch_on)
                switchFlashlight.isChecked = true
            } else {
                cameraManager.setTorchMode(cameraListId, false)
                isFlash = false
                switchFlashlight.text = getString(R.string.switch_off)
                switchFlashlight.isChecked = false
            }
        } catch (e: Exception) {
            // Handle any exceptions that may occur when accessing the camera or turning on/off the flashlight.
            // If there is an exception, disable the switch.
            switchFlashlight.isEnabled = false
            textMessage("Flashlight not available on this device", this)
        }
    }

    private fun textMessage(s: String, c:Context) {
        Toast.makeText(c,s,Toast.LENGTH_SHORT).show()
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
                turnOffFlashlight()
                val intent = Intent(this, Home::class.java)
                startActivity(intent)
                finish()  // Finish the current activity
                return@OnNavigationItemSelectedListener true
            }

            R.id.tools -> {
                // Check if the current activity is not Profile before starting it
                if (this !is Tools) {
                    turnOffFlashlight()
                    val intent = Intent(this, Tools::class.java)
                    startActivity(intent)
                    finish()  // Finish the current activity
                }
//                binding.bottomNavView.isSelected= true
                return@OnNavigationItemSelectedListener true
            }

            R.id.info -> {
                turnOffFlashlight()
                val intent = Intent(this, Information::class.java)
                startActivity(intent)
                finish()  // Finish the current activity
                return@OnNavigationItemSelectedListener true
            }

            R.id.profile -> {
                turnOffFlashlight()
                val intent = Intent(this, Profile::class.java)
                startActivity(intent)
                finish()  // Finish the current activity
                return@OnNavigationItemSelectedListener true


            }
        }
        return@OnNavigationItemSelectedListener false
    }

    private fun turnOffFlashlight() {
        if (isFlash) {
            val cameraListId = cameraManager.cameraIdList[0]
            cameraManager.setTorchMode(cameraListId, false)
            isFlash = false
            flashlightSwitch.text = getString(R.string.switch_off)
            textMessage("Flashlight is turned off.", this)
        }
    }

}