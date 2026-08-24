package com.rick.finnstock.feature.stocks.domain.usecase

import com.rick.finnstock.feature.stocks.domain.model.MarketResult
import com.rick.finnstock.feature.stocks.domain.model.Quote
import com.rick.finnstock.feature.stocks.domain.repository.MarketRepository
import javax.inject.Inject

class GetQuotesUseCase @Inject constructor(
    private val repository: MarketRepository,
) {
    suspend operator fun invoke(): MarketResult<List<Quote>> = repository.getQuotes()
}
