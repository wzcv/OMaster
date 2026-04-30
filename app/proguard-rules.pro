# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# 优化配置 - 减小 APK 体积

# 保留 Compose 相关类
-keep class androidx.compose.** { *; }
-keep class kotlin.Metadata { *; }

# 保留 Gson 相关
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# 保留 ViewModel
-keep class * extends androidx.lifecycle.ViewModel { *; }

# 保留 Parcelable
-keep class * implements android.os.Parcelable { *; }

# 保留 Serializable
-keep class * implements java.io.Serializable { *; }

# 保留 Coil 图片加载
-keep class coil.** { *; }
-dontwarn coil.**

# 保留导航序列化
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class kotlinx.serialization.** { *; }

# 保留数据模型
-keep class com.silas.omaster.model.** { *; }

# 【关键】保留资源 ID 不被混淆，确保 getIdentifier() 正常工作
# Realme 预设使用 @string/xxx 格式通过反射获取资源
-keep class com.silas.omaster.R$string { *; }
-keepclassmembers class com.silas.omaster.R$string { *; }

# 优化移除未使用的代码
-dontwarn java.lang.invoke.**
-dontwarn sun.misc.**

# 混淆优化
-optimizationpasses 5
-allowaccessmodification
-mergeinterfacesaggressively

# 忽略 Ktor 调试检测引用的 ManagementFactory 类（Android 不支持）
-dontwarn java.lang.management.ManagementFactory
-dontwarn java.lang.management.RuntimeMXBean

# 忽略 SLF4J 静态绑定器缺失警告
-dontwarn org.slf4j.impl.StaticLoggerBinder

# ===== Xposed Hook 模块 =====
# 保留 Hook 入口类（Xposed 通过反射加载）
-keep class com.silas.omaster.xposed.CameraHook { *; }
-keep class de.robv.android.xposed.** { *; }
-dontwarn de.robv.android.xposed.**

# libxposed api101 Hook 入口
-keep class com.silas.omaster.xposed.CameraHookV2 { *; }
-keep class io.github.libxposed.** { *; }
-dontwarn io.github.libxposed.**

# ===== MMKV =====
-keep class com.tencent.mmkv.** { *; }
-dontwarn com.tencent.mmkv.**

# ===== libsu (Root 权限管理) =====
-keep class com.topjohnwu.superuser.** { *; }
-dontwarn com.topjohnwu.superuser.**
