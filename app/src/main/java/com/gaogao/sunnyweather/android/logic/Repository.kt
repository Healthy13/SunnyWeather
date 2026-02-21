package com.gaogao.sunnyweather.android.logic

import android.util.Log
import androidx.lifecycle.liveData
import com.gaogao.sunnyweather.android.logic.model.Place
import com.gaogao.sunnyweather.android.logic.network.SunnyWeatherNetwork
import kotlinx.coroutines.Dispatchers

/**
 * 仓库层单例类代码
 * 仓库层的主要作用是判断调用方请求的数据应该是从本地数据源获取还是从网络数据源中获取，并将获得的数据返回给调用方
 * 因此，仓库层有点像是一个数据获取与缓存的中间层，在本地没有缓存数据的情况下去调用网络层获取
 */
object Repository {

    private val TAG = "Repository"

    /*
       为了能将异步获取的数据以响应式编程的方式通知给上一层，通常会返回一个LiveData对象
       这个emit()方法其实类似于调用LiveData的setValue()方法来通知数据变化，只不过这里我们无法直接取得返回的LiveData对象，所以lifecycle-livedata-ktx库提供了这样一个替代方法。
       将liveData()函数的线程参数类型指定成了Dispatchers.IO，这样代码块中的所有代码就都运行在子线程中了
     */
    fun searchPlaces(query: String) = liveData(Dispatchers.IO) {
        val result = try {
            val placeResponse = SunnyWeatherNetwork.searchPlaces(query)
            if (placeResponse.status == "ok") {
                val places = placeResponse.places
                Log.d(TAG, "${query}是本次查询的地区")
                Result.success(places)
            } else {
                Result.failure(RuntimeException("response status is ${placeResponse.status}"))
            }
        } catch (e: Exception) {
            Result.failure<List<Place>>(e)
        }
        emit(result)
    }

}