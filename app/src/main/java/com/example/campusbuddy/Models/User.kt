package com.example.campusbuddy.Models

import com.example.campusbuddy.utils.LANG_ENGLISH
import java.io.Serializable

data class User(
    // Core user fields
    var userId: String? = null,
    var name: String? = null,
    var email: String? = null,
    var password: String? = null,
    var location: String? = null,
    var contact: String? = null,
    var role: String? = null,
    var language: String = LANG_ENGLISH,

    // Notification fields
    var fcmToken: String? = null,
    var unreadNotifications: Int = 0,

    // Seller-specific fields
    var productsUploaded: Int? = null,
    var totalSales: Double? = null,
    var ratings: Double? = null,             // Changed from Double? to Double with default 0.0
    var upiId: String? = null,
    var bankAccountNumber: String? = null,

    // New field for addresses
    var addresses: List<Address> = emptyList(),

    // Metadata
    var createdAt: Long = System.currentTimeMillis(),  // Changed from Long? to Long
    var lastLogin: Long? = null
) : Serializable {

    // Firestore-required empty constructor
    constructor() : this(
        userId = null,
        name = null,
        email = null,
        password = null,
        location = null,
        contact = null,
        role = null,
        language = LANG_ENGLISH,
        fcmToken = null,
        unreadNotifications = 0,
        productsUploaded = null,
        totalSales = null,
        ratings = null,
        upiId = null,
        bankAccountNumber = null,
        addresses = emptyList(),
        createdAt = System.currentTimeMillis(),
        lastLogin = null
    )

    // Registration constructor
    constructor(
        userId: String?,
        name: String?,
        email: String?,
        contact: String?,
        password: String?,
        role: String?,
        language: String = LANG_ENGLISH
    ) : this(
        userId = userId,
        name = name,
        email = email,
        password = password,
        location = null,
        contact = contact,
        role = role,
        language = language,
        fcmToken = null,
        unreadNotifications = 0,
        productsUploaded = null,
        totalSales = null,
        ratings = null,
        upiId = null,
        bankAccountNumber = null,
        addresses = emptyList(),
        createdAt = System.currentTimeMillis(),
        lastLogin = null
    )

    // Login constructor
    constructor(email: String?, password: String?) : this(
        email = email,
        password = password,
        userId = null,
        name = null,
        contact = null,
        role = null,
        language = LANG_ENGLISH
    )
}

// New Address data class for user addresses
data class Address(
    var addressId: String = "",
    var fullName: String = "",
    var mobileNumber: String = "",
    var pincode: String = "",
    var houseNo: String = "",
    var area: String = "",
    var landmark: String = "",
    var city: String = "",
    var state: String = "",
    var isDefault: Boolean = false
) : Serializable




