package com.rick.finnstock.feataure.stocks.presentation.mvi

import com.rick.finnstock.feataure.stocks.domain.model.Quote

object StocksContract {

    data class State(
        val isLoading: Boolean = false,
        val quotes: List<Quote> = emptyList(),
        val errorMessage: String? = null,
    )

    sealed interface Intent {
        data object LoadQuotes : Intent
        data object Retry : Intent
    }

    sealed interface Effect

    sealed interface PartialChange {
        data object Loading : PartialChange
        data class QuotesLoaded(val quotes: List<Quote>) : PartialChange
        data class Failed(val message: String) : PartialChange
    }
}
