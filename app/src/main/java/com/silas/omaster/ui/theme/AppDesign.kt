package com.silas.omaster.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object AppDesign {
    // 圆角
    val SmallRadius = 8.dp
    val MediumRadius = 12.dp
    val LargeRadius = 18.dp
    val FullRadius = 20.dp

    val PillShape = RoundedCornerShape(LargeRadius)
    val CardShape = RoundedCornerShape(MediumRadius)
    val ButtonShape = RoundedCornerShape(FullRadius)

    // 间距
    val ScreenPadding = 24.dp
    val ContentPadding = 16.dp
    val ItemSpacing = 8.dp
    val SectionSpacing = 16.dp
    val TabBarPadding = 16.dp

    // 尺寸
    val TabBarHeight = 36.dp
    val IconButtonSize = 40.dp
    val FABSize = 56.dp

    // 字体大小
    val TabTextSize = 14.sp
    val BodySmallSize = 12.sp
    val BodyMediumSize = 14.sp
    val TitleMediumSize = 16.sp

    // 文字透明度
    const val PrimaryAlpha = 1.0f
    const val SecondaryAlpha = 0.7f
    const val TertiaryAlpha = 0.6f
    const val DisabledAlpha = 0.3f
}
