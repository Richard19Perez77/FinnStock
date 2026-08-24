package com.rick.finnstock.feature.stocks.presentation.mvi

import com.rick.finnstock.feature.stocks.domain.model.NewsArticle
import com.rick.finnstock.feature.stocks.domain.model.Quote

object StocksContract {

    data class State(
        val isLoadingQuotes: Boolean = false,
        val quotes: List<Quote> = emptyList(),
        val quotesError: String? = null,
        val isLoadingNews: Boolean = false,
        val news: List<NewsArticle> = emptyList(),
        val shuffledNews: List<NewsArticle> = emptyList(),
        val newsError: String? = null,
        val isShuffleEnabled: Boolean = false,
        val isRefreshing: Boolean = false,
    ) {
        val displayedNews: List<NewsArticle>
            get() = if (isShuffleEnabled) shuffledNews else news
    }

    sealed interface Intent {
        data object Load : Intent
        data object Refresh : Intent
        data object RetryQuotes : Intent
        data object RetryNews : Intent
        data class ToggleShuffle(val enabled: Boolean) : Intent
    }

    sealed interface Effect

    sealed interface PartialChange {
        data object QuotesLoading : PartialChange
        data class QuotesLoaded(val quotes: List<Quote>) : PartialChange
        data class QuotesFailed(val message: String) : PartialChange
        data object NewsLoading : PartialChange
        data class NewsLoaded(val news: List<NewsArticle>) : PartialChange
        data class NewsFailed(val message: String) : PartialChange
        data object Refreshing : PartialChange
        data object RefreshFinished : PartialChange
        data class ShuffleToggled(val enabled: Boolean) : PartialChange
    }
}
