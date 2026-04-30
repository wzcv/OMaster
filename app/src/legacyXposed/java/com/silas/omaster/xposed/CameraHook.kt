package com.silas.omaster.xposed

import android.content.Context
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Xposed 模块入口，运行在 com.oplus.camera 进程中
 *
 * 核心功能：
 * 1. Hook FilterGroupManager.initFromIpu(Context, HashMap) 捕获滤镜列表
 * 2. 解析 HashMap<String, List<HashMap<String, Object>>> 为结构化数据
 * 3. 将滤镜映射写入 JSON 文件供 OMaster 主进程读取
 */
class CameraHook : IXposedHookLoadPackage {

    companion object {
        private const val TAG = "OMaster-CameraHook"
        private const val CAMERA_PACKAGE = "com.oplus.camera"

        // Hook 目标类和方法
        private const val FILTER_GROUP_MANAGER = "com.oplus.camera.filter.FilterGroupManager"
        private const val METHOD_INIT_FROM_IPU = "initFromIpu"

        // 输出文件路径（写入相机 data 目录，OMaster 通过 Root 读取）
        private const val OUTPUT_DIR = "/data/data/$CAMERA_PACKAGE/files"
        private const val OUTPUT_FILE = "omaster_filter_map.json"

        // HashMap 中滤镜条目的 key
        private const val KEY_FILTER_TYPE = "drawing_item_filter_type"     // LUT 文件名
        private const val KEY_FILTER_NAME = "drawing_item_filter_name"     // 资源 ID (Int)
        private const val KEY_IS_MASTER = "drawing_item_is_master"         // 分组标记
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName != CAMERA_PACKAGE) return

        XposedBridge.log("$TAG: 检测到目标包: ${lpparam.packageName}")
        hookFilterGroupManager(lpparam.classLoader)
    }

    /**
     * Hook FilterGroupManager.initFromIpu(Context, HashMap)
     *
     * HashMap 结构:
     * - key: String = 模式标识 (如 "master-back", "night-back" 等)
     * - value: List<HashMap<String, Object>> = 该模式下的滤镜列表
     *   每个滤镜条目含:
     *   - "drawing_item_filter_type": String (LUT 文件名, 如 "800t.bin")
     *   - "drawing_item_filter_name": Integer (资源 ID, 如 2131821255)
     *   - "drawing_item_is_master": Integer (分组标记, 0 或正整数)
     */
    private fun hookFilterGroupManager(classLoader: ClassLoader) {
        try {
            XposedHelpers.findAndHookMethod(
                FILTER_GROUP_MANAGER,
                classLoader,
                METHOD_INIT_FROM_IPU,
                Context::class.java,
                HashMap::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        try {
                            val context = param.args[0] as? Context ?: return
                            @Suppress("UNCHECKED_CAST")
                            val filterMap = param.args[1] as? HashMap<String, List<HashMap<String, Any>>>
                                ?: return

                            XposedBridge.log("$TAG: 捕获到滤镜映射，共 ${filterMap.size} 个模式")
                            val jsonResult = parseFilterMap(context, filterMap)
                            writeFilterMapToFile(jsonResult)
                        } catch (e: Exception) {
                            XposedBridge.log("$TAG: Hook afterHookedMethod 异常: ${e.message}")
                            e.printStackTrace()
                        }
                    }
                }
            )
            XposedBridge.log("$TAG: Hook $FILTER_GROUP_MANAGER.$METHOD_INIT_FROM_IPU 成功")
        } catch (e: Exception) {
            XposedBridge.log("$TAG: Hook 失败: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * 解析 HashMap 为 JSON 结构
     *
     * 输出格式:
     * {
     *   "captureTime": 1708761234567,
     *   "modes": {
     *     "master-back": [
     *       {"index": 0, "name": "原图", "lutFile": "default", "isMaster": 0, "resourceId": 2131821209},
     *       ...
     *     ]
     *   }
     * }
     */
    private fun parseFilterMap(
        context: Context,
        filterMap: HashMap<String, List<HashMap<String, Any>>>
    ): JSONObject {
        val root = JSONObject()
        root.put("captureTime", System.currentTimeMillis())
        root.put("cameraPackage", CAMERA_PACKAGE)

        val modesObj = JSONObject()

        for ((mode, filterList) in filterMap) {
            val filtersArray = JSONArray()

            for ((index, filterEntry) in filterList.withIndex()) {
                val filterObj = JSONObject()
                filterObj.put("index", index)

                // LUT 文件名
                val lutFile = filterEntry[KEY_FILTER_TYPE]?.toString() ?: "unknown"
                filterObj.put("lutFile", lutFile)

                // 资源 ID → 解析为中文名
                val resourceId = when (val nameVal = filterEntry[KEY_FILTER_NAME]) {
                    is Number -> nameVal.toInt()
                    is String -> nameVal.toIntOrNull() ?: 0
                    else -> 0
                }
                filterObj.put("resourceId", resourceId)

                val displayName = resolveFilterName(context, resourceId)
                filterObj.put("name", displayName)

                // 分组标记
                val isMaster = when (val masterVal = filterEntry[KEY_IS_MASTER]) {
                    is Number -> masterVal.toInt()
                    is String -> masterVal.toIntOrNull() ?: 0
                    else -> 0
                }
                filterObj.put("isMaster", isMaster)

                filtersArray.put(filterObj)
            }

            modesObj.put(mode, filtersArray)
            XposedBridge.log("$TAG: 模式 [$mode] 共 ${filterList.size} 个滤镜")
        }

        root.put("modes", modesObj)
        return root
    }

    /**
     * 通过资源 ID 解析滤镜中文名
     * 使用相机 Context 的 getString() 方法
     */
    private fun resolveFilterName(context: Context, resourceId: Int): String {
        if (resourceId == 0) return "未知"
        return try {
            context.getString(resourceId)
        } catch (e: Exception) {
            // 某些资源 ID 存在运行时偏移问题
            XposedBridge.log("$TAG: 资源 ID $resourceId 解析失败: ${e.message}")
            "ID:$resourceId"
        }
    }

    /**
     * 将 JSON 写入文件
     */
    private fun writeFilterMapToFile(json: JSONObject) {
        try {
            val dir = File(OUTPUT_DIR)
            if (!dir.exists()) dir.mkdirs()

            val file = File(dir, OUTPUT_FILE)
            file.writeText(json.toString(2))

            // 设置文件权限为全局可读，便于 OMaster 通过 Root 访问
            file.setReadable(true, false)

            XposedBridge.log("$TAG: 滤镜映射已写入 ${file.absolutePath} (${file.length()} bytes)")
        } catch (e: Exception) {
            XposedBridge.log("$TAG: 写入文件失败: ${e.message}")
            e.printStackTrace()
        }
    }
}
