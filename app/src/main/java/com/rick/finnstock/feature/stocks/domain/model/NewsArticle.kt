package com.rick.finnstock.feature.stocks.domain.model

data class NewsArticle(
    val id: Long,
    val headline: String,
    val source: String,
    val summary: String,
    val url: String,
    val datetimeSeconds: Long,
)
