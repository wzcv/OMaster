package com.silas.omaster.xposed

import com.silas.omaster.model.MasterPreset
import com.silas.omaster.model.PresetSection

/**
 * MasterPreset 到 MMKV Key-Value 的转换器
 *
 * 支持两种数据源:
 * 1. 顶层字段 (v1 格式): saturation, tone, warmCool 等 Int?/String? 字段
 * 2. sections 列表 (v2 格式): PresetSection → PresetItem (label + value)
 *
 * 核心映射关系:
 * - filter → key_master_mode_effect_filter (索引) + key_master_mode_effect_filter_intensity (强度)
 * - saturation → key_master_mode_effect_saturation
 * - tone (影调) → key_master_mode_effect_contrast (OPPO 称"对比度")
 * - warmCool → key_master_mode_effect_cold_warm
 * - cyanMagenta → key_master_mode_effect_cyan_magenta
 * - sharpness → key_master_mode_effect_sharpness
 * - vignette → key_master_mode_effect_vignette
 * - softLight → key_master_mode_effect_soft_light
 *
 * MMKV Key 后缀规则:
 * - 索引 0: 无后缀 (如 key_master_mode_effect_saturation)
 * - 索引 N (N≥1): 追加 "_N" (如 key_master_mode_effect_saturation_9)
 *
 * MMKV 文件路由:
 * - 索引 0 → "com.oplus.camera_preferences_0"
 * - 索引 ≥1 → "mmkv"
 */
object PresetToMmkvMapper {

    /**
     * Section item label → MMKV 参数名映射
     * 支持三种格式: @string/ 引用、中文名、英文名
     */
    private val LABEL_TO_MMKV_PARAM = mapOf(
        // @string 资源引用（JSON 原始格式）
        "@string/param_filter" to "filter",
        "@string/param_soft_light" to "soft_light",
        "@string/param_tone_curve" to "contrast",
        "@string/param_saturation" to "saturation",
        "@string/param_warm_cool" to "cold_warm",
        "@string/param_cyan_magenta" to "cyan_magenta",
        "@string/param_sharpness" to "sharpness",
        "@string/param_vignette" to "vignette",
        // 中文名（自定义预设或已解析的情况）
        "滤镜" to "filter",
        "柔光" to "soft_light",
        "影调" to "contrast",
        "饱和度" to "saturation",
        "冷暖" to "cold_warm",
        "青品" to "cyan_magenta",
        "锐度" to "sharpness",
        "暗角" to "vignette",
        // 英文名
        "Filter" to "filter",
        "Soft Light" to "soft_light",
        "Tone Curve" to "contrast",
        "Saturation" to "saturation",
        "Warm/Cool" to "cold_warm",
        "Cyan/Magenta" to "cyan_magenta",
        "Sharpness" to "sharpness",
        "Vignette" to "vignette",
    )

    // MMKV 文件名常量
    const val MMKV_FILE_PREFERENCES_0 = "com.oplus.camera_preferences_0"
    const val MMKV_FILE_DEFAULT = "mmkv"

    /**
     * 滤镜字符串解析结果
     * @param name 滤镜名称（如 "复古"）
     * @param intensity 滤镜强度百分比（如 100），null 表示未指定
     */
    data class FilterParseResult(
        val name: String,
        val intensity: Int?
    )

    /**
     * 参数写入结果
     * @param targetFile 目标 MMKV 文件名
     * @param params 要写入的键值对 Map<String, Int>
     */
    data class MmkvWriteParams(
        val targetFile: String,
        val params: Map<String, Int>
    )

    /**
     * 构建带索引后缀的 MMKV key
     */
    private fun buildKey(param: String, filterIndex: Int): String {
        return if (filterIndex == 0) {
            "key_master_mode_effect_$param"
        } else {
            "key_master_mode_effect_${param}_$filterIndex"
        }
    }

    /**
     * 确定目标 MMKV 文件
     */
    fun getTargetMmkvFile(filterIndex: Int): String {
        return if (filterIndex == 0) MMKV_FILE_PREFERENCES_0 else MMKV_FILE_DEFAULT
    }

    /**
     * 将 MasterPreset 的调色参数转换为 MMKV 键值对
     *
     * 优先使用顶层字段 (v1 格式)，不足时从 sections (v2 格式) 补充
     *
     * @param preset 源预设
     * @param filterIndex 目标滤镜索引（从 FilterMapManager 查得）
     * @return MmkvWriteParams 包含目标文件名和键值对
     */
    fun mapPresetToMmkvParams(
        preset: MasterPreset,
        filterIndex: Int
    ): MmkvWriteParams {
        val params = mutableMapOf<String, Int>()

        // 滤镜索引（写入当前选中的滤镜）
        params[buildKey("filter", filterIndex)] = filterIndex

        // === Phase 1: 从顶层字段读取 (v1 格式) ===
        preset.filter?.let { filterStr ->
            val parsed = parseFilterString(filterStr)
            parsed.intensity?.let {
                params[buildKey("filter_intensity", filterIndex)] = it
            }
        }
        preset.saturation?.let {
            params[buildKey("saturation", filterIndex)] = it
        }
        preset.tone?.let {
            params[buildKey("contrast", filterIndex)] = it
        }
        preset.warmCool?.let {
            params[buildKey("cold_warm", filterIndex)] = it
        }
        preset.cyanMagenta?.let {
            params[buildKey("cyan_magenta", filterIndex)] = it
        }
        preset.sharpness?.let {
            params[buildKey("sharpness", filterIndex)] = it
        }
        preset.vignette?.let {
            params[buildKey("vignette", filterIndex)] = mapVignetteValue(it)
        }
        preset.softLight?.let {
            params[buildKey("soft_light", filterIndex)] = mapSoftLightValue(it)
        }

        // === Phase 2: 从 sections 补充 (v2 格式) ===
        // 当顶层字段不足时（如 v2 预设仅有 sections），从 sections 提取
        if (params.size <= 1 && !preset.sections.isNullOrEmpty()) {
            extractFromSections(preset.sections, filterIndex, params)
        }

        return MmkvWriteParams(
            targetFile = getTargetMmkvFile(filterIndex),
            params = params
        )
    }

    /**
     * 从 sections 列表中提取调色参数到 MMKV 键值对
     *
     * 通过 label (支持 @string/ 引用、中文、英文) 匹配参数名，
     * 解析 value 字符串为对应的 MMKV Int 值。
     */
    private fun extractFromSections(
        sections: List<PresetSection>,
        filterIndex: Int,
        params: MutableMap<String, Int>
    ) {
        sections.flatMap { it.items }.forEach { item ->
            val mmkvParam = LABEL_TO_MMKV_PARAM[item.label] ?: return@forEach
            val value = item.value.trim()

            when (mmkvParam) {
                "filter" -> {
                    // 滤镜: "清新 94%" → filter index 已由用户选择，只解析强度
                    val parsed = parseFilterString(value)
                    parsed.intensity?.let {
                        params[buildKey("filter_intensity", filterIndex)] = it
                    }
                }
                "soft_light" -> {
                    params[buildKey("soft_light", filterIndex)] = mapSoftLightValue(value)
                }
                "vignette" -> {
                    params[buildKey("vignette", filterIndex)] = mapVignetteValue(value)
                }
                else -> {
                    // 整数值: "+16", "-13", "0"
                    val intValue = value.removePrefix("+").toIntOrNull()
                    if (intValue != null) {
                        params[buildKey(mmkvParam, filterIndex)] = intValue
                    }
                }
            }
        }
    }

    /**
     * 解析滤镜字符串
     * 支持格式: "复古 100%", "复古100%", "复古", "原图"
     */
    fun parseFilterString(filterStr: String): FilterParseResult {
        val regex = Regex("^(.+?)\\s*(\\d+)%?$")
        val match = regex.find(filterStr.trim())

        return if (match != null) {
            FilterParseResult(
                name = match.groupValues[1].trim(),
                intensity = match.groupValues[2].toIntOrNull()
            )
        } else {
            FilterParseResult(
                name = filterStr.trim(),
                intensity = null  // 未指定强度时不写入，保留相机默认值
            )
        }
    }

    /**
     * 暗角值映射
     * 在 OPPO 相机中:
     * - 0 = 默认值（暗角关闭）
     * - 101 = 暗角开启（最大效果）
     */
    private fun mapVignetteValue(vignette: String): Int {
        return when (vignette.trim()) {
            "开", "开启", "on", "On", "ON" -> 101
            "关", "关闭", "off", "Off", "OFF" -> 0
            else -> {
                // 尝试解析为数字
                vignette.trim().toIntOrNull() ?: 0
            }
        }
    }

    /**
     * 柔光值映射
     */
    private fun mapSoftLightValue(softLight: String): Int {
        return when (softLight.trim()) {
            "无", "none", "None" -> 0
            "朦胧", "hazy", "Hazy" -> 1
            "柔美", "gentle", "Gentle" -> 2
            "梦幻", "dreamy", "Dreamy" -> 3
            else -> {
                // 尝试解析为数字
                softLight.trim().toIntOrNull() ?: 0
            }
        }
    }

    /**
     * 从预设中提取滤镜字符串（优先顶层字段，回退 sections）
     * 用于 WriteFilterDialog 自动匹配滤镜索引
     */
    fun extractFilterString(preset: MasterPreset): String? {
        if (preset.filter != null) return preset.filter
        return preset.sections?.flatMap { it.items }
            ?.find { LABEL_TO_MMKV_PARAM[it.label] == "filter" }
            ?.value
    }

    /**
     * 获取将被写入的参数预览（用于 UI 显示）
     * 支持 v1 (顶层字段) 和 v2 (sections) 两种格式
     * @return 参数名到值的列表
     */
    fun getParamsPreview(
        preset: MasterPreset,
        filterIndex: Int
    ): List<Pair<String, String>> {
        val preview = mutableListOf<Pair<String, String>>()

        // 尝试从 sections 提取（v2 格式，信息更完整）
        if (!preset.sections.isNullOrEmpty()) {
            preset.sections.flatMap { it.items }.forEach { item ->
                val mmkvParam = LABEL_TO_MMKV_PARAM[item.label] ?: return@forEach
                when (mmkvParam) {
                    "filter" -> {
                        val parsed = parseFilterString(item.value)
                        preview.add("滤镜" to "索引 $filterIndex (${parsed.name})")
                        parsed.intensity?.let { i -> preview.add("滤镜强度" to "$i%") }
                    }
                    "soft_light" -> preview.add("柔光" to item.value)
                    "contrast" -> preview.add("影调→对比度" to item.value)
                    "saturation" -> preview.add("饱和度" to item.value)
                    "cold_warm" -> preview.add("冷暖" to item.value)
                    "cyan_magenta" -> preview.add("青品" to item.value)
                    "sharpness" -> preview.add("锐度" to item.value)
                    "vignette" -> preview.add("暗角" to item.value)
                }
            }
            return preview
        }

        // Fallback: 顶层字段 (v1 格式)
        preset.filter?.let {
            val parsed = parseFilterString(it)
            preview.add("滤镜" to "索引 $filterIndex (${parsed.name})")
            parsed.intensity?.let { i -> preview.add("滤镜强度" to "$i%") }
        }
        preset.saturation?.let { preview.add("饱和度" to "$it") }
        preset.tone?.let { preview.add("影调→对比度" to "$it") }
        preset.warmCool?.let { preview.add("冷暖" to "$it") }
        preset.cyanMagenta?.let { preview.add("青品" to "$it") }
        preset.sharpness?.let { preview.add("锐度" to "$it") }
        preset.vignette?.let { preview.add("暗角" to it) }
        preset.softLight?.let { preview.add("柔光" to it) }

        return preview
    }
}
