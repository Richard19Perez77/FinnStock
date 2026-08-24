package com.rick.finnstock.feataure.stocks.presentation.mvi

object StocksReducer {

    fun reduce(
        state: StocksContract.State,
        change: StocksContract.PartialChange,
    ): StocksContract.State =
        when (change) {
            StocksContract.PartialChange.Loading ->
                state.copy(
                    isLoading = true,
                    errorMessage = null,
                )

            is StocksContract.PartialChange.QuotesLoaded ->
                state.copy(
                    isLoading = false,
                    quotes = change.quotes,
                    errorMessage = null,
                )

            is StocksContract.PartialChange.Failed ->
                state.copy(
                    isLoading = false,
                    errorMessage = change.message,
                )
        }
}
