package com.silas.omaster.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * 日志导出工具
 * 支持导出和分享日志文件
 */
object LogExporter {

    private const val AUTHORITY = "com.silas.omaster.fileprovider"

    /**
     * 导出日志并通过系统分享
     * @param context 上下文
     * @param onComplete 导出完成回调（成功/失败）
     */
    fun exportAndShare(context: Context, onComplete: ((Boolean) -> Unit)? = null) {
        try {
            val logFiles = Logger.getAllLogFiles()
            if (logFiles.isEmpty()) {
                Toast.makeText(context, "暂无日志文件", Toast.LENGTH_SHORT).show()
                onComplete?.invoke(false)
                return
            }

            // 创建临时导出文件
            val exportDir = File(context.cacheDir, "logs_export").apply {
                if (!exists()) mkdirs()
            }
            val exportFile = File(exportDir, "omaster_logs.zip")

            // 打包日志文件
            zipFiles(logFiles, exportFile)

            // 获取 FileProvider URI
            val uri = FileProvider.getUriForFile(context, AUTHORITY, exportFile)

            // 创建分享 Intent
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/zip"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "OMaster 日志文件")
                putExtra(Intent.EXTRA_TEXT, "请查看附件中的日志文件，用于分析问题。")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            // 显示分享选择器
            val chooser = Intent.createChooser(shareIntent, "分享日志到")
            context.startActivity(chooser)

            onComplete?.invoke(true)
        } catch (e: Exception) {
            Logger.e("LogExporter", "Failed to export logs", e)
            Toast.makeText(context, "导出日志失败: ${e.message}", Toast.LENGTH_SHORT).show()
            onComplete?.invoke(false)
        }
    }

    /**
     * 获取格式化的日志大小
     */
    fun getFormattedLogSize(): String {
        val size = Logger.getTotalLogSize()
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "%.2f KB".format(size / 1024.0)
            else -> "%.2f MB".format(size / (1024.0 * 1024.0))
        }
    }

    /**
     * 压缩多个文件为 ZIP
     */
    private fun zipFiles(files: List<File>, outputFile: File) {
        ZipOutputStream(FileOutputStream(outputFile)).use { zos ->
            files.forEach { file ->
                if (file.exists()) {
                    FileInputStream(file).use { fis ->
                        val entry = ZipEntry(file.name)
                        zos.putNextEntry(entry)
                        fis.copyTo(zos)
                        zos.closeEntry()
                    }
                }
            }
        }
    }

    /**
     * 清理导出的临时文件
     */
    fun cleanExportCache(context: Context) {
        try {
            val exportDir = File(context.cacheDir, "logs_export")
            exportDir.listFiles()?.forEach { it.delete() }
        } catch (e: IOException) {
            Logger.e("LogExporter", "Failed to clean export cache", e)
        }
    }
}
