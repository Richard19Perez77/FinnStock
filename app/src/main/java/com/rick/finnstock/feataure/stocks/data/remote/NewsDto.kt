package com.rick.finnstock.feataure.stocks.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NewsDto(
    @property:Json(name = "id") val id: Long? = null,
    @property:Json(name = "headline") val headline: String? = null,
    @property:Json(name = "source") val source: String? = null,
    @property:Json(name = "summary") val summary: String? = null,
    @property:Json(name = "url") val url: String? = null,
    @property:Json(name = "datetime") val datetime: Long? = null,
    @property:Json(name = "category") val category: String? = null,
    @property:Json(name = "image") val image: String? = null,
    @property:Json(name = "related") val related: String? = null,
)
