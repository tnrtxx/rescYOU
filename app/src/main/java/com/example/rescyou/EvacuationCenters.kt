package com.example.rescyou

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rescyou.databinding.ActivityEvacuationCentersBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EvacuationCenters : AppCompatActivity() {

    private lateinit var binding: ActivityEvacuationCentersBinding
    private lateinit var dbReference: DatabaseReference
    private lateinit var evacuationCenterRecyclerView: RecyclerView
    private lateinit var evacuationCenterAdapter: EvacuationAdapter
    private lateinit var evacuationCenterArrayList: ArrayList<EvacuationCenter>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEvacuationCentersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        evacuationCenterRecyclerView = binding.evacuationCenterRecyclerView
        evacuationCenterRecyclerView.layoutManager = LinearLayoutManager(this)
        evacuationCenterRecyclerView.setHasFixedSize(true)

        evacuationCenterArrayList = arrayListOf()
        evacuationCenterAdapter = EvacuationAdapter(evacuationCenterArrayList)
        evacuationCenterRecyclerView.adapter = evacuationCenterAdapter

        getUserData()
    }

    private fun getUserData() {
        dbReference = FirebaseDatabase.getInstance("https://rescyou-57570-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Evacuation Centers")
        dbReference.keepSynced(true)

        dbReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    evacuationCenterArrayList.clear()
                    for (evacuationCenterSnapshot in snapshot.children) {
                        val evacuationCenter = evacuationCenterSnapshot.getValue(EvacuationCenter::class.java)
                        evacuationCenterArrayList.add(evacuationCenter!!)
                    }
                    evacuationCenterAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("EvacuationCenters", "Database operation cancelled: ${error.message}")
            }
        })
    }
}
