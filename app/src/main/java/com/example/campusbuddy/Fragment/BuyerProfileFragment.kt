package com.example.campusbuddy.Fragment


import User
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.campusbuddy.SignUpActivity
import com.example.campusbuddy.databinding.FragmentBuyerProfileBinding
import com.example.campusbuddy.utils.USER_NODE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase

class BuyerProfileFragment : Fragment() {

    private lateinit var binding: FragmentBuyerProfileBinding
    private lateinit var auth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBuyerProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        // Handle Logout Button Click
        binding.logoutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(activity, SignUpActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }


        // Fetch and display user details
        fetchUserDetails()
    }

    private fun fetchUserDetails() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid // Use the current user's UID
            Log.d("UserDebug", "Fetching user details for userId: $userId")

            Firebase.firestore.collection(USER_NODE).document(userId).get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val user = documentSnapshot.toObject<User>()
                        if (user != null) {
                            // Set user's details
                            binding.userName.text = user.name ?: "N/A"
                            binding.emailText.text = user.email ?: "N/A"
                            binding.buyerPhone.text = user.contact ?: "N/A"
                        } else {
                            Log.d("UserDebug", "User object is null")
                            binding.userName.text = "Guest"
                            binding.emailText.text = "User conversion failed"
                            binding.buyerPhone.text = "N/A"
                        }
                    } else {
                        Log.d("UserDebug", "User document does not exist")
                        binding.userName.text = "Guest"
                        binding.emailText.text = "User document does not exist"
                        binding.buyerPhone.text = "N/A"
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("UserDebug", "Failed to get user document", exception)
                    binding.userName.text = "Query Failed"
                    binding.emailText.text = "Error: ${exception.message}"
                    binding.buyerPhone.text = "N/A"
                }
        } else {
            Log.d("UserDebug", "Current user is null (not logged in)")
            binding.userName.text = "Guest"
            binding.emailText.text = "Not logged in"
            binding.buyerPhone.text = "N/A"
        }
    }


}