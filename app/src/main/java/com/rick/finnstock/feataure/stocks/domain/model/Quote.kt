package com.rick.finnstock.feataure.stocks.domain.model

data class Quote(
    val symbol: String,
    val displayName: String,
    val currentPrice: Double,
    val change: Double,
    val percentChange: Double,
)
