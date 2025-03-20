package com.example.campusbuddy

import User
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.campusbuddy.Models.Post
import com.example.campusbuddy.databinding.ActivityPostDetailBinding
import com.example.campusbuddy.utils.USER_NODE
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

import android.view.animation.AnimationUtils
import android.widget.LinearLayout


class PostDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostDetailBinding
    private lateinit var post: Post

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the post data from the intent
        post = intent.getSerializableExtra("POST") as Post

        // Display post details
        displayPostDetails(post)

        // Fetch Farmer Details from Firestore
        fetchFarmerDetails(post.userId)

        // Handle Contact Farmer Button Click
        binding.buttonContactFarmer.setOnClickListener {
            showContactOptions(post.userId)
        }
    }

    private fun displayPostDetails(post: Post) {
        binding.textProductName.text = "Product: ${post.productName ?: "N/A"}"
        binding.textProductCategory.text = "Category: ${post.productCategory ?: "N/A"}"
        binding.textProductPrice.text = "Price: ${post.productPrice ?: "N/A"}"
        binding.textProductAvailability.text = "Availability: ${post.productAvailability ?: "N/A"}"
        binding.textProductDescription.text = "Description: ${post.productDescription ?: "N/A"}"

        if (!post.imageUrl.isNullOrEmpty()) {
            Picasso.get()
                .load(post.imageUrl)
                .placeholder(R.drawable.user)
                .error(R.drawable.user)
                .into(binding.imageProduct)
            binding.imageProduct.visibility = View.VISIBLE
        } else {
            binding.imageProduct.visibility = View.GONE
        }
    }

    private fun fetchFarmerDetails(userId: String?) {
        if (userId.isNullOrEmpty()) return

        Firebase.firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                if (user != null) {
                    binding.textFarmerName.text = user.name ?: "Unknown Farmer"
                    binding.textLocation.text = "Location: ${user.location ?: "N/A"}"
                }
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }




    private fun showContactOptions(userId: String?) {
        if (userId.isNullOrEmpty()) {
            Toast.makeText(this, "Farmer details not available", Toast.LENGTH_SHORT).show()
            return
        }

        Firebase.firestore.collection(USER_NODE).document(userId).get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                if (user != null) {
                    // Find the sliding panel
                    val slidingPanel = findViewById<LinearLayout>(R.id.slidingPanel)

                    // Set up click listeners for the options
                    slidingPanel.findViewById<View>(R.id.optionCall).setOnClickListener {
                        callFarmer(user.contact)
                        hideSlidingPanel(slidingPanel)
                    }

                    slidingPanel.findViewById<View>(R.id.optionMessage).setOnClickListener {
                        messageFarmer(user.contact)
                        hideSlidingPanel(slidingPanel)
                    }

                    slidingPanel.findViewById<View>(R.id.optionEmail).setOnClickListener {
                        emailFarmer(user.email)
                        hideSlidingPanel(slidingPanel)
                    }

                    // Show the sliding panel with animation
                    showSlidingPanel(slidingPanel)
                } else {
                    Toast.makeText(this, "Farmer details not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
                Toast.makeText(this, "Failed to fetch farmer details", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showSlidingPanel(panel: View) {
        panel.visibility = View.VISIBLE
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        panel.startAnimation(slideUp)
    }

    private fun hideSlidingPanel(panel: View) {
        val slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down)
        panel.startAnimation(slideDown)
        panel.visibility = View.GONE
    }

    private fun callFarmer(phoneNumber: String?) {
        if (phoneNumber.isNullOrEmpty()) {
            Toast.makeText(this, "Phone number not available", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$phoneNumber")
        startActivity(intent)
    }

    private fun emailFarmer(email: String?) {
        if (email.isNullOrEmpty()) {
            Toast.makeText(this, "Email not available", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:$email")
        startActivity(intent)
    }

    private fun messageFarmer(phoneNumber: String?) {
        if (phoneNumber.isNullOrEmpty()) {
            Toast.makeText(this, "Phone number not available", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("sms:$phoneNumber")
        startActivity(intent)
    }
}