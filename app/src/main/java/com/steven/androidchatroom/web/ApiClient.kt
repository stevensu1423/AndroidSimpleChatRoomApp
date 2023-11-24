package com.steven.androidchatroom.web

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.net.ssl.X509TrustManager


class ApiClient {
    private var retrofit: Retrofit? = null
    private val baseUrl = WebConfig.API_URL   //TODO input your server url


    fun getRetrofit(): Retrofit {
        if (retrofit == null) {
            val builder: Retrofit.Builder = Retrofit.Builder().baseUrl(baseUrl).addConverterFactory(GsonConverterFactory.create())
            val httpClient = OkHttpClient.Builder()
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY
            httpClient.connectTimeout(60, TimeUnit.SECONDS)
            httpClient.readTimeout(60, TimeUnit.SECONDS)
            httpClient.addNetworkInterceptor(logging)
            retrofit = builder.client(httpClient.build()).build()
        }
        return retrofit!!
    }
}