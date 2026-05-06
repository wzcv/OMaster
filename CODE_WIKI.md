# OMaster - Code Wiki

> **项目简介**: OMaster 是一款专为 OPPO/一加/Realme 手机打造的摄影调色参数管理工具，帮助用户轻松管理和使用各种专业摄影预设。

---

## 📑 目录

1. [项目概述](#1-项目概述)
2. [项目架构](#2-项目架构)
3. [模块详解](#3-模块详解)
4. [核心类与函数](#4-核心类与函数)
5. [数据流与依赖关系](#5-数据流与依赖关系)
6. [项目运行方式](#6-项目运行方式)
7. [技术栈](#7-技术栈)
8. [开发规范](#8-开发规范)

---

## 1. 项目概述

### 1.1 基本信息

| 属性 | 值 |
|------|-----|
| **项目名称** | OMaster |
| **包名** | `com.silas.omaster` |
| **开发语言** | Kotlin |
| **最低SDK** | Android 13 (API 33) |
| **目标SDK** | Android 14 (API 36) |
| **版本号** | 1.5.0 (versionCode: 12) |
| **UI框架** | Jetpack Compose |
| **开源协议** | CC BY-NC-SA 4.0 |

### 1.2 核心功能

- **丰富的预设库**: 23+ 款专业预设，涵盖胶片、复古、清新、黑白、美食等多种风格
- **配置云更新**: 支持从云端获取最新配置，支持自定义更新源
- **收藏管理**: 一键收藏喜欢的预设，本地存储无需网络
- **自定义预设**: 支持创建和编辑自定义预设
- **悬浮窗模式**: 拍照时可悬浮显示参数，支持左右滑动切换预设
- **多语言支持**: 支持中文和英文

---

## 2. 项目架构

### 2.1 整体架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                         Presentation Layer                       │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌─────────┐ │
│  │HomeScreen│ │DetailScr │ │CreateScr │ │Settings  │ │Discover │ │
│  └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬────┘ │
│       │            │            │            │            │      │
│  ┌────┴────────────┴────────────┴────────────┴────────────┴────┐ │
│  │                      UI Components                            │ │
│  │  • PresetCard  • PillNavBar  • ImageGallery  • CommonComp   │ │
│  └─────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────┐
│                         ViewModel Layer                          │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────────────────┐ │
│  │HomeViewModel │ │DetailViewMod │ │UniversalCreatePresetVM   │ │
│  └──────┬───────┘ └──────┬───────┘ └───────────┬──────────────┘ │
│         │                │                      │               │
│  ┌──────┴────────────────┴──────────────────────┴────────────┐  │
│  │                    Repository Layer                        │  │
│  │  ┌──────────────────────────────────────────────────────┐ │  │
│  │  │              PresetRepository (单例)                  │ │  │
│  │  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────────┐ │ │  │
│  │  │  │FavoriteMgr  │ │CustomPreset │ │  JsonUtil       │ │ │  │
│  │  │  │(SharedPref) │ │Manager      │ │(Assets/Remote)  │ │ │  │
│  │  │  └─────────────┘ └─────────────┘ └─────────────────┘ │ │  │
│  │  └──────────────────────────────────────────────────────┘ │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────┐
│                          Data Layer                              │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────────────────┐ │
│  │   Models     │ │    Local     │ │    Remote/Network        │ │
│  │ •MasterPreset│ │•SharedPrefs  │ │ •PresetRemoteManager     │ │
│  │ •ColorCard   │ │•File Storage │ │ •Ktor Client             │ │
│  │ •Subscription│ │              │ │                          │ │
│  └──────────────┘ └──────────────┘ └──────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────┐
│                        Core/Utils Layer                          │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌────────┐ │
│  │ConfigCen │ │Logger    │ │HapticExt │ │ImageUtil │ │Theme   │ │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 目录结构

```
app/src/main/java/com/silas/omaster/
├── MainActivity.kt                 # 主Activity，导航入口
├── OMasterApplication.kt           # Application类，全局初始化
├── model/                          # 数据模型层
│   ├── MasterPreset.kt            # 预设数据模型
│   ├── ColorCard.kt               # 色彩卡片模型
│   └── Subscription.kt            # 订阅配置模型
├── data/                          # 数据层
│   ├── config/                    # 配置管理
│   │   ├── ConfigCenter.kt        # 配置中心（主题、语言等）
│   │   ├── ConfigMigration.kt     # 配置迁移
│   │   └── SubscriptionConfig.kt  # 订阅配置
│   ├── local/                     # 本地数据管理
│   │   ├── FavoriteManager.kt     # 收藏管理
│   │   ├── CustomPresetManager.kt # 自定义预设管理
│   │   ├── CheckInManager.kt      # 签到管理
│   │   └── ...                    # 其他Manager
│   ├── repository/                # 数据仓库
│   │   └── PresetRepository.kt    # 预设数据仓库
│   └── ColorCardLibrary.kt        # 色彩卡片库
├── network/                       # 网络层
│   └── PresetRemoteManager.kt     # 远程预设管理
├── ui/                           # UI层
│   ├── home/                     # 首页
│   ├── detail/                   # 详情页
│   ├── create/                   # 创建预设
│   ├── discover/                 # 发现页
│   ├── frame/                    # 相框功能
│   ├── service/                  # 悬浮窗服务
│   ├── settings/                 # 设置页
│   ├── subscription/             # 订阅管理
│   ├── components/               # 通用组件
│   ├── theme/                    # 主题配置
│   └── animation/                # 动画配置
└── util/                         # 工具类
    ├── JsonUtil.kt               # JSON解析工具
    ├── ImageExporter.kt          # 图片导出
    ├── ColorExtractor.kt         # 颜色提取
    ├── UpdateChecker.kt          # 更新检查
    └── ...                       # 其他工具
```

---

## 3. 模块详解

### 3.1 模型层 (Model)

#### MasterPreset - 核心预设模型

```kotlin
@Serializable
data class MasterPreset(
    val id: String? = null,                    // 唯一标识符
    val name: String,                          // 预设名称
    val coverPath: String,                     // 封面图片路径
    val galleryImages: List<String>? = null,   // 图库图片列表
    val author: String = "@OPPO影像",           // 作者
    val mode: String? = null,                  // 模式：auto/pro
    val filter: String? = null,                // 滤镜类型
    val whiteBalance: String? = null,          // 白平衡
    val colorTone: String? = null,             // 色调
    val exposureCompensation: String? = null,  // 曝光补偿
    val colorTemperature: Int? = null,         // 色温
    val colorHue: Int? = null,                 // 色调数值
    val iso: String? = null,                   // ISO
    val shutterSpeed: String? = null,          // 快门速度
    val softLight: String? = null,             // 柔光强度
    val tone: Int? = null,                     // 影调
    val saturation: Int? = null,               // 饱和度
    val warmCool: Int? = null,                 // 冷暖
    val cyanMagenta: Int? = null,              // 青品
    val sharpness: Int? = null,                // 锐度
    val vignette: String? = null,              // 暗角
    val isFavorite: Boolean = false,           // 是否收藏
    val isCustom: Boolean = false,             // 是否自定义
    val isNew: Boolean = false,                // 是否新预设
    val description: PresetDescription? = null,// 描述信息
    val sections: List<PresetSection>? = null, // 动态参数分组
    val tags: List<String>? = emptyList()      // 标签
)
```

#### ColorCard - 色彩卡片模型

```kotlin
@Serializable
data class ColorCard(
    val id: String,                           // 卡片ID
    val colors: List<ColorInfo>,              // 颜色列表
    val themeResId: Int,                      // 主题资源ID
    val descriptionResId: Int,                // 描述资源ID
    val tipsResId: Int,                       // 提示资源ID
    val challengeResId: Int,                  // 挑战资源ID
    val sceneTags: List<Int>                  // 场景标签
)
```

### 3.2 数据层 (Data)

#### PresetRepository - 预设数据仓库

**职责**: 统一管理默认预设、自定义预设和收藏数据的访问

**核心方法**:

| 方法 | 返回类型 | 说明 |
|------|----------|------|
| `getAllPresets()` | `Flow<List<MasterPreset>>` | 获取所有预设（默认+自定义） |
| `getCustomPresets()` | `Flow<List<MasterPreset>>` | 获取自定义预设 |
| `getFavoritePresets()` | `Flow<List<MasterPreset>>` | 获取收藏预设 |
| `getPresetById(id)` | `MasterPreset?` | 根据ID获取预设 |
| `toggleFavorite(id)` | `Boolean` | 切换收藏状态 |
| `addCustomPreset(preset)` | `Unit` | 添加自定义预设 |
| `updateCustomPreset(preset)` | `Unit` | 更新自定义预设 |
| `deleteCustomPreset(id)` | `Unit` | 删除自定义预设 |

#### ConfigCenter - 配置中心

**职责**: 统一管理所有应用配置

**管理配置**:
- 主题设置 (BrandTheme)
- 深色模式 (DarkMode)
- 应用语言 (AppLanguage)
- 震动反馈 (Boolean)
- 悬浮窗透明度/模式
- 订阅配置
- 更新渠道

**使用示例**:

```kotlin
// 获取实例
val config = ConfigCenter.getInstance(context)

// 读取配置
val theme = config.currentTheme
val opacity = config.floatingWindowOpacity

// 观察配置变化
config.themeFlow.collect { theme ->
    // 主题变化时自动触发
}

// 修改配置
config.currentTheme = BrandTheme.Fuji
config.floatingWindowOpacity = 60
```

#### FavoriteManager - 收藏管理器

**存储位置**: `SharedPreferences (omaster_prefs.xml)`

**核心功能**:
- 收藏ID集合管理
- 支持Flow观察变化
- App更新时数据保留

#### CustomPresetManager - 自定义预设管理器

**存储位置**: `SharedPreferences (omaster_custom_presets.xml)`

**核心功能**:
- 自定义预设CRUD操作
- 图片文件管理（内部存储）
- 数据持久化

### 3.3 UI层

#### MainActivity - 主Activity

**职责**:
- 应用入口
- 导航图配置 (NavHost)
- 全局状态管理
- 主题应用

**导航结构**:

```
Screen (Sealed Class)
├── Home              # 首页
├── Detail(presetId)  # 预设详情
├── PresetSelection   # 预设选择（创建时）
├── CreatePreset      # 创建预设
├── EditPreset        # 编辑预设
├── Settings          # 设置
├── Profile           # 个人中心
├── Discover          # 发现
├── ColorWalk         # 色彩漫步
├── PhotoFrame        # 照片相框
├── Subscription      # 订阅管理
├── PrivacyPolicy     # 隐私政策
└── OpenSourceLicense # 开源许可
```

#### HomeScreen - 首页

**功能**:
- 三Tab切换（全部/收藏/我的）
- 瀑布流展示预设卡片
- 下拉刷新
- 悬浮添加按钮

**组件**:
- `PresetGrid`: 预设网格
- `PresetCard`: 预设卡片
- `HomeTabRow`: Tab切换栏

#### FloatingWindowService - 悬浮窗服务

**功能**:
- 系统级悬浮窗显示
- 支持标准/紧凑两种模式
- 左右滑动切换预设
- 收起/展开动画
- 贴边吸附

**两种模式**:

| 模式 | 特点 | 适用场景 |
|------|------|----------|
| STANDARD | 完整参数展示 | 参数较多时 |
| COMPACT | 精简参数条 | 需要节省屏幕空间 |

### 3.4 网络层

#### PresetRemoteManager - 远程预设管理

**职责**:
- 从远程URL获取预设数据
- 保存到本地文件
- 更新订阅状态

**核心方法**:

```kotlin
// 获取预设列表
suspend fun fetchPresets(url: String): PresetList?

// 获取并保存
suspend fun fetchAndSave(
    context: Context, 
    url: String, 
    forceUpdate: Boolean = false
): Result<PresetList>
```

### 3.5 工具层

#### JsonUtil - JSON工具

**职责**:
- 从assets加载内置预设
- 从本地文件加载远程预设
- 缓存管理
- ID生成

**数据加载优先级**:
1. 已下载的订阅文件
2. Assets内置预设

#### UpdateChecker - 更新检查

**职责**:
- 检查新版本
- 下载APK
- 自动安装

---

## 4. 核心类与函数

### 4.1 关键类清单

| 类名 | 包路径 | 职责 | 重要性 |
|------|--------|------|--------|
| `MasterPreset` | `model` | 预设数据模型 | ⭐⭐⭐⭐⭐ |
| `PresetRepository` | `data.repository` | 数据仓库 | ⭐⭐⭐⭐⭐ |
| `ConfigCenter` | `data.config` | 配置中心 | ⭐⭐⭐⭐⭐ |
| `FavoriteManager` | `data.local` | 收藏管理 | ⭐⭐⭐⭐ |
| `CustomPresetManager` | `data.local` | 自定义预设管理 | ⭐⭐⭐⭐ |
| `JsonUtil` | `util` | JSON解析 | ⭐⭐⭐⭐ |
| `FloatingWindowService` | `ui.service` | 悬浮窗服务 | ⭐⭐⭐⭐ |
| `PresetRemoteManager` | `network` | 远程数据获取 | ⭐⭐⭐ |
| `MainActivity` | 根包 | 主Activity | ⭐⭐⭐⭐⭐ |
| `OMasterApplication` | 根包 | 应用入口 | ⭐⭐⭐⭐ |

### 4.2 关键函数详解

#### MasterPreset.getDisplaySections()

**功能**: 动态生成预设参数的展示分组

**逻辑**:
1. 如果存在sections字段，使用JSON定义的sections
2. 否则，根据旧版字段动态生成sections
3. 支持国际化字符串解析

**返回**: `List<PresetSection>`

#### PresetRepository.getAllPresets()

**功能**: 获取所有预设（默认+自定义）

**数据流**:
```
_defaultPresets (内置)
    ↓
combine ← customPresetsFlow (自定义)
    ↓
combine ← favoritesFlow (收藏)
    ↓
map → 标记收藏状态 + 排序（新预设置顶）
    ↓
Flow<List<MasterPreset>>
```

#### JsonUtil.loadPresets()

**功能**: 加载预设数据

**加载顺序**:
1. 检查内存缓存
2. 加载启用的订阅文件
3. 如果订阅文件不存在，加载assets内置预设
4. 处理并返回预设列表

#### FloatingWindowService.show()

**功能**: 显示悬浮窗

**参数**:
- `context`: Context
- `preset`: 要显示的预设
- `presetIndex`: 当前预设索引
- `presetIds`: 预设ID列表（用于切换）

---

## 5. 数据流与依赖关系

### 5.1 数据流向图

```
┌─────────────────────────────────────────────────────────────────┐
│                        数据来源                                 │
├─────────────────────────────────────────────────────────────────┤
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐  │
│  │  Assets      │  │  Remote URL  │  │  User Input          │  │
│  │ 内置预设     │  │  订阅预设    │  │  自定义预设          │  │
│  └──────┬───────┘  └──────┬───────┘  └──────────┬───────────┘  │
│         │                 │                     │              │
│         └─────────────────┼─────────────────────┘              │
│                           ▼                                    │
│                  ┌─────────────────┐                           │
│                  │   JsonUtil      │                           │
│                  │  (数据解析)      │                           │
│                  └────────┬────────┘                           │
│                           ▼                                    │
│                  ┌─────────────────┐                           │
│                  │PresetRepository │                           │
│                  │  (数据仓库)      │                           │
│                  └────────┬────────┘                           │
│                           ▼                                    │
│         ┌─────────────────┼─────────────────┐                  │
│         ▼                 ▼                 ▼                  │
│  ┌─────────────┐   ┌─────────────┐   ┌─────────────┐          │
│  │ ViewModel   │   │ FloatingWin │   │ Export/Share│          │
│  └──────┬──────┘   └─────────────┘   └─────────────┘          │
│         ▼                                                      │
│  ┌─────────────┐                                               │
│  │   UI层      │                                               │
│  └─────────────┘                                               │
└─────────────────────────────────────────────────────────────────┘
```

### 5.2 依赖关系图

```
MainActivity
├── OMasterApplication
├── ConfigCenter
├── PresetRepository
│   ├── FavoriteManager
│   ├── CustomPresetManager
│   └── JsonUtil
├── FloatingWindowController
│   └── FloatingWindowService
└── NavHost
    ├── HomeScreen
    │   └── HomeViewModel
    ├── DetailScreen
    │   └── DetailViewModel
    ├── UniversalCreatePresetScreen
    │   └── UniversalCreatePresetViewModel
    └── ...
```

### 5.3 存储结构

```
/data/data/com.silas.omaster/
├── shared_prefs/
│   ├── omaster_prefs.xml           # 收藏、隐私协议等
│   ├── omaster_custom_presets.xml  # 自定义预设
│   └── omaster_config_center.xml   # 应用配置
├── files/
│   ├── presets/                    # 自定义预设图片
│   └── subscription_*.json         # 订阅预设文件
└── cache/
    └── coil_cache/                 # 图片缓存
```

---

## 6. 项目运行方式

### 6.1 开发环境要求

| 工具 | 版本要求 |
|------|----------|
| Android Studio | 最新稳定版 |
| JDK | 17+ |
| Gradle | 8.0+ |
| Kotlin | 2.3.10 |
| Android SDK | API 33-36 |

### 6.2 构建命令

```bash
# 清理项目
./gradlew clean

# 构建Debug版本
./gradlew assembleDebug

# 构建Release版本
./gradlew assembleRelease

# 运行测试
./gradlew test

# 安装到设备
./gradlew installDebug
```

### 6.3 项目配置

#### build.gradle.kts (App级别)

```kotlin
android {
    namespace = "com.silas.omaster"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.silas.omaster"
        minSdk = 33
        targetSdk = 36
        versionCode = 12
        versionName = "1.5.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}
```

### 6.4 运行配置

1. **打开项目**: 使用 Android Studio 打开项目根目录
2. **同步Gradle**: 点击 "Sync Now" 同步依赖
3. **选择设备**: 连接 Android 13+ 设备或启动模拟器
4. **运行**: 点击运行按钮或按 Shift+F10

### 6.5 权限要求

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />  <!-- 悬浮窗 -->
<uses-permission android:name="android.permission.VIBRATE" />              <!-- 震动反馈 -->
<uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" /> <!-- 安装更新 -->
```

---

## 7. 技术栈

### 7.1 核心依赖

| 技术 | 版本 | 用途 |
|------|------|------|
| **Kotlin** | 2.3.10 | 开发语言 |
| **Jetpack Compose** | BOM 2026.03.01 | UI框架 |
| **Material Design 3** | 1.4.0 | 设计语言 |
| **Navigation Compose** | 2.9.7 | 导航组件 |
| **Kotlin Serialization** | 1.10.0 | 类型安全导航 |
| **Ktor Client** | 3.4.2 | HTTP客户端 |
| **Coil** | 2.7.0 | 图片加载 |
| **Gson** | 2.13.2 | JSON解析 |

### 7.2 架构组件

| 组件 | 用途 |
|------|------|
| **ViewModel** | 状态管理 |
| **StateFlow** | 响应式数据流 |
| **Repository Pattern** | 数据访问抽象 |
| **Service** | 悬浮窗后台服务 |

### 7.3 构建工具

| 工具 | 版本 | 用途 |
|------|------|------|
| **Android Gradle Plugin** | 9.1.0 | 构建系统 |
| **Gradle** | 8.x | 构建工具 |
| **Version Catalog** | - | 依赖版本管理 |

---

## 8. 开发规范

### 8.1 代码规范

- **命名**: 使用Kotlin命名规范，类名大驼峰，函数/变量小驼峰
- **注释**: 关键类和函数必须添加KDoc注释
- **架构**: 遵循MVVM架构，UI层不直接访问数据层
- **异步**: 使用Kotlin Coroutines和Flow处理异步操作

### 8.2 提交规范

使用 [Conventional Commits](https://www.conventionalcommits.org/) 规范:

```
<type>(<scope>): <subject>

<body>

<footer>
```

Type类型:
- `feat`: 新功能
- `fix`: 修复
- `docs`: 文档
- `style`: 格式
- `refactor`: 重构
- `test`: 测试
- `chore`: 构建/工具

### 8.3 版本规范

```
主版本号.次版本号.修订号[-预发布标识]

示例:
- 1.0.0        # 正式版
- 1.0.0-beta1  # 测试版
- 1.1.0        # 功能更新
- 1.1.1        # 修复更新
```

### 8.4 文件组织

- 每个类单独文件
- 相关类可放在同一文件（如密封类）
- 包结构按功能模块划分
- 工具类放在 `util` 包

---

## 附录

### A. 预设数据结构

```json
{
  "version": 2,
  "name": "预设库名称",
  "author": "作者",
  "build": 6,
  "presets": [
    {
      "name": "预设名称",
      "coverPath": "图片路径",
      "galleryImages": ["图片1", "图片2"],
      "author": "@作者",
      "isNew": true,
      "sections": [
        {
          "title": "分组标题",
          "items": [
            {
              "label": "参数名",
              "value": "参数值",
              "span": 1
            }
          ]
        }
      ],
      "tags": ["Auto"],
      "description": {
        "title": "提示标题",
        "content": "拍摄建议内容"
      }
    }
  ]
}
```

### B. 主题配置

```kotlin
enum class BrandTheme(
    val id: String,
    val brandNameResId: Int,
    val colorNameResId: Int,
    val primaryColor: Color,
    val hexCode: String
) {
    Hasselblad("hasselblad", ..., Color(0xFFFF6600), "#FF6600"),
    Zeiss("zeiss", ..., Color(0xFF005A9C), "#005A9C"),
    Leica("leica", ..., Color(0xFFCC0000), "#CC0000"),
    // ... 更多主题
}
```

### C. 相关链接

- **项目主页**: https://github.com/iCurrer/OMaster
- **社区仓库**: https://github.com/fengyec2/OMaster-Community
- **开源协议**: CC BY-NC-SA 4.0

---

*文档版本: 1.0.0*  
*最后更新: 2026-05-06*
