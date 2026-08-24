package com.rick.finnstock.feature.stocks.data.repository

import com.rick.finnstock.BuildConfig
import com.rick.finnstock.core.di.IoDispatcher
import com.rick.finnstock.feature.stocks.data.mapper.toDomain
import com.rick.finnstock.feature.stocks.data.mapper.toDomainOrNull
import com.rick.finnstock.feature.stocks.data.mapper.toMarketError
import com.rick.finnstock.feature.stocks.data.remote.FinnhubApi
import com.rick.finnstock.feature.stocks.data.remote.TickerSymbol
import com.rick.finnstock.feature.stocks.domain.model.MarketError
import com.rick.finnstock.feature.stocks.domain.model.MarketResult
import com.rick.finnstock.feature.stocks.domain.model.NewsArticle
import com.rick.finnstock.feature.stocks.domain.model.Quote
import com.rick.finnstock.feature.stocks.domain.repository.MarketRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import javax.inject.Inject

class NetworkMarketRepository @Inject constructor(
    private val api: FinnhubApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : MarketRepository {

    override suspend fun getQuotes(): MarketResult<List<Quote>> {
        if (isApiKeyMissing()) return MarketResult.Failure(MarketError.MissingApiKey)

        return runCatchingMarket {
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

    override suspend fun getMarketNews(): MarketResult<List<NewsArticle>> {
        if (isApiKeyMissing()) return MarketResult.Failure(MarketError.MissingApiKey)

        return runCatchingMarket {
            api.getMarketNews(category = NEWS_CATEGORY)
                .mapNotNull { it.toDomainOrNull() }
                .distinctBy { it.id }
                .sortedByDescending { it.datetimeSeconds }
        }
    }

    private fun isApiKeyMissing(): Boolean = BuildConfig.FINNHUB_API_KEY.isBlank()

    private suspend fun <T> runCatchingMarket(
        block: suspend CoroutineScope.() -> T,
    ): MarketResult<T> =
        try {
            MarketResult.Success(withContext(ioDispatcher, block))
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            MarketResult.Failure(throwable.toMarketError())
        }

    private companion object {
        const val NEWS_CATEGORY = "general"
    }
}
