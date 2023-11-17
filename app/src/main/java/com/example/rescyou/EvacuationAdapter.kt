package com.example.rescyou

import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.rescyou.databinding.ItemEvacuationCentersBinding
import com.google.android.gms.maps.model.LatLng
import java.io.IOException
import java.util.Locale


class EvacuationAdapter(private val evacuationCenterList: List<EvacuationCenter>) :
    RecyclerView.Adapter<EvacuationAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemEvacuationCentersBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return evacuationCenterList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = evacuationCenterList[position]
        holder.bind(currentItem)
    }

    class MyViewHolder(private val binding: ItemEvacuationCentersBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: EvacuationCenter) {
            // Bind data to the views using view binding
            binding.addressTextview.text = item.address.toString()
            binding.statusTextview.text = item.status.toString()
            binding.inChargeTextview.text = item.inCharge.toString()
            binding.inChargeContactNumTextview.text = item.inChargeContactNum.toString()
            binding.occupantsTextview.text = item.occupants.toString()

            // Set background color based on the value of the status
            val context = binding.root.context
            val statusBgColor = if (item.status.equals("AVAILABLE", ignoreCase = true)) R.color.navy_blue else R.color.slight_dark_gray
            val statusTextColor = if (item.status.equals("AVAILABLE", ignoreCase = true)) R.color.light_gray else R.color.darker_gray
            binding.statusTextview.setBackgroundColor(ContextCompat.getColor(context, statusBgColor))
            binding.statusTextview.setTextColor(ContextCompat.getColor(context, statusTextColor))

            binding.viewDirectionButton.setOnClickListener {
                // Perform geocoding in the background
                GeocodeTask(binding.root.context).execute(item.address)
            }
        }
    }

    private class GeocodeTask(private val context: Context) : AsyncTask<String, Void, LatLng?>() {
        override fun doInBackground(vararg addresses: String?): LatLng? {
            val geocoder = Geocoder(context, Locale.getDefault())
            val address = addresses[0]

            try {
                val locationList = address?.let { geocoder.getFromLocationName(it, 1) }
                if (!locationList.isNullOrEmpty()) {
                    val location = locationList[0]
                    return LatLng(location.latitude, location.longitude)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return null
        }

        override fun onPostExecute(result: LatLng?) {
            super.onPostExecute(result)

            if (result != null) {
                // Start MapActivity with latitude and longitude data
                val intent = Intent(context, EvacuationCenterMap::class.java)
                intent.putExtra("latitude", result.latitude)
                intent.putExtra("longitude", result.longitude)
                context.startActivity(intent)
            } else {
                // Handle the case where geocoding failed
                Toast.makeText(context, "Geocoding failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}