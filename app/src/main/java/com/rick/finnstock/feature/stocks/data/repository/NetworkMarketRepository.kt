package com.rick.finnstock.feature.stocks.data.repository

import com.rick.finnstock.BuildConfig
import com.rick.finnstock.feature.stocks.data.mapper.toDomain
import com.rick.finnstock.feature.stocks.data.mapper.toDomainOrNull
import com.rick.finnstock.feature.stocks.data.remote.FinnhubApi
import com.rick.finnstock.feature.stocks.data.remote.TickerSymbol
import com.rick.finnstock.feature.stocks.domain.model.NewsArticle
import com.rick.finnstock.feature.stocks.domain.model.Quote
import com.rick.finnstock.feature.stocks.domain.repository.MarketRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class NetworkMarketRepository @Inject constructor(
    private val api: FinnhubApi,
) : MarketRepository {

    override suspend fun getQuotes(): Result<List<Quote>> = runCatching {
        ensureApiKey()
        coroutineScope {
            TickerSymbol.entries.map { ticker ->
                async {
                    api.getQuote(ticker.requestSymbol)
                        .toDomain(
                            symbol = ticker.requestSymbol,
                            displayName = ticker.displayName,
                        )
                }
            }.awaitAll()
        }
    }

    override suspend fun getMarketNews(): Result<List<NewsArticle>> = runCatching {
        ensureApiKey()
        api.getMarketNews(category = "general")
            .mapNotNull { it.toDomainOrNull() }
            .distinctBy { it.id }
            .sortedByDescending { it.datetimeSeconds }
    }

    private fun ensureApiKey() {
        if (BuildConfig.FINNHUB_API_KEY.isBlank()) {
            error("Add FINNHUB_API_KEY=your_key to local.properties and rebuild.")
        }
    }
}
