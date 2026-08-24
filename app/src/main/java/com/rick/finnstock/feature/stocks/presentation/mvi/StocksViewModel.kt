package com.rick.finnstock.feature.stocks.presentation.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rick.finnstock.feature.stocks.domain.model.MarketResult
import com.rick.finnstock.feature.stocks.domain.usecase.GetMarketNewsUseCase
import com.rick.finnstock.feature.stocks.domain.usecase.GetQuotesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
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

    private var quotesJob: Job? = null
    private var newsJob: Job? = null

    init {
        onIntent(StocksContract.Intent.Load)
    }

    fun onIntent(intent: StocksContract.Intent) {
        when (intent) {
            StocksContract.Intent.Load -> load(isRefresh = false)
            StocksContract.Intent.Refresh -> load(isRefresh = true)
            StocksContract.Intent.RetryQuotes -> loadQuotesOnce()
            StocksContract.Intent.RetryNews -> loadNewsOnce()
            is StocksContract.Intent.ToggleShuffle ->
                dispatch(
                    StocksContract.PartialChange.ShuffleToggled(
                        enabled = intent.enabled,
                        seed = Random.nextLong(),
                    ),
                )
        }
    }

    private fun load(isRefresh: Boolean) {
        viewModelScope.launch {
            if (isRefresh) {
                dispatch(StocksContract.PartialChange.Refreshing)
            }
            joinAll(loadQuotesOnce(), loadNewsOnce())
            if (isRefresh) {
                dispatch(StocksContract.PartialChange.RefreshFinished)
            }
        }
    }

    /** An in-flight section is reused so repeated pulls never multiply free-tier calls. */
    private fun loadQuotesOnce(): Job =
        quotesJob?.takeIf { it.isActive }
            ?: viewModelScope.launch { loadQuotes() }.also { quotesJob = it }

    private fun loadNewsOnce(): Job =
        newsJob?.takeIf { it.isActive }
            ?: viewModelScope.launch { loadNews() }.also { newsJob = it }

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
