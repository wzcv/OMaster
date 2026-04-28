package com.silas.omaster.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.CachePolicy
import com.silas.omaster.R
import com.silas.omaster.model.MasterPreset
import com.silas.omaster.ui.animation.AnimationSpecs
import com.silas.omaster.util.DownloadResult
import com.silas.omaster.util.ImageCacheManager
import com.silas.omaster.util.ImageDownloadCallback
import com.silas.omaster.ui.theme.AppDesign
import com.silas.omaster.ui.theme.DarkGray
import com.silas.omaster.ui.theme.PureBlack
import com.silas.omaster.ui.theme.themedBackground
import com.silas.omaster.ui.theme.themedTextPrimary
import com.silas.omaster.ui.theme.themedTextSecondary
import com.silas.omaster.ui.theme.themedCardBackground
import com.silas.omaster.ui.theme.themedBorderLight
import java.io.File

/**
 * 通用顶部导航栏组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OMasterTopAppBar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = themedTextPrimary()
                )
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        navigationIcon = {
            onBack?.let {
                IconButton(onClick = it) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = themedTextPrimary()
                    )
                }
            } ?: Box {}
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = themedBackground(),
            titleContentColor = themedTextPrimary()
        ),
        modifier = modifier
    )
}

/**
 * 功能特性卡片组件
 */
@Composable
fun FeatureCard(
    icon: @Composable () -> Unit,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = AppDesign.CardShape,
        colors = CardDefaults.cardColors(
            containerColor = themedCardBackground()
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDesign.ContentPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(AppDesign.IconButtonSize),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }

            Spacer(modifier = Modifier.width(AppDesign.ItemSpacing * 1.5f))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = themedTextPrimary()
                )

                Spacer(modifier = Modifier.height(AppDesign.ItemSpacing / 2))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = themedTextSecondary().copy(alpha = AppDesign.SecondaryAlpha)
                )
            }
        }
    }
}

/**
 * 功能特性卡片组件（带图标资源ID）
 */
@Composable
fun FeatureCard(
    iconResId: Int,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    FeatureCard(
        icon = {
            Icon(
                imageVector = when (iconResId) {
                    0 -> Icons.Default.CameraAlt
                    1 -> Icons.Default.Palette
                    2 -> Icons.Default.Groups
                    else -> Icons.Default.Lightbulb
                },
                contentDescription = null,
                tint = when (iconResId) {
                    0 -> Color(0xFF4CAF50)  // 相机 - 绿色
                    1 -> Color(0xFF2196F3)  // 调色板 - 蓝色
                    2 -> Color(0xFFFF9800)  // 人群 - 橙色
                    else -> MaterialTheme.colorScheme.primary
                },
                modifier = Modifier.size(28.dp)
            )
        },
        title = title,
        description = description,
        modifier = modifier
    )
}

/**
 * 参数显示项组件
 */
@Composable
fun ParameterItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = themedTextSecondary().copy(alpha = AppDesign.TertiaryAlpha)
        )
        Spacer(modifier = Modifier.height(AppDesign.ItemSpacing / 2))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = themedTextPrimary()
        )
    }
}

/**
 * 通用卡片组件
 */
@Composable
fun OMasterCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = themedCardBackground()
        ),
        content = content
    )
}

/**
 * 垂直间距组件
 */
@Composable
fun VerticalSpacer(height: Dp) = Spacer(modifier = Modifier.height(height))

/**
 * 水平间距组件
 */
@Composable
fun HorizontalSpacer(width: Dp) = Spacer(modifier = Modifier.width(width))

/**
 * 模式标签组件
 * 支持显示多个标签
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ModeBadge(
    tags: List<String>?,
    modifier: Modifier = Modifier
) {
    if (tags.isNullOrEmpty()) return

    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tags.forEach { tag ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(themedCardBackground())
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = tag,
                    style = MaterialTheme.typography.labelMedium,
                    color = themedTextPrimary(),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * 预设图片组件
 * 支持 assets、内部存储和网络图片（带本地缓存）
 * 优化：使用更短的 crossfade 动画时长，优先加载本地缓存，带下载状态
 */
@Composable
fun PresetImage(
    preset: MasterPreset,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    showDownloadIndicator: Boolean = true
) {
    val context = LocalContext.current
    var downloadState by remember { mutableStateOf<DownloadState>(DownloadState.Idle) }

    // 使用 ImageCacheManager 获取加载路径（优先本地缓存）
    val imageUri = ImageCacheManager.getImageLoadPath(context, preset.coverPath)

    // 如果是网络图片且未缓存，后台下载
    LaunchedEffect(preset.coverPath) {
        if (preset.coverPath.startsWith("http") &&
            !ImageCacheManager.isImageCached(context, preset.coverPath)) {

            downloadState = DownloadState.Downloading

            val result = ImageCacheManager.downloadAndCacheImage(
                context, preset.coverPath,
                callback = object : ImageDownloadCallback {
                    override fun onStart(url: String) {}
                    override fun onProgress(url: String, bytesDownloaded: Long, totalBytes: Long) {}
                    override fun onSuccess(url: String, file: File) {
                        downloadState = DownloadState.Success
                    }
                    override fun onError(url: String, error: Throwable, retryCount: Int) {
                        downloadState = DownloadState.Error(error.message ?: "下载失败")
                    }
                    override fun onRetry(url: String, attempt: Int) {}
                }
            )

            downloadState = when (result) {
                is DownloadResult.Success -> DownloadState.Success
                is DownloadResult.Error -> DownloadState.Error("下载失败")
            }
        }
    }

    Box(modifier = modifier) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageUri)
                .crossfade(AnimationSpecs.FastTween.durationMillis)
                .diskCachePolicy(CachePolicy.ENABLED)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .build(),
            contentDescription = preset.name,
            contentScale = contentScale,
            modifier = Modifier.fillMaxSize()
        )

        // 显示加载状态
        if (showDownloadIndicator && downloadState is DownloadState.Downloading) {
            LoadingIndicator()
        }
    }
}

/**
 * 简单加载指示器 - Material 3 风格
 */
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(32.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 3.dp
        )
    }
}

/**
 * 下载状态
 */
private sealed class DownloadState {
    object Idle : DownloadState()
    object Downloading : DownloadState()
    object Success : DownloadState()
    data class Error(val message: String) : DownloadState()
}

/**
 * 章节标题组件
 */
@Composable
fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = modifier
    )
}

/**
 * 带卡片的参数项组件
 */
@Composable
fun ParameterCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = themedCardBackground()
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 0.5.dp,
            color = themedTextPrimary().copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = themedTextSecondary().copy(alpha = AppDesign.TertiaryAlpha)
            )
            Spacer(modifier = Modifier.height(AppDesign.ItemSpacing / 2))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = themedTextPrimary()
            )
        }
    }
}

/**
 * 拍摄建议卡片组件
 */
@Composable
fun ShootingTipsCard(
    tips: String,
    modifier: Modifier = Modifier
) {
    DescriptionCard(
        title = stringResource(R.string.shooting_tips),
        content = tips,
        modifier = modifier
    )
}

/**
 * 通用描述卡片组件（支持折叠）
 */
@Composable
fun DescriptionCard(
    title: String,
    content: String,
    modifier: Modifier = Modifier,
    collapsedByDefault: Boolean = true
) {
    var isExpanded by remember { mutableStateOf(!collapsedByDefault) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = themedCardBackground().copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "收起" else "展开",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))

                    content.split("\n").forEach { line ->
                        if (line.isNotBlank()) {
                            Text(
                                text = line.trim(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = themedTextPrimary().copy(alpha = 0.9f),
                                lineHeight = 22.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}
