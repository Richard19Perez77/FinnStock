package com.rick.finnstock.feature.stocks.data.repository

import com.rick.finnstock.core.di.FinnhubApiKey
import com.rick.finnstock.core.di.IoDispatcher
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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import javax.inject.Inject

class NetworkMarketRepository @Inject constructor(
    private val api: FinnhubApi,
    @FinnhubApiKey private val apiKey: String,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : MarketRepository {

    /**
     * Each symbol is fetched independently so one rejected ticker cannot empty the whole banner.
     */
    override suspend fun getQuotes(): MarketResult<List<Quote>> {
        if (apiKey.isBlank()) return MarketResult.Failure(MarketError.MissingApiKey)

        val perSymbol = try {
            withContext(ioDispatcher) {
                supervisorScope {
                    TickerSymbol.entries
                        .map { ticker -> async { runCatchingCancellable { fetchQuote(ticker) } } }
                        .awaitAll()
                }
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            return MarketResult.Failure(throwable.toMarketError())
        }

        val quotes = perSymbol.mapNotNull { it.getOrNull() }
        if (quotes.isNotEmpty()) return MarketResult.Success(quotes)

        val firstFailure = perSymbol.firstNotNullOfOrNull { it.exceptionOrNull() }
        return MarketResult.Failure(
            firstFailure?.toMarketError()
                ?: MarketError.Unknown("No quotes available for these symbols."),
        )
    }

    override suspend fun getMarketNews(): MarketResult<List<NewsArticle>> {
        if (apiKey.isBlank()) return MarketResult.Failure(MarketError.MissingApiKey)

        return runCatchingMarket {
            api.getMarketNews(category = NEWS_CATEGORY)
                .mapNotNull { it.toDomainOrNull() }
                .distinctBy { it.id }
                .sortedByDescending { it.datetimeSeconds }
        }
    }

    private suspend fun fetchQuote(ticker: TickerSymbol): Quote? =
        api.getQuote(ticker.requestSymbol)
            .toDomainOrNull(
                symbol = ticker.requestSymbol,
                displayName = ticker.displayName,
            )

    private suspend fun <T> runCatchingMarket(
        block: suspend () -> T,
    ): MarketResult<T> =
        try {
            MarketResult.Success(withContext(ioDispatcher) { block() })
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            MarketResult.Failure(throwable.toMarketError())
        }

    private suspend fun <T> runCatchingCancellable(
        block: suspend () -> T,
    ): Result<T> =
        try {
            Result.success(block())
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            Result.failure(throwable)
        }

    private companion object {
        const val NEWS_CATEGORY = "general"
    }
}
