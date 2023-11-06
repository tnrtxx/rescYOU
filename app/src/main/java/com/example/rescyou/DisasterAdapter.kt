package com.example.rescyou

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class DisasterAdapter(private val context: Context, private var dataList: List<DataClass>) : RecyclerView.Adapter<DisasterAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        //CARDVIEW
        val recCard: CardView = itemView.findViewById(R.id.disaster_cardView)

        // ICON OF THE DISASTER
        val recImageIcon: ImageView = itemView.findViewById(R.id.disasterIcon_imageView)

        // IMAGE OF THE DISASTER
        val recImage: ImageView = itemView.findViewById(R.id.disaster_imageView)

        //TITLE OR DISASTER NAME/CATEGORY
        val recTitle: TextView = itemView.findViewById(R.id.disaster_category)

        //DISASTER TIPS DESCRIPTION
        val recDesc: TextView = itemView.findViewById(R.id.disaster_desc)

        // SOURCES
        val recImageSource: TextView = itemView.findViewById(R.id.disaster_imageSource)
        val recArticleSource: TextView = itemView.findViewById(R.id.disaster_articleSource)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_tips, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        //PHOTO OR ICON
        Glide.with(context).load(dataList[position].dataImageIcon).into(holder.recImageIcon)
        Glide.with(context).load(dataList[position].dataImage).into(holder.recImage)


        //TEXT
        holder.recTitle.text = dataList[position].dataTitle
        holder.recDesc.text = dataList[position].dataDesc

        //SOURCES
        holder.recImageSource.text = dataList[position].dataImageSource
        holder.recArticleSource.text = dataList[position].dataArticleSource



        holder.recCard.setOnClickListener {
            val intent = Intent(context, PerDisaster::class.java)
            intent.putExtra("Image", dataList[holder.adapterPosition].dataImage)
            intent.putExtra("Title", dataList[holder.adapterPosition].dataTitle)
            intent.putExtra("Description", dataList[holder.adapterPosition].dataDesc)

            intent.putExtra("Image Source", dataList[holder.adapterPosition].dataImageSource)
            intent.putExtra("Article Source", dataList[holder.adapterPosition].dataArticleSource)

            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

}

