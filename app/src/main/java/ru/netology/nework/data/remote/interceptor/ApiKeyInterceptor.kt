package ru.netology.nework.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import ru.netology.nework.BuildConfig
import javax.inject.Inject

class ApiKeyInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestWithApiKey = originalRequest.newBuilder()
            .addHeader("Api-Key", BuildConfig.NETOLOGY_API_KEY)
            .build()
        return chain.proceed(requestWithApiKey)
    }
}