package com.gaogao.sunnyweather.android.logic.model

import com.google.gson.annotations.SerializedName

/**
 *
 * 定义的类与属性
 * 搜索城市数据接口返回的JSON格式来定义的
 *
 */
data class PlaceResponse(val status: String, val places: List<Place>)

data class Place(val name: String, val location: Location,
                @SerializedName("formatted_address") val address: String)

data class Location(val lng: String, val lat: String)