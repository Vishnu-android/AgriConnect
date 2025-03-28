package com.example.campusbuddy.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.campusbuddy.Models.Post
import com.example.campusbuddy.R
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class MyPostRvAdapter(
    private val context: Context,
    private val postList: List<Post>,
    private val onPostUpdated: () -> Unit,
    private val onPostEdit: (Post) -> Unit
) : RecyclerView.Adapter<MyPostRvAdapter.PostViewHolder>() {

    // Initialize Firestore instance at class level
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image)
        val productName: TextView = itemView.findViewById(R.id.ProductName)
        val productPrice: TextView = itemView.findViewById(R.id.ProductPrice)
        val productCategory: TextView = itemView.findViewById(R.id.ProductCategory)
        val productAvailability: TextView = itemView.findViewById(R.id.ProductAvail)
        val btnOptions: ImageButton = itemView.findViewById(R.id.btnOptions)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]

        // Load image if URL exists
        if (!post.imageUrl.isNullOrEmpty()) {
            Picasso.get().load(post.imageUrl).into(holder.imageView)
        }

        holder.productName.text = post.productName ?: "No Name"
        holder.productPrice.text = post.productPrice ?: "N/A"
        holder.productCategory.text = post.productCategory ?: "Uncategorized"
        holder.productAvailability.text = post.productAvailability ?: Post.AVAIL_IN_STOCK
        updateAvailabilityUI(holder.productAvailability, post.productAvailability)

        holder.btnOptions.setOnClickListener { view ->
            showOptionsMenu(view, post)
        }
        // Display price with unit if applicable
        val priceText = if (post.pricePerUnit) {
            "${post.productPrice} ₹/${post.unit}"
        } else {
            "${post.productPrice} ₹ (total)"
        }

        holder.productPrice.text = priceText

        // Add quantity display (optional)
        holder.productCategory.text = buildString {
            append(post.productCategory ?: "Uncategorized")
            append(" • ")
            append("${post.quantity} ${post.unit}")
        }
    }

    //new


    private fun updateAvailabilityUI(textView: TextView, availability: String?) {
        val (colorRes, iconRes) = when (availability) {
            Post.AVAIL_IN_STOCK -> Pair(R.color.green, R.drawable.instock)
            Post.AVAIL_LOW_STOCK -> Pair(R.color.orange, R.drawable.warning)
            Post.AVAIL_OUT_OF_STOCK -> Pair(R.color.red, R.drawable.sold)
            Post.AVAIL_COMING_SOON -> Pair(R.color.blue, R.drawable.comingsoon)
            else -> Pair(R.color.gray, R.drawable.user)

        }


        val icon = ContextCompat.getDrawable(context, iconRes)?.apply {
            val size = (textView.textSize * 1.2).toInt() // Slightly larger than text
            setBounds(0, 0, size, size)
        }

        textView.setTextColor(ContextCompat.getColor(context, colorRes))
        textView.setCompoundDrawables(icon, null, null, null)
    }

    private fun showOptionsMenu(view: View, post: Post) {
        PopupMenu(context, view).apply {
            menuInflater.inflate(R.menu.farmer_post_menu, menu)
            menu.findItem(R.id.menu_archive).title =
                if (post.isArchived) "Unarchive" else "Archive"

            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_edit_availability -> {
                        showAvailabilityDialog(post)
                        true
                    }
                    R.id.menu_edit_price -> {
                        showPriceDialog(post)
                        true
                    }
                    R.id.menu_archive -> {
                        toggleArchiveStatus(post)
                        true
                    }
                    else -> false
                }
            }
            show()
        }
    }

    private fun showAvailabilityDialog(post: Post) {
        val options = arrayOf(
            Post.AVAIL_IN_STOCK,
            Post.AVAIL_LOW_STOCK,
            Post.AVAIL_OUT_OF_STOCK,
            Post.AVAIL_COMING_SOON
        )

        AlertDialog.Builder(context)
            .setTitle("Update Availability")
            .setSingleChoiceItems(options, options.indexOf(post.productAvailability)) { dialog, which ->
                updatePostField(post.postId, "productAvailableIlity", options[which])
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showPriceDialog(post: Post) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_price, null)
        val editPrice = dialogView.findViewById<EditText>(R.id.editPrice).apply {
            setText(post.productPrice?.replace("₹", ""))
        }

        AlertDialog.Builder(context)
            .setTitle("Update Price")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val newPrice = editPrice.text.toString().trim()
                if (newPrice.isNotBlank() && newPrice != post.productPrice?.replace("₹", "")) {
                    updatePostPrice(post.postId, newPrice)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updatePostPrice(postId: String?, newPrice: String) {
        postId?.let { id ->
            val updates = hashMapOf<String, Any>(
                "productPrice" to newPrice,
                "priceHistory" to FieldValue.arrayUnion(
                    hashMapOf(
                        "price" to newPrice,
                        "changedAt" to System.currentTimeMillis()
                    )
                )
            )

            firestore.collection("posts").document(id)
                .update(updates)
                .addOnSuccessListener { onPostUpdated() }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Price update failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updatePostField(postId: String?, field: String, value: Any) {
        postId?.let { id ->
            firestore.collection("posts").document(id)
                .update(field, value)
                .addOnSuccessListener { onPostUpdated() }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun toggleArchiveStatus(post: Post) {
        val newStatus = !post.isArchived
        val action = if (newStatus) "Archive" else "Unarchive"

        AlertDialog.Builder(context)
            .setTitle("$action Post")
            .setMessage("Are you sure you want to $action this product?")
            .setPositiveButton(action) { _, _ ->
                updatePostField(post.postId, "isArchived", newStatus)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun getItemCount(): Int = postList.size
}



