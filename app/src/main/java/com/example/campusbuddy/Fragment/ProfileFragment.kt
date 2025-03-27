package com.example.campusbuddy.Fragment



import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.campusbuddy.Models.User

import com.example.campusbuddy.SignUpActivity
import com.example.campusbuddy.databinding.FragmentProfileBinding
import com.example.campusbuddy.utils.USER_NODE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var auth: FirebaseAuth
    private val TAG = "ProfileFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Handle Edit Profile Button Click
//        binding.btnEditProfile.setOnClickListener {
//            val intent = Intent(activity, SignUpActivity::class.java)
//            intent.putExtra("MODE", SignUpActivity.MODE_EDIT_PROFILE)
//            startActivity(intent)
//        }

        // Handle Logout Button Click
        binding.btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(activity, SignUpActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        // Fetch and display user's profile data
        fetchUserDetails()
    }
    private fun fetchUserDetails() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid // Use the current user's UID
            Log.d(TAG, "Fetching user details for userId: $userId")

            Firebase.firestore.collection(USER_NODE).document(userId).get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val user = documentSnapshot.toObject<User>()
                        if (user != null) {
                            // Set user's details
                            binding.sellerName.text = user.name ?: "N/A"
                            binding.sellerEmail.text = user.email ?: "N/A"
//                            binding.location.text = "Location: ${user.location ?: "N/A"}"
                            binding.sellerPhone.text = "Contact: ${user.contact ?: "N/A"}"
                            binding.upiId.text = "UPI ID: ${user.upiId ?: "N/A"}"
                            binding.bankAccountNumber.text = "Bank Account: ${user.bankAccountNumber ?: "N/A"}"

//                            // Set user's stats
//                            binding.productsCount.text = user.productsUploaded.toString()
//                            binding.salesCount.text = "₹${user.totalSales}"
//                            binding.ratingsCount.text = user.ratings.toString()

                            // Display payment details for Sellers
                            if (user.role == "Seller") {
                                binding.upiId.text = "UPI ID: ${user.upiId ?: "N/A"}"
                                binding.bankAccountNumber.text = "Bank Account: ${user.bankAccountNumber ?: "N/A"}"
                            }
                        } else {
                            Log.d(TAG, "User object is null")
                            Toast.makeText(requireContext(), "Failed to load user data", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.d(TAG, "User document does not exist")
                        Toast.makeText(requireContext(), "User document does not exist", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Failed to get user document", exception)
                    Toast.makeText(requireContext(), "Failed to fetch user details", Toast.LENGTH_SHORT).show()
                }
        } else {
            Log.d(TAG, "Current user is null (not logged in)")
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }
}