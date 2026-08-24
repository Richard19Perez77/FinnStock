package com.rick.finnstock.feature.stocks.presentation.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rick.finnstock.feature.stocks.domain.model.MarketResult
import com.rick.finnstock.feature.stocks.domain.usecase.GetMarketNewsUseCase
import com.rick.finnstock.feature.stocks.domain.usecase.GetQuotesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class StocksViewModel @Inject constructor(
    private val getQuotesUseCase: GetQuotesUseCase,
    private val getMarketNewsUseCase: GetMarketNewsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(StocksContract.State())
    val state: StateFlow<StocksContract.State> = _state.asStateFlow()

    private var loadJob: Job? = null

    init {
        onIntent(StocksContract.Intent.Load)
    }

    fun onIntent(intent: StocksContract.Intent) {
        when (intent) {
            StocksContract.Intent.Load -> load(isRefresh = false)
            StocksContract.Intent.Refresh -> load(isRefresh = true)
            StocksContract.Intent.RetryQuotes -> launchOnce { loadQuotes() }
            StocksContract.Intent.RetryNews -> launchOnce { loadNews() }
            is StocksContract.Intent.ToggleShuffle ->
                dispatch(
                    StocksContract.PartialChange.ShuffleToggled(
                        enabled = intent.enabled,
                        seed = Random.nextLong(),
                    ),
                )
        }
    }

    private fun load(isRefresh: Boolean) = launchOnce {
        if (isRefresh) {
            dispatch(StocksContract.PartialChange.Refreshing)
        }
        coroutineScope {
            val quotes = async { loadQuotes() }
            val news = async { loadNews() }
            quotes.await()
            news.await()
        }
        if (isRefresh) {
            dispatch(StocksContract.PartialChange.RefreshFinished)
        }
    }

    /** Repeated pulls would otherwise multiply the calls counted against the free-tier limit. */
    private fun launchOnce(block: suspend () -> Unit) {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch { block() }
    }

    private suspend fun loadQuotes() {
        dispatch(StocksContract.PartialChange.QuotesLoading)
        when (val result = getQuotesUseCase()) {
            is MarketResult.Success ->
                dispatch(StocksContract.PartialChange.QuotesLoaded(result.data))

            is MarketResult.Failure ->
                dispatch(StocksContract.PartialChange.QuotesFailed(result.error))
        }
    }

    private suspend fun loadNews() {
        dispatch(StocksContract.PartialChange.NewsLoading)
        when (val result = getMarketNewsUseCase()) {
            is MarketResult.Success ->
                dispatch(
                    StocksContract.PartialChange.NewsLoaded(
                        news = result.data,
                        seed = Random.nextLong(),
                    ),
                )

            is MarketResult.Failure ->
                dispatch(StocksContract.PartialChange.NewsFailed(result.error))
        }
    }

    private fun dispatch(change: StocksContract.PartialChange) {
        _state.update { current -> StocksReducer.reduce(current, change) }
    }
}
