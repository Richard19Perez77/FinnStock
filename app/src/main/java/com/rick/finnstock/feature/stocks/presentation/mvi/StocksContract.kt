package com.rick.finnstock.feature.stocks.presentation.mvi

import com.rick.finnstock.feature.stocks.domain.model.MarketError
import com.rick.finnstock.feature.stocks.domain.model.NewsArticle
import com.rick.finnstock.feature.stocks.domain.model.Quote

object StocksContract {

    data class State(
        val quotes: SectionState<List<Quote>> = SectionState.Loading,
        val news: SectionState<List<NewsArticle>> = SectionState.Loading,
        val isShuffleEnabled: Boolean = false,
        val shuffleSeed: Long = 0L,
        val isRefreshing: Boolean = false,
    )

    sealed interface Intent {
        data object Load : Intent
        data object Refresh : Intent
        data object RetryQuotes : Intent
        data object RetryNews : Intent
        data class ToggleShuffle(val enabled: Boolean) : Intent
    }

    sealed interface PartialChange {
        data object QuotesLoading : PartialChange
        data class QuotesLoaded(val quotes: List<Quote>) : PartialChange
        data class QuotesFailed(val error: MarketError) : PartialChange
        data object NewsLoading : PartialChange
        data class NewsLoaded(val news: List<NewsArticle>, val seed: Long) : PartialChange
        data class NewsFailed(val error: MarketError) : PartialChange
        data object Refreshing : PartialChange
        data object RefreshFinished : PartialChange
        data class ShuffleToggled(val enabled: Boolean, val seed: Long) : PartialChange
    }
}
