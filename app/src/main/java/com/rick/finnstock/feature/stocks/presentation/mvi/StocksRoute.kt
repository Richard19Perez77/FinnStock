package com.rick.finnstock.feature.stocks.presentation.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun StocksRoute(
    modifier: Modifier = Modifier,
    viewModel: StocksViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    StocksScreen(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}
