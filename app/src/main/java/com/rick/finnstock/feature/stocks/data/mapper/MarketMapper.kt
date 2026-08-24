package com.rick.finnstock.feature.stocks.data.mapper

import com.rick.finnstock.feature.stocks.data.remote.NewsDto
import com.rick.finnstock.feature.stocks.data.remote.QuoteDto
import com.rick.finnstock.feature.stocks.domain.model.NewsArticle
import com.rick.finnstock.feature.stocks.domain.model.Quote

/**
 * Finnhub answers 200 with zeroed fields for symbols a key cannot access, so a zero price means
 * "no data" rather than a real quote.
 */
fun QuoteDto.toDomainOrNull(
    symbol: String,
    displayName: String,
): Quote? {
    val price = currentPrice ?: return null
    if (price <= 0.0) return null
    return Quote(
        symbol = symbol,
        displayName = displayName,
        currentPrice = price,
        change = change ?: 0.0,
        percentChange = percentChange ?: 0.0,
    )
}

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
