package com.example.profittargeter.network

import com.example.profittargeter.model.StrategyRequest
import com.example.profittargeter.model.StrategyResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface StrategyService {
    @POST("strategy")
    suspend fun getStrategy(@Body req: StrategyRequest): StrategyResponse
}
