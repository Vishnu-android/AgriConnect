package com.example.campusbuddy.Fragment

import Order
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusbuddy.adapters.OrderAdapter
import com.example.campusbuddy.databinding.FragmentOrdersBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class OrdersFragment : Fragment() {

    private var _binding: FragmentOrdersBinding? = null
    private val binding get() = _binding!!
    private lateinit var orderAdapter: OrderAdapter
    private val ordersList = mutableListOf<Order>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        fetchOrders()
    }

    private fun setupRecyclerView() {
        orderAdapter = OrderAdapter(ordersList)
        binding.recyclerViewOrders.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = orderAdapter
            setHasFixedSize(true)
        }
    }

    private fun fetchOrders() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        binding.progressBar.visibility = View.VISIBLE

        Firebase.firestore.collection("orders")
            .whereEqualTo("sellerId", userId)
            .addSnapshotListener { snapshots, error ->
                binding.progressBar.visibility = View.GONE

                if (error != null) {
                    // Handle error
                    return@addSnapshotListener
                }

                ordersList.clear()
                snapshots?.documents?.forEach { document ->
                    val order = document.toObject<Order>()?.apply {
                        orderId = document.id
                    }
                    order?.let { ordersList.add(it) }
                }
                orderAdapter.notifyDataSetChanged()

                if (ordersList.isEmpty()) {
                    binding.textNoOrders.visibility = View.VISIBLE
                    binding.recyclerViewOrders.visibility = View.GONE
                } else {
                    binding.textNoOrders.visibility = View.GONE
                    binding.recyclerViewOrders.visibility = View.VISIBLE
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}