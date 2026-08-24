package com.rick.finnstock.feataure.stocks.data.mapper

import com.rick.finnstock.feataure.stocks.data.remote.QuoteDto
import com.rick.finnstock.feataure.stocks.domain.model.Quote

fun QuoteDto.toDomain(
    symbol: String,
    displayName: String,
): Quote =
    Quote(
        symbol = symbol,
        displayName = displayName,
        currentPrice = currentPrice ?: 0.0,
        change = change ?: 0.0,
        percentChange = percentChange ?: 0.0,
    )
