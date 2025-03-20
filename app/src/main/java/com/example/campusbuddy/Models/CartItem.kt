package com.example.campusbuddy.Models

data class CartItem(
    val productId: String,
    val productName: String,
    val productPrice: String,
    val productImageUrl: String?,
    var quantity: Int = 1
)