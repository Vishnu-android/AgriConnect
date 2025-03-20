package com.example.campusbuddy.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.campusbuddy.Models.Post
import com.example.campusbuddy.R
import com.squareup.picasso.Picasso

class MyPostRvAdapter(
    private val context: Context,
    private val postList: List<Post>,
    private val onPostDeleted: () -> Unit
) : RecyclerView.Adapter<MyPostRvAdapter.PostViewHolder>() {

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image)
        val productNameTextView: TextView = itemView.findViewById(R.id.ProductName)
        val productPriceTextView: TextView = itemView.findViewById(R.id.ProductPrice)
        val productCategoryTextView: TextView = itemView.findViewById(R.id.ProductCategory)
        val productAvailabilityTextView: TextView = itemView.findViewById(R.id.ProductAvail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]

        if (!post.imageUrl.isNullOrEmpty()) {
            Picasso.get().load(post.imageUrl).into(holder.imageView)
        }

        holder.productNameTextView.text = post.productName
        holder.productPriceTextView.text = post.productPrice
        holder.productCategoryTextView.text = post.productCategory
        holder.productAvailabilityTextView.text = post.productAvailability
    }

    override fun getItemCount(): Int = postList.size
}