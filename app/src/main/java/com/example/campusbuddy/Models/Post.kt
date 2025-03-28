package com.example.campusbuddy.Models

import com.google.firebase.firestore.PropertyName
import java.io.Serializable

data class Post(

    @get:PropertyName("productQuantity") @set:PropertyName("productQuantity")
    var quantity: Double = 0.0,

    @get:PropertyName("productUnit") @set:PropertyName("productUnit")
    var unit: String = "kg",  // Default unit

    @get:PropertyName("pricePerUnit") @set:PropertyName("pricePerUnit")
    var pricePerUnit: Boolean = true,

    @get:PropertyName("postId") @set:PropertyName("postId")
    var postId: String? = null,

    @get:PropertyName("userId") @set:PropertyName("userId")
    var userId: String? = null,

    @get:PropertyName("username") @set:PropertyName("username")
    var username: String? = null,

    @get:PropertyName("timestamp") @set:PropertyName("timestamp")
    var timestamp: Long = System.currentTimeMillis(),

    @get:PropertyName("imageUrl") @set:PropertyName("imageUrl")
    var imageUrl: String? = null,

    @get:PropertyName("productName") @set:PropertyName("productName")
    var productName: String? = null,

    @get:PropertyName("productPrice") @set:PropertyName("productPrice")
    var productPrice: String? = null,

    @get:PropertyName("productCategory") @set:PropertyName("productCategory")
    var productCategory: String? = null,

    @get:PropertyName("productAvailableIlity") @set:PropertyName("productAvailableIlity")
    var productAvailability: String = AVAIL_IN_STOCK,

    @get:PropertyName("productDescription") @set:PropertyName("productDescription")
    var productDescription: String? = null,

    @get:PropertyName("isArchived") @set:PropertyName("isArchived")
    var isArchived: Boolean = false,

    @get:PropertyName("images") @set:PropertyName("images")
    var images: List<String> = emptyList(),

    @get:PropertyName("priceHistory") @set:PropertyName("priceHistory")
    var priceHistory: List<Map<String, Any>> = emptyList()


) : Serializable {
    companion object {
        const val AVAIL_IN_STOCK = "In Stock"
        const val AVAIL_LOW_STOCK = "Low Stock"
        const val AVAIL_OUT_OF_STOCK = "Out of Stock"
        const val AVAIL_COMING_SOON = "Coming Soon"

        const val UNIT_KG = "kg"
        const val UNIT_GRAM = "g"
        const val UNIT_LB = "lb"
        const val UNIT_PIECE = "piece"
        const val UNIT_LITER = "liter"
        const val UNIT_DOZEN = "dozen"
        const val UNIT_BAG = "bag"
    }

    constructor() : this(timestamp = System.currentTimeMillis())

}





