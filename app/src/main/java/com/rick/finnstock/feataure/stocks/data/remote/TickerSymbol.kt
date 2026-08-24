package com.rick.finnstock.feataure.stocks.data.remote

/**
 * Display labels match the Finnhub-style ticker banner; request symbols are Finnhub free-tier IDs.
 */
enum class TickerSymbol(
    val requestSymbol: String,
    val displayName: String,
) {
    BTC("BINANCE:BTCUSDT", "BTC-USD"),
    ETH("BINANCE:ETHUSDT", "ETH-USD"),
    EUR_USD("OANDA:EUR_USD", "EUR/USD"),
    GBP_USD("OANDA:GBP_USD", "GBP/USD"),
    AAPL("AAPL", "AAPL"),
    MSFT("MSFT", "MSFT"),
    AMZN("AMZN", "AMZN"),
}
