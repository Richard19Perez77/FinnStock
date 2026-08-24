package com.rick.finnstock.feataure.stocks.presentation.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rick.finnstock.feataure.stocks.domain.usecase.GetStocksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StocksViewModel @Inject constructor(
    private val getStocksUseCase: GetStocksUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(StocksContract.State())
    val state: StateFlow<StocksContract.State> = _state.asStateFlow()

    init {
        onIntent(StocksContract.Intent.LoadQuotes)
    }

    fun onIntent(intent: StocksContract.Intent) {
        when (intent) {
            StocksContract.Intent.LoadQuotes,
            StocksContract.Intent.Retry,
            -> loadQuotes()
        }
    }

    private fun loadQuotes() {
        viewModelScope.launch {
            dispatch(StocksContract.PartialChange.Loading)
            getStocksUseCase()
                .onSuccess { quotes ->
                    dispatch(StocksContract.PartialChange.QuotesLoaded(quotes))
                }
                .onFailure { error ->
                    dispatch(
                        StocksContract.PartialChange.Failed(
                            error.message ?: "Failed to load quotes",
                        ),
                    )
                }
        }
    }

    private fun dispatch(change: StocksContract.PartialChange) {
        _state.update { current -> StocksReducer.reduce(current, change) }
    }
}
