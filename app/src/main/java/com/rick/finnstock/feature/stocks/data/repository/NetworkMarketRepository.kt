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
    @param:FinnhubApiKey private val apiKey: String,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : MarketRepository {

    /**
     * Each symbol is fetched independently so one rejected ticker cannot empty the whole banner.
     */
    override suspend fun getQuotes(): MarketResult<List<Quote>> {
        // early return with missing api key
        if (apiKey.isBlank()) return MarketResult.Failure(MarketError.MissingApiKey)

        // grouping isolated into individual calls
        val perSymbol = runCatchingCancellable {
            withContext(ioDispatcher) {
                supervisorScope {
                    // each entry will have its own scope to fail in
                    TickerSymbol.entries
                        .map { ticker -> async { runCatchingCancellable { fetchQuote(ticker) } } }
                        .awaitAll()
                }
            }
        }.getOrElse { return MarketResult.Failure(it.toMarketError()) }

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
        runCatchingCancellable { withContext(ioDispatcher) { block() } }
            .fold(
                onSuccess = { MarketResult.Success(it) },
                onFailure = { MarketResult.Failure(it.toMarketError()) },
            )

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
