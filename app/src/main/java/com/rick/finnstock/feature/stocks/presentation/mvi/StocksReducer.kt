package com.rick.finnstock.feature.stocks.presentation.mvi

object StocksReducer {

    fun reduce(
        state: StocksContract.State,
        change: StocksContract.PartialChange,
    ): StocksContract.State =
        when (change) {
            StocksContract.PartialChange.QuotesLoading ->
                state.copy(
                    isLoadingQuotes = true,
                    quotesError = null,
                )

            is StocksContract.PartialChange.QuotesLoaded ->
                state.copy(
                    isLoadingQuotes = false,
                    quotes = change.quotes,
                    quotesError = null,
                )

            is StocksContract.PartialChange.QuotesFailed ->
                state.copy(
                    isLoadingQuotes = false,
                    quotesError = change.message,
                )

            StocksContract.PartialChange.NewsLoading ->
                state.copy(
                    isLoadingNews = true,
                    newsError = null,
                )

            is StocksContract.PartialChange.NewsLoaded ->
                state.copy(
                    isLoadingNews = false,
                    news = change.news,
                    shuffledNews = if (state.isShuffleEnabled) change.news.shuffled() else emptyList(),
                    newsError = null,
                )

            is StocksContract.PartialChange.NewsFailed ->
                state.copy(
                    isLoadingNews = false,
                    newsError = change.message,
                )

            StocksContract.PartialChange.Refreshing ->
                state.copy(isRefreshing = true)

            StocksContract.PartialChange.RefreshFinished ->
                state.copy(isRefreshing = false)

            is StocksContract.PartialChange.ShuffleToggled ->
                state.copy(
                    isShuffleEnabled = change.enabled,
                    shuffledNews = if (change.enabled) state.news.shuffled() else emptyList(),
                )
        }
}
