package com.example.campusbuddy.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.campusbuddy.Models.CartItem
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
        val existingItem = currentCart.find { it.productId == cartItem.productId && cartItem.productId != null }

        if (existingItem != null) {
            existingItem.quantity += cartItem.quantity
        } else {
            currentCart.add(cartItem)
        }

        _cartItems.postValue(currentCart)
        saveCartItems(currentCart)
    }

    fun removeFromCart(cartItem: CartItem) {
        val currentCart = _cartItems.value ?: mutableListOf()
        currentCart.remove(cartItem)
        _cartItems.postValue(currentCart)
        saveCartItems(currentCart)
    }

    fun updateProductQuantity(productId: String, newQuantity: Int) {
        val currentCart = _cartItems.value ?: mutableListOf()
        val cartItem = currentCart.find { it.productId == productId }

        if (cartItem != null) {
            cartItem.quantity = newQuantity
            if (newQuantity <= 0) {
                currentCart.remove(cartItem)
            }
            _cartItems.postValue(currentCart)
            saveCartItems(currentCart)
        }
    }

    fun clearCart() {
        _cartItems.postValue(mutableListOf())
        saveCartItems(mutableListOf())
    }

    private fun saveCartItems(cartItems: List<CartItem>) {
        val json = gson.toJson(cartItems)
        sharedPreferences.edit().putString("CART_ITEMS", json).apply()
        Log.d("CartViewModel", "Cart items saved: $json")
    }

    private fun loadCartItems(): MutableList<CartItem> {
        val json = sharedPreferences.getString("CART_ITEMS", null)
        return if (!json.isNullOrEmpty()) {
            try {
                Log.d("CartViewModel", "JSON from SharedPreferences: $json")
                val type = object : TypeToken<MutableList<CartItem>>() {}.type
                val items: MutableList<CartItem> = gson.fromJson(json, type) // Explicitly specify the type
                Log.d("CartViewModel", "Cart items loaded: ${items.size} items")
                items
            } catch (e: Exception) {
                Log.e("CartViewModel", "Error loading cart items: ${e.message}", e)
                mutableListOf()
            }
        } else {
            Log.d("CartViewModel", "No cart items found in SharedPreferences")
            mutableListOf()
        }
    }
}