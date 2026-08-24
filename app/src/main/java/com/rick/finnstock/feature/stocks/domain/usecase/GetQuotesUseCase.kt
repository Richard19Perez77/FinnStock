package com.rick.finnstock.feature.stocks.domain.usecase

import com.rick.finnstock.feature.stocks.domain.model.Quote
import com.rick.finnstock.feature.stocks.domain.repository.MarketRepository
import javax.inject.Inject

class GetQuotesUseCase @Inject constructor(
    private val repository: MarketRepository,
) {
    suspend operator fun invoke(): Result<List<Quote>> = repository.getQuotes()
}
