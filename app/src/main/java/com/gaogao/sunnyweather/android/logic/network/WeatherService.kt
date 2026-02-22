package com.gaogao.sunnyweather.android.logic.network

import com.gaogao.sunnyweather.android.SunnyWeatherApplication
import com.gaogao.sunnyweather.android.logic.model.DailyResponse
import com.gaogao.sunnyweather.android.logic.model.RealtimeResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * 在每个方法的上面仍然还是使用
 * @GET注解来声明要访问的API接口，并且我们还使用了@Path注解来向请求接口中动态传入经纬度的坐标
 *
 *
 */
interface WeatherService {

    @GET("v2.6/${SunnyWeatherApplication.TOKEN}/{lng},{lat}/realtime.json")
    fun getRealtimeWeather(@Path("lng") lng: String, @Path("lat") lat: String):
            Call<RealtimeResponse>

    @GET("v2.6/${SunnyWeatherApplication.TOKEN}/{lng},{lat}/daily?dailysteps=5")
    fun getDailyWeather(@Path("lng") lng: String, @Path("lat") lat: String):
            Call<DailyResponse>

}