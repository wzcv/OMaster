package com.silas.omaster.xposed

import android.content.Context
import android.util.Log
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * libxposed api101 模块入口，运行在 com.oplus.camera 进程中
 */
class CameraHookV2 : XposedModule() {

    companion object {
        private const val TAG = "OMaster-CameraHook"
        private const val CAMERA_PACKAGE = "com.oplus.camera"

        private const val FILTER_GROUP_MANAGER = "com.oplus.camera.filter.FilterGroupManager"
        private const val METHOD_INIT_FROM_IPU = "initFromIpu"

        private const val OUTPUT_DIR = "/data/data/$CAMERA_PACKAGE/files"
        private const val OUTPUT_FILE = "omaster_filter_map.json"

        private const val KEY_FILTER_TYPE = "drawing_item_filter_type"
        private const val KEY_FILTER_NAME = "drawing_item_filter_name"
        private const val KEY_IS_MASTER = "drawing_item_is_master"
    }

    override fun onModuleLoaded(param: ModuleLoadedParam) {
        log(Log.INFO, TAG, "onModuleLoaded: ${param.processName}")
    }

    override fun onPackageReady(param: PackageReadyParam) {
        if (param.packageName != CAMERA_PACKAGE) return

        log(Log.INFO, TAG, "检测到目标包: ${param.packageName}")
        hookFilterGroupManager(param.classLoader)
    }

    private fun hookFilterGroupManager(classLoader: ClassLoader) {
        try {
            val clazz = Class.forName(FILTER_GROUP_MANAGER, false, classLoader)
            val method = clazz.getDeclaredMethod(
                METHOD_INIT_FROM_IPU,
                Context::class.java,
                HashMap::class.java
            )

            hook(method).intercept { chain ->
                val result = chain.proceed()
                try {
                    val context = chain.args[0] as? Context
                    @Suppress("UNCHECKED_CAST")
                    val filterMap = chain.args[1] as? HashMap<String, List<HashMap<String, Any>>>
                    if (context != null && filterMap != null) {
                        log(Log.INFO, TAG, "捕获到滤镜映射，共 ${filterMap.size} 个模式")
                        val jsonResult = parseFilterMap(context, filterMap)
                        writeFilterMapToFile(jsonResult)
                    }
                } catch (e: Exception) {
                    log(Log.ERROR, TAG, "afterHookedMethod 异常: ${e.message}", e)
                }
                result
            }

            log(Log.INFO, TAG, "Hook $FILTER_GROUP_MANAGER.$METHOD_INIT_FROM_IPU 成功")
        } catch (e: Exception) {
            log(Log.ERROR, TAG, "Hook 失败: ${e.message}", e)
        }
    }

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

                val lutFile = filterEntry[KEY_FILTER_TYPE]?.toString() ?: "unknown"
                filterObj.put("lutFile", lutFile)

                val resourceId = when (val nameVal = filterEntry[KEY_FILTER_NAME]) {
                    is Number -> nameVal.toInt()
                    is String -> nameVal.toIntOrNull() ?: 0
                    else -> 0
                }
                filterObj.put("resourceId", resourceId)
                filterObj.put("name", resolveFilterName(context, resourceId))

                val isMaster = when (val masterVal = filterEntry[KEY_IS_MASTER]) {
                    is Number -> masterVal.toInt()
                    is String -> masterVal.toIntOrNull() ?: 0
                    else -> 0
                }
                filterObj.put("isMaster", isMaster)

                filtersArray.put(filterObj)
            }
            modesObj.put(mode, filtersArray)
            log(Log.INFO, TAG, "模式 [$mode] 共 ${filterList.size} 个滤镜")
        }

        root.put("modes", modesObj)
        return root
    }

    private fun resolveFilterName(context: Context, resourceId: Int): String {
        if (resourceId == 0) return "未知"
        return try {
            context.getString(resourceId)
        } catch (e: Exception) {
            log(Log.WARN, TAG, "资源 ID $resourceId 解析失败: ${e.message}")
            "ID:$resourceId"
        }
    }

    private fun writeFilterMapToFile(json: JSONObject) {
        try {
            val dir = File(OUTPUT_DIR)
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, OUTPUT_FILE)
            file.writeText(json.toString(2))
            file.setReadable(true, false)
            log(Log.INFO, TAG, "滤镜映射已写入 ${file.absolutePath} (${file.length()} bytes)")
        } catch (e: Exception) {
            log(Log.ERROR, TAG, "写入文件失败: ${e.message}", e)
        }
    }
}
