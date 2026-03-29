package com.silas.omaster.util

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import com.silas.omaster.R

/**
 * Iconfont 图标字体工具类
 *
 * 使用阿里 Iconfont 图标库
 * 字体文件: res/font/iconfont.ttf
 */
object IconFont {

    // 8个参数的图标编码
    const val FILTER = "\ue660"      // 滤镜
    const val SOFT_LIGHT = "\ue600"  // 柔光
    const val TONE = "\ue6e4"        // 影调
    const val SATURATION = "\ue748"  // 饱和度
    const val WARM_COOL = "\ue61e"   // 冷暖
    const val CYAN = "\ue601"        // 青品
    const val SHARPNESS = "\ue6bd"   // 锐度
    const val VIGNETTE = "\ue627"    // 暗角

    // 图标列表（按顺序）
    val ICONS = listOf(
        FILTER,
        SOFT_LIGHT,
        TONE,
        SATURATION,
        WARM_COOL,
        CYAN,
        SHARPNESS,
        VIGNETTE
    )

    // 缓存字体，避免重复加载
    private var cachedTypeface: Typeface? = null

    /**
     * 获取 Iconfont 字体
     */
    fun getTypeface(context: Context): Typeface {
        return cachedTypeface ?: try {
            val typeface = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.resources.getFont(R.font.iconfont)
            } else {
                // 兼容旧版本，从 assets 加载
                Typeface.createFromAsset(context.assets, "font/iconfont.ttf")
            }
            cachedTypeface = typeface
            typeface
        } catch (e: Exception) {
            // 如果加载失败，返回默认字体
            android.util.Log.e("IconFont", "Failed to load iconfont", e)
            Typeface.DEFAULT
        }
    }
}
