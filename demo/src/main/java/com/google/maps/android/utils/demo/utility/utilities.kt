package com.google.maps.android.utils.demo.utility

import com.google.maps.android.utils.demo.WellData

fun combineLatLng(wellData: WellData): String {
    val latitide = String.format("%.2f", wellData.latitude)
    val longitude = String.format("%.2f", wellData.longitude)
    return "$latitide,$longitude"
}