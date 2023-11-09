package com.example.rescyou

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MainAdapter(private val arrayList: ArrayList<Uri>, private val countListener: CountOfImagesWhenRemoved) :  RecyclerView.Adapter<MainAdapter.ViewHolder>()//ARRAY LIST FOR THE PHOTOS
{

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView= itemView.findViewById(R.id.item_imageView)
        var delete: ImageView =  itemView.findViewById(R.id.delete)

}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var inflater: LayoutInflater = LayoutInflater.from(parent.context)
        var view: View=inflater.inflate(R.layout.item_photo, parent, false)

        return ViewHolder(view)
    }

    //UPDATE ITEM COUNT
    fun updateItemCount() {
        // Notify the listener about the updated count
        val size = arrayList.size
        if (size > 0) {
            countListener.clicked(size)
        } else {
            // If the list is empty, notify with a count of 0
            countListener.clicked(0)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        holder.imageView.setImageURI(arrayList.get(position))

        holder.imageView.setImageURI(arrayList[position])

        holder.delete.setOnClickListener {
            if (arrayList.isNotEmpty() && position < arrayList.size) {
                arrayList.removeAt(position)
                notifyItemRemoved(position)
                updateItemCount()
                notifyItemRangeChanged(position, itemCount) // Add this line
            }


            ////
//            arrayList.remove(arrayList.get(position))
//            notifyItemRemoved(position)
//            notifyItemRangeChanged(position,itemCount)
//
//            countListener.clicked(arrayList.size) // Notify the listener about the updated count

            ////
//            arrayList.removeAt(position)
//            notifyDataSetChanged() // Notify the adapter that the dataset has changed
//
//            countListener.clicked(arrayList.size) // Notify the listener about the updated count
        }

//        val imagePath = arrayList[position]
//        Glide.with(holder.itemView.context)
//            .load("file://$imagePath") // Load the image from the file path
//            .into(holder.imageView)
    }


    override fun getItemCount(): Int {
        return arrayList.size
    }

    interface CountOfImagesWhenRemoved {
        fun clicked(getSize: Int)
    }


}




