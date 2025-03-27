package com.example.campusbuddy.adapters


import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.campusbuddy.Models.CartItem
import com.example.campusbuddy.R
import com.example.campusbuddy.databinding.PostRvBinding
import com.example.campusbuddy.utils.USER_NODE
import com.example.campusbuddy.Models.Post
import com.example.campusbuddy.Models.User
import com.example.campusbuddy.PostDetailActivity
import com.example.campusbuddy.viewmodels.CartViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class PostAdapter(
    var context: Context,
    var postList: ArrayList<Post>,
    var followedUsers: Set<String>,
    private val cartViewModel: CartViewModel
) : RecyclerView.Adapter<PostAdapter.MyHolder>() {

    inner class MyHolder(var binding: PostRvBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val binding = PostRvBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyHolder(binding)
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        val post = postList[position]

        Log.d("PostAdapter", "Post image URL: ${post.imageUrl}")


        // Highlight posts from followed users
        if (followedUsers.contains(post.userId)) {
            holder.binding.cardView.setCardBackgroundColor(Color.LTGRAY)
        } else {
            holder.binding.cardView.setCardBackgroundColor(Color.WHITE)
        }

        // Fetch Farmer Details from Firestore
        Firebase.firestore.collection(USER_NODE).document(post.userId ?: "").get()
            .addOnSuccessListener { document ->
                val user = document.toObject<User>()
                if (user != null) {
                    holder.binding.textFarmerName.text = user.name ?: "Unknown Farmer"
                    holder.binding.textLocation.text = "Location: ${user.location ?: "N/A"}"
                } else {
                    holder.binding.textFarmerName.text = "Unknown Farmer"
                    holder.binding.textLocation.text = "Location: N/A"
                }
            }
            .addOnFailureListener { exception ->
                holder.binding.textFarmerName.text = "Unknown Farmer"
                holder.binding.textLocation.text = "Location: N/A"
                exception.printStackTrace()
            }

        // Load Product Image using Picasso
        if (!post.imageUrl.isNullOrEmpty()) {
            Picasso.get()
                .load(post.imageUrl)
                .placeholder(R.drawable.user)
                .error(R.drawable.user)
                .into(holder.binding.imageProduct)
            holder.binding.imageProduct.visibility = View.VISIBLE
        } else {
            holder.binding.imageProduct.visibility = View.GONE
        }

        // Set Product Details
        holder.binding.textProductName.text = "Product: ${post.productName ?: "N/A"}"
        holder.binding.textProductCategory.text = "Category: ${post.productCategory ?: "Uncategorized"}"
        holder.binding.textProductPrice.text = "Price: ${post.productPrice ?: "N/A"}"
        holder.binding.textProductAvailability.text = "Availability: ${post.productAvailability ?: "N/A"}"
        holder.binding.textProductDescription.text = "Description: ${post.productDescription ?: "N/A"}"

        // Handle item click to open PostDetailActivity
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PostDetailActivity::class.java)
            intent.putExtra("POST", post)
            context.startActivity(intent)
        }

        // Handle Add to Cart Button Click
        holder.binding.buttonAddCart.setOnClickListener {
            cartViewModel.addItemToCart(
                CartItem(
                    productId = post.postId ?: "",
                    productName = post.productName ?: "N/A",
                    productPrice = post.productPrice ?: "N/A",
                    productImageUrl = post.imageUrl
                )
            )
            Toast.makeText(context, "${post.productName ?: "Product"} added to cart!", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to update the list of posts
    fun updateList(newList: List<Post>) {
        postList.clear()
        postList.addAll(newList)
        notifyDataSetChanged() // Notify the adapter of data changes
    }
}