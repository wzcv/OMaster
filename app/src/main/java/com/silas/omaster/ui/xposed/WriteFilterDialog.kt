package com.silas.omaster.ui.xposed

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.silas.omaster.R
import com.silas.omaster.model.MasterPreset
import com.silas.omaster.ui.theme.DarkGray
import com.silas.omaster.ui.theme.NearBlack
import com.silas.omaster.xposed.FilterMapManager
import com.silas.omaster.xposed.MmkvParamWriter
import com.silas.omaster.xposed.PresetToMmkvMapper
import com.silas.omaster.xposed.RootManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

private const val TAG = "OMaster-WriteDialog"

private fun buildPreview(
    preset: com.silas.omaster.model.MasterPreset,
    entry: FilterMapManager.FilterEntry,
    format: RootManager.MmkvKeyFormat
): List<Pair<String, String>> {
    return if (format == RootManager.MmkvKeyFormat.LEGACY) {
        PresetToMmkvMapper.getParamsPreview(preset, entry.lutFile).toMutableList().also { list ->
            list[0] = "目标滤镜" to "[${entry.index}] ${entry.name}"
        }
    } else {
        PresetToMmkvMapper.getParamsPreview(preset, entry.lutFile)
    }
}

/**
 * 写入状态枚举
 */
private enum class WriteStatus {
    Idle, StoppingCamera, Backing, CopyingFiles, WritingParams, RestoringFiles, Success, Error;

    val isWriting: Boolean
        get() = this != Idle && this != Success && this != Error
}

/**
 * 写入滤镜参数对话框
 * 从详情页调用，接收当前查看的 MasterPreset
 * 功能: 环境检测 → 滤镜槽位选择 → 参数预览 → 一键写入
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteFilterDialog(
    preset: MasterPreset,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 管理器实例
    val rootManager = remember { RootManager.getInstance() }
    val filterMapManager = remember { FilterMapManager.getInstance(context) }
    val mmkvWriter = remember { MmkvParamWriter(context) }

    // 环境状态
    var rootStatus by remember { mutableStateOf(RootManager.RootStatus.Unknown) }
    var mmkvFormat by remember { mutableStateOf(RootManager.MmkvKeyFormat.UNKNOWN) }
    var filterEntries by remember { mutableStateOf<List<FilterMapManager.FilterEntry>>(emptyList()) }
    var isEnvReady by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    // 选择状态（新版用 lutFile，旧版用 filterIndex）
    var selectedEntry by remember { mutableStateOf<FilterMapManager.FilterEntry?>(null) }
    var paramsPreview by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var filterExpanded by remember { mutableStateOf(false) }

    // 写入状态
    var writeStatus by remember { mutableStateOf(WriteStatus.Idle) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // 打开对话框时自动检测环境
    LaunchedEffect(Unit) {
        rootStatus = rootManager.checkRoot()
        if (rootStatus == RootManager.RootStatus.Available) {
            mmkvFormat = rootManager.detectMmkvKeyFormat()
        }
        filterMapManager.loadFilterMap()
        filterEntries = filterMapManager.filterMap.value
        isEnvReady = rootStatus == RootManager.RootStatus.Available && filterEntries.isNotEmpty()
        isLoading = false

        if (isEnvReady) {
            val filterStr = PresetToMmkvMapper.extractFilterString(preset)
            filterStr?.let { str ->
                filterMapManager.getFilterByName(str)?.let { entry ->
                    selectedEntry = entry
                    paramsPreview = buildPreview(preset, entry, mmkvFormat)
                }
            }
        }
    }

    fun onSelectFilter(entry: FilterMapManager.FilterEntry) {
        selectedEntry = entry
        paramsPreview = buildPreview(preset, entry, mmkvFormat)
    }

    fun doWrite() {
        val entry = selectedEntry ?: return
        scope.launch {
            try {
                errorMessage = null

                writeStatus = WriteStatus.StoppingCamera
                if (!rootManager.stopCameraApp()) {
                    errorMessage = "无法停止相机应用"
                    writeStatus = WriteStatus.Error
                    return@launch
                }
                delay(500)

                writeStatus = WriteStatus.Backing
                val backupDir = "${context.filesDir.absolutePath}/mmkv_backup"
                rootManager.backupMmkv(backupDir)

                // 根据格式选择 key 构建方式和目标文件
                val writeParams = if (mmkvFormat == RootManager.MmkvKeyFormat.LEGACY) {
                    PresetToMmkvMapper.mapPresetToMmkvParamsLegacy(preset, entry.index)
                } else {
                    // NEW 或 UNKNOWN 均用新版格式（安全兜底）
                    PresetToMmkvMapper.mapPresetToMmkvParamsNew(preset, entry.lutFile)
                }

                writeStatus = WriteStatus.CopyingFiles
                val tempDir = "${context.cacheDir.absolutePath}/mmkv_temp"
                File(tempDir).deleteRecursively()
                if (!rootManager.copyMmkvToTemp(tempDir, writeParams.targetFile)) {
                    errorMessage = "无法拷贝 MMKV 文件: ${writeParams.targetFile}"
                    writeStatus = WriteStatus.Error
                    return@launch
                }

                writeStatus = WriteStatus.WritingParams
                Log.d(TAG, "写入[$mmkvFormat]: file=${writeParams.targetFile}, lutFile=${entry.lutFile}, index=${entry.index}, params=${writeParams.params.keys}")
                if (!mmkvWriter.writeParams(tempDir, writeParams.targetFile, writeParams.params)) {
                    errorMessage = "MMKV 参数写入失败"
                    writeStatus = WriteStatus.Error
                    return@launch
                }

                writeStatus = WriteStatus.RestoringFiles
                if (!rootManager.writeMmkvBack(tempDir, writeParams.targetFile)) {
                    errorMessage = "无法写回相机目录"
                    writeStatus = WriteStatus.Error
                    return@launch
                }

                rootManager.cleanupTempDir(tempDir)
                writeStatus = WriteStatus.Success
                Log.d(TAG, "写入完成: 预设[${preset.name}] → lutFile=${entry.lutFile}, 参数数=${writeParams.params.size}")
            } catch (e: Exception) {
                Log.e(TAG, "写入流程异常", e)
                errorMessage = "写入失败: ${e.message}"
                writeStatus = WriteStatus.Error
            }
        }
    }

    // 从备份恢复
    fun doRestore() {
        scope.launch {
            val backupDir = "${context.filesDir.absolutePath}/mmkv_backup"
            if (File(backupDir).exists()) {
                if (rootManager.restoreMmkvFromBackup(backupDir)) {
                    errorMessage = null
                    writeStatus = WriteStatus.Idle
                } else {
                    errorMessage = "恢复失败"
                }
            } else {
                errorMessage = "未找到备份数据"
            }
        }
    }

    val selectedFilter = selectedEntry

    Dialog(onDismissRequest = { if (!writeStatus.isWriting) onDismiss() }) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = NearBlack),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // === 标题栏 ===
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.xposed_write_dialog_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = { if (!writeStatus.isWriting) onDismiss() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.Gray)
                    }
                }

                // 当前预设
                Text(
                    text = stringResource(R.string.xposed_current_preset, preset.name),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                // === 加载中 ===
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    return@Column
                }

                // === 环境状态 ===
                CompactStatusRow(
                    label = "格式",
                    isOk = mmkvFormat != RootManager.MmkvKeyFormat.UNKNOWN,
                    detail = when (mmkvFormat) {
                        RootManager.MmkvKeyFormat.NEW -> "新版 (LUT文件名)"
                        RootManager.MmkvKeyFormat.LEGACY -> "旧版 (整数索引)"
                        RootManager.MmkvKeyFormat.UNKNOWN -> "未知(请先进入大师模式)"
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                CompactStatusRow(
                    label = "Root",
                    isOk = rootStatus == RootManager.RootStatus.Available,
                    detail = when (rootStatus) {
                        RootManager.RootStatus.Available -> stringResource(R.string.xposed_root_granted)
                        RootManager.RootStatus.Denied -> stringResource(R.string.xposed_root_denied)
                        RootManager.RootStatus.Unavailable -> stringResource(R.string.xposed_root_unavailable)
                        RootManager.RootStatus.Unknown -> stringResource(R.string.checking)
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                CompactStatusRow(
                    label = "Xposed",
                    isOk = filterEntries.isNotEmpty(),
                    detail = if (filterEntries.isNotEmpty()) {
                        stringResource(R.string.xposed_module_active, filterEntries.size)
                    } else {
                        stringResource(R.string.xposed_module_inactive)
                    }
                )

                // === 环境未就绪提示 ===
                if (!isEnvReady) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.xposed_hint_open_camera),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFFB300),
                        lineHeight = 18.sp
                    )
                    return@Column
                }

                Spacer(modifier = Modifier.height(16.dp))

                // === 滤镜槽位选择 ===
                Text(
                    text = stringResource(R.string.xposed_target_filter),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))

                ExposedDropdownMenuBox(
                    expanded = filterExpanded,
                    onExpandedChange = { filterExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedFilter?.let { "${it.name} (${it.lutFile})" } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        placeholder = {
                            Text(stringResource(R.string.xposed_filter_hint), color = Color.Gray)
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = filterExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = DarkGray
                        ),
                        textStyle = MaterialTheme.typography.bodyMedium
                    )

                    ExposedDropdownMenu(
                        expanded = filterExpanded,
                        onDismissRequest = { filterExpanded = false },
                        containerColor = DarkGray
                    ) {
                        filterEntries.forEach { entry ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(entry.name, color = Color.White)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            entry.lutFile,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                },
                                onClick = {
                                    onSelectFilter(entry)
                                    filterExpanded = false
                                }
                            )
                        }
                    }
                }

                // === 参数预览（紧凑） ===
                if (paramsPreview.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.xposed_params_preview),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    paramsPreview.chunked(2).forEach { row ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            row.forEach { (label, value) ->
                                Text(
                                    text = "$label: $value",
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(vertical = 1.dp)
                                )
                            }
                            if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // === 写入按钮 ===
                Button(
                    onClick = {
                        when (writeStatus) {
                            WriteStatus.Success, WriteStatus.Error -> {
                                writeStatus = WriteStatus.Idle
                                errorMessage = null
                            }
                            WriteStatus.Idle -> doWrite()
                            else -> {} // 写入中不响应
                        }
                    },
                    enabled = selectedEntry != null && !writeStatus.isWriting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when (writeStatus) {
                            WriteStatus.Success -> Color(0xFF4CAF50)
                            WriteStatus.Error -> Color(0xFFE53935)
                            else -> MaterialTheme.colorScheme.primary
                        }
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (writeStatus.isWriting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = when (writeStatus) {
                            WriteStatus.Idle -> stringResource(R.string.xposed_write_btn)
                            WriteStatus.StoppingCamera -> stringResource(R.string.xposed_stopping_camera)
                            WriteStatus.Backing -> stringResource(R.string.xposed_backing_up)
                            WriteStatus.CopyingFiles -> stringResource(R.string.xposed_copying)
                            WriteStatus.WritingParams -> stringResource(R.string.xposed_writing)
                            WriteStatus.RestoringFiles -> stringResource(R.string.xposed_restoring)
                            WriteStatus.Success -> stringResource(R.string.xposed_write_success)
                            WriteStatus.Error -> stringResource(R.string.xposed_write_retry)
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                // 写入中进度条
                if (writeStatus.isWriting) {
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // 错误信息
                errorMessage?.let {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(it, color = Color(0xFFFFB300), style = MaterialTheme.typography.bodySmall)
                }

                // 成功提示
                if (writeStatus == WriteStatus.Success) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.xposed_write_success_hint),
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // 恢复按钮
                if (writeStatus == WriteStatus.Success || writeStatus == WriteStatus.Error) {
                    TextButton(
                        onClick = { doRestore() },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = stringResource(R.string.xposed_restore_backup),
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

/**
 * 紧凑环境状态行
 */
@Composable
private fun CompactStatusRow(label: String, isOk: Boolean, detail: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (isOk) Icons.Default.Check else Icons.Default.Close,
            contentDescription = null,
            tint = if (isOk) Color(0xFF4CAF50) else Color(0xFFE53935),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text("$label: ", color = Color.White, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
        Text(detail, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
    }
}
