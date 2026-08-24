package com.rick.finnstock.feature.stocks.domain.model

sealed interface MarketError {
    data object MissingApiKey : MarketError
    data object Unauthorized : MarketError
    data object RateLimited : MarketError
    data object Network : MarketError
    data class Unknown(val message: String?) : MarketError
}
