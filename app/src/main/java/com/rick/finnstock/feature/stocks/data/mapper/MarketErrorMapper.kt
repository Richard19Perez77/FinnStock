package com.rick.finnstock.feature.stocks.data.mapper

import com.rick.finnstock.feature.stocks.domain.model.MarketError
import retrofit2.HttpException
import java.io.IOException

fun Throwable.toMarketError(): MarketError =
    when (this) {
        is HttpException -> when (code()) {
            401, 403 -> MarketError.Unauthorized
            429 -> MarketError.RateLimited
            else -> MarketError.Unknown(message)
        }

        is IOException -> MarketError.Network
        else -> MarketError.Unknown(message)
    }
