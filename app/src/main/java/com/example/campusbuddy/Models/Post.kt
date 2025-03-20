package com.example.campusbuddy.Models

import java.io.Serializable

data class Post(
    var postId: String? = null,
    var timestamp: Long? = null,
    var userId: String? = null,
    var username: String? = null,
    var imageUrl: String? = null,
    var productName: String? = null,
    var productPrice: String? = null,
    var productCategory: String? = null,
    var productAvailability: String? = null,
    var productDescription: String? = null // Add this field
): Serializable {
    // Default constructor (required for Firestore)
    constructor() : this(null, null, null, null, null, null, null, null, null, null)
}