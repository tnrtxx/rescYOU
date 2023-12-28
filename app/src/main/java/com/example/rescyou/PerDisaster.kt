package com.example.rescyou

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.rescyou.databinding.ActivityPerDisasterBinding


class PerDisaster : AppCompatActivity() {

    private lateinit var binding: ActivityPerDisasterBinding

    private lateinit var disasterDesc: TextView
    private lateinit var disasterTitle: TextView
    private lateinit var disasterImage: ImageView

    private lateinit var disasterImageSource: TextView
    private lateinit var disasterArticleSource: TextView
    private var key = ""
    private var imageUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerDisasterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //BACK BUTTON
        binding.backButton.setOnClickListener {
            val intent = Intent(this, PreparednessTips::class.java)
            startActivity(intent)
        }

        //INITIALIZE THE VARIABLES
        disasterDesc = binding.perDisasterTips
        disasterTitle = binding.tipsTextview
        disasterImage = binding.perDisasterImage


        disasterImageSource= binding.imageSourceTextView
        disasterArticleSource =  binding.articleSourceTextView

        val bundle = intent.extras
        if (bundle != null) {
            disasterDesc.text = bundle.getString("Description")
            disasterTitle.text = bundle.getString("Title")

            disasterImageSource.text = bundle.getString("Image Source")
            disasterArticleSource.text = bundle.getString("Article Source")

            key = bundle.getString("Key") ?: ""
            imageUrl = bundle.getString("Image") ?: ""
            Glide.with(this).load(bundle.getString("Image")).into(disasterImage)
        }

        disasterImageSource.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(disasterImageSource.text.toString()))
            startActivity(browserIntent)
        }

        disasterArticleSource.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(disasterArticleSource.text.toString()))
            startActivity(browserIntent)
        }

    }
}