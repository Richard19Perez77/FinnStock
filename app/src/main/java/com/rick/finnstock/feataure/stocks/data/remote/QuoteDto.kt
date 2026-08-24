package com.rick.finnstock.feataure.stocks.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class QuoteDto(
    @property:Json(name = "c") val currentPrice: Double? = null,
    @property:Json(name = "d") val change: Double? = null,
    @property:Json(name = "dp") val percentChange: Double? = null,
    @property:Json(name = "h") val high: Double? = null,
    @property:Json(name = "l") val low: Double? = null,
    @property:Json(name = "o") val open: Double? = null,
    @property:Json(name = "pc") val previousClose: Double? = null,
    @property:Json(name = "t") val timestamp: Long? = null,
)
