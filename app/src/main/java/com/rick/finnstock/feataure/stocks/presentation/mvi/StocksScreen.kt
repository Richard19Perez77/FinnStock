package com.rick.finnstock.feataure.stocks.presentation.mvi

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rick.finnstock.feataure.stocks.domain.model.Quote
import com.rick.finnstock.ui.theme.FinnStockTheme
import java.util.Locale
import kotlin.math.abs

@Composable
fun StocksScreen(
    state: StocksContract.State,
    onIntent: (StocksContract.Intent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        QuoteTickerBanner(
            quotes = state.quotes,
            isLoading = state.isLoading,
            errorMessage = state.errorMessage,
            onRetry = { onIntent(StocksContract.Intent.Retry) },
        )
        HorizontalDivider()
        NewsListPlaceholder(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        )
    }
}

@Composable
private fun QuoteTickerBanner(
    quotes: List<Quote>,
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        when {
            isLoading && quotes.isEmpty() -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .height(24.dp)
                            .width(24.dp),
                        strokeWidth = 2.dp,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Loading quotes…",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            errorMessage != null && quotes.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                    Button(onClick = onRetry) {
                        Text("Retry")
                    }
                }
            }

            else -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    quotes.forEach { quote ->
                        QuoteTickerItem(quote = quote)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuoteTickerItem(
    quote: Quote,
    modifier: Modifier = Modifier,
) {
    val isPositive = quote.change >= 0
    val changeColor = if (isPositive) Color(0xFF1B7A3D) else Color(0xFFB3261E)
    val sign = if (isPositive) "" else "-"

    Column(modifier = modifier) {
        Text(
            text = quote.displayName,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = formatPrice(quote.currentPrice),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = String.format(
                Locale.US,
                "%s%.2f%% (%s%.2f)",
                if (isPositive) "+" else "",
                quote.percentChange,
                sign,
                abs(quote.change),
            ),
            style = MaterialTheme.typography.bodySmall,
            color = changeColor,
        )
    }
}

@Composable
private fun NewsListPlaceholder(
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = "Market news",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Coming next — Finnhub free general news",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
            )
        }
        items(6) { index ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        shape = MaterialTheme.shapes.medium,
                    )
                    .padding(14.dp),
            ) {
                Text(
                    text = "News placeholder ${index + 1}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Headline and source will load from /news?category=general",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun formatPrice(price: Double): String =
    if (price >= 100) {
        String.format(Locale.US, "%.2f", price)
    } else if (price >= 1) {
        String.format(Locale.US, "%.4f", price)
    } else {
        String.format(Locale.US, "%.5f", price)
    }

@Preview(showBackground = true)
@Composable
private fun StocksScreenPreview() {
    FinnStockTheme {
        StocksScreen(
            state = StocksContract.State(
                quotes = listOf(
                    Quote("AAPL", "AAPL", 309.39, -1.91, -0.61),
                    Quote("MSFT", "MSFT", 483.28, 2.13, 0.44),
                    Quote("OANDA:EUR_USD", "EUR/USD", 1.1683, 0.0006, 0.05),
                ),
            ),
            onIntent = {},
        )
    }
}
