package com.silas.omaster.xposed

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * 滤镜映射管理器
 * 读取 Xposed Hook 捕获的滤镜列表数据
 * 单例模式，与项目其他 Manager 保持一致
 */
class FilterMapManager private constructor(private val context: Context) {

    /**
     * 单个滤镜条目
     * @param index 滤镜在列表中的索引（对应 MMKV key 后缀）
     * @param name 滤镜中文名（由 Hook 通过资源 ID 解析）
     * @param lutFile LUT 文件名（如 "800t.bin"）
     * @param isMaster 分组标记（0=普通, >0=分组头，值为组内子滤镜数）
     * @param resourceId 原始资源 ID
     * @param mode 所属模式（如 "master-back"）
     */
    data class FilterEntry(
        val index: Int,
        val name: String,
        val lutFile: String,
        val isMaster: Int = 0,
        val resourceId: Int = 0,
        val mode: String = "master-back"
    )

    private val _filterMap = MutableStateFlow<List<FilterEntry>>(emptyList())
    val filterMap: StateFlow<List<FilterEntry>> = _filterMap.asStateFlow()

    private val _allModes = MutableStateFlow<Map<String, List<FilterEntry>>>(emptyMap())
    val allModes: StateFlow<Map<String, List<FilterEntry>>> = _allModes.asStateFlow()

    private val _lastCaptureTime = MutableStateFlow(0L)
    val lastCaptureTime: StateFlow<Long> = _lastCaptureTime.asStateFlow()

    private val _isAvailable = MutableStateFlow(false)
    val isAvailable: StateFlow<Boolean> = _isAvailable.asStateFlow()

    /**
     * 通过 Root 读取 Hook 写入的滤镜映射文件
     * 文件位于: /data/data/com.oplus.camera/files/omaster_filter_map.json
     */
    suspend fun loadFilterMap(): Boolean = withContext(Dispatchers.IO) {
        try {
            val rootManager = RootManager.getInstance()
            val json = rootManager.readFilterMapJson() ?: run {
                resetState()
                return@withContext false
            }

            val root = JSONObject(json)
            _lastCaptureTime.value = root.optLong("captureTime", 0)

            val modesObj = root.optJSONObject("modes") ?: run {
                resetState()
                return@withContext false
            }
            val allModesMap = mutableMapOf<String, List<FilterEntry>>()

            val modeKeys = modesObj.keys()
            while (modeKeys.hasNext()) {
                val mode = modeKeys.next()
                val filtersArray = modesObj.getJSONArray(mode)
                val entries = mutableListOf<FilterEntry>()

                for (i in 0 until filtersArray.length()) {
                    val filterObj = filtersArray.getJSONObject(i)
                    entries.add(
                        FilterEntry(
                            index = filterObj.getInt("index"),
                            name = filterObj.getString("name"),
                            lutFile = filterObj.getString("lutFile"),
                            isMaster = filterObj.optInt("isMaster", 0),
                            resourceId = filterObj.optInt("resourceId", 0),
                            mode = mode
                        )
                    )
                }
                allModesMap[mode] = entries
            }

            _allModes.value = allModesMap
            // 默认显示 master-back 模式
            _filterMap.value = allModesMap["master-back"] ?: allModesMap.values.firstOrNull() ?: emptyList()
            _isAvailable.value = _filterMap.value.isNotEmpty()

            Log.d(TAG, "加载滤镜映射成功: ${allModesMap.size} 个模式, master-back: ${_filterMap.value.size} 个滤镜")
            true
        } catch (e: Exception) {
            Log.e(TAG, "加载滤镜映射失败", e)
            resetState()
            false
        }
    }

    private fun resetState() {
        _filterMap.value = emptyList()
        _allModes.value = emptyMap()
        _lastCaptureTime.value = 0L
        _isAvailable.value = false
    }

    /**
     * 根据滤镜中文名查找对应的 FilterEntry（含 lutFile）
     * 支持模糊匹配（去除百分比后缀和空格）
     */
    fun getFilterByName(filterName: String): FilterEntry? {
        val cleanName = filterName
            .replace(Regex("\\s*\\d+%$"), "")
            .trim()

        _filterMap.value.find { it.name == cleanName }?.let { return it }
        return _filterMap.value.find {
            it.name.contains(cleanName) || cleanName.contains(it.name)
        }
    }

    /**
     * 根据索引获取滤镜条目
     */
    fun getFilterByIndex(index: Int): FilterEntry? {
        return _filterMap.value.find { it.index == index }
    }

    /**
     * 切换显示的模式
     */
    fun switchMode(mode: String) {
        _filterMap.value = _allModes.value[mode] ?: emptyList()
    }

    /**
     * 检查滤镜映射是否可用
     */
    fun isFilterMapAvailable(): Boolean = _isAvailable.value

    companion object {
        private const val TAG = "OMaster-FilterMap"

        @Volatile
        private var instance: FilterMapManager? = null

        fun getInstance(context: Context): FilterMapManager {
            return instance ?: synchronized(this) {
                instance ?: FilterMapManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
