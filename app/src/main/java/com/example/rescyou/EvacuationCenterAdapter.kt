package com.example.rescyou

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.rescyou.databinding.ItemEvacuationCentersBinding


/**
 * EvacuationCenterAdapter
 * Adapter for the RecyclerView in the EvacuationCenters activity
 *
 * evacuationCenterArrayList: It contains the list of evacuation centers to be displayed.
 */

private const val TAG = "EvacuationCenterAdapter"

class EvacuationCenterAdapter(private var evacuationCenterArrayList: List<EvacuationCenterData>) :
    RecyclerView.Adapter<EvacuationCenterAdapter.MyViewHolder>() {

    // This function is responsible for creating new views when needed by the layout manager.
    // It returns a new ViewHolder, which will hold the inflated layout of the item view.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemEvacuationCentersBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    // This function is responsible for returning the size of the list.
    // It is used in determining how many evacuation centers are in the database.
    override fun getItemCount(): Int = evacuationCenterArrayList.size

    // This function is responsible for binding the data to the views.
    // It is called by the layout manager when it wants new data to be displayed.
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = evacuationCenterArrayList[position]
        holder.bind(currentItem)

        // View in Map Button
        holder.binding.viewInMapEvacuationCenterButton.setOnClickListener {
            // Launch the EvacuationCenterMap activity and pass the evacuation center data to it
            val context = holder.itemView.context
            val intent = Intent(context, EvacuationCenterMap::class.java)
            intent.putExtra("evacuationCenterId", currentItem.evacuationCenterId)
            intent.putExtra("placeId", currentItem.placeId)
            intent.putExtra("name", currentItem.name)
            intent.putExtra("status", currentItem.status)
            intent.putExtra("inCharge", currentItem.inCharge)
            intent.putExtra("inChargeContactNum", currentItem.inChargeContactNum)
            intent.putExtra("occupants", currentItem.occupants)
            intent.putExtra("address", currentItem.address)
            intent.putExtra("latitude", currentItem.latitude)
            intent.putExtra("longitude", currentItem.longitude)
            startActivity(context, intent, null)

            // Logs the data of the evacuation center that was clicked
            // !! This is for debugging purposes only!!
            // TODO: Remove this later
            Log.d(TAG, toString())
        }
    }

    // This class is responsible for holding the views that will be used to display the data.
    class MyViewHolder(val binding: ItemEvacuationCentersBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: EvacuationCenterData) {
            // Bind data to the views using view binding
            binding.nameTextview.text = item.name.toString()
            binding.addressTextview.text = item.address.toString()
            binding.statusTextview.text = item.status.toString()
            binding.inChargeTextview.text = item.inCharge.toString()
            binding.inChargeContactNumTextview.text = item.inChargeContactNum.toString()
            binding.occupantsTextview.text = item.occupants.toString()

            // !! This if for the Evacuation Centers activity only !!
            // Set background color and text color based on the value of the status
            val context = binding.root.context
            val isAvailable = item.status.equals("AVAILABLE", ignoreCase = true)
            val isFull = item.status.equals("FULL", ignoreCase = true)

            val statusColor = when {
                isAvailable -> R.color.isAvailable
                isFull -> R.color.isFull
                else -> R.color.isNotAvailable
            }

            val tintList = ColorStateList.valueOf(ContextCompat.getColor(context, statusColor))

            // Set compound drawable tint list using TextViewCompat
            TextViewCompat.setCompoundDrawableTintList(binding.statusTextview, tintList)

        }
    }

}
