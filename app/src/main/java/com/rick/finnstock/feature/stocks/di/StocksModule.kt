package com.rick.finnstock.feature.stocks.di

import com.rick.finnstock.feature.stocks.data.remote.FinnhubApi
import com.rick.finnstock.feature.stocks.data.repository.NetworkMarketRepository
import com.rick.finnstock.feature.stocks.domain.repository.MarketRepository
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
    fun provideFinnhubApi(retrofit: Retrofit): FinnhubApi =
        retrofit.create(FinnhubApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class StocksBindModule {

    @Binds
    @Singleton
    abstract fun bindMarketRepository(
        impl: NetworkMarketRepository,
    ): MarketRepository
}
