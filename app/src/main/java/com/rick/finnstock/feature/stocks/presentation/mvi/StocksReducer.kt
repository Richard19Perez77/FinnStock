package com.rick.finnstock.feature.stocks.presentation.mvi

object StocksReducer {

    fun reduce(
        state: StocksContract.State,
        change: StocksContract.PartialChange,
    ): StocksContract.State =
        when (change) {
            StocksContract.PartialChange.QuotesLoading ->
                state.copy(quotes = state.quotes.toLoadingKeepingContent())

            is StocksContract.PartialChange.QuotesLoaded ->
                state.copy(quotes = SectionState.Content(change.quotes))

            is StocksContract.PartialChange.QuotesFailed ->
                state.copy(quotes = state.quotes.toFailureKeepingContent(change.error))

            StocksContract.PartialChange.NewsLoading ->
                state.copy(news = state.news.toLoadingKeepingContent())

            is StocksContract.PartialChange.NewsLoaded ->
                state.copy(
                    news = SectionState.Content(change.news),
                    shuffleSeed = if (state.isShuffleEnabled) change.seed else state.shuffleSeed,
                )

            is StocksContract.PartialChange.NewsFailed ->
                state.copy(news = state.news.toFailureKeepingContent(change.error))

            StocksContract.PartialChange.Refreshing ->
                state.copy(isRefreshing = true)

            StocksContract.PartialChange.RefreshFinished ->
                state.copy(isRefreshing = false)

            is StocksContract.PartialChange.ShuffleToggled ->
                state.copy(
                    isShuffleEnabled = change.enabled,
                    shuffleSeed = if (change.enabled) change.seed else state.shuffleSeed,
                )
        }
}
