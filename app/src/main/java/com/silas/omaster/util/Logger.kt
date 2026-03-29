package com.silas.omaster.util

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 统一日志管理器
 * 同时输出到 Logcat 和本地文件，支持日志轮转
 */
object Logger {

    private const val TAG = "OMaster"
    private const val LOG_FILE_NAME = "app.log"
    private const val MAX_LOG_SIZE = 5 * 1024 * 1024L // 5MB
    private const val MAX_BACKUP_COUNT = 3

    private var logDir: File? = null
    private var logFile: File? = null
    private var isInitialized = false

    /**
     * 初始化日志系统
     * 在 Application.onCreate() 中调用
     */
    fun init(context: Context) {
        if (isInitialized) return

        logDir = File(context.filesDir, "logs").apply {
            if (!exists()) mkdirs()
        }
        logFile = File(logDir, LOG_FILE_NAME)
        isInitialized = true

        // 记录启动信息
        i(TAG, "=".repeat(50))
        i(TAG, "App started")
        i(TAG, "Version: ${VersionInfo.VERSION_NAME}")
        i(TAG, "Android SDK: ${android.os.Build.VERSION.SDK_INT}")
        i(TAG, "Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
        i(TAG, "=".repeat(50))
    }

    /**
     * Debug 级别日志
     */
    fun d(tag: String, message: String) {
        log(LogLevel.DEBUG, tag, message, null)
    }

    /**
     * Info 级别日志
     */
    fun i(tag: String, message: String) {
        log(LogLevel.INFO, tag, message, null)
    }

    /**
     * Warning 级别日志
     */
    fun w(tag: String, message: String) {
        log(LogLevel.WARNING, tag, message, null)
    }

    /**
     * Error 级别日志
     */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        log(LogLevel.ERROR, tag, message, throwable)
    }

    /**
     * 核心日志方法
     */
    private fun log(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
        // 1. 输出到 Logcat
        when (level) {
            LogLevel.DEBUG -> Log.d(tag, message, throwable)
            LogLevel.INFO -> Log.i(tag, message, throwable)
            LogLevel.WARNING -> Log.w(tag, message, throwable)
            LogLevel.ERROR -> Log.e(tag, message, throwable)
        }

        // 2. 写入本地文件
        if (isInitialized) {
            writeToFile(level, tag, message, throwable)
        }
    }

    /**
     * 写入日志文件
     */
    private fun writeToFile(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
        try {
            // 检查并轮转日志
            rotateLogIfNeeded()

            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
                .format(Date())

            val logLine = buildString {
                append("[$timestamp] [${level.name}] $tag: $message")
                throwable?.let {
                    append("\n")
                    append(Log.getStackTraceString(it))
                }
                append("\n")
            }

            FileWriter(logFile, true).use { writer ->
                writer.write(logLine)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to write log to file", e)
        }
    }

    /**
     * 检查日志大小并轮转
     */
    private fun rotateLogIfNeeded() {
        val file = logFile ?: return
        if (!file.exists()) return

        if (file.length() > MAX_LOG_SIZE) {
            // 删除最旧的备份
            val oldestBackup = File(logDir, "$LOG_FILE_NAME.$MAX_BACKUP_COUNT")
            if (oldestBackup.exists()) {
                oldestBackup.delete()
            }

            // 重命名其他备份
            for (i in MAX_BACKUP_COUNT - 1 downTo 1) {
                val oldFile = File(logDir, "$LOG_FILE_NAME.$i")
                val newFile = File(logDir, "$LOG_FILE_NAME.${i + 1}")
                if (oldFile.exists()) {
                    oldFile.renameTo(newFile)
                }
            }

            // 重命名当前日志
            file.renameTo(File(logDir, "$LOG_FILE_NAME.1"))
        }
    }

    /**
     * 获取日志文件
     */
    fun getLogFile(): File? = logFile

    /**
     * 获取所有日志文件（包括备份）
     */
    fun getAllLogFiles(): List<File> {
        val files = mutableListOf<File>()
        logFile?.let { if (it.exists()) files.add(it) }

        for (i in 1..MAX_BACKUP_COUNT) {
            val backup = File(logDir, "$LOG_FILE_NAME.$i")
            if (backup.exists()) files.add(backup)
        }

        return files
    }

    /**
     * 获取日志总大小（字节）
     */
    fun getTotalLogSize(): Long {
        return getAllLogFiles().sumOf { it.length() }
    }

    /**
     * 清空所有日志
     */
    fun clearLogs() {
        getAllLogFiles().forEach { it.delete() }
        i(TAG, "Logs cleared")
    }

    /**
     * 日志级别枚举
     */
    private enum class LogLevel {
        DEBUG, INFO, WARNING, ERROR
    }
}
