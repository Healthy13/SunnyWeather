package com.gaogao.sunnyweather.android.logic.network

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 *
 * 定义一个统一的网络数据源访问入口，对所有的网络请求的API进行封装
 * 非常关键的类
 */
object SunnyWeatherNetwork {

    // 动态代理对象
    private val placeService = ServiceCreator.create<PlaceService>()

    /**
     * 这个函数用来调用PlaceService接口中定义的searchPlaces()方法，以发起搜索城市数据请求
     */
    suspend fun searchPlaces(query: String) = placeService.searchPlaces(query).await()

    private suspend fun <T> Call<T>.await(): T {
        return suspendCoroutine { continuation ->
            enqueue(object : Callback<T> {
                override fun onResponse(call: Call<T?>, response: Response<T?>) {
                    val body = response.body()
                    if (body != null) continuation.resume(body)
                    else continuation.resumeWithException(
                        RuntimeException("response body is null")
                    )
                }

                override fun onFailure(call: Call<T?>, t: Throwable) {
                    continuation.resumeWithException(t)
                }
            })
        }
    }
}