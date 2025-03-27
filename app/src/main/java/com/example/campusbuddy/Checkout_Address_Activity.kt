package com.example.campusbuddy

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.campusbuddy.Models.Address
import com.example.campusbuddy.Models.Post
import com.example.campusbuddy.Models.User
import com.example.campusbuddy.databinding.ActivityCheckoutAddressBinding
import com.example.campusbuddy.utils.USER_NODE
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Checkout_Address_Activity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckoutAddressBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutAddressBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get the current user's ID from Firebase Authentication
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            fetchCurrentUserData(userId) // Fetch the user's data
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }

        // Find the Continue button
        val btnContinue = findViewById<Button>(R.id.btnContinue)

        // Set click listener for the Continue button
        btnContinue.setOnClickListener {
            // Pass all the EditText fields to validateFields
            if (validateFields(
                    binding.editName,
                    binding.editPhone,
                    binding.editAddress,
                    binding.editCity,
                    binding.editState,
                    binding.editPincode
                )) {

                // Get current user first
                val currentUser = FirebaseAuth.getInstance().currentUser
                Firebase.firestore.collection(USER_NODE).document(currentUser?.uid ?: "").get()
                    .addOnSuccessListener { document ->
                        val user = document.toObject(User::class.java)?.apply {
                            // Create new address from form fields
                            val newAddress = Address(
                                fullName = binding.editName.text.toString(),
                                mobileNumber = binding.editPhone.text.toString(),
                                houseNo = binding.editAddress.text.toString(),
                                area = "", // You might want to add this field to your form
                                landmark = binding.editNotes.text.toString(),
                                city = binding.editCity.text.toString(),
                                state = binding.editState.text.toString(),
                                pincode = binding.editPincode.text.toString(),
                                isDefault = true
                            )

                            // Add to user's addresses
                            addresses = listOf(newAddress)
                        }

                        // Get product and seller data
                        val post = intent.getSerializableExtra("POST") as? Post
                        val seller = intent.getSerializableExtra("SELLER") as? User

                        if (user != null && post != null && seller != null) {
                            Intent(this@Checkout_Address_Activity, OrderSummaryActivity::class.java).apply {
                                putExtra("POST", post)
                                putExtra("SELLER", seller)
                                putExtra("USER", user)
                                startActivity(this)
                            }
                        } else {
                            Toast.makeText(this@Checkout_Address_Activity, "Error loading data", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this@Checkout_Address_Activity, "Failed to load user data", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this@Checkout_Address_Activity, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Fetch the current user's data from Firestore
    private fun fetchCurrentUserData(userId: String) {
        Firebase.firestore.collection(USER_NODE).document(userId).get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                if (user != null) {
                    // Populate the EditText fields with the user's data
                    binding.editName.setText(user.name)
                    binding.editPhone.setText(user.contact)
                } else {
                    Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
                Toast.makeText(this, "Failed to fetch user data", Toast.LENGTH_SHORT).show()
            }
    }

    // Function to validate all fields
    private fun validateFields(
        editName: TextInputEditText,
        editPhone: TextInputEditText,
        editAddress: TextInputEditText,
        editCity: TextInputEditText,
        editState: TextInputEditText,
        editPincode: TextInputEditText
    ): Boolean {
        var isValid = true

        // Check if any field is empty
        if (editName.text.isNullOrEmpty()) {
            editName.error = "Full Name is required"
            isValid = false
        }
        if (editPhone.text.isNullOrEmpty()) {
            editPhone.error = "Phone Number is required"
            isValid = false
        }
        if (editAddress.text.isNullOrEmpty()) {
            editAddress.error = "Full Address is required"
            isValid = false
        }
        if (editCity.text.isNullOrEmpty()) {
            editCity.error = "City is required"
            isValid = false
        }
        if (editState.text.isNullOrEmpty()) {
            editState.error = "State is required"
            isValid = false
        }
        if (editPincode.text.isNullOrEmpty()) {
            editPincode.error = "PIN Code is required"
            isValid = false
        }

        return isValid
    }
}



