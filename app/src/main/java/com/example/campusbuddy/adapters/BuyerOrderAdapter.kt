package com.example.campusbuddy.adapters

import Order
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.campusbuddy.R
import com.example.campusbuddy.databinding.ItemBuyerOrderBinding
import java.text.SimpleDateFormat
import java.util.*

class BuyerOrderAdapter(
private val onOrderClick: (Order) -> Unit
) : ListAdapter<Order, BuyerOrderAdapter.OrderViewHolder>(OrderDiffCallback()) {

    // Cache date formatter to avoid recreating it for each item
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemBuyerOrderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderViewHolder(binding, onOrderClick)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = getItem(position)
        holder.bind(order)
    }

    inner class OrderViewHolder(
        private val binding: ItemBuyerOrderBinding,
        private val onOrderClick: (Order) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(order: Order) {
            try {
                binding.apply {
                    // Set order details with null checks
                    productName.text = order.productName.ifEmpty { "N/A" }
                    orderId.text = root.context.getString(
                        R.string.order_id_format,
                        order.orderId.takeLast(6).uppercase()
                    )

                    // Format date safely
                    orderDate.text = try {
                        order.orderDate?.toDate()?.let { dateFormat.format(it) }
                            ?: "Date not available"
                    } catch (e: Exception) {
                        "Invalid date"
                    }

                    // Set price and quantity
                    price.text = root.context.getString(R.string.price_format, order.price)
                    quantity.text = root.context.getString(R.string.quantity_format, order.quantity)

                    // Set status with appropriate color
                    status.text = order.status
                    val statusColor = when (order.status.lowercase(Locale.ROOT)) {
                        "pending" -> ContextCompat.getColor(root.context, R.color.medium_green)
                        "shipped" -> ContextCompat.getColor(root.context, R.color.dark_green)
                        "delivered" -> ContextCompat.getColor(root.context, R.color.medium_green)
                        "cancelled" -> ContextCompat.getColor(root.context, R.color.moss_green)
                        else -> ContextCompat.getColor(root.context, R.color.very_light_green)
                    }
                    status.setTextColor(statusColor)

                    // Handle click
                    root.setOnClickListener { onOrderClick(order) }
                }
            } catch (e: Exception) {
                Log.e("OrderAdapter", "Error binding order", e)
            }
        }
    }

    class OrderDiffCallback : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order) =
            oldItem.orderId == newItem.orderId

        override fun areContentsTheSame(oldItem: Order, newItem: Order) =
            oldItem == newItem
    }
}