package com.example.campusbuddy.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
        cartViewModel = ViewModelProvider(requireActivity()).get(CartViewModel::class.java)

        // Set up RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.cartRecyclerView)
        cartAdapter = CartAdapter(mutableListOf(), cartViewModel)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = cartAdapter

        // Observe cartItems LiveData
        cartViewModel.cartItems.observe(viewLifecycleOwner, Observer { items ->
            cartAdapter.updateCartItems(items)
        })

        return view
    }
}