package com.rick.finnstock.feataure.stocks.domain.repository

import com.rick.finnstock.feataure.stocks.domain.model.Quote

interface StocksRepository {
    suspend fun getQuotes(): Result<List<Quote>>
}
