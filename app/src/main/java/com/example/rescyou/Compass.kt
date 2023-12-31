package com.example.rescyou

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.TextView
import com.example.rescyou.databinding.ActivityCompassBinding




class Compass : AppCompatActivity(), SensorEventListener {

    private lateinit var binding: ActivityCompassBinding

    private var currentDegree = 0f
    private var mSendorManager: SensorManager? = null
    private lateinit var compass: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompassBinding.inflate(layoutInflater)
        setContentView(binding.root)
        compass = findViewById(R.id.compass)
        initData()

        //BACK BUTTON
        binding.backButton.setOnClickListener {
            val intent = Intent(this, Tools::class.java)
            startActivity(intent)
        }

    }

    private fun initData(){
        mSendorManager = getSystemService(SENSOR_SERVICE) as SensorManager?
    }

    override fun onResume(){
        super.onResume()
        @Suppress("DEPRECATION")
        mSendorManager?.registerListener(this,
            mSendorManager?.getDefaultSensor(Sensor.TYPE_ORIENTATION),
            SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    override fun onSensorChanged(event: SensorEvent?) {
        val degree = Math.round(event?.values?.get(0)!!)
        val rotateAnimation = RotateAnimation(
            currentDegree,
            (-degree).toFloat(),
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )

        rotateAnimation.duration = 210
        rotateAnimation.fillAfter = true

        compass.startAnimation(rotateAnimation)
        currentDegree = (-degree).toFloat()

    }

}