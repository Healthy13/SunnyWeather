package com.gaogao.sunnyweather.android.logic

import android.util.Log
import androidx.lifecycle.liveData
import com.gaogao.sunnyweather.android.logic.model.Weather
import com.gaogao.sunnyweather.android.logic.network.SunnyWeatherNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlin.coroutines.CoroutineContext

/**
 * 仓库层单例类代码
 * 仓库层的主要作用是判断调用方请求的数据应该是从本地数据源获取还是从网络数据源中获取，并将获得的数据返回给调用方
 * 因此，仓库层有点像是一个数据获取与缓存的中间层，在本地没有缓存数据的情况下去调用网络层获取
 */
object Repository {

    private val TAG = "Repository"

    /**
     * 改进了try-catch，searchPlaces是改进后的，refreshWeather未进行改进，可以参考一下节省的代码量与复杂度，多个网络请求时节省的更多
     */

    /*
       为了能将异步获取的数据以响应式编程的方式通知给上一层，通常会返回一个LiveData对象
       这个emit()方法其实类似于调用LiveData的setValue()方法来通知数据变化，只不过这里我们无法直接取得返回的LiveData对象，所以lifecycle-livedata-ktx库提供了这样一个替代方法。
       将liveData()函数的线程参数类型指定成了Dispatchers.IO，这样代码块中的所有代码就都运行在子线程中了
     */
    fun searchPlaces(query: String) = fire(Dispatchers.IO) {
        val placeResponse = SunnyWeatherNetwork.searchPlaces(query)
        if (placeResponse.status == "ok") {
            val places = placeResponse.places
            Log.d(TAG, "${query}是本次查询的地区")
            Result.success(places)
        } else {
            Result.failure(RuntimeException("response status is ${placeResponse.status}"))
        }
    }

    /*
        获取实时天气信息和获取未来天气信息这两个请求是没有先后顺序的，因此让它们并发
     执行可以提升程序的运行效率，但是要在同时得到它们的响应结果后才能进一步执行程序。这
     种需求有没有让你想起什么呢？没错，这不恰好就是我们在第11章学习协程时使用的async函
     数的作用吗？只需要分别在两个async函数中发起网络请求，然后再分别调用它们的await()
     方法，就可以保证只有在两个网络请求都成功响应之后，才会进一步执行程序。另外，由于
     async函数必须在协程作用域内才能调用，所以这里又使用coroutineScope函数创建了一个
     协程作用域。
     */
    fun refreshWeather(lng: String, lat: String) = liveData(Dispatchers.IO) {
        val result = try {
            coroutineScope {
                val deferredRealtime = async {
                    SunnyWeatherNetwork.getRealtimeWeather(lng, lat)
                }
                // 这段代码是为了解决QPS=1的问题
                delay(1000)
                val deferredDaily = async {
                    SunnyWeatherNetwork.getDailyWeather(lng, lat)
                }
                val realtimeResponse = deferredRealtime.await()
                val dailyResponse = deferredDaily.await()
                if (realtimeResponse.status == "ok" && dailyResponse.status == "ok") {
                    val weather = Weather(realtimeResponse.result.realtime,
                                            dailyResponse.result.daily)
                    Result.success(weather)
                } else {
                    Result.failure(
                        RuntimeException(
                            "realtime response status is ${realtimeResponse.status}" +
                            "daily response status is ${dailyResponse.status}"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Result.failure<Weather>(e)
        }
        emit(result)
    }

    /*
            由于我们使用了协程来简化网络回调的写法，导致SunnyWeatherNetwork中封装的每个网络请求接口都可
        能会抛出异常，于是我们必须在仓库层中为每个网络请求都进行try catch处理，这无疑增加了
        仓库层代码实现的复杂度。然而之前我就说过，其实完全可以在某个统一的入口函数中进行封
        装，使得只要进行一次try catch处理就行了

            这是一个按照liveData()函数的参数
         接收标准定义的一个高阶函数。在fire()函数的内部会先调用一下liveData()函数，然后在
         liveData()函数的代码块中统一进行了try catch处理，并在try语句中调用传入的Lambda
         表达式中的代码，最终获取Lambda表达式的执行结果并调用emit()方法发射出去
     */
    private fun <T> fire(context: CoroutineContext, block: suspend () -> Result<T>) =
        liveData<Result<T>>(context) {
            val result = try {
                block()
            } catch (e: Exception) {
                Result.failure<T>(e)
            }
            emit(result)
        }
}