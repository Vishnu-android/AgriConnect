package com.example.campusbuddy.adapters

import Order
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.campusbuddy.R
import java.text.SimpleDateFormat
import java.util.*

class OrderAdapter(private val orders: List<Order>) :
    RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    // Use ContextCompat for color resources
    private var pendingColor: Int = 0
    private var shippedColor: Int = 0
    private var deliveredColor: Int = 0
    private var defaultColor: Int = 0

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textOrderId: TextView = itemView.findViewById(R.id.textOrderId)
        val textProductName: TextView = itemView.findViewById(R.id.textProductName)
        val textCustomerName: TextView = itemView.findViewById(R.id.textCustomerName)
        val textOrderDate: TextView = itemView.findViewById(R.id.textOrderDate)
        val textOrderTotal: TextView = itemView.findViewById(R.id.textOrderTotal)
        val textOrderStatus: TextView = itemView.findViewById(R.id.textOrderStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        // Initialize colors once
        if (pendingColor == 0) {
            pendingColor = ContextCompat.getColor(parent.context, R.color.dark_green)
            shippedColor = ContextCompat.getColor(parent.context, R.color.light_green)
            deliveredColor = ContextCompat.getColor(parent.context, R.color.medium_green)
            defaultColor = ContextCompat.getColor(parent.context, R.color.black)
        }

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders.getOrNull(position) ?: return // Safe array access

        with(holder) {
            textOrderId.text = "Order #${order.orderId.takeLast(6)}"
            textProductName.text = order.productName ?: "N/A"
            textCustomerName.text = order.buyerName ?: "N/A"
            textOrderTotal.text = "₹${order.price}"

            // Format date safely
            val dateText = try {
                val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                order.orderDate?.toDate()?.let { dateFormat.format(it) } ?: "Date not available"
            } catch (e: Exception) {
                "Invalid date"
            }
            textOrderDate.text = dateText

            // Handle status with null safety
            when (order.status?.lowercase(Locale.ROOT)) {
                "pending" -> textOrderStatus.setTextColor(pendingColor)
                "shipped" -> textOrderStatus.setTextColor(shippedColor)
                "delivered" -> textOrderStatus.setTextColor(deliveredColor)
                else -> textOrderStatus.setTextColor(defaultColor)
            }
            textOrderStatus.text = order.status ?: "Status unknown"
        }
    }

    override fun getItemCount() = orders.size

    // Optional: Add DiffUtil support for better performance
    fun updateOrders(newOrders: List<Order>) {
        if (orders is MutableList) {
            (orders as MutableList).clear()
            (orders as MutableList).addAll(newOrders)
            notifyDataSetChanged()
        }
    }
}