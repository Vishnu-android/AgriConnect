package com.example.campusbuddy


import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusbuddy.adapters.CommodityAdapter
import com.example.campusbuddy.databinding.ActivityDashboardBinding
import com.example.campusbuddy.viewmodel.DashboardViewModel

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        viewModel.commodities.observe(this) { commodities ->
            binding.recyclerView.adapter = CommodityAdapter(commodities)
        }

        viewModel.fetchCommodityPrices()
    }
}
