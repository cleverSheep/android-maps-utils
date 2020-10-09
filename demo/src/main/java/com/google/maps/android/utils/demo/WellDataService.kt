package com.google.maps.android.utils.demo

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class WellDataService {
    companion object {
        val gsonBuilder = GsonBuilder()
        val gson = gsonBuilder.create()
        val gsonConverterFactory = GsonConverterFactory.create(gson)

        // Retrofit is not an instance of this service so it needs to be set here
        private val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl("https://192.168.0.25:5000/")
                .addConverterFactory(gsonConverterFactory)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()

        fun service(): WellDataClient = retrofit.create(WellDataClient::class.java)
    }
}