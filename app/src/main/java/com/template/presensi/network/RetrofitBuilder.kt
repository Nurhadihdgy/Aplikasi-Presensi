package com.template.presensi.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ApiService
import com.google.gson.GsonBuilder


object RetrofitBuilder {
    const val BASE_URL = "http://192.168.70.53:8000/api/"

    private val gson = GsonBuilder().setLenient().create()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}