package com.example.campusbuddy

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.campusbuddy.Models.User
import com.example.campusbuddy.databinding.ActivitySignUpBinding
import com.example.campusbuddy.databinding.DialogRoleSelectionBinding
import com.example.campusbuddy.utils.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import java.util.*

class SignUpActivity : AppCompatActivity() {
    companion object {
        const val MODE_SIGNUP = 0
        const val MODE_EDIT_PROFILE = 1
    }

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var user = User()
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = Firebase.auth
        isEditMode = intent.getIntExtra("MODE", MODE_SIGNUP) == MODE_EDIT_PROFILE

        binding.signupbtn.text = if (isEditMode) "Update Profile" else "Sign Up"
        binding.upiContainer.visibility = View.GONE
        binding.bankContainer.visibility = View.GONE

        if (isEditMode) {
            loadUserProfile()
        }

        binding.signupbtn.setOnClickListener {
            val name = binding.name.text.toString().trim()
            val email = binding.email.text.toString().trim()
            val contact = binding.contact.text.toString().trim()
            val password = binding.password.text.toString().trim()

            when {
                name.isEmpty() -> binding.name.error = "Name is required"
                email.isEmpty() -> binding.email.error = "Email is required"
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> binding.email.error = "Valid email required"
                contact.isEmpty() -> binding.contact.error = "Phone number is required"
                password.isEmpty() && !isEditMode -> binding.password.error = "Password is required"
                password.length < 6 && !isEditMode -> binding.password.error = "Password must be 6+ chars"
                else -> {
                    if (isEditMode) {
                        showLanguageSelection()
                    } else {
                        showRoleSelection()
                    }
                }
            }
        }

        binding.logIn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun showRoleSelection() {
        val dialogBinding = DialogRoleSelectionBinding.inflate(LayoutInflater.from(this))
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()

        dialogBinding.btnBuyer.setOnClickListener {
            user.role = "Buyer"
            dialog.dismiss()
            showLanguageSelection()
        }

        dialogBinding.btnSeller.setOnClickListener {
            user.role = "Seller"
            dialog.dismiss()
            binding.upiContainer.visibility = View.VISIBLE
            binding.bankContainer.visibility = View.VISIBLE

            binding.signupbtn.setOnClickListener {
                val upiId = binding.upiId.text.toString().trim()
                val bankAccount = binding.bankAccountNumber.text.toString().trim()

                when {
                    upiId.isNotEmpty() && !isValidUpiId(upiId) -> {
                        binding.upiId.error = "Invalid UPI format"
                    }
                    upiId.isEmpty() && bankAccount.isEmpty() -> {
                        Toast.makeText(this, "Please provide either UPI ID or Bank Account", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        // Accept if at least one is provided
                        user.upiId = if (upiId.isNotEmpty()) upiId else null
                        user.bankAccountNumber = if (bankAccount.isNotEmpty()) bankAccount else null
                        showLanguageSelection()
                    }
                }
            }
        }
        dialog.show()
    }

    private fun showLanguageSelection() {
        val languages = mapOf(
            "English" to LANG_ENGLISH,
            "हिंदी" to LANG_HINDI,
            "ગુજરાતી" to LANG_GUJARATI,
            "ਪੰਜਾਬੀ" to LANG_PUNJABI
        )

        AlertDialog.Builder(this)
            .setTitle("Choose Language")
            .setItems(languages.keys.toTypedArray()) { _, which ->
                val selectedCode = languages.values.elementAt(which)

                // 1. Save preference
                getSharedPreferences("AppSettings", MODE_PRIVATE).edit()
                    .putString("APP_LANGUAGE", selectedCode)
                    .apply()

                // 2. Update user object
                user.language = selectedCode

                // 3. Apply language immediately
                setAppLanguage(selectedCode)

                // 4. Refresh the current activity
                recreate()

                // 5. Proceed with registration
                completeRegistration()
            }
            .setCancelable(false)
            .show()
    }
    private fun updateAppLanguage(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val resources = resources
        val configuration = Configuration(resources.configuration)
        configuration.setLocale(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }
    private fun setAppLanguage(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val resources = resources
        val configuration = Configuration(resources.configuration)
        configuration.setLocale(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

    private fun completeRegistration() {
        val name = binding.name.text.toString().trim()
        val email = binding.email.text.toString().trim()
        val contact = binding.contact.text.toString().trim()
        val password = binding.password.text.toString().trim()

        if (isEditMode) {
            updateUserProfile(name, email, contact, password)
        } else {
            signUpNewUser(name, email, contact, password)
        }
    }

    private fun updateUserProfile(name: String, email: String, contact: String, password: String) {
        val currentUser = firebaseAuth.currentUser ?: run {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        if (email != user.email) {
            currentUser.updateEmail(email).addOnCompleteListener {
                if (it.isSuccessful) {
                    user.email = email
                    saveUserData()
                } else {
                    Toast.makeText(this, "Email update failed", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            saveUserData()
        }

        if (password.isNotEmpty()) {
            currentUser.updatePassword(password).addOnCompleteListener {
                if (!it.isSuccessful) {
                    Toast.makeText(this, "Password update failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun signUpNewUser(name: String, email: String, contact: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    user.userId = firebaseAuth.currentUser?.uid ?: ""
                    user.name = name
                    user.email = email
                    user.contact = contact
                    user.password = password
                    saveUserData()
                } else {
                    Toast.makeText(this, "Sign up failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUserData() {
        Firebase.firestore.collection(USER_NODE)
            .document(firebaseAuth.currentUser?.uid ?: return)
            .set(user)
            .addOnSuccessListener {
                Toast.makeText(this, if (isEditMode) "Profile updated" else "Registration successful", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, HomeActivity::class.java).apply {
                    putExtra("ROLE", user.role)
                })
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Data save failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadUserProfile() {
        Firebase.firestore.collection(USER_NODE)
            .document(firebaseAuth.currentUser!!.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                snapshot.toObject<User>()?.let {
                    user = it
                    binding.name.setText(user.name)
                    binding.email.setText(user.email)
                    binding.contact.setText(user.contact)
                    if (user.role == "Seller") {
                        binding.upiContainer.visibility = View.VISIBLE
                        binding.bankContainer.visibility = View.VISIBLE
                        binding.upiId.setText(user.upiId)
                        binding.bankAccountNumber.setText(user.bankAccountNumber)
                    }
                }
            }
    }

    private fun isValidUpiId(upiId: String): Boolean {
        return upiId.matches("^[a-zA-Z0-9.+_-]+@[a-zA-Z0-9.-]+$".toRegex())
    }
}