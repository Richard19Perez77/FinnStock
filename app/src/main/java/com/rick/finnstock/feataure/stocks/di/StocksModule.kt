package com.rick.finnstock.feataure.stocks.di

import com.rick.finnstock.feataure.stocks.data.remote.StocksApi
import com.rick.finnstock.feataure.stocks.data.repository.NetworkStocksRepository
import com.rick.finnstock.feataure.stocks.domain.repository.StocksRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StocksApiModule {

    @Provides
    @Singleton
    fun provideStocksApi(retrofit: Retrofit): StocksApi =
        retrofit.create(StocksApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class StocksBindModule {

    @Binds
    @Singleton
    abstract fun bindStocksRepository(
        impl: NetworkStocksRepository,
    ): StocksRepository
}
