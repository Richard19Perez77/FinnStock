package com.rick.finnstock.feataure.stocks.data.repository

import com.rick.finnstock.BuildConfig
import com.rick.finnstock.feataure.stocks.data.mapper.toDomain
import com.rick.finnstock.feataure.stocks.data.remote.StocksApi
import com.rick.finnstock.feataure.stocks.data.remote.TickerSymbol
import com.rick.finnstock.feataure.stocks.domain.model.Quote
import com.rick.finnstock.feataure.stocks.domain.repository.StocksRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class NetworkStocksRepository @Inject constructor(
    private val api: StocksApi,
) : StocksRepository {

    override suspend fun getQuotes(): Result<List<Quote>> {
        if (BuildConfig.FINNHUB_API_KEY.isBlank()) {
            return Result.failure(
                IllegalStateException(
                    "Add FINNHUB_API_KEY=your_key to local.properties and rebuild.",
                ),
            )
        }

        return runCatching {
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
    }
}
