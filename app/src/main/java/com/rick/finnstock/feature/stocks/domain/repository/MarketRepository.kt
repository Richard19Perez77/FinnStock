package com.rick.finnstock.feature.stocks.domain.repository

import com.rick.finnstock.feature.stocks.domain.model.MarketResult
import com.rick.finnstock.feature.stocks.domain.model.NewsArticle
import com.rick.finnstock.feature.stocks.domain.model.Quote

interface MarketRepository {
    suspend fun getQuotes(): MarketResult<List<Quote>>
    suspend fun getMarketNews(): MarketResult<List<NewsArticle>>
}
