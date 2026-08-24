package com.rick.finnstock.feature.stocks.data.mapper

import com.rick.finnstock.feature.stocks.data.remote.NewsDto
import com.rick.finnstock.feature.stocks.data.remote.QuoteDto
import com.rick.finnstock.feature.stocks.domain.model.NewsArticle
import com.rick.finnstock.feature.stocks.domain.model.Quote

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

fun NewsDto.toDomainOrNull(): NewsArticle? {
    val headlineText = headline?.trim().orEmpty()
    if (headlineText.isEmpty()) return null
    return NewsArticle(
        id = id ?: headlineText.hashCode().toLong(),
        headline = headlineText,
        source = source?.trim().orEmpty().ifEmpty { "Unknown" },
        summary = summary?.trim().orEmpty(),
        url = url.orEmpty(),
        datetimeSeconds = datetime ?: 0L,
    )
}
