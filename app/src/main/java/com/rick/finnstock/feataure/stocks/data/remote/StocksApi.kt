package com.rick.finnstock.feataure.stocks.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface StocksApi {
    @GET("quote")
    suspend fun getQuote(
        @Query("symbol") symbol: String,
    ): QuoteDto

    @GET("news")
    suspend fun getMarketNews(
        @Query("category") category: String = "general",
    ): List<NewsDto>
}
