package com.gaogao.sunnyweather.android

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

/**
 * 使用MVVM这种分层架构的设计，由于从ViewModel层开始就不再持有Activity的引用了，因此经常会出现“缺Context”的情况
 * SunnyWeather项目提供一种全局获取Context的方式
 *
 */
class SunnyWeatherApplication : Application() {

    companion object {

        const val TOKEN = "yoIZXYO4re9aU68r"

        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }


    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }
}