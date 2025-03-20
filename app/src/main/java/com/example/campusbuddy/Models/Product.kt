package com.example.campusbuddy.Models

data class Product(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val category: String = "",
    val imageUrl: String = "",
    var purchaseDate: String = "", // Added for purchased products
    var quantity: Int = 0 // Added for purchased products
)