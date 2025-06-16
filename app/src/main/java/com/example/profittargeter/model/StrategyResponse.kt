package com.example.profittargeter.model

import com.squareup.moshi.Json

data class StrategyResponse(
    val stop_loss_price: Double,
    val stop_loss_pct: String,
    val target_profit_price: Double,
    val target_profit_pct: String,
    val iv_used_days: Int,
    @Json(name = "25%") val q25: Double,
    @Json(name = "50%") val q50: Double,
    @Json(name = "75%") val q75: Double,
    val iv_quartiles: Map<String, Double>,
    val notes: List<String>,
    val exit_reminder: String?
)
