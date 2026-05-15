package com.silas.omaster.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object PermissionUtil {

    /**
     * 根据当前 Android 版本返回需要请求的媒体权限
     * Android 13+ (API 33+) 使用细粒度媒体权限 READ_MEDIA_IMAGES
     * Android 12 及以下使用传统存储权限 READ_EXTERNAL_STORAGE
     */
    val mediaPermission: String
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    /**
     * 检查是否已授予媒体权限
     */
    fun hasMediaPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, mediaPermission) ==
                PackageManager.PERMISSION_GRANTED
    }

    /**
     * 判断是否应该显示权限解释理由
     * 如果用户之前拒绝过权限但未勾选"不再询问"，返回 true
     */
    fun shouldShowRationale(context: Context, activity: androidx.activity.ComponentActivity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activity.shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            activity.shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
}
