package com.example.campusbuddy.Models

data class Commodity(
    val state: String,
    val district: String,
    val market: String,
    val commodity: String,
    val variety: String,
    val grade: String,
    val arrival_date: String,
    val min_price: String,
    val max_price: String,
    val modal_price: String
)
