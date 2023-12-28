package com.example.rescyou

import HotlinesDataClass
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HotlinesAdapter (private val hotlinesList: MutableList<HotlinesDataClass>) : RecyclerView.Adapter<HotlinesAdapter.MyViewHolder>(){

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dataNameTextView: TextView = itemView.findViewById(R.id.data_name_textview)
        val dataPhoneTextView: TextView = itemView.findViewById(R.id.data_phone_textview)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HotlinesAdapter.MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recycler_hotlines, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: HotlinesAdapter.MyViewHolder, position: Int) {
        val currentItem = hotlinesList[position]
        holder.dataNameTextView.text = currentItem.dataName
        holder.dataPhoneTextView.text = currentItem.dataPhone

        holder.dataPhoneTextView.setOnClickListener {
            val dialIntent = Intent(Intent.ACTION_DIAL)
            dialIntent.data = Uri.parse("tel:${currentItem.dataPhone}")
            holder.itemView.context.startActivity(dialIntent)
        }
    }

    override fun getItemCount(): Int {
        return hotlinesList.size
    }


}