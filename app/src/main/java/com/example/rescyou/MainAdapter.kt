package com.example.rescyou

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MainAdapter(private val arrayList: ArrayList<Uri>) :  RecyclerView.Adapter<MainAdapter.ViewHolder>()//ARRAY LIST FOR THE PHOTOS
{

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView= itemView.findViewById(R.id.item_imageView)

}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainAdapter.ViewHolder {
        var inflater: LayoutInflater = LayoutInflater.from(parent.context)
        var view: View=inflater.inflate(R.layout.item_photo, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: MainAdapter.ViewHolder, position: Int) {
        holder.imageView.setImageURI(arrayList.get(position))

//        val imagePath = arrayList[position]
//        Glide.with(holder.itemView.context)
//            .load("file://$imagePath") // Load the image from the file path
//            .into(holder.imageView)
    }


    override fun getItemCount(): Int {
        return arrayList.size
    }

}




