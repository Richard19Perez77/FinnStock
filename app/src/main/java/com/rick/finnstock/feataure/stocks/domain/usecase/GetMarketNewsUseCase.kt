package com.rick.finnstock.feataure.stocks.domain.usecase

import com.rick.finnstock.feataure.stocks.domain.model.NewsArticle
import com.rick.finnstock.feataure.stocks.domain.repository.StocksRepository
import javax.inject.Inject

class GetMarketNewsUseCase @Inject constructor(
    private val repository: StocksRepository,
) {
    suspend operator fun invoke(): Result<List<NewsArticle>> = repository.getMarketNews()
}
