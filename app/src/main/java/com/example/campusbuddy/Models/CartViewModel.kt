package com.example.campusbuddy.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.campusbuddy.Models.CartItem

class CartViewModel : ViewModel() {
    // LiveData to hold the list of cart items
    private val _cartItems = MutableLiveData<MutableList<CartItem>>()
    val cartItems: MutableLiveData<MutableList<CartItem>> get() = _cartItems

    init {
        _cartItems.value = mutableListOf()
    }

    /**
     * Add a product to the cart.
     * If the product already exists in the cart, increase its quantity.
     */
    fun addItemToCart(cartItem: CartItem) {
        val currentCart = _cartItems.value ?: mutableListOf()

        // Check if the product is already in the cart
        val existingItem = currentCart.find { it.productId == cartItem.productId }

        if (existingItem != null) {
            // If the product exists, increase its quantity
            existingItem.quantity += cartItem.quantity
        } else {
            // If the product doesn't exist, add it to the cart
            currentCart.add(cartItem)
        }

        _cartItems.value = currentCart
    }

    /**
     * Remove a product from the cart.
     */
    fun removeFromCart(cartItem: CartItem) { // Change parameter type to CartItem
        val currentCart = _cartItems.value ?: mutableListOf()
        currentCart.remove(cartItem) // Remove the CartItem object
        _cartItems.value = currentCart
    }

    /**
     * Update the quantity of a product in the cart.
     */
    fun updateProductQuantity(productId: String, newQuantity: Int) {
        val currentCart = _cartItems.value ?: mutableListOf()

        // Find the product in the cart
        val cartItem = currentCart.find { it.productId == productId }

        if (cartItem != null) {
            // Update the product's quantity
            cartItem.quantity = newQuantity

            // If the quantity is 0, remove the product from the cart
            if (newQuantity <= 0) {
                currentCart.remove(cartItem)
            }

            _cartItems.value = currentCart
        }
    }

    /**
     * Clear the cart.
     */
    fun clearCart() {
        _cartItems.value = mutableListOf()
    }

    /**
     * Get the total price of all items in the cart.
     */

}