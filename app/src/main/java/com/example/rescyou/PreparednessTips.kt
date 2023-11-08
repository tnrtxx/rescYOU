package com.example.rescyou

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rescyou.databinding.ActivityPreparednessTipsBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class PreparednessTips : AppCompatActivity() {

    private lateinit var binding: ActivityPreparednessTipsBinding

    private lateinit var data: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var eventListener: ValueEventListener
    private lateinit var recyclerView: RecyclerView
    private lateinit var dataList: MutableList<DataClass>
    private lateinit var adapter: DisasterAdapter




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreparednessTipsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //BACK BUTTON
        binding.backButton.setOnClickListener {
            val intent = Intent(this, Information::class.java)
            startActivity(intent)
        }

        recyclerView= findViewById(R.id.tips_recyclerView)
        dataList = mutableListOf() // Initialize dataList here


        val gridLayoutManager = GridLayoutManager(this, 2)
        this.recyclerView.layoutManager = gridLayoutManager

        adapter = DisasterAdapter(this, dataList!!)
        recyclerView.adapter = adapter



        databaseReference = FirebaseDatabase.getInstance("https://rescyou-57570-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Preparedness Tips")
        databaseReference.keepSynced(true)  //add this line of code after nung Firebase get instance para sa mga page na need ioffline.
        // (ex. hotlines and evacuation center(?) not sure if gagana sa evacuation center


        eventListener = databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                dataList.clear()
                for (itemSnapshot in snapshot.children) {
                    val dataClass = itemSnapshot.getValue(DataClass::class.java)
                    if (dataClass != null) {
                        dataClass.replaceNewlines()
                    }
                    dataClass?.key = itemSnapshot.key
                    dataClass?.let { dataList.add(it) }
                }
                adapter.notifyDataSetChanged()

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }
}