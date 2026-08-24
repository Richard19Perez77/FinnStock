package com.rick.finnstock.feature.stocks.domain.model

sealed interface MarketResult<out T> {
    data class Success<out T>(val data: T) : MarketResult<T>
    data class Failure(val error: MarketError) : MarketResult<Nothing>
}
