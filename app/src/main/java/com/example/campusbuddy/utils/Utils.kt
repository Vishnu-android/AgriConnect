package com.example.campusbuddy.utils

import com.google.firebase.firestore.FirebaseFirestore

// Function to upload text to Firestore
fun uploadText(text: String, callback: (Boolean) -> Unit) {
    val post = hashMapOf("content" to text, "timestamp" to System.currentTimeMillis())
    FirebaseFirestore.getInstance()
        .collection("posts")
        .add(post)
        .addOnSuccessListener { callback(true) }
        .addOnFailureListener { callback(false) }
}
