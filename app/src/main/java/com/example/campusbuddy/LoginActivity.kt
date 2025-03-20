package com.example.campusbuddy

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.campusbuddy.databinding.ActivityLoginBinding
import com.example.campusbuddy.utils.USER_NODE
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        binding.loginButton.setOnClickListener {
            if (binding.logEmail.editableText?.toString().equals("") ||
                binding.logPassword.editableText?.toString().equals("")
            ) {
                Toast.makeText(this@LoginActivity, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                val email = binding.logEmail.text.toString().trim()
                val password = binding.logPassword.text.toString().trim()

                Firebase.auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Fetch user role from Firestore
                            val userId = Firebase.auth.currentUser?.uid
                            if (userId != null) {
                                Firebase.firestore.collection(USER_NODE)
                                    .document(userId)
                                    .get()
                                    .addOnSuccessListener { document ->
                                        if (document.exists()) {
                                            val role = document.getString("role") ?: "Buyer" // Default to Buyer if role is missing
                                            // Navigate to HomeActivity with the role
                                            val intent = Intent(this, HomeActivity::class.java)
                                            intent.putExtra("ROLE", role)
                                            startActivity(intent)
                                            finish()
                                        } else {
                                            Toast.makeText(
                                                this@LoginActivity,
                                                "User data not found",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(
                                            this@LoginActivity,
                                            "Failed to fetch user data: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                        } else {
                            Toast.makeText(
                                this@LoginActivity,
                                task.exception?.localizedMessage,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}