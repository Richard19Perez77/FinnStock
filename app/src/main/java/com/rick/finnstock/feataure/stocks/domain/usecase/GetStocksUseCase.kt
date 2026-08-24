package com.rick.finnstock.feataure.stocks.domain.usecase

import com.rick.finnstock.feataure.stocks.domain.model.Quote
import com.rick.finnstock.feataure.stocks.domain.repository.StocksRepository
import javax.inject.Inject

class GetStocksUseCase @Inject constructor(
    private val repository: StocksRepository,
) {
    suspend operator fun invoke(): Result<List<Quote>> = repository.getQuotes()
}
