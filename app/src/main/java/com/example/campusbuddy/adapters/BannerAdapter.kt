package com.example.campusbuddy.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.campusbuddy.Fragment.HomeFragment
import com.example.campusbuddy.R

class BannerAdapter(private val banners: List<HomeFragment.Banner>) : RecyclerView.Adapter<BannerAdapter.BannerViewHolder>() {

    inner class BannerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val bannerImage: ImageView = itemView.findViewById(R.id.bannerImage)
        private val bannerTitle: TextView = itemView.findViewById(R.id.bannerTitle)
        private val bannerDescription: TextView = itemView.findViewById(R.id.bannerDescription)

        fun bind(banner: HomeFragment.Banner) {
            bannerImage.setImageResource(banner.imageRes)
            bannerTitle.text = banner.title
            bannerDescription.text = banner.description
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_banner, parent, false)
        return BannerViewHolder(view)
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        holder.bind(banners[position])
    }

    override fun getItemCount(): Int = banners.size
}