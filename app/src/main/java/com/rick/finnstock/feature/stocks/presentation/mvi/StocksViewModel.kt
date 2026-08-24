package com.rick.finnstock.feature.stocks.presentation.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rick.finnstock.feature.stocks.domain.usecase.GetMarketNewsUseCase
import com.rick.finnstock.feature.stocks.domain.usecase.GetQuotesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StocksViewModel @Inject constructor(
    private val getQuotesUseCase: GetQuotesUseCase,
    private val getMarketNewsUseCase: GetMarketNewsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(StocksContract.State())
    val state: StateFlow<StocksContract.State> = _state.asStateFlow()

    init {
        onIntent(StocksContract.Intent.Load)
    }

    fun onIntent(intent: StocksContract.Intent) {
        when (intent) {
            StocksContract.Intent.Load -> load(isRefresh = false)
            StocksContract.Intent.Refresh -> load(isRefresh = true)
            StocksContract.Intent.RetryQuotes -> viewModelScope.launch { loadQuotes() }
            StocksContract.Intent.RetryNews -> viewModelScope.launch { loadNews() }
            is StocksContract.Intent.ToggleShuffle ->
                dispatch(StocksContract.PartialChange.ShuffleToggled(intent.enabled))
        }
    }

    private fun load(isRefresh: Boolean) {
        viewModelScope.launch {
            if (isRefresh) {
                dispatch(StocksContract.PartialChange.Refreshing)
            }
            coroutineScope {
                val quotesJob = async { loadQuotes() }
                val newsJob = async { loadNews() }
                quotesJob.await()
                newsJob.await()
            }
            if (isRefresh) {
                dispatch(StocksContract.PartialChange.RefreshFinished)
            }
        }
    }

    private suspend fun loadQuotes() {
        dispatch(StocksContract.PartialChange.QuotesLoading)
        getQuotesUseCase()
            .onSuccess { quotes ->
                dispatch(StocksContract.PartialChange.QuotesLoaded(quotes))
            }
            .onFailure { error ->
                dispatch(
                    StocksContract.PartialChange.QuotesFailed(
                        error.message ?: "Failed to load quotes",
                    ),
                )
            }
    }

    private suspend fun loadNews() {
        dispatch(StocksContract.PartialChange.NewsLoading)
        getMarketNewsUseCase()
            .onSuccess { news ->
                dispatch(StocksContract.PartialChange.NewsLoaded(news))
            }
            .onFailure { error ->
                dispatch(
                    StocksContract.PartialChange.NewsFailed(
                        error.message ?: "Failed to load news",
                    ),
                )
            }
    }

    private fun dispatch(change: StocksContract.PartialChange) {
        _state.update { current -> StocksReducer.reduce(current, change) }
    }
}
