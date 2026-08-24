package com.rick.finnstock.feataure.stocks.data.remote

/**
 * Display labels match the Finnhub-style ticker banner; request symbols are Finnhub free-tier IDs.
 */
enum class TickerSymbol(
    val requestSymbol: String,
    val displayName: String,
) {
    TSMC("TSM","TSMC"),
    AAPL("AAPL", "AAPL"),
    Nvidia("NVDA","NVDA"),
    Alphabet("GOOGL","GOOGL"),
    Microsoft("MSFT","MSFT"),
    Amazon("AMZN","AMZN")
}
