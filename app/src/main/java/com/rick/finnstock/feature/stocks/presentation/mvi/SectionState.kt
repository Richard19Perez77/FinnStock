package com.rick.finnstock.feature.stocks.presentation.mvi

import com.rick.finnstock.feature.stocks.domain.model.MarketError

sealed interface SectionState<out T> {
    data object Loading : SectionState<Nothing>
    data class Content<out T>(val data: T) : SectionState<T>
    data class Failure(val error: MarketError) : SectionState<Nothing>
}

val <T> SectionState<T>.contentOrNull: T?
    get() = (this as? SectionState.Content)?.data

/** Content already on screen survives a reload so a refresh never blanks the section. */
fun <T> SectionState<T>.toLoadingKeepingContent(): SectionState<T> =
    if (this is SectionState.Content) this else SectionState.Loading

fun <T> SectionState<T>.toFailureKeepingContent(error: MarketError): SectionState<T> =
    if (this is SectionState.Content) this else SectionState.Failure(error)
