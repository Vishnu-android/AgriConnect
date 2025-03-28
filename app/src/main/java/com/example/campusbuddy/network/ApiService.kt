package com.example.campusbuddy.network

import com.example.campusbuddy.Models.Commodity
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("resource/9ef84268-d588-465a-a308-a864a43d0070")
    suspend fun getCommodityPrices(
        @Query("api-key") apiKey: String = "579b464db66ec23bdd0000019c90c64b20094d2776293735a90420dd",
        @Query("format") format: String = "json",
        @Query("filters[state.keyword]") state: String = "Gujarat",
        @Query("filters[district]") district: String = "Amreli",
        @Query("filters[market]") market: String = "Damnagar"
    ): Response<ApiResponse>
}

data class ApiResponse(val records: List<Commodity>)
