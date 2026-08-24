package com.rick.finnstock.core.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class FinnhubApiKey

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthInterceptor

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LoggingInterceptor

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher
