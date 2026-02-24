package com.gaogao.sunnyweather.android.ui.weather

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.gaogao.sunnyweather.R
import com.gaogao.sunnyweather.android.logic.model.Weather
import com.gaogao.sunnyweather.android.logic.model.getSky
import com.gaogao.sunnyweather.android.utils.ScreenUtils
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.Locale

class WeatherActivity : AppCompatActivity() {

    val viewModel by lazy { ViewModelProvider(this).get(WeatherViewModel::class.java) }

    public lateinit var drawerLayout: DrawerLayout
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var weatherLayout: ScrollView
    private lateinit var nowLayout: RelativeLayout
    private lateinit var forecastLayout: LinearLayout
    private lateinit var lifeIndexLayout: MaterialCardView

    private lateinit var navBtn: Button
    private lateinit var placeName: TextView

    private lateinit var currentTemp: TextView

    private lateinit var currentSky: TextView
    private lateinit var currentAQI: TextView
    private lateinit var coldRiskText: TextView
    private lateinit var dressingText: TextView
    private lateinit var ultravioletText: TextView
    private lateinit var carWashingText: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 使用binding进行绑定布局，更好的处理include进来的布局
        setContentView(R.layout.activity_weather)
        // 调用工具类来实现
        ScreenUtils.setImmersiveStatusBar(window, isDarkText = true)
        drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        swipeRefresh = findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
        weatherLayout = findViewById<ScrollView>(R.id.weatherLayout)
        initViews()
        if (viewModel.locationLng.isEmpty()) {
            viewModel.locationLng = intent.getStringExtra("location_lng") ?: ""
        }
        if (viewModel.locationLat.isEmpty()) {
            viewModel.locationLat = intent.getStringExtra("location_lat") ?: ""
        }
        if (viewModel.placeName.isEmpty()) {
            viewModel.placeName = intent.getStringExtra("place_name") ?: ""
        }
        viewModel.weatherLiveData.observe(this, Observer { result ->
            val weather = result.getOrNull()
            if (weather != null) {
                showWeatherInfo(weather)
            } else {
                Toast.makeText(this, "无法成功获取天气信息", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
            swipeRefresh.isRefreshing = false
        })
        swipeRefresh.setColorSchemeResources(com.google.android.material.R.color.design_default_color_primary)
        refreshWeather()
        swipeRefresh.setOnRefreshListener {
            refreshWeather()
        }
        viewModel.refreshWeather(viewModel.locationLng, viewModel.locationLat)
        // 第一，在切换城市按钮的点击事件中调用DrawerLayout的openDrawer()方法来打开滑动菜单
        navBtn.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        // 第二，监听DrawerLayout的状态，当滑动菜单被隐藏的时候，同时也要隐藏输入法
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
            }

            override fun onDrawerOpened(drawerView: View) {
            }

            override fun onDrawerClosed(drawerView: View) {
                val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                manager.hideSoftInputFromWindow(drawerView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }

            override fun onDrawerStateChanged(newState: Int) {
            }
        })
    }

    private fun initViews() {
        // 全局变量binding可以直接访问控件
        nowLayout = weatherLayout.findViewById<RelativeLayout>(R.id.nowLayout)
        forecastLayout = weatherLayout.findViewById<LinearLayout>(R.id.forecastLayout)
        lifeIndexLayout = weatherLayout.findViewById<MaterialCardView>(R.id.lifeIndexLayout)
        navBtn = weatherLayout.findViewById<Button>(R.id.navBtn)
        placeName = weatherLayout.findViewById<TextView>(R.id.placeName)
        currentTemp = weatherLayout.findViewById<TextView>(R.id.currentTemp)
        currentSky = weatherLayout.findViewById<TextView>(R.id.currentSky)
        currentAQI = weatherLayout.findViewById<TextView>(R.id.currentAQI)
        coldRiskText = weatherLayout.findViewById<TextView>(R.id.coldRiskText)
        dressingText = weatherLayout.findViewById<TextView>(R.id.dressingText)
        ultravioletText = weatherLayout.findViewById<TextView>(R.id.ultravioletText)
        carWashingText = weatherLayout.findViewById<TextView>(R.id.carWashingText)
    }

    private fun showWeatherInfo(weather: Weather) {
        placeName.text = viewModel.placeName
        val realtime = weather.realtime
        val daily = weather.daily
        // 填充now.xml布局的数据
        val currentTempText = "${realtime.temperature.toInt()} ℃"
        currentTemp.text = currentTempText
        currentSky.text = getSky(realtime.skycon).info
        val currentPM25Text = "空气指数 ${realtime.airQuality.aqi.chn.toInt()}"
        currentAQI.text = currentPM25Text
        nowLayout.setBackgroundResource(getSky(realtime.skycon).bg)
        // 填充forecast.xml布局的数据
        forecastLayout.removeAllViews()
        val days = daily.skycon.size
        for (i in 0 until days) {
            val skycon = daily.skycon[i]
            val temperature = daily.temperature[i]
            val view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false)
            val dateInfo = view.findViewById<TextView>(R.id.dateInfo)
            val skyIcon = view.findViewById<ImageView>(R.id.skyIcon)
            val skyInfo = view.findViewById<TextView>(R.id.skyInfo)
            val temperatureInfo = view.findViewById<TextView>(R.id.temperatureInfo)
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateInfo.text = simpleDateFormat.format(skycon.date)
            val sky = getSky(skycon.value)
            skyIcon.setImageResource(sky.icon)
            skyInfo.text = sky.info
            val tempText = "${temperature.min.toInt()} ~ ${temperature.max.toInt()} ℃"
            temperatureInfo.text = tempText
            forecastLayout.addView(view)
        }
        // 填充life_index.xml布局中的数据
        val lifeIndex = daily.lifeIndex
        coldRiskText.text = lifeIndex.coldRisk[0].desc
        dressingText.text = lifeIndex.dressing[0].desc
        ultravioletText.text = lifeIndex.ultraviolet[0].desc
        carWashingText.text = lifeIndex.carWashing[0].desc
        weatherLayout.visibility = View.VISIBLE
    }

    fun refreshWeather() {
        viewModel.refreshWeather(viewModel.locationLng, viewModel.locationLat)
        swipeRefresh.isRefreshing = true
    }
}