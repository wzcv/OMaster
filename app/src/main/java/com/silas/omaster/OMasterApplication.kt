package com.silas.omaster

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.silas.omaster.data.config.ConfigCenter
import com.silas.omaster.util.HapticSettings
import com.silas.omaster.util.Logger

class OMasterApplication : Application() {
    companion object {
        private const val PREFS_NAME = "omaster_prefs"
        private const val KEY_USER_AGREED = "user_agreed_to_policy"

        private lateinit var instance: OMasterApplication
        private lateinit var prefs: SharedPreferences

        fun getInstance(): OMasterApplication = instance
        fun getPrefs(): SharedPreferences = prefs
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // 初始化日志系统
        Logger.init(this)

        // 初始化震动设置
        HapticSettings.enabled = ConfigCenter.getInstance(this).isVibrationEnabled
    }

    fun hasUserAgreed(): Boolean {
        return prefs.getBoolean(KEY_USER_AGREED, false)
    }

    fun setUserAgreed(agreed: Boolean) {
        prefs.edit().putBoolean(KEY_USER_AGREED, agreed).apply()
    }
}
