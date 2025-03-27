package com.example.campusbuddy

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusbuddy.adapters.BuyerOrderAdapter
import com.example.campusbuddy.databinding.ActivityBuyerOrderBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class BuyerOrderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBuyerOrderBinding
    private lateinit var adapter: BuyerOrderAdapter
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBuyerOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        loadOrders()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = BuyerOrderAdapter { order ->
            // Handle order click (you can implement order details later)
        }

        binding.ordersRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@BuyerOrderActivity)
            adapter = this@BuyerOrderActivity.adapter
            addItemDecoration(
                DividerItemDecoration(
                    this@BuyerOrderActivity,
                    DividerItemDecoration.VERTICAL
                )
            )
        }
    }

    private fun loadOrders() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            finish()
            return
        }

        binding.progressBar.visibility = View.VISIBLE

        db.collection("orders")
            .whereEqualTo("buyerId", currentUser.uid)
            .orderBy("orderDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                binding.progressBar.visibility = View.GONE

                if (error != null) {
                    Toast.makeText(this, "Your orders", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val orders = snapshots?.mapNotNull { document ->
                    try {
                        document.toObject(Order::class.java).apply {
                            orderId = document.id
                        }
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                adapter.submitList(orders)

                // Update empty state visibility
                if (orders.isEmpty()) {
                    binding.emptyState.visibility = View.VISIBLE
                    binding.ordersRecyclerView.visibility = View.GONE
                } else {
                    binding.emptyState.visibility = View.GONE
                    binding.ordersRecyclerView.visibility = View.VISIBLE
                }
            }
    }
}