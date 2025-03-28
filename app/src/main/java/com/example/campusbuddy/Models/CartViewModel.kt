package com.example.campusbuddy.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.campusbuddy.Models.CartItem
import com.example.campusbuddy.Models.Post
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CartViewModel(application: Application) : AndroidViewModel(application) {

    private val _cartItems = MutableLiveData<MutableList<CartItem>>()
    val cartItems: MutableLiveData<MutableList<CartItem>> get() = _cartItems

    private val sharedPreferences = application.getSharedPreferences("CART_PREFERENCES", Context.MODE_PRIVATE)
    private val gson = Gson()

    init {
        _cartItems.value = loadCartItems()
    }

    fun addItemToCart(cartItem: CartItem) {
        val currentCart = _cartItems.value ?: mutableListOf()

        // Check if item with same ID and unit already exists
        val existingItem = currentCart.find {
            it.productId == cartItem.productId &&
                    it.unit == cartItem.unit
        }

        if (existingItem != null) {
            // Update quantity if item exists
            existingItem.quantity += cartItem.quantity
            Log.d("CartViewModel", "Updated quantity for ${cartItem.productName}. New quantity: ${existingItem.quantity}")
        } else {
            // Add new item if it doesn't exist
            currentCart.add(cartItem)
            Log.d("CartViewModel", "Added new item to cart: ${cartItem.productName}")
        }

        _cartItems.postValue(currentCart)
        saveCartItems(currentCart)
    }

    fun addPostToCart(post: Post, customQuantity: Double? = null) {
        val cartItem = CartItem(
            productId = post.postId ?: generateUniqueId(),
            productName = post.productName ?: "Unnamed Product",
            productPrice = post.productPrice ?: "0",
            productImageUrl = post.imageUrl,
            quantity = customQuantity ?: post.quantity,
            unit = post.unit,
            pricePerUnit = post.pricePerUnit
        )
        addItemToCart(cartItem)
    }

    fun removeFromCart(cartItem: CartItem) {
        val currentCart = _cartItems.value ?: mutableListOf()
        currentCart.remove(cartItem)
        _cartItems.postValue(currentCart)
        saveCartItems(currentCart)
        Log.d("CartViewModel", "Removed item from cart: ${cartItem.productName}")
    }

    fun updateProductQuantity(productId: String, newQuantity: Double) {
        val currentCart = _cartItems.value ?: mutableListOf()
        val cartItem = currentCart.find { it.productId == productId }

        if (cartItem != null) {
            cartItem.quantity = newQuantity
            if (newQuantity <= 0) {
                currentCart.remove(cartItem)
                Log.d("CartViewModel", "Removed item due to zero quantity: ${cartItem.productName}")
            }
            _cartItems.postValue(currentCart)
            saveCartItems(currentCart)
            Log.d("CartViewModel", "Updated quantity for $productId to $newQuantity")
        }
    }

    fun clearCart() {
        _cartItems.postValue(mutableListOf())
        saveCartItems(mutableListOf())
        Log.d("CartViewModel", "Cart cleared")
    }

    fun getCartTotal(): Double {
        return _cartItems.value?.sumOf { item ->
            val price = item.productPrice.toDoubleOrNull() ?: 0.0
            if (item.pricePerUnit) {
                price * item.quantity
            } else {
                price
            }
        } ?: 0.0
    }

    fun getFormattedCartTotal(): String {
        return "₹%.2f".format(getCartTotal())
    }

    private fun saveCartItems(cartItems: List<CartItem>) {
        val json = gson.toJson(cartItems)
        sharedPreferences.edit().putString("CART_ITEMS", json).apply()
        Log.d("CartViewModel", "Cart items saved: ${cartItems.size} items")
    }

    private fun loadCartItems(): MutableList<CartItem> {
        return try {
            val json = sharedPreferences.getString("CART_ITEMS", null)
            if (!json.isNullOrEmpty()) {
                val type = object : TypeToken<MutableList<CartItem>>() {}.type
                gson.fromJson(json, type) ?: mutableListOf()
            } else {
                mutableListOf()
            }
        } catch (e: Exception) {
            Log.e("CartViewModel", "Error loading cart items", e)
            mutableListOf()
        }
    }

    private fun generateUniqueId(): String {
        return "cart_${System.currentTimeMillis()}"
    }

    companion object {
        private const val TAG = "CartViewModel"
    }
}