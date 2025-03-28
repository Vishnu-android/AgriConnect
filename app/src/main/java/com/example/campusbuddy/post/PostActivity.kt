//package com.example.campusbuddy.post
//
//
//import android.app.Activity
//import android.content.Intent
//import android.net.Uri
//import android.os.Bundle
//import android.util.Log
//import android.widget.Button
//import android.widget.ImageView
//import android.widget.Toast
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.appcompat.app.AppCompatActivity
//import com.cloudinary.android.MediaManager
//import com.cloudinary.android.callback.UploadCallback
//import com.example.campusbuddy.HomeActivity
//import com.example.campusbuddy.Models.Post
//import com.example.campusbuddy.databinding.ActivityPostBinding
//import com.google.android.material.textfield.TextInputEditText
//import com.google.firebase.auth.ktx.auth
//import com.google.firebase.firestore.ktx.firestore
//import com.google.firebase.ktx.Firebase
//import com.squareup.picasso.Picasso
//
//class PostActivity : AppCompatActivity() {
//
//    private val binding by lazy { ActivityPostBinding.inflate(layoutInflater) }
//    private lateinit var imageView: ImageView
//    private lateinit var productNameEditText: TextInputEditText
//    private lateinit var productPriceEditText: TextInputEditText
//    private lateinit var productCategoryEditText: TextInputEditText
//    private lateinit var productAvailabilityEditText: TextInputEditText
//    private lateinit var productDescriptionEditText: TextInputEditText
//    private lateinit var btnSelectImage: Button
//    private lateinit var btnPost: Button
//    private lateinit var btnCancel: Button
//    private var imagePath: Uri? = null
//    private var userRole: String? = null
//
//    // Image picker launcher
//    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//        if (result.resultCode == Activity.RESULT_OK) {
//            val data: Intent? = result.data
//            data?.data?.let { uri ->
//                imagePath = uri
//                Picasso.get().load(uri).into(imageView) // Load the selected image into the ImageView
//            }
//        }
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(binding.root)
//
//        // Initialize views
//        imageView = binding.imageView
//        productNameEditText = binding.productNameEditText
//        productPriceEditText = binding.productPriceEditText
//        productCategoryEditText = binding.productCategoryEditText
//        productAvailabilityEditText = binding.productAvailabilityEditText
//        productDescriptionEditText = binding.productDescriptionEditText
//        btnSelectImage = binding.btnSelectImage
//        btnPost = binding.btnPost
//        btnCancel = binding.btnCancel
//
//        // Retrieve the role from the intent that started this activity
//        userRole = intent.getStringExtra("ROLE") ?: "Seller"
//
//        // Initialize Cloudinary
//        initCloudinary()
//
//        // Set up click listeners
//        btnSelectImage.setOnClickListener {
//            openImagePicker()
//        }
//
//        btnPost.setOnClickListener {
//            if (imagePath != null) {
//                val productName = productNameEditText.text.toString().trim()
//                val productPrice = productPriceEditText.text.toString().trim()
//                val productCategory = productCategoryEditText.text.toString().trim()
//                val productAvailability = productAvailabilityEditText.text.toString().trim()
//                val productDescription = productDescriptionEditText.text.toString().trim()
//
//                if (productName.isEmpty() || productPrice.isEmpty() || productCategory.isEmpty() || productAvailability.isEmpty() || productDescription.isEmpty()) {
//                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
//                } else {
//                    Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show()
//                    uploadImageToCloudinary { imageUrl ->
//                        savePostToFirestore(productName, productPrice, productCategory, productAvailability, productDescription, imageUrl)
//                    }
//                }
//            } else {
//                Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        btnCancel.setOnClickListener {
//            // Pass the role back when canceling
//            val intent = Intent(this@PostActivity, HomeActivity::class.java)
//            intent.putExtra("ROLE", userRole)
//            startActivity(intent)
//            finish()
//        }
//    }
//
//    private fun initCloudinary() {
//        val config = hashMapOf(
//            "cloud_name" to "deo53rj5g",
//            "api_key" to "431559213483915",
//            "api_secret" to "arDGi18_ajFU6GHmGSS-o6JvVQw"
//        )
//        MediaManager.init(this, config)
//    }
//
//    private fun openImagePicker() {
//        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
//            type = "image/*"
//        }
//        imagePickerLauncher.launch(intent)
//    }
//
//    private fun uploadImageToCloudinary(onSuccess: (String) -> Unit) {
//        imagePath?.let { uri ->
//            MediaManager.get().upload(uri)
//                .callback(object : UploadCallback {
//                    override fun onStart(requestId: String) {
//                        Log.d(TAG, "Image upload started")
//                    }
//
//                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
//                        Log.d(TAG, "Image upload progress: $bytes/$totalBytes")
//                    }
//
//                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
//                        val imageUrl = (resultData["secure_url"] as? String)
//                            ?: (resultData["url"] as? String)?.replace("http://", "https://")
//                            ?: (resultData["public_id"] as? String)?.let { publicId ->
//                                "https://res.cloudinary.com/deo53rj5g/image/upload/$publicId"
//                            }
//
//                        if (imageUrl != null) {
//                            Log.d(TAG, "Image uploaded successfully: $imageUrl")
//                            onSuccess(imageUrl)
//                        } else {
//                            Log.e(TAG, "No URL found in result data")
//                            Toast.makeText(this@PostActivity, "Image upload failed: No URL found", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//
//                    override fun onError(requestId: String, error: com.cloudinary.android.callback.ErrorInfo) {
//                        Log.e(TAG, "Image upload failed: ${error.description}")
//                        Toast.makeText(this@PostActivity, "Image upload failed: ${error.description}", Toast.LENGTH_SHORT).show()
//                    }
//
//                    override fun onReschedule(requestId: String, error: com.cloudinary.android.callback.ErrorInfo) {
//                        Log.d(TAG, "Image upload rescheduled")
//                    }
//                })
//                .dispatch(this)
//        } ?: run {
//            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun savePostToFirestore(
//        productName: String,
//        productPrice: String,
//        productCategory: String,
//        productAvailability: String,
//        productDescription: String,
//        imageUrl: String
//    ) {
//        val user = Firebase.auth.currentUser
//        if (user != null) {
//            // Create a new Post object using the correct constructor
//            val post = Post().apply {
//                postId = null
//                timestamp = System.currentTimeMillis()
//                userId = user.uid
//                username = user.displayName ?: "Anonymous"
//                this.imageUrl = imageUrl
//                this.productName = productName
//                this.productPrice = productPrice
//                this.productCategory = productCategory
//                this.productAvailability = productAvailability
//                this.productDescription = productDescription
//                isArchived = false
//                images = if (imageUrl.isNotEmpty()) listOf(imageUrl) else emptyList()
//            }
//
//            // Simplified Firestore save operation
//            Firebase.firestore.collection("posts")
//                .add(post)
//                .addOnSuccessListener { documentReference ->
//                    // Update the postId with the generated document ID
//                    val postId = documentReference.id
//                    Firebase.firestore.collection("posts")
//                        .document(postId)
//                        .update("postId", postId)
//                        .addOnSuccessListener {
//                            Log.d(TAG, "Post successfully saved with ID: $postId")
//                            Toast.makeText(this, "Product uploaded successfully", Toast.LENGTH_SHORT).show()
//
//                            val intent = Intent(this@PostActivity, HomeActivity::class.java)
//                            intent.putExtra("ROLE", userRole)
//                            startActivity(intent)
//                            finish()
//                        }
//                        .addOnFailureListener { e ->
//                            Log.e(TAG, "Error updating postId: ${e.message}")
//                            Toast.makeText(this, "Product uploaded but ID update failed", Toast.LENGTH_SHORT).show()
//                        }
//                }
//                .addOnFailureListener { e ->
//                    Log.e(TAG, "Error adding product: ${e.message}")
//                    Toast.makeText(this, "Error adding product: ${e.message}", Toast.LENGTH_SHORT).show()
//                }
//        } else {
//            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
//        }
//    }
//    companion object {
//        private const val TAG = "PostActivity"
//    }
//}

package com.example.campusbuddy.post


import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.UploadCallback
import com.example.campusbuddy.HomeActivity
import com.example.campusbuddy.Models.Post
import com.example.campusbuddy.databinding.ActivityPostBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class PostActivity : AppCompatActivity() {

    private val binding by lazy { ActivityPostBinding.inflate(layoutInflater) }
    private lateinit var imageView: ImageView
    private lateinit var productNameEditText: TextInputEditText
    private lateinit var productPriceEditText: TextInputEditText
    private lateinit var productCategoryEditText: TextInputEditText
    private lateinit var productAvailabilityEditText: TextInputEditText
    private lateinit var productDescriptionEditText: TextInputEditText
    private lateinit var btnSelectImage: Button
    private lateinit var btnPost: Button
    private lateinit var btnCancel: Button
    private var imagePath: Uri? = null
    private var userRole: String? = null

    // Image picker launcher
    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                data?.data?.let { uri ->
                    imagePath = uri
                    Picasso.get().load(uri)
                        .into(imageView) // Load the selected image into the ImageView
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Initialize views
        imageView = binding.imageView
        productNameEditText = binding.productNameEditText
        productPriceEditText = binding.productPriceEditText
        productCategoryEditText = binding.productCategoryEditText
        productAvailabilityEditText = binding.productAvailabilityEditText
        productDescriptionEditText = binding.productDescriptionEditText
        btnSelectImage = binding.btnSelectImage
        btnPost = binding.btnPost
        btnCancel = binding.btnCancel

        // Retrieve the role from the intent that started this activity
        userRole = intent.getStringExtra("ROLE") ?: "Seller"

        // Initialize Cloudinary
        initCloudinary()

        // Set up click listeners
        btnSelectImage.setOnClickListener {
            openImagePicker()
        }

        btnPost.setOnClickListener {
            if (imagePath != null) {
                val productName = productNameEditText.text.toString().trim()
                val productPrice = productPriceEditText.text.toString().trim()
                val productCategory = productCategoryEditText.text.toString().trim()
                val productAvailability = productAvailabilityEditText.text.toString().trim()
                val productDescription = productDescriptionEditText.text.toString().trim()

                if (productName.isEmpty() || productPrice.isEmpty() || productCategory.isEmpty() || productAvailability.isEmpty() || productDescription.isEmpty()) {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show()
                    uploadImageToCloudinary { imageUrl ->
                        savePostToFirestore(
                            productName,
                            productPrice,
                            productCategory,
                            productAvailability,
                            productDescription,
                            imageUrl
                        )
                    }
                }
            } else {
                Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancel.setOnClickListener {
            // Pass the role back when canceling
            val intent = Intent(this@PostActivity, HomeActivity::class.java)
            intent.putExtra("ROLE", userRole)
            startActivity(intent)
            finish()
        }
    }

    private fun initCloudinary() {
        val config = hashMapOf(
            "cloud_name" to "deo53rj5g",
            "api_key" to "431559213483915",
            "api_secret" to "arDGi18_ajFU6GHmGSS-o6JvVQw"
        )
        MediaManager.init(this, config)
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        imagePickerLauncher.launch(intent)
    }

    private fun uploadImageToCloudinary(onSuccess: (String) -> Unit) {
        imagePath?.let { uri ->
            MediaManager.get().upload(uri)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {
                        Log.d(TAG, "Image upload started")
                    }

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                        Log.d(TAG, "Image upload progress: $bytes/$totalBytes")
                    }

                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        val imageUrl = (resultData["secure_url"] as? String)
                            ?: (resultData["url"] as? String)?.replace("http://", "https://")
                            ?: (resultData["public_id"] as? String)?.let { publicId ->
                                "https://res.cloudinary.com/deo53rj5g/image/upload/$publicId"
                            }

                        if (imageUrl != null) {
                            Log.d(TAG, "Image uploaded successfully: $imageUrl")
                            onSuccess(imageUrl)
                        } else {
                            Log.e(TAG, "No URL found in result data")
                            Toast.makeText(
                                this@PostActivity,
                                "Image upload failed: No URL found",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onError(
                        requestId: String,
                        error: com.cloudinary.android.callback.ErrorInfo
                    ) {
                        Log.e(TAG, "Image upload failed: ${error.description}")
                        Toast.makeText(
                            this@PostActivity,
                            "Image upload failed: ${error.description}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    override fun onReschedule(
                        requestId: String,
                        error: com.cloudinary.android.callback.ErrorInfo
                    ) {
                        Log.d(TAG, "Image upload rescheduled")
                    }
                })
                .dispatch(this)
        } ?: run {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun savePostToFirestore(
        productName: String,
        productPrice: String,
        productCategory: String,
        productAvailability: String,
        productDescription: String,
        imageUrl: String
    ) {
        val user = Firebase.auth.currentUser
        if (user != null) {
            val post = Post(
                postId = null,
                timestamp = System.currentTimeMillis(),
                userId = user.uid,
                username = user.displayName ?: "Anonymous",
                imageUrl = imageUrl, // Make sure this is being set
                productName = productName,
                productPrice = productPrice,
                productCategory = productCategory,
                productAvailability = productAvailability,
                productDescription = productDescription
            )

            // Simplified Firestore save operation
            Firebase.firestore.collection("posts")
                .add(post)
                .addOnSuccessListener { documentReference ->
                    // Update the postId with the generated document ID
                    val postId = documentReference.id
                    Firebase.firestore.collection("posts")
                        .document(postId)
                        .update("postId", postId)
                        .addOnSuccessListener {
                            Log.d(TAG, "Post successfully saved with ID: $postId")
                            Toast.makeText(
                                this,
                                "Product uploaded successfully",
                                Toast.LENGTH_SHORT
                            ).show()

                            val intent = Intent(this@PostActivity, HomeActivity::class.java)
                            intent.putExtra("ROLE", userRole)
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error updating postId: ${e.message}")
                            Toast.makeText(
                                this,
                                "Product uploaded but ID update failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error adding product: ${e.message}")
                    Toast.makeText(this, "Error adding product: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val TAG = "PostActivity"
    }
}
