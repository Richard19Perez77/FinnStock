package com.rick.finnstock.feature.stocks.presentation.mvi

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rick.finnstock.feature.stocks.domain.model.MarketError
import com.rick.finnstock.feature.stocks.domain.model.NewsArticle
import com.rick.finnstock.feature.stocks.domain.model.Quote
import com.rick.finnstock.ui.theme.FinnStockTheme
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StocksScreen(
    state: StocksContract.State,
    onIntent: (StocksContract.Intent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val newsToShow = remember(state.news, state.isShuffleEnabled, state.shuffleSeed) {
        val articles = state.news.contentOrNull.orEmpty()
        if (state.isShuffleEnabled) {
            articles.shuffled(Random(state.shuffleSeed))
        } else {
            articles
        }
    }

    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = { onIntent(StocksContract.Intent.Refresh) },
        modifier = modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                QuoteTickerBanner(
                    state = state.quotes,
                    onRetry = { onIntent(StocksContract.Intent.RetryQuotes) },
                )
                HorizontalDivider()
            }
            item {
                NewsHeader(
                    isShuffleEnabled = state.isShuffleEnabled,
                    onShuffleChanged = { enabled ->
                        onIntent(StocksContract.Intent.ToggleShuffle(enabled))
                    },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            when (val news = state.news) {
                SectionState.Loading -> item {
                    LoadingRow(
                        label = "Loading news…",
                        modifier = Modifier.padding(24.dp),
                    )
                }

                is SectionState.Failure -> item {
                    ErrorSection(
                        error = news.error,
                        onRetry = { onIntent(StocksContract.Intent.RetryNews) },
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }

                is SectionState.Content -> items(
                    items = newsToShow,
                    key = { it.id },
                ) { article ->
                    NewsRow(
                        article = article,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun NewsHeader(
    isShuffleEnabled: Boolean,
    onShuffleChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Market news",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = if (isShuffleEnabled) {
                    "Shuffled — pull to refresh for a new mix"
                } else {
                    "Latest first"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(
            modifier = Modifier.toggleable(
                value = isShuffleEnabled,
                onValueChange = onShuffleChanged,
                role = Role.Switch,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Shuffle",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(end = 8.dp),
            )
            Switch(
                checked = isShuffleEnabled,
                onCheckedChange = null,
            )
        }
    }
}

@Composable
private fun NewsRow(
    article: NewsArticle,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                shape = MaterialTheme.shapes.medium,
            )
            .padding(14.dp),
    ) {
        Text(
            text = article.headline,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = listOf(
                article.source,
                formatRelativeTime(article.datetimeSeconds),
            ).filter { it.isNotBlank() }.joinToString(" · "),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun QuoteTickerBanner(
    state: SectionState<List<Quote>>,
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
        when (state) {
            SectionState.Loading ->
                LoadingRow(
                    label = "Loading quotes…",
                    modifier = Modifier.padding(horizontal = 16.dp),
                )

            is SectionState.Failure ->
                ErrorSection(
                    error = state.error,
                    onRetry = onRetry,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )

            is SectionState.Content ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    state.data.forEach { quote ->
                        QuoteTickerItem(quote = quote)
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
    val changeColor = if (quote.change >= 0) Color(0xFF1B7A3D) else Color(0xFFB3261E)

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
                "%.2f%% (%.2f)",
                quote.percentChange,
                quote.change,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = changeColor,
        )
    }
}

@Composable
private fun LoadingRow(
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
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
            text = label,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun ErrorSection(
    error: MarketError,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = error.toMessage(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
        )
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

private fun MarketError.toMessage(): String =
    when (this) {
        MarketError.MissingApiKey ->
            "Add FINNHUB_API_KEY to local.properties and rebuild."

        MarketError.Unauthorized ->
            "Finnhub rejected this API key. Check the key and rebuild."

        MarketError.RateLimited ->
            "Rate limit reached. The free tier allows 60 calls per minute."

        MarketError.Network ->
            "Can't reach Finnhub. Check your connection and retry."

        is MarketError.Unknown ->
            message ?: "Something went wrong."
    }

private fun formatPrice(price: Double): String =
    String.format(Locale.US, "%.2f", price)

private fun formatRelativeTime(datetimeSeconds: Long): String {
    if (datetimeSeconds <= 0L) return ""
    val elapsedMs = System.currentTimeMillis() - datetimeSeconds * 1000
    val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMs)
    val hours = TimeUnit.MILLISECONDS.toHours(elapsedMs)
    val days = TimeUnit.MILLISECONDS.toDays(elapsedMs)
    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        else -> "${days}d ago"
    }
}

@Preview(showBackground = true)
@Composable
private fun StocksScreenPreview() {
    FinnStockTheme {
        StocksScreen(
            state = StocksContract.State(
                quotes = SectionState.Content(
                    listOf(
                        Quote("AAPL", "AAPL", 309.39, -1.91, -0.61),
                        Quote("MSFT", "MSFT", 483.28, 2.13, 0.44),
                    ),
                ),
                news = SectionState.Content(
                    listOf(
                        NewsArticle(
                            id = 1,
                            headline = "Markets open mixed as tech leads gains",
                            source = "Reuters",
                            summary = "",
                            url = "",
                            datetimeSeconds = System.currentTimeMillis() / 1000,
                        ),
                    ),
                ),
            ),
            onIntent = {},
        )
    }
}
