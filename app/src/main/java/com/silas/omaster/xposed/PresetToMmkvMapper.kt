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
 * - filter_intensity → key_master_mode_effect_filter_intensity_{lutFile}
 * - saturation      → key_master_mode_effect_saturation_{lutFile}
 * - tone (影调)     → key_master_mode_effect_contrast_{lutFile}
 * - warmCool        → key_master_mode_effect_cold_warm_{lutFile}
 * - cyanMagenta     → key_master_mode_effect_cyan_magenta_{lutFile}
 * - sharpness       → key_master_mode_effect_sharpness_{lutFile}
 * - vignette        → key_master_mode_effect_vignette_{lutFile}
 * - softLight       → key_master_mode_effect_soft_light_{lutFile}
 *
 * 新版 MMKV Key 后缀规则:
 * - 所有参数后缀为 LUT 文件名，如 "fuji_cc.bin"、"kodak.cube.rgb.bin"
 * - 格式: key_master_mode_effect_{param}_{lutFile}
 *
 * MMKV 文件路由:
 * - 所有滤镜参数统一写入 "mmkv" 文件
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
     * @param targetFile 目标 MMKV 文件名（新版固定为 "mmkv"）
     * @param params 要写入的键值对 Map<String, Int>
     */
    data class MmkvWriteParams(
        val targetFile: String,
        val params: Map<String, Int>
    )

    /**
     * 新版写入：按 LUT 文件名寻址（相机 6.x 新格式）
     * key 格式: key_master_mode_effect_{param}_{lutFile}
     * 写入文件: mmkv（所有滤镜统一）
     */
    fun mapPresetToMmkvParamsNew(
        preset: MasterPreset,
        lutFile: String
    ): MmkvWriteParams {
        val params = mutableMapOf<String, Int>()

        preset.filter?.let { parseFilterString(it).intensity?.let { i -> params[buildKeyNew("filter_intensity", lutFile)] = i } }
        preset.saturation?.let { params[buildKeyNew("saturation", lutFile)] = it }
        preset.tone?.let { params[buildKeyNew("contrast", lutFile)] = it }
        preset.warmCool?.let { params[buildKeyNew("cold_warm", lutFile)] = it }
        preset.cyanMagenta?.let { params[buildKeyNew("cyan_magenta", lutFile)] = it }
        preset.sharpness?.let { params[buildKeyNew("sharpness", lutFile)] = it }
        preset.vignette?.let { params[buildKeyNew("vignette", lutFile)] = mapVignetteValue(it) }
        preset.softLight?.let { params[buildKeyNew("soft_light", lutFile)] = mapSoftLightValue(it) }

        if (params.isEmpty() && !preset.sections.isNullOrEmpty()) {
            extractFromSectionsNew(preset.sections, lutFile, params)
        }

        return MmkvWriteParams(targetFile = MMKV_FILE_DEFAULT, params = params)
    }

    /**
     * 旧版写入：按整数索引寻址（相机 5.x 旧格式）
     * key 格式: key_master_mode_effect_{param}（索引0）或 key_master_mode_effect_{param}_{index}（索引≥1）
     * 写入文件: com.oplus.camera_preferences_0（索引0）或 mmkv（索引≥1）
     */
    fun mapPresetToMmkvParamsLegacy(
        preset: MasterPreset,
        filterIndex: Int
    ): MmkvWriteParams {
        val params = mutableMapOf<String, Int>()

        params[buildKeyLegacy("filter", filterIndex)] = filterIndex
        preset.filter?.let { parseFilterString(it).intensity?.let { i -> params[buildKeyLegacy("filter_intensity", filterIndex)] = i } }
        preset.saturation?.let { params[buildKeyLegacy("saturation", filterIndex)] = it }
        preset.tone?.let { params[buildKeyLegacy("contrast", filterIndex)] = it }
        preset.warmCool?.let { params[buildKeyLegacy("cold_warm", filterIndex)] = it }
        preset.cyanMagenta?.let { params[buildKeyLegacy("cyan_magenta", filterIndex)] = it }
        preset.sharpness?.let { params[buildKeyLegacy("sharpness", filterIndex)] = it }
        preset.vignette?.let { params[buildKeyLegacy("vignette", filterIndex)] = mapVignetteValue(it) }
        preset.softLight?.let { params[buildKeyLegacy("soft_light", filterIndex)] = mapSoftLightValue(it) }

        if (params.size <= 1 && !preset.sections.isNullOrEmpty()) {
            extractFromSectionsLegacy(preset.sections, filterIndex, params)
        }

        val targetFile = if (filterIndex == 0) MMKV_FILE_PREFERENCES_0 else MMKV_FILE_DEFAULT
        return MmkvWriteParams(targetFile = targetFile, params = params)
    }

    private fun buildKeyNew(param: String, lutFile: String): String =
        "key_master_mode_effect_${param}_${lutFile}"

    private fun buildKeyLegacy(param: String, filterIndex: Int): String =
        if (filterIndex == 0) "key_master_mode_effect_$param"
        else "key_master_mode_effect_${param}_$filterIndex"

    fun getTargetMmkvFile(): String = MMKV_FILE_DEFAULT

    private fun extractFromSectionsNew(
        sections: List<PresetSection>,
        lutFile: String,
        params: MutableMap<String, Int>
    ) {
        sections.flatMap { it.items }.forEach { item ->
            val mmkvParam = LABEL_TO_MMKV_PARAM[item.label] ?: return@forEach
            val value = item.value.trim()
            when (mmkvParam) {
                "filter" -> parseFilterString(value).intensity?.let { params[buildKeyNew("filter_intensity", lutFile)] = it }
                "soft_light" -> params[buildKeyNew("soft_light", lutFile)] = mapSoftLightValue(value)
                "vignette" -> params[buildKeyNew("vignette", lutFile)] = mapVignetteValue(value)
                else -> value.removePrefix("+").toIntOrNull()?.let { params[buildKeyNew(mmkvParam, lutFile)] = it }
            }
        }
    }

    private fun extractFromSectionsLegacy(
        sections: List<PresetSection>,
        filterIndex: Int,
        params: MutableMap<String, Int>
    ) {
        sections.flatMap { it.items }.forEach { item ->
            val mmkvParam = LABEL_TO_MMKV_PARAM[item.label] ?: return@forEach
            val value = item.value.trim()
            when (mmkvParam) {
                "filter" -> parseFilterString(value).intensity?.let { params[buildKeyLegacy("filter_intensity", filterIndex)] = it }
                "soft_light" -> params[buildKeyLegacy("soft_light", filterIndex)] = mapSoftLightValue(value)
                "vignette" -> params[buildKeyLegacy("vignette", filterIndex)] = mapVignetteValue(value)
                else -> value.removePrefix("+").toIntOrNull()?.let { params[buildKeyLegacy(mmkvParam, filterIndex)] = it }
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
     * 参数预览（新版，显示 lutFile 后缀）
     */
    fun getParamsPreview(
        preset: MasterPreset,
        lutFile: String
    ): List<Pair<String, String>> {
        val preview = mutableListOf<Pair<String, String>>()
        preview.add("目标滤镜" to lutFile)

        if (!preset.sections.isNullOrEmpty()) {
            preset.sections.flatMap { it.items }.forEach { item ->
                val mmkvParam = LABEL_TO_MMKV_PARAM[item.label] ?: return@forEach
                when (mmkvParam) {
                    "filter" -> {
                        val parsed = parseFilterString(item.value)
                        preview.add("滤镜名" to parsed.name)
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
            preview.add("滤镜名" to parsed.name)
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
