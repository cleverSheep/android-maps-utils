package com.google.maps.android.utils.demo

import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface WellDataClient {
    @GET("getWellData/{id}")
    fun getWellData(@Path("id") id: Int): Single<WellData>
}