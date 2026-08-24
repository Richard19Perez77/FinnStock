package com.rick.finnstock.feature.stocks.domain.repository

import com.rick.finnstock.feature.stocks.domain.model.NewsArticle
import com.rick.finnstock.feature.stocks.domain.model.Quote

interface MarketRepository {
    suspend fun getQuotes(): Result<List<Quote>>
    suspend fun getMarketNews(): Result<List<NewsArticle>>
}
