package com.silas.omaster.model

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

@Serializable
data class ColorCard(
    val id: String,
    val colors: List<ColorInfo>,
    val themeResId: Int,
    val descriptionResId: Int,
    val tipsResId: Int,
    val challengeResId: Int,
    val sceneTags: List<Int>
)

@Serializable
data class ColorInfo(
    val hex: String,
    val nameResId: Int,
    val role: ColorRole
) {
    fun toColor(): Color {
        return try {
            Color(android.graphics.Color.parseColor("#$hex"))
        } catch (e: IllegalArgumentException) {
            Color.Gray
        }
    }
}

enum class ColorRole {
    PRIMARY,
    ACCENT
}
