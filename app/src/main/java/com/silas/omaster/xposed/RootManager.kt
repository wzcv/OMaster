package com.silas.omaster.xposed

import android.util.Log
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/**
 * Root 权限管理器
 * 使用 libsu (topjohnwu) 库执行 Root Shell 命令
 * 单例模式，与项目其他 Manager 保持一致
 */
class RootManager private constructor() {

    enum class RootStatus {
        Unknown,      // 未检测
        Available,    // 有 Root
        Unavailable,  // 无 Root（设备未 Root）
        Denied        // 用户拒绝授权
    }

    enum class MmkvKeyFormat {
        NEW,      // 新版: key 后缀为 LUT 文件名（如 fuji_cc.bin）
        LEGACY,   // 旧版: key 后缀为整数索引（如 _9）
        UNKNOWN   // 无法确定（相机从未进入大师模式）
    }

    private val _rootStatus = MutableStateFlow(RootStatus.Unknown)
    val rootStatus: StateFlow<RootStatus> = _rootStatus.asStateFlow()

    @Volatile
    private var shellConfigured = false

    /**
     * 配置 Shell.Builder（需在首次使用前调用）
     * 设置超时和挂载 master namespace 标志
     */
    private fun ensureShellConfigured() {
        if (shellConfigured) return
        try {
            Shell.enableVerboseLogging = true
            Shell.setDefaultBuilder(
                Shell.Builder.create()
                    .setFlags(Shell.FLAG_MOUNT_MASTER)
                    .setTimeout(15)
            )
            Log.d(TAG, "Shell.Builder 配置完成")
        } catch (e: Exception) {
            // 部分 libsu 版本 API 可能不同，降级处理
            Log.w(TAG, "Shell.Builder 配置异常（降级处理）: ${e.message}")
        }
        shellConfigured = true
    }

    /**
     * 检查 Root 权限
     * 使用标准 libsu API + fallback 命令检测双重策略
     */
    suspend fun checkRoot(): RootStatus = withContext(Dispatchers.IO) {
        ensureShellConfigured()

        try {
            // 方法 1: 标准 libsu API
            val result = Shell.isAppGrantedRoot()
            Log.d(TAG, "Shell.isAppGrantedRoot() = $result")

            when (result) {
                true -> RootStatus.Available.also { _rootStatus.value = it }
                false -> RootStatus.Denied.also { _rootStatus.value = it }
                null -> {
                    // null 表示无法确定，尝试 fallback
                    Log.w(TAG, "标准 root 检测返回 null，尝试 fallback")
                    fallbackRootCheck()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "标准 root 检测异常: ${e.message}", e)
            fallbackRootCheck()
        }
    }

    /**
     * Fallback Root 检测
     * 直接执行 id 命令，检查 uid 是否为 0
     */
    private fun fallbackRootCheck(): RootStatus {
        return try {
            val result = Shell.cmd("id").exec()
            Log.d(TAG, "fallback id 命令: success=${result.isSuccess}, out=${result.out}")

            if (result.isSuccess && result.out.any { "uid=0" in it }) {
                Log.d(TAG, "fallback 检测: 有 Root 权限")
                RootStatus.Available
            } else {
                Log.d(TAG, "fallback 检测: 无 Root 权限")
                RootStatus.Unavailable
            }
        } catch (e: Exception) {
            Log.e(TAG, "fallback root 检测失败: ${e.message}")
            RootStatus.Unavailable
        }.also { _rootStatus.value = it }
    }

    /**
     * 停止相机应用进程
     * 必须在修改 MMKV 前调用，避免内存映射冲突
     */
    suspend fun stopCameraApp(): Boolean = withContext(Dispatchers.IO) {
        Shell.cmd("am force-stop $CAMERA_PACKAGE").exec().isSuccess
    }

    /**
     * 强制杀死相机进程（killall）
     * 用于用户手动重启相机，确保 MMKV 从磁盘重新加载
     */
    suspend fun killCameraApp(): Boolean = withContext(Dispatchers.IO) {
        Shell.cmd("killall $CAMERA_PACKAGE").exec().isSuccess
    }

    /**
     * 读取相机目录中的滤镜映射 JSON
     * @return JSON 字符串，失败返回 null
     */
    suspend fun readFilterMapJson(): String? = withContext(Dispatchers.IO) {
        val result = Shell.cmd("cat $CAMERA_DATA_DIR/files/$FILTER_MAP_FILE").exec()
        if (result.isSuccess) {
            result.out.joinToString("\n")
        } else {
            Log.e(TAG, "读取滤镜映射失败: ${result.err.joinToString()}")
            null
        }
    }

    /**
     * 拷贝指定 MMKV 文件到临时目录（仅拷贝需要修改的文件 + .crc）
     * @param tempDir 临时目录路径（OMaster 缓存目录下）
     * @param targetFile 目标 MMKV 文件名（如 "com.oplus.camera_preferences_0" 或 "mmkv"）
     */
    suspend fun copyMmkvToTemp(tempDir: String, targetFile: String): Boolean = withContext(Dispatchers.IO) {
        // 创建临时目录
        Shell.cmd("mkdir -p '$tempDir'").exec()

        // 拷贝目标文件
        val copyResult = Shell.cmd("cp -a '$CAMERA_MMKV_DIR/$targetFile' '$tempDir/'").exec()
        if (!copyResult.isSuccess) {
            Log.e(TAG, "拷贝 MMKV 文件失败: $targetFile, ${copyResult.err.joinToString()}")
            return@withContext false
        }

        // .crc 校验文件（可能不存在，忽略失败）
        Shell.cmd("cp -a '$CAMERA_MMKV_DIR/$targetFile.crc' '$tempDir/' 2>/dev/null; true").exec()

        // 赋予 OMaster 进程可读写权限
        Shell.cmd("chmod 666 '$tempDir/$targetFile'", "chmod 666 '$tempDir/$targetFile.crc' 2>/dev/null; true").exec()

        Log.d(TAG, "精确拷贝完成: $targetFile → $tempDir")
        true
    }

    /**
     * 备份相机 MMKV 到 OMaster 数据目录（用于恢复）
     * @param backupDir 备份目录路径
     */
    suspend fun backupMmkv(backupDir: String): Boolean = withContext(Dispatchers.IO) {
        val result = Shell.cmd(
            "mkdir -p '$backupDir'",
            "cp -a '$CAMERA_MMKV_DIR/'* '$backupDir/'"
        ).exec()
        if (!result.isSuccess) {
            Log.e(TAG, "备份 MMKV 失败: ${result.err.joinToString()}")
        }
        result.isSuccess
    }

    /**
     * 将修改后的 MMKV 文件写回相机目录（仅写回指定文件）
     * 需恢复正确的文件权限和所有者（SELinux 安全上下文）
     * @param tempDir 临时目录路径（含已修改的 MMKV 文件）
     * @param targetFile 目标 MMKV 文件名
     */
    suspend fun writeMmkvBack(tempDir: String, targetFile: String): Boolean = withContext(Dispatchers.IO) {
        // 获取相机 MMKV 目录的原始 UID:GID
        val uidResult = Shell.cmd("stat -c '%u:%g' '$CAMERA_MMKV_DIR'").exec()
        val ownerInfo = uidResult.out.firstOrNull()?.trim() ?: run {
            Log.e(TAG, "无法获取 MMKV 目录所有者信息")
            return@withContext false
        }

        // 写回目标文件 + .crc，恢复权限
        val result = Shell.cmd(
            "cp -a '$tempDir/$targetFile' '$CAMERA_MMKV_DIR/'",
            "[ -f '$tempDir/$targetFile.crc' ] && cp -a '$tempDir/$targetFile.crc' '$CAMERA_MMKV_DIR/' || true",
            "chown $ownerInfo '$CAMERA_MMKV_DIR/$targetFile'",
            "[ -f '$CAMERA_MMKV_DIR/$targetFile.crc' ] && chown $ownerInfo '$CAMERA_MMKV_DIR/$targetFile.crc' || true",
            "chmod 660 '$CAMERA_MMKV_DIR/$targetFile'",
            "[ -f '$CAMERA_MMKV_DIR/$targetFile.crc' ] && chmod 660 '$CAMERA_MMKV_DIR/$targetFile.crc' || true",
            "chmod 770 '$CAMERA_MMKV_DIR'",
            "restorecon -R '$CAMERA_MMKV_DIR'"
        ).exec()
        if (!result.isSuccess) {
            Log.e(TAG, "写回 MMKV 失败: $targetFile, ${result.err.joinToString()}")
        }
        Log.d(TAG, "精确写回完成: $targetFile")
        result.isSuccess
    }

    /**
     * 从备份恢复相机 MMKV（安全恢复机制）
     * @param backupDir 备份目录路径
     */
    suspend fun restoreMmkvFromBackup(backupDir: String): Boolean = withContext(Dispatchers.IO) {
        stopCameraApp()
        kotlinx.coroutines.delay(500)

        val uidResult = Shell.cmd("stat -c '%u:%g' '$CAMERA_MMKV_DIR'").exec()
        val ownerInfo = uidResult.out.firstOrNull()?.trim() ?: return@withContext false

        val result = Shell.cmd(
            "cp -a '$backupDir/'* '$CAMERA_MMKV_DIR/'",
            "chown -R $ownerInfo '$CAMERA_MMKV_DIR/'*",
            "chmod 660 '$CAMERA_MMKV_DIR/'*",
            "chmod 770 '$CAMERA_MMKV_DIR'",
            "restorecon -R '$CAMERA_MMKV_DIR'"
        ).exec()
        result.isSuccess
    }

    /**
     * 检测相机 MMKV 的 key 格式版本
     *
     * 检测特征：
     * - 新版: mmkv 文件中存在 key_master_mode_effect_xxx_{name}.bin 格式
     * - 旧版: mmkv 文件中存在 key_master_mode_effect_xxx_\d+{0x01|0x02} 格式，
     *         或 com.oplus.camera_preferences_0 中存在无后缀的 effect key
     *
     * @return MmkvKeyFormat.NEW / LEGACY / UNKNOWN
     */
    suspend fun detectMmkvKeyFormat(): MmkvKeyFormat = withContext(Dispatchers.IO) {
        // 读 mmkv 文件前 16KB 足够检测特征
        val mmkvHead = Shell.cmd(
            "dd if='$CAMERA_MMKV_DIR/mmkv' bs=1 count=16384 2>/dev/null | cat"
        ).exec()

        if (mmkvHead.isSuccess && mmkvHead.out.isNotEmpty()) {
            val raw = mmkvHead.out.joinToString("\n")
            // 新版特征: key 后缀含 .bin / .cube.rgb.bin
            if (raw.contains(".bin") && raw.contains("key_master_mode_effect")) {
                return@withContext MmkvKeyFormat.NEW
            }
        }

        val newCheck = Shell.cmd(
            "grep -ac 'key_master_mode_effect.*\\.bin' '$CAMERA_MMKV_DIR/mmkv' 2>/dev/null || echo 0"
        ).exec()
        val newCount = newCheck.out.firstOrNull()?.trim()?.toIntOrNull() ?: 0
        if (newCount > 0) return@withContext MmkvKeyFormat.NEW

        val legacyCheck = Shell.cmd(
            "grep -ac 'key_master_mode_effect.*_[0-9]' '$CAMERA_MMKV_DIR/mmkv' " +
            "'$CAMERA_MMKV_DIR/com.oplus.camera_preferences_0' 2>/dev/null || echo 0"
        ).exec()
        val legacyCount = legacyCheck.out.firstOrNull()?.trim()?.toIntOrNull() ?: 0
        if (legacyCount > 0) return@withContext MmkvKeyFormat.LEGACY

        // 兜底: preferences_0 存在无后缀 effect key 也是旧版
        val p0Check = Shell.cmd(
            "grep -ac 'key_master_mode_effect_saturation' '$CAMERA_MMKV_DIR/com.oplus.camera_preferences_0' 2>/dev/null || echo 0"
        ).exec()
        val p0Count = p0Check.out.firstOrNull()?.trim()?.toIntOrNull() ?: 0
        if (p0Count > 0) return@withContext MmkvKeyFormat.LEGACY

        MmkvKeyFormat.UNKNOWN
    }

    /**
     * 清理临时文件
     */
    suspend fun cleanupTempDir(tempDir: String) = withContext(Dispatchers.IO) {
        Shell.cmd("rm -rf '$tempDir'").exec()
    }

    companion object {
        private const val TAG = "OMaster-RootManager"
        private const val CAMERA_PACKAGE = "com.oplus.camera"
        private const val CAMERA_DATA_DIR = "/data/data/$CAMERA_PACKAGE"
        private const val CAMERA_MMKV_DIR = "$CAMERA_DATA_DIR/files/mmkv"
        private const val FILTER_MAP_FILE = "omaster_filter_map.json"

        @Volatile
        private var instance: RootManager? = null

        fun getInstance(): RootManager {
            return instance ?: synchronized(this) {
                instance ?: RootManager().also { instance = it }
            }
        }
    }
}
