package com.example.campusbuddy.Models

data class CartItem(
    val productId: String,
    val productName: String,
    val productPrice: String,
    val productImageUrl: String?,
    var quantity: Double,      // Changed from Int to Double if needed
    val unit: String,          // New field
    val pricePerUnit: Boolean
)