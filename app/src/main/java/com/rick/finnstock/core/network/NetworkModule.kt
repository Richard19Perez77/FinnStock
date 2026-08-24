package com.rick.finnstock.core.network

import com.rick.finnstock.BuildConfig
import com.rick.finnstock.core.di.AuthInterceptor
import com.rick.finnstock.core.di.FinnhubApiKey
import com.rick.finnstock.core.di.LoggingInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val FINNHUB_BASE_URL = "https://finnhub.io/api/v1/"

    @Provides
    @Singleton
    @FinnhubApiKey
    fun provideFinnhubApiKey(): String = BuildConfig.FINNHUB_API_KEY

    @Provides
    @Singleton
    fun provideMoshi(): Moshi =
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

    @Provides
    @Singleton
    @AuthInterceptor
    fun provideAuthInterceptor(
        @FinnhubApiKey apiKey: String,
    ): Interceptor = Interceptor { chain ->
        val request = chain.request()
        val url = request.url.newBuilder()
            .addQueryParameter("token", apiKey)
            .build()
        chain.proceed(request.newBuilder().url(url).build())
    }

    @Provides
    @Singleton
    @LoggingInterceptor
    fun provideLoggingInterceptor(): Interceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        @AuthInterceptor authInterceptor: Interceptor,
        @LoggingInterceptor loggingInterceptor: Interceptor,
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        moshi: Moshi,
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(FINNHUB_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
}
