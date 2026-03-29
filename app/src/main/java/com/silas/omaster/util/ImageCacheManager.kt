package com.silas.omaster.util

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import coil.request.ImageRequest
import coil.request.CachePolicy
import com.silas.omaster.model.MasterPreset
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest

/**
 * 图片下载回调接口
 */
interface ImageDownloadCallback {
    fun onStart(url: String)
    fun onProgress(url: String, bytesDownloaded: Long, totalBytes: Long)
    fun onSuccess(url: String, file: File)
    fun onError(url: String, error: Throwable, retryCount: Int)
    fun onRetry(url: String, attempt: Int)
}

/**
 * 下载状态
 */
sealed class DownloadResult {
    data class Success(val file: File) : DownloadResult()
    data class Error(val exception: Throwable, val retryCount: Int) : DownloadResult()
}

/**
 * 图片缓存管理器
 * 管理网络图片的本地缓存，减少对象存储流量费用
 */
object ImageCacheManager {

    private const val CACHE_DIR = "presets/images"
    private const val TIMEOUT_MS = 30000L
    private const val MAX_RETRIES = 3
    private const val TAG = "ImageCacheManager"

    private val client by lazy {
        HttpClient(CIO) {
            install(HttpTimeout) {
                requestTimeoutMillis = TIMEOUT_MS
                connectTimeoutMillis = TIMEOUT_MS
                socketTimeoutMillis = TIMEOUT_MS
            }
        }
    }

    // 记录失败的下载，用于后台重试
    private val failedDownloads = mutableSetOf<String>()

    /**
     * 获取图片的本地缓存路径
     */
    fun getLocalImagePath(context: Context, url: String): File {
        val fileName = generateFileName(url)
        return File(context.filesDir, "$CACHE_DIR/$fileName")
    }

    /**
     * 检查图片是否已缓存到本地
     */
    fun isImageCached(context: Context, url: String): Boolean {
        if (!url.startsWith("http")) return false
        return getLocalImagePath(context, url).exists()
    }

    /**
     * 获取图片的加载路径（优先本地缓存）
     * @return 本地路径（如果存在），否则返回原始URL
     */
    fun getImageLoadPath(context: Context, url: String): String {
        // 非网络图片直接返回
        if (!url.startsWith("http")) {
            return when {
                url.startsWith("/") -> File(url).toUri().toString()
                url.startsWith("presets/") -> File(context.filesDir, url).toUri().toString()
                else -> "file:///android_asset/$url"
            }
        }

        // 检查本地缓存
        val localFile = getLocalImagePath(context, url)
        return if (localFile.exists()) {
            localFile.toUri().toString()
        } else {
            url
        }
    }

    /**
     * 下载并缓存图片（带重试机制）
     * @param maxRetries 最大重试次数，默认3次
     * @param callback 下载回调
     * @return 下载结果
     */
    suspend fun downloadAndCacheImage(
        context: Context,
        url: String,
        maxRetries: Int = MAX_RETRIES,
        callback: ImageDownloadCallback? = null
    ): DownloadResult {
        if (!url.startsWith("http")) {
            return DownloadResult.Error(IllegalArgumentException("非网络URL: $url"), 0)
        }

        val localFile = getLocalImagePath(context, url)

        // 已存在则直接返回
        if (localFile.exists()) {
            failedDownloads.remove(url)  // 从失败列表移除
            return DownloadResult.Success(localFile)
        }

        callback?.onStart(url)

        return withContext(Dispatchers.IO) {
            var lastException: Exception? = null

            repeat(maxRetries) { attempt ->
                try {
                    if (attempt > 0) {
                        callback?.onRetry(url, attempt)
                        Log.d(TAG, "第${attempt + 1}次重试下载: $url")
                        // 指数退避：1s, 2s, 3s
                        delay(1000L * attempt)
                    }

                    // 创建目录
                    localFile.parentFile?.mkdirs()

                    // 下载图片
                    val bytes = client.get(url).bodyAsBytes()

                    // 使用临时文件写入，成功后重命名（原子操作）
                    val tempFile = File(localFile.absolutePath + ".tmp")
                    tempFile.writeBytes(bytes)
                    tempFile.renameTo(localFile)

                    // 下载成功
                    failedDownloads.remove(url)
                    callback?.onSuccess(url, localFile)
                    Log.d(TAG, "下载成功: $url (${bytes.size / 1024}KB)")

                    return@withContext DownloadResult.Success(localFile)

                } catch (e: Exception) {
                    lastException = e
                    Log.w(TAG, "下载失败 (尝试 ${attempt + 1}/$maxRetries): $url - ${e.message}")

                    if (attempt == maxRetries - 1) {
                        // 最终失败
                        failedDownloads.add(url)
                        callback?.onError(url, e, maxRetries)
                        Log.e(TAG, "下载最终失败: $url", e)
                    }
                }
            }

            DownloadResult.Error(lastException ?: Exception("未知错误"), maxRetries)
        }
    }

    /**
     * 简化的下载方法（兼容旧代码）
     */
    suspend fun downloadAndCacheImage(context: Context, url: String): File? {
        return when (val result = downloadAndCacheImage(context, url, MAX_RETRIES, null)) {
            is DownloadResult.Success -> result.file
            is DownloadResult.Error -> null
        }
    }

    /**
     * 预下载预设的所有图片（封面 + 图库）
     */
    suspend fun prefetchPresetImages(
        context: Context,
        preset: MasterPreset,
        callback: ImageDownloadCallback? = null
    ) {
        // 下载封面
        downloadAndCacheImage(context, preset.coverPath, callback = callback)

        // 下载图库图片
        preset.galleryImages?.forEach { url ->
            downloadAndCacheImage(context, url, callback = callback)
        }
    }

    /**
     * 获取失败的下载列表
     */
    fun getFailedDownloads(): Set<String> = failedDownloads.toSet()

    /**
     * 重试所有失败的下载
     */
    suspend fun retryFailedDownloads(
        context: Context,
        callback: ImageDownloadCallback? = null
    ): Int {
        val toRetry = failedDownloads.toList()
        var successCount = 0

        toRetry.forEach { url ->
            val result = downloadAndCacheImage(context, url, callback = callback)
            if (result is DownloadResult.Success) {
                failedDownloads.remove(url)
                successCount++
            }
        }

        Log.d(TAG, "重试完成: $successCount/${toRetry.size} 成功")
        return successCount
    }

    /**
     * 清理过期缓存
     * @param maxAgeDays 缓存最大保留天数
     */
    suspend fun cleanOldCache(context: Context, maxAgeDays: Int = 30) {
        withContext(Dispatchers.IO) {
            val cacheDir = File(context.filesDir, CACHE_DIR)
            if (!cacheDir.exists()) return@withContext

            val maxAge = maxAgeDays * 24 * 60 * 60 * 1000L
            val now = System.currentTimeMillis()
            var cleanedCount = 0

            cacheDir.listFiles()?.forEach { file ->
                if (now - file.lastModified() > maxAge) {
                    file.delete()
                    cleanedCount++
                }
            }

            Log.d(TAG, "清理旧缓存: $cleanedCount 个文件")
        }
    }

    /**
     * 获取缓存大小（MB）
     */
    fun getCacheSize(context: Context): Double {
        val cacheDir = File(context.filesDir, CACHE_DIR)
        if (!cacheDir.exists()) return 0.0

        val size = cacheDir.walkTopDown()
            .filter { it.isFile }
            .map { it.length() }
            .sum()

        return size / (1024.0 * 1024.0)
    }

    /**
     * 清除所有缓存
     */
    fun clearCache(context: Context) {
        File(context.filesDir, CACHE_DIR).deleteRecursively()
        failedDownloads.clear()
        Log.d(TAG, "缓存已清空")
    }

    /**
     * 生成缓存文件名（基于URL路径，忽略域名）
     */
    private fun generateFileName(url: String): String {
        return try {
            // 去掉协议和域名，只保留路径部分
            val path = url.replace(Regex("^https?://[^/]+/"), "")
                .replace("/", "_")
                .take(100)

            if (path.contains(".")) {
                path
            } else {
                "$path.webp"
            }
        } catch (e: Exception) {
            val md5 = MessageDigest.getInstance("MD5")
                .digest(url.toByteArray())
                .joinToString("") { "%02x".format(it) }
                .take(8)
            "$md5.webp"
        }
    }

    /**
     * 创建 Coil ImageRequest
     */
    fun createImageRequest(context: Context, url: String): ImageRequest {
        val loadPath = getImageLoadPath(context, url)

        return ImageRequest.Builder(context)
            .data(loadPath)
            .crossfade(true)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .build()
    }
}
