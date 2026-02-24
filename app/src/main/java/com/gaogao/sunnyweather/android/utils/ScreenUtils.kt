package com.gaogao.sunnyweather.android.utils

import android.os.Build
import android.view.Window
import android.view.WindowInsetsController
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

object ScreenUtils {
    /**
     * AndroidX 沉浸式状态栏工具类
     * @param window Activity的Window
     * @param isDarkText 是否设置状态栏文字为深色（true=黑字，false=白字）
     */
    fun setImmersiveStatusBar(window: Window, isDarkText: Boolean = true) {
        // 1. 让页面内容延伸到状态栏（AndroidX 推荐方式）
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 2. 设置状态栏透明
        window.statusBarColor = android.graphics.Color.TRANSPARENT

        // 3. 适配状态栏文字颜色（区分 Android 11+ 和低版本）
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ 用新API
            insetsController.isAppearanceLightStatusBars = isDarkText
        } else {
            // Android 6.0 - 10
            @Suppress("DEPRECATION")
            insetsController.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_BARS_BY_TOUCH
            @Suppress("DEPRECATION")
            insetsController.isAppearanceLightStatusBars = isDarkText
        }

        // 可选：适配导航栏（如需沉浸式导航栏）
        // window.navigationBarColor = android.graphics.Color.TRANSPARENT
    }
}