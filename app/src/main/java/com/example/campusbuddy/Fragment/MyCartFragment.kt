package com.example.campusbuddy.Fragment


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.campusbuddy.Models.CartViewModelFactory
import com.example.campusbuddy.R
import com.example.campusbuddy.adapters.CartAdapter
import com.example.campusbuddy.viewmodels.CartViewModel


class MyCartFragment : Fragment() {

    private lateinit var cartAdapter: CartAdapter
    private lateinit var cartViewModel: CartViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_cart, container, false)

        // Initialize ViewModel
        cartViewModel = ViewModelProvider(this, CartViewModelFactory(requireActivity().application))
            .get(CartViewModel::class.java)

        // Set up RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.cartRecyclerView)
        cartAdapter = CartAdapter(mutableListOf()) { cartItem ->
            // Call the ViewModel's removeFromCart method
            cartViewModel.removeFromCart(cartItem)
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = cartAdapter

        // Observe cartItems LiveData
        cartViewModel.cartItems.observe(viewLifecycleOwner, Observer { items ->
            Log.d("MyCartFragment", "Cart items updated: ${items.size} items")
            cartAdapter.updateCartItems(items.toMutableList())
        })

        return view
    }
}