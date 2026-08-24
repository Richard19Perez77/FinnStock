package com.rick.finnstock.feature.stocks.domain.usecase

import com.rick.finnstock.feature.stocks.domain.model.NewsArticle
import com.rick.finnstock.feature.stocks.domain.repository.MarketRepository
import javax.inject.Inject

class GetMarketNewsUseCase @Inject constructor(
    private val repository: MarketRepository,
) {
    suspend operator fun invoke(): Result<List<NewsArticle>> = repository.getMarketNews()
}
