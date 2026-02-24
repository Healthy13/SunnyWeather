package com.gaogao.sunnyweather.android.logic.dao

import android.content.Context
import androidx.core.content.edit
import com.gaogao.sunnyweather.android.SunnyWeatherApplication
import com.gaogao.sunnyweather.android.logic.model.Place
import com.google.gson.Gson

/*
    savePlace()方法用于将
  Place对象存储到SharedPreferences文件中，这里使用了一个技巧，我们先通过GSON将
  Place对象转成一个JSON字符串，然后就可以用字符串存储的方式来保存数据了
 */
object PlaceDao {

    fun savePlace(place: Place) {
        sharedPreferences().edit() {
            putString("place", Gson().toJson(place))
        }
    }

    fun getSavedPlace(): Place {
        val placeJson = sharedPreferences().getString("place", "")
        return Gson().fromJson(placeJson, Place::class.java)
    }

    fun isPlaceSaved(): Boolean {
        return sharedPreferences().contains("place")
    }

    private fun sharedPreferences() =
        SunnyWeatherApplication.context.getSharedPreferences("sunny_weather", Context.MODE_PRIVATE)

}