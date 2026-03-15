package ru.netology.nework.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.netology.nework.data.remote.api.AuthApi
import ru.netology.nework.data.remote.api.EventsApi
import ru.netology.nework.data.remote.api.JobsApi
import ru.netology.nework.data.remote.api.PostsApi
import ru.netology.nework.data.remote.api.UsersApi
import ru.netology.nework.data.remote.interceptor.ApiKeyInterceptor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        apiKeyInterceptor: ApiKeyInterceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(apiKeyInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("http://94.228.125.136:8080/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    fun provideAuthApi(retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    @Provides
    fun providePostsApi(retrofit: Retrofit): PostsApi =
        retrofit.create(PostsApi::class.java)

    @Provides
    fun provideEventsApi(retrofit: Retrofit): EventsApi =
        retrofit.create(EventsApi::class.java)

    @Provides
    fun provideUsersApi(retrofit: Retrofit): UsersApi =
        retrofit.create(UsersApi::class.java)

    @Provides
    fun provideJobsApi(retrofit: Retrofit): JobsApi =
        retrofit.create(JobsApi::class.java)
}