package com.example.campusbuddy.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusbuddy.Models.Commodity
import com.example.campusbuddy.network.RetrofitClient
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {

    private val _commodities = MutableLiveData<List<Commodity>>()
    val commodities: LiveData<List<Commodity>> get() = _commodities

    fun fetchCommodityPrices() {
        viewModelScope.launch {
            val response = RetrofitClient.apiService.getCommodityPrices()
            if (response.isSuccessful) {
                _commodities.postValue(response.body()?.records ?: emptyList())
            }
        }
    }
}
