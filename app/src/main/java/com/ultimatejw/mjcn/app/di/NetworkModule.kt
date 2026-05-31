package com.ultimatejw.mjcn.app.di

import com.ultimatejw.mjcn.data.remote.AuthApiService
import com.ultimatejw.mjcn.data.remote.CourseHistoryApiService
import com.ultimatejw.mjcn.data.remote.CurrentCourseApiService
import com.ultimatejw.mjcn.data.remote.InterestApiService
import com.ultimatejw.mjcn.data.remote.MjcnApiService
import com.ultimatejw.mjcn.data.remote.ProfileApiService
import com.ultimatejw.mjcn.data.remote.interceptor.AuthInterceptor
import com.ultimatejw.mjcn.data.remote.interceptor.TokenRefreshAuthenticator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    @Singleton
    @Provides
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor,
        tokenRefreshAuthenticator: TokenRefreshAuthenticator
    ): OkHttpClient =
        OkHttpClient.Builder()
            // AuthInterceptor가 먼저 토큰을 붙인 뒤 logging이 그 결과를 로깅하도록 순서 유지.
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .authenticator(tokenRefreshAuthenticator)
            .build()

    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("http://3.34.185.127:8000/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Singleton
    @Provides
    fun provideMjcnApiService(retrofit: Retrofit): MjcnApiService =
        retrofit.create(MjcnApiService::class.java)

    @Singleton
    @Provides
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService =
        retrofit.create(AuthApiService::class.java)

    @Singleton
    @Provides
    fun provideProfileApiService(retrofit: Retrofit): ProfileApiService =
        retrofit.create(ProfileApiService::class.java)

    @Singleton
    @Provides
    fun provideInterestApiService(retrofit: Retrofit): InterestApiService =
        retrofit.create(InterestApiService::class.java)

    @Singleton
    @Provides
    fun provideCourseHistoryApiService(retrofit: Retrofit): CourseHistoryApiService =
        retrofit.create(CourseHistoryApiService::class.java)

    @Singleton
    @Provides
    fun provideCurrentCourseApiService(retrofit: Retrofit): CurrentCourseApiService =
        retrofit.create(CurrentCourseApiService::class.java)
}
