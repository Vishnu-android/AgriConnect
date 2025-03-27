package com.example.campusbuddy.adapters



import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.campusbuddy.Models.CartItem
import com.example.campusbuddy.R
import com.example.campusbuddy.databinding.CartItemBinding
import com.squareup.picasso.Picasso



class CartAdapter(
    private var cartItems: MutableList<CartItem>,
    private val onRemoveFromCart: (CartItem) -> Unit // Lambda function for removal
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(val binding: CartItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = CartItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cartItem = cartItems[position]

        // Bind data to views
        holder.binding.textProductName.text = cartItem.productName
        holder.binding.textProductPrice.text = "Price: ${cartItem.productPrice}"
        holder.binding.textQuantity.text = "Quantity: ${cartItem.quantity}"

        if (!cartItem.productImageUrl.isNullOrEmpty()) {
            Picasso.get()
                .load(cartItem.productImageUrl)
                .placeholder(R.drawable.user)
                .error(R.drawable.user)
                .into(holder.binding.imageProduct)
        } else {
            holder.binding.imageProduct.visibility = View.GONE
        }

        // Handle Remove from Cart Button Click
        holder.binding.buttonRemoveCart.setOnClickListener {
            onRemoveFromCart(cartItem)
            Toast.makeText(
                holder.itemView.context,
                "${cartItem.productName} removed from cart!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun getItemCount(): Int {
        return cartItems.size
    }

    fun updateCartItems(newCartItems: MutableList<CartItem>) {
        Log.d("CartAdapter", "Updating cart items: ${newCartItems.size} items")
        cartItems.clear()
        cartItems.addAll(newCartItems)
        notifyDataSetChanged()
    }
}