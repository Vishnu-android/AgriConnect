package com.example.campusbuddy.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.campusbuddy.R
import com.example.campusbuddy.Models.Commodity

class CommodityAdapter(private val commodityList: List<Commodity>) :
    RecyclerView.Adapter<CommodityAdapter.CommodityViewHolder>() {

    class CommodityViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.commodityName)
        val market: TextView = view.findViewById(R.id.marketName)
        val price: TextView = view.findViewById(R.id.commodityPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommodityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_commodity, parent, false)
        return CommodityViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommodityViewHolder, position: Int) {
        val commodity = commodityList[position]
        holder.name.text = commodity.commodity
        holder.market.text = "Market: ${commodity.market}"
        holder.price.text = "Price: ₹${commodity.modal_price}"
    }

    override fun getItemCount() = commodityList.size
}
