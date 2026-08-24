package com.rick.finnstock.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.rick.finnstock.feataure.stocks.presentation.mvi.StocksRoute

/**
 * Single-destination host for now. Expand when more features land.
 */
@Composable
fun FinnStockNavHost(
    modifier: Modifier = Modifier,
) {
    StocksRoute(modifier = modifier)
}
