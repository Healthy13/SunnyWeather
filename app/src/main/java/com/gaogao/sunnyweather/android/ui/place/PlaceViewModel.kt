package com.gaogao.sunnyweather.android.ui.place

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.gaogao.sunnyweather.android.logic.Repository
import com.gaogao.sunnyweather.android.logic.model.Place

class PlaceViewModel : ViewModel() {

    private val searchLiveData = MutableLiveData<String>()

    val placeList = ArrayList<Place>()

    val placeLiveData = searchLiveData.switchMap { query ->
        Repository.searchPlaces(query)
    }

    fun searchPlaces(query: String) {
        searchLiveData.value = query
    }

    /**
     * 由于仓库层中这几个接口的内部没有开启线程，因此也不必借助LiveData对象来观察数据变
     * 化，直接调用仓库层中相应的接口并返回即可
     */
    fun savePlace(place: Place) = Repository.savaPlace(place)

    fun getSavedPlace() = Repository.getSavedPlace()

    fun isPlaceSaved(): Boolean = Repository.isPlaceSaved()
}