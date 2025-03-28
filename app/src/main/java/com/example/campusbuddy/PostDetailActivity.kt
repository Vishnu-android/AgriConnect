package com.example.campusbuddy


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.campusbuddy.Models.Post

import com.example.campusbuddy.Models.User
import com.example.campusbuddy.databinding.ActivityPostDetailBinding
import com.example.campusbuddy.utils.USER_NODE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class PostDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostDetailBinding
    private lateinit var post: Post
    private var currentUser: User? = null
    private lateinit var seller: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the post data from the intent
        post = intent.getSerializableExtra("POST") as Post


        // Set up the toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Product Details"

        // Handle toolbar navigation icon click
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        // Handle share button click
        binding.btnShare.setOnClickListener {
            sharePost(post)
        }

        // Display post details
        displayPostDetails(post)

        // Fetch Farmer Details from Firestore
        fetchFarmerDetails(post.userId)

        // Handle Contact Farmer Button Click
        binding.buttonContactFarmer.setOnClickListener {
            showContactOptions(post.userId)
        }
        binding.buttonBuy.setOnClickListener {
            if (post == null) {
                Toast.makeText(this, "Post is null", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            fetchSellerDetails(post.userId) { seller ->
                val intent = Intent(this, Checkout_Address_Activity::class.java).apply {
                    putExtra("POST", post)
                    putExtra("SELLER", seller)
                }
                startActivity(intent)
            }
        }
        fetchCurrentUser()
    }

    private fun fetchSellerDetails(userId: String?, callback: (User) -> Unit) {
        if (userId.isNullOrEmpty()) {
            Toast.makeText(this, "Seller ID not available", Toast.LENGTH_SHORT).show()
            return
        }

        Firebase.firestore.collection(USER_NODE).document(userId).get()
            .addOnSuccessListener { document ->
                val seller = document.toObject(User::class.java)
                if (seller != null) {
                    callback(seller)
                } else {
                    Toast.makeText(this, "Seller details not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to fetch seller: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchCurrentUser() {
        // Get the current user's ID from Firebase Authentication
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // Fetch the user's data from Firestore
        Firebase.firestore.collection(USER_NODE).document(currentUserId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    currentUser = document.toObject(User::class.java)
                } else {
                    Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    "Failed to fetch user data: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }


    private fun displayPostDetails(post: Post) {
        // Set product details
        binding.textProductName.text = post.productName ?: "N/A"
        binding.textProductCategory.text = "Category: ${post.productCategory ?: "N/A"}"
        binding.textProductPrice.text = "Price: ${post.productPrice ?: "N/A"}"
        binding.textProductAvailability.text = "In Stock: ${post.productAvailability ?: "N/A"}"
        binding.textProductDescription.text = post.productDescription ?: "N/A"

        // Load product image
        if (!post.imageUrl.isNullOrEmpty()) {
            Picasso.get()
                .load(post.imageUrl)
                .placeholder(R.drawable.user)
                .error(R.drawable.user)
                .into(binding.imageProduct)
        } else {
            binding.imageProduct.setImageResource(R.drawable.user)
        }
//
//        // Set farmer details (if available)
//        binding.textHarvestDate.text = "Harvest Date: ${post.harvestDate ?: "N/A"}"
//        binding.textFarmingMethod.text = "Farming Method: ${post.farmingMethod ?: "N/A"}"
//        binding.textDeliveryOptions.text = "Delivery: ${post.deliveryOptions ?: "N/A"}"
    }

    private fun fetchFarmerDetails(userId: String?) {
        if (userId.isNullOrEmpty()) return

        Firebase.firestore.collection(USER_NODE).document(userId).get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                if (user != null) {
                    // Set farmer details
                    binding.textFarmerName.text = user.name ?: "Unknown Farmer"
                    binding.textLocation.text = "Location: ${user.location ?: "N/A"}"

                    // Load farmer image (if available)
//                    if (!user.imageUrl.isNullOrEmpty()) {
//                        Picasso.get()
//                            .load(user.imageUrl)
//                            .placeholder(R.drawable.user)
//                            .error(R.drawable.user)
//                            .into(binding.imageFarmer)
//                    } else {
//                        binding.imageFarmer.setImageResource(R.drawable.user)
//                    }
                }
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
                Toast.makeText(this, "Failed to fetch farmer details", Toast.LENGTH_SHORT).show()
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

    private fun sharePost(post: Post) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        val shareMessage = """
            Check out this product: ${post.productName}
            Price: ${post.productPrice}
            Description: ${post.productDescription}
            Available at: AgriConnect App
        """.trimIndent()
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
        startActivity(Intent.createChooser(shareIntent, "Share Product"))
    }

}