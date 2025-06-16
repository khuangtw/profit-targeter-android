package com.example.profittargeter.model

data class StrategyRequest(
    val option_type: String,
    val underlying_ticker: String,
    val strike_price: Double,
    val entry_price: Double,
    val days_to_expiration: Int,
    val delta: Double,
    val theta: Double,
    val iv_percent: Double,
    val iv_percentile: Double? = null,
    val days_for_iv: Int = 252
)