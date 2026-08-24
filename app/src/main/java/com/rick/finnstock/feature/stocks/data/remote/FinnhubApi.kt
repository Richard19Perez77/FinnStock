package com.rick.finnstock.feature.stocks.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface FinnhubApi {
    @GET("quote")
    suspend fun getQuote(
        @Query("symbol") symbol: String,
    ): QuoteDto

    @GET("news")
    suspend fun getMarketNews(
        @Query("category") category: String,
    ): List<NewsDto>
}
