package com.example.rescyou

import HotlinesDataClass
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rescyou.databinding.ActivityHotlinesBinding
import com.example.rescyou.databinding.ActivityPreparednessTipsBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Hotlines : AppCompatActivity() {

    private lateinit var binding: ActivityHotlinesBinding
    private lateinit var data: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var eventListener: ValueEventListener
    private lateinit var recyclerView: RecyclerView
    private lateinit var hotlinesList: MutableList<HotlinesDataClass>
    private lateinit var adapter: HotlinesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHotlinesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // BACK BUTTON
        binding.backButton.setOnClickListener {
            val intent = Intent(this, Information::class.java)
            startActivity(intent)
        }

        recyclerView = findViewById(R.id.hotlines_recyclerView)
        hotlinesList = mutableListOf() // Initialize dataList here

        val gridLayoutManager = GridLayoutManager(this, 1)
        this.recyclerView.layoutManager = gridLayoutManager

        adapter = HotlinesAdapter(hotlinesList)
        recyclerView.adapter = adapter

        databaseReference = FirebaseDatabase.getInstance("https://rescyou-57570-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("Hotlines")
        databaseReference.keepSynced(true)

        eventListener = databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                hotlinesList.clear()
                for (itemSnapshot in snapshot.children) {
                    val hotlinesDataClass = itemSnapshot.getValue(HotlinesDataClass::class.java)
                    if (hotlinesDataClass != null) {
                        hotlinesDataClass.key = itemSnapshot.key
                        hotlinesList.add(hotlinesDataClass)
                    }
                }

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled event
            }
        })
    }
}