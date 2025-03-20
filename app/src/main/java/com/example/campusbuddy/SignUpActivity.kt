package com.example.campusbuddy

import User
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import com.example.campusbuddy.databinding.ActivitySignUpBinding
import com.example.campusbuddy.databinding.DialogRoleSelectionBinding
import com.example.campusbuddy.utils.USER_NODE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase

class SignUpActivity : AppCompatActivity() {
    companion object {
        const val MODE_SIGNUP = 0
        const val MODE_EDIT_PROFILE = 1
    }

    private val binding by lazy { ActivitySignUpBinding.inflate(layoutInflater) }
    private lateinit var firebaseAuth: FirebaseAuth
    private var user = User()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        setupUI()

        if (intent.hasExtra("MODE") && intent.getIntExtra("MODE", MODE_SIGNUP) == MODE_EDIT_PROFILE) {
            loadUserProfile()
        }

        binding.signupbtn.setOnClickListener { showRoleSelectionDialog() }
        binding.logIn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun setupUI() {
        // Set up UI (e.g., padding for system bars)
    }

    private fun loadUserProfile() {
        binding.signupbtn.text = "Update Profile"
        Firebase.firestore.collection(USER_NODE).document(Firebase.auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                snapshot.toObject<User>()?.let {
                    user = it
                    binding.name.setText(user.name)
                    binding.email.setText(user.email)
                    binding.contact.setText(user.contact)
                } ?: run {
                    Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun showRoleSelectionDialog() {
        // Inflate the dialog layout using View Binding
        val dialogBinding = DialogRoleSelectionBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()

        // Set click listeners
        dialogBinding.btnBuyer.setOnClickListener {
            // Hide payment details fields
            binding.upiId.visibility = View.GONE
            binding.bankAccountNumber.visibility = View.GONE
            handleSignUpOrUpdate("Buyer")
            dialog.dismiss()
        }

        dialogBinding.btnSeller.setOnClickListener {
            // Show payment details fields
            binding.upiId.visibility = View.VISIBLE
            binding.bankAccountNumber.visibility = View.VISIBLE
            handleSignUpOrUpdate("Seller")
            dialog.dismiss()
        }

        // Show the dialog
        dialog.show()
    }

    private fun handleSignUpOrUpdate(role: String) {
        val name = binding.name.text.toString().trim()
        val email = binding.email.text.toString().trim()
        val password = binding.password.text.toString().trim()
        val contact = binding.contact.text.toString().trim()
        val upiId = binding.upiId.text.toString().trim()
        val bankAccountNumber = binding.bankAccountNumber.text.toString().trim()

        // Validate input fields
        if (name.isEmpty() || email.isEmpty() || contact.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate UPI ID for Sellers
        if (role == "Seller") {
            if (upiId.isEmpty() || bankAccountNumber.isEmpty()) {
                Toast.makeText(this, "UPI ID and Bank Account Number are required for Sellers", Toast.LENGTH_SHORT).show()
                return
            }

            // Validate UPI ID format using regex
            if (!isValidUpiId(upiId)) {
                Toast.makeText(this, "Invalid UPI ID format. Please use the format: username@bankname", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // Proceed with sign-up or update
        if (intent.getIntExtra("MODE", MODE_SIGNUP) == MODE_EDIT_PROFILE) {
            updateUserProfile(name, email, contact, password, role, upiId, bankAccountNumber)
        } else {
            if (password.isEmpty() || password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return
            }
            signUpNewUser(name, email, contact, password, role, upiId, bankAccountNumber)
        }
    }

    private fun isValidUpiId(upiId: String): Boolean {
        val upiRegex = "^[a-zA-Z0-9.+_-]+@[a-zA-Z0-9.-]+$".toRegex()
        return upiRegex.matches(upiId)
    }

    private fun updateUserProfile(name: String, email: String, contact: String, password: String, role: String, upiId: String, bankAccountNumber: String) {
        val currentUser = Firebase.auth.currentUser

        currentUser?.let {
            // Update email if it has changed
            if (email != user.email) {
                currentUser.updateEmail(email)
                    .addOnSuccessListener {
                        user.email = email
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to update email: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
            }

            // Update password if provided and changed
            if (password.isNotEmpty()) {
                currentUser.updatePassword(password)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to update password: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
            }

            // Update Firestore with updated name, role, and payment details
            user.name = name
            user.email = email
            user.contact = contact
            user.role = role
            if (role == "Seller") {
                user.upiId = upiId
                user.bankAccountNumber = bankAccountNumber
            }
            saveUserData()
        } ?: run {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun signUpNewUser(name: String, email: String, contact: String, password: String, role: String, upiId: String, bankAccountNumber: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = Firebase.auth.currentUser?.uid
                    if (userId == null) {
                        Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
                        return@addOnCompleteListener
                    }

                    // Create a new User object with the userId
                    user = User(
                        userId = userId, // Pass the userId
                        name = name,
                        email = email,
                        contact = contact,
                        password = password,
                        role = role
                    )

                    // Set payment details for Sellers
                    if (role == "Seller") {
                        user.upiId = upiId
                        user.bankAccountNumber = bankAccountNumber
                    }

                    // Save user data to Firestore
                    saveUserData()
                } else {
                    Toast.makeText(this, task.exception?.localizedMessage, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUserData() {
        val userId = Firebase.auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        Firebase.firestore.collection(USER_NODE)
            .document(userId) // Use the userId as the document ID
            .set(user)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile saved successfully", Toast.LENGTH_SHORT).show()
                // Redirect to home activity
                startActivity(Intent(this, HomeActivity::class.java).apply {
                    putExtra("ROLE", user.role)
                })
                        finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Failed to save profile: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}