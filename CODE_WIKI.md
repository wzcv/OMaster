# OMaster - Code Wiki

> **项目简介**: OMaster 是一款为各品牌手机（OPPO/一加/Realme/哈苏/蔡司/徕卡/富士等）打造的摄影调色参数管理工具，帮助用户轻松管理和使用各种专业摄影预设。

---

## 目录

1. [项目概述](#1-项目概述)
2. [技术栈](#2-技术栈)
3. [项目架构](#3-项目架构)
4. [模块详解](#4-模块详解)
5. [核心类与函数](#5-核心类与函数)
6. [数据流与依赖关系](#6-数据流与依赖关系)
7. [项目运行方式](#7-项目运行方式)
8. [构建配置](#8-构建配置)
9. [开发规范](#9-开发规范)

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
| **编译SDK** | API 36 |
| **版本号** | 1.5.0 (versionCode: 12) |
| **UI框架** | Jetpack Compose |
| **架构模式** | MVVM + Repository |
| **开源协议** | CC BY-NC-SA 4.0 |
| **Java版本** | Java 11 |

### 1.2 核心功能

- **丰富的预设库** — 23+ 款专业预设，涵盖胶片、复古、清新、黑白、美食等多种风格
- **多品牌主题** — 支持哈苏、蔡司、徕卡、理光、富士、佳能、尼康、索尼、Phase One 共9种品牌主题配色
- **配置云更新** — 支持从云端获取最新配置，支持自定义订阅源
- **收藏管理** — 一键收藏喜欢的预设，本地存储无需网络
- **自定义预设** — 支持创建、编辑和删除自定义预设
- **悬浮窗模式** — 拍照时可悬浮显示参数，支持标准/紧凑两种模式，左右滑动切换预设
- **多语言支持** — 支持中文和英文
- **照片相框** — 支持图片边框和主色调提取
- **色彩漫步** — 色彩卡片浏览功能
- **自动更新** — 支持 GitHub/Gitee 双渠道检查更新，应用内下载安装

---

## 2. 技术栈

### 2.1 核心依赖

| 技术 | 版本 | 用途 |
|------|------|------|
| **Kotlin** | 2.3.10 | 开发语言 |
| **Jetpack Compose** | BOM 2026.03.01 | UI框架 |
| **Material Design 3** | 1.4.0 | 设计语言 |
| **Navigation Compose** | 2.9.7 | 导航组件 |
| **Kotlin Serialization** | 1.10.0 | 类型安全导航、JSON序列化 |
| **Ktor Client** | 3.4.2 | HTTP客户端 |
| **Coil** | 2.7.0 | 图片加载 |
| **Gson** | 2.13.2 | JSON解析 |
| **友盟统计** | 9.8.9 / 1.8.7.2 | 数据分析（需用户同意） |
| **AndroidX Palette** | 1.0.0 | 图片主色调提取 |
| **AndroidX ExifInterface** | 1.4.1 | EXIF信息读取 |

### 2.2 构建工具

| 工具 | 版本 |
|------|------|
| **Android Gradle Plugin** | 9.1.0 |
| **Gradle** | 8.x |
| **Version Catalog** | libs.versions.toml |

### 2.3 架构组件

| 组件 | 用途 |
|------|------|
| **ViewModel** | 页面状态管理 |
| **StateFlow** | 响应式数据流 |
| **Repository Pattern** | 数据访问抽象层 |
| **Service** | 悬浮窗后台服务 |
| **BroadcastReceiver** | 下载完成监听、预设切换广播 |
| **DownloadManager** | 应用内更新下载 |

---

## 3. 项目架构

### 3.1 整体架构图

```
┌──────────────────────────────────────────────────────────────────────┐
│                         Presentation Layer                            │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌────────────┐ │
│  │HomeScreen│ │DetailScr │ │CreateScr │ │Settings  │ │Discover    │ │
│  └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘ └─────┬──────┘ │
│       │            │            │            │             │        │
│  ┌────┴────────────┴────────────┴────────────┴─────────────┴──────┐ │
│  │                      UI Components                              │ │
│  │  PresetCard │ PillNavBar │ ImageGallery │ WelcomeDialog │ ...  │ │
│  └───────────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌──────────────────────────────────────────────────────────────────────┐
│                         ViewModel Layer                               │
│  ┌──────────────┐ ┌──────────────────────────┐ ┌──────────────────┐ │
│  │HomeViewModel │ │UniversalCreatePresetVM   │ │FloatingWindowCtrl│ │
│  └──────┬───────┘ └──────────────────────────┘ └────────┬─────────┘ │
│         │                                               │           │
└─────────┼───────────────────────────────────────────────┼───────────┘
          │                                               │
          ▼                                               ▼
┌──────────────────────────────────────────────────────────────────────┐
│                         Data Layer                                    │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                    PresetRepository (单例)                     │   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌────────────────────┐  │   │
│  │  │FavoriteMgr   │  │CustomPreset  │  │  JsonUtil          │  │   │
│  │  │(SharedPref)  │  │Manager       │  │(Assets/Sub文件)    │  │   │
│  │  └──────────────┘  └──────────────┘  └────────────────────┘  │   │
│  └──────────────────────────────────────────────────────────────┘   │
│  ┌──────────────────┐  ┌─────────────────┐  ┌──────────────────┐   │
│  │  ConfigCenter    │  │SubscriptionCfg  │  │PresetRemoteManager│  │
│  │  (所有配置入口)   │  │ (订阅管理子模块)  │  │ (Ktor HTTP)       │  │
│  └──────────────────┘  └─────────────────┘  └──────────────────┘   │
└──────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌──────────────────────────────────────────────────────────────────────┐
│                        Core / Utils Layer                              │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌────────────┐ │
│  │Logger    │ │PresetI18n│ │HapticExt │ │ImageUtil │ │UpdateCheck │ │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └────────────┘ │
└──────────────────────────────────────────────────────────────────────┘
```

### 3.2 目录结构

```
app/src/main/
├── AndroidManifest.xml                 # 清单文件（权限、组件声明）
├── assets/
│   ├── presets.json                    # 内置预设数据
│   └── iconfont.ttf                    # 图标字体
├── java/com/silas/omaster/
│   ├── MainActivity.kt                 # 主Activity，导航入口，欢迎流程
│   ├── OMasterApplication.kt           # Application类，全局初始化
│   │
│   ├── model/                          # 数据模型层
│   │   ├── MasterPreset.kt            # 预设数据模型（核心）
│   │   ├── ColorCard.kt               # 色彩卡片模型
│   │   └── Subscription.kt            # 订阅配置模型
│   │
│   ├── data/                          # 数据层
│   │   ├── config/                    # 配置管理
│   │   │   ├── ConfigCenter.kt        # 配置中心（主题、语言等）
│   │   │   ├── ConfigMigration.kt     # 配置迁移
│   │   │   └── SubscriptionConfig.kt  # 订阅配置子模块
│   │   ├── local/                     # 本地数据管理
│   │   │   ├── FavoriteManager.kt     # 收藏管理
│   │   │   ├── CustomPresetManager.kt # 自定义预设管理
│   │   │   ├── FloatingWindowMode.kt  # 悬浮窗模式枚举
│   │   │   ├── DarkMode.kt            # 深色模式枚举
│   │   │   ├── AppLanguage.kt         # 语言枚举
│   │   │   └── UpdateChannel.kt       # 更新渠道枚举
│   │   ├── repository/                # 数据仓库
│   │   │   └── PresetRepository.kt    # 预设数据仓库
│   │   └── ColorCardLibrary.kt        # 色彩卡片库
│   │
│   ├── network/                       # 网络层
│   │   └── PresetRemoteManager.kt     # 远程预设获取（Ktor）
│   │
│   ├── ui/                            # UI层
│   │   ├── home/                      # 首页
│   │   │   ├── HomeScreen.kt          # 首页界面
│   │   │   └── HomeViewModel.kt       # 首页ViewModel
│   │   ├── detail/                    # 详情与个人中心
│   │   │   ├── DetailScreen.kt        # 预设详情页
│   │   │   ├── ProfileScreen.kt       # 个人中心（关于页）
│   │   │   ├── OpenSourceLicenseScreen.kt
│   │   │   └── PrivacyPolicyScreen.kt
│   │   ├── create/                    # 创建/编辑预设
│   │   │   ├── UniversalCreatePresetScreen.kt
│   │   │   ├── UniversalCreatePresetViewModel.kt
│   │   │   └── PresetSelectionScreen.kt
│   │   ├── discover/                  # 发现页
│   │   │   ├── DiscoverScreen.kt
│   │   │   ├── ColorWalkScreen.kt
│   │   └── frame/                     # 照片相框
│   │   │   └── PhotoFrameScreen.kt
│   │   ├── subscription/              # 订阅管理
│   │   │   └── SubscriptionScreen.kt
│   │   ├── settings/                  # 设置页
│   │   │   └── SettingsScreen.kt
│   │   ├── service/                   # 悬浮窗服务
│   │   │   ├── FloatingWindowService.kt  # 系统悬浮窗Service
│   │   │   └── FloatingWindowController.kt  # 悬浮窗控制器
│   │   ├── components/                # 通用UI组件
│   │   │   ├── PresetCard.kt
│   │   │   ├── PillNavBar.kt
│   │   │   ├── ImageGallery.kt
│   │   │   └── ...
│   │   ├── theme/                     # 主题配置
│   │   │   ├── Theme.kt               # Compose主题
│   │   │   ├── AppTheme.kt            # 品牌主题枚举
│   │   │   ├── Color.kt               # 颜色定义
│   │   │   └── Typography.kt          # 字体定义
│   │   └── animation/                 # 动画配置
│   │       ├── AnimationSpecs.kt
│   │       └── ...
│   │
│   └── util/                          # 工具类
│       ├── JsonUtil.kt                # JSON解析与预设加载
│       ├── PresetI18n.kt              # 国际化本地化
│       ├── UpdateChecker.kt           # 应用更新检查
│       ├── Logger.kt                  # 日志管理器
│       ├── LanguageUtil.kt            # 语言切换
│       ├── HapticSettings.kt          # 震动设置
│       ├── VersionInfo.kt             # 版本信息
│       ├── IconFont.kt                # 图标字体
│       └── ...
├── res/                               # 资源文件
│   ├── values/strings.xml             # 中文字符串
│   ├── values-en/strings.xml          # 英文字符串
│   ├── values/colors.xml
│   └── ...
└── ic_launcher-playstore.png          # 应用图标
```

---

## 4. 模块详解

### 4.1 模型层 (Model)

#### MasterPreset — 核心预设模型

**文件**: [MasterPreset.kt](file:///workspace/app/src/main/java/com/silas/omaster/model/MasterPreset.kt)

```kotlin
@Serializable
data class MasterPreset(
    val id: String? = null,                    // 唯一标识符（UUID或生成ID）
    val name: String,                          // 预设名称
    val coverPath: String,                     // 封面图片路径
    val galleryImages: List<String>? = null,   // 详情页样片列表
    val author: String = "@OPPO影像",           // 作者
    val mode: String? = null,                  // 模式: "auto" / "pro"
    val filter: String? = null,                // 滤镜类型
    val whiteBalance: String? = null,          // 白平衡
    val colorTone: String? = null,             // 色调
    val exposureCompensation: String? = null,  // 曝光补偿
    val colorTemperature: Int? = null,         // 色温(2000-8000)
    val colorHue: Int? = null,                 // 色调(-150~150)
    val iso: String? = null,                   // ISO感光度
    val shutterSpeed: String? = null,          // 快门速度
    val softLight: String? = null,             // 柔光强度
    val tone: Int? = null,                     // 影调(-100~+100)
    val saturation: Int? = null,               // 饱和度(-100~+100)
    val warmCool: Int? = null,                 // 冷暖(-100~+100)
    val cyanMagenta: Int? = null,              // 青品(-100~+100)
    val sharpness: Int? = null,                // 锐度(0-100)
    val vignette: String? = null,              // 暗角(开/关)
    val isFavorite: Boolean = false,           // 是否收藏（运行时状态）
    val isCustom: Boolean = false,             // 是否自定义（运行时状态）
    val isNew: Boolean = false,                // 是否新预设
    val description: PresetDescription? = null,// 描述信息
    val shootingTips: String? = null,          // 拍摄建议（已废弃，兼容用）
    val sections: List<PresetSection>? = null, // 动态参数分组
    val tags: List<String>? = emptyList()      // 标签列表
) : Parcelable
```

**关键方法**:

- `getDisplaySections(context)`: 动态生成预设参数展示分组，支持新版JSON sections和旧版硬编码字段兼容
- `allImages`: 计算属性，返回封面+图库的完整图片列表

**辅助数据类**:

- `PresetDescription`: 描述信息（title + content）
- `PresetItem`: 单个参数项（label + value + span）
- `PresetSection`: 参数分组（title + items）
- `PresetList`: 预设列表包装类（name, author, build, version, presets）

#### BrandTheme — 品牌主题枚举

**文件**: [AppTheme.kt](file:///workspace/app/src/main/java/com/silas/omaster/ui/theme/AppTheme.kt)

| 主题 | ID | 主色 | 色值 |
|------|-----|------|------|
| Hasselblad | hasselblad | 橙色 | `#FF6600` |
| Zeiss | zeiss | 蓝色 | `#005A9C` |
| Leica | leica | 红色 | `#CC0000` |
| Ricoh | ricoh | 绿色 | `#00A95C` |
| Fujifilm | fujifilm | 绿色 | `#009B3A` |
| Canon | canon | 红色 | `#CC0000` |
| Nikon | nikon | 黄色 | `#FFC20E` |
| Sony | sony | 橙色 | `#F15A24` |
| PhaseOne | phaseone | 灰色 | `#5A5A5A` |

#### Subscription — 订阅模型

**文件**: [Subscription.kt](file:///workspace/app/src/main/java/com/silas/omaster/model/Subscription.kt)

```kotlin
@Serializable
data class Subscription(
    val url: String,                // 订阅URL
    val name: String = "",          // 订阅名称
    val author: String = "",        // 作者
    val build: Int = 1,             // 构建版本号
    val isEnabled: Boolean = true,  // 是否启用
    val presetCount: Int = 0,       // 预设数量
    val lastUpdateTime: Long = 0    // 最后更新时间
)
```

### 4.2 数据层 (Data)

#### PresetRepository — 预设数据仓库

**文件**: [PresetRepository.kt](file:///workspace/app/src/main/java/com/silas/omaster/data/repository/PresetRepository.kt)

**职责**: 统一管理默认预设、自定义预设和收藏数据的访问，作为数据层的统一入口。

**数据源协调**:
1. 内置预设 — 来自 `assets/presets.json`（通过 `JsonUtil`）
2. 自定义预设 — 来自 `SharedPreferences`（通过 `CustomPresetManager`）
3. 收藏数据 — 来自 `SharedPreferences`（通过 `FavoriteManager`）

**核心方法**:

| 方法 | 返回类型 | 说明 |
|------|----------|------|
| `getAllPresets()` | `Flow<List<MasterPreset>>` | 获取全部预设（内置+自定义），标记收藏状态，新预设置顶 |
| `getDefaultPresets()` | `Flow<List<MasterPreset>>` | 仅获取内置预设 |
| `getCustomPresets()` | `Flow<List<MasterPreset>>` | 仅获取自定义预设 |
| `getFavoritePresets()` | `Flow<List<MasterPreset>>` | 获取收藏的预设 |
| `getPresetById(id)` | `MasterPreset?` | 按ID查找预设 |
| `getPresetByName(name)` | `MasterPreset?` | 按名称查找预设 |
| `toggleFavorite(id)` | `Boolean` | 切换收藏状态 |
| `addCustomPreset(preset)` | `Unit` | 添加自定义预设 |
| `updateCustomPreset(preset)` | `Unit` | 更新自定义预设 |
| `deleteCustomPreset(id)` | `Unit` | 删除自定义预设（级联删除收藏和图片文件） |
| `reloadDefaultPresets()` | `Unit` | 重新加载内置预设（清除缓存后重载） |

**设计亮点**: 使用 `combine` 合并多个Flow实现响应式数据合并，订阅状态变化时自动触发重载。

#### ConfigCenter — 配置管理中心

**文件**: [ConfigCenter.kt](file:///workspace/app/src/main/java/com/silas/omaster/data/config/ConfigCenter.kt)

**职责**: 统一管理所有应用配置的访问入口，提供同步读写和Flow监听两种方式。

**管理的配置**:

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `currentTheme` | `BrandTheme` | Hasselblad | 品牌主题 |
| `darkMode` | `DarkMode` | SYSTEM | 深色模式(LIGHT/DARK/SYSTEM) |
| `appLanguage` | `AppLanguage` | SYSTEM | 应用语言 |
| `isVibrationEnabled` | `Boolean` | true | 震动反馈 |
| `floatingWindowOpacity` | `Int` | 56 | 悬浮窗透明度(30-70) |
| `floatingWindowMode` | `FloatingWindowMode` | COMPACT | 悬浮窗模式 |
| `defaultStartTab` | `Int` | 0 | 默认启动Tab |
| `isAnalyticsEnabled` | `Boolean` | true | 统计开关 |
| `isPremiumGlassEnabled` | `Boolean` | false | 高级Glass效果 |
| `updateChannel` | `UpdateChannel` | GITEE | 更新渠道 |
| `isAutoCheckUpdateEnabled` | `Boolean` | true | 自动检查更新 |
| `subscriptionsFlow` | `Flow<List<Subscription>>` | — | 订阅列表 |

**组合Flow**:

- `floatingWindowConfigFlow`: 悬浮窗配置（透明度 + 模式）
- `uiConfigFlow`: UI配置（主题 + 语言 + 深色模式）
- `userPreferencesFlow`: 用户偏好（震动 + 统计 + 启动Tab）
- `systemConfigFlow`: 系统配置（更新渠道 + 默认订阅URL）

**存储位置**: `SharedPreferences (omaster_config_center.xml)`

#### FavoriteManager — 收藏管理器

**文件**: [FavoriteManager.kt](file:///workspace/app/src/main/java/com/silas/omaster/data/local/FavoriteManager.kt)

**职责**: 管理预设收藏状态，使用 `Set<String>` 存储收藏的预设ID集合。

**存储位置**: `SharedPreferences (omaster_prefs.xml)`，Key: `favorite_presets`

#### CustomPresetManager — 自定义预设管理器

**文件**: [CustomPresetManager.kt](file:///workspace/app/src/main/java/com/silas/omaster/data/local/CustomPresetManager.kt)

**职责**: 管理用户创建的自定义预设，支持CRUD操作和图片文件管理。

**存储位置**: `SharedPreferences (omaster_custom_presets.xml)`，Key: `custom_presets`

**存储特点**:
- 预设数据以JSON字符串存储（使用Gson序列化）
- 图片文件存储在内部存储 `files/` 目录
- 删除预设时自动清理关联图片文件
- 使用UUID作为自定义预设ID

#### SubscriptionConfig — 订阅配置子模块

**文件**: [SubscriptionConfig.kt](file:///workspace/app/src/main/java/com/silas/omaster/data/config/SubscriptionConfig.kt)

**职责**: 管理订阅列表的持久化和状态，作为 `ConfigCenter` 的子模块。

**默认订阅源**:

| 名称 | URL | 默认启用 |
|------|-----|----------|
| OPPO/一加大师预设 | `cdn.jsdelivr.net/gh/fengyec2/OMaster-Community@main/presets/v2/oppo.json` | 是 |
| Realme GR预设 | `cdn.jsdelivr.net/gh/fengyec2/OMaster-Community@main/presets/v2/realme.json` | 否 |

**存储**: `SharedPreferences (omaster_config_subscriptions.xml)`，JSON格式序列化

### 4.3 UI层

#### MainActivity — 主Activity

**文件**: [MainActivity.kt](file:///workspace/app/src/main/java/com/silas/omaster/MainActivity.kt)

**职责**:
- 应用入口点
- 管理欢迎流程（隐私政策同意）
- 配置NavHost导航图
- 管理底部导航栏（PillNavBar）
- 自动检查更新弹窗
- 数据迁移对话框

**导航路由** (`sealed class Screen`):

| 路由 | 说明 |
|------|------|
| `Home` | 首页（三Tab：全部/收藏/我的） |
| `Detail(presetId)` | 预设详情页 |
| `PresetSelection` | 预设选择（创建自定义预设时） |
| `CreatePreset(templateId?)` | 创建预设 |
| `EditPreset(presetId)` | 编辑预设 |
| `Settings` | 设置页 |
| `About` | 个人中心/关于 |
| `Discover` | 发现页 |
| `ColorWalk` | 色彩漫步 |
| `PhotoFrame` | 照片相框 |
| `Subscription` | 订阅管理 |
| `PrivacyPolicy` | 隐私政策 |
| `OpenSourceLicense` | 开源许可 |

**导航动画**: 根据底部Tab索引自动判断滑动方向（左/右），实现自然的页面切换动画。

#### HomeScreen — 首页

**文件**: [HomeScreen.kt](file:///workspace/app/src/main/java/com/silas/omaster/ui/home/HomeScreen.kt)

**功能**:
- 三Tab切换（全部预设 / 收藏 / 自定义）
- 瀑布流网格展示（`LazyVerticalStaggeredGrid`，2列）
- 下拉刷新（Pull-to-refresh，触发订阅更新）
- 悬浮添加按钮（仅在"我的"Tab显示）
- 交错入场动画
- 删除确认对话框
- 滚动状态上报（控制底部导航栏显示/隐藏）

#### FloatingWindowService — 悬浮窗服务

**文件**: [FloatingWindowService.kt](file:///workspace/app/src/main/java/com/silas/omaster/ui/service/FloatingWindowService.kt)

**职责**: 实现系统级悬浮窗，在相机应用上方显示摄影参数。

**三种视图模式**:

| 模式 | 说明 | 特点 |
|------|------|------|
| EXPANDED | 展开模式 | 完整参数展示，带分组标题 |
| COMPACT | 紧凑模式 | 参数条样式，固定宽度，适合横屏拍摄 |
| COLLAPSED | 收起模式 | 圆形悬浮球，仅显示App图标 |

**核心功能**:
- 拖拽移动 + 自动贴边吸附（带动画）
- 左右切换预设（通过BroadcastReceiver通信）
- 内容热更新（切换预设时无闪动）
- 透明度动态调整（跟随设置）
- Realme预设特殊处理（强制使用标准模式）

**关键设计**: 使用 `ACTION_UPDATE` 和 `ACTION_SHOW` 区分更新和创建，避免不必要的窗口重建导致闪烁。

#### FloatingWindowController — 悬浮窗控制器

**文件**: [FloatingWindowController.kt](file:///workspace/app/src/main/java/com/silas/omaster/ui/service/FloatingWindowController.kt)

**职责**: 在MainActivity中注册广播接收器，管理悬浮窗的预设切换逻辑。

**工作方式**:
1. 注册接收 `ACTION_SWITCH_PRESET` 广播
2. 维护当前预设列表和索引
3. 收到切换广播后调用 `FloatingWindowService.update()` 更新内容

### 4.4 网络层

#### PresetRemoteManager — 远程预设管理

**文件**: [PresetRemoteManager.kt](file:///workspace/app/src/main/java/com/silas/omaster/network/PresetRemoteManager.kt)

**职责**: 从远程URL获取预设数据并保存到本地。

**HttpClient配置**:
```kotlin
HttpClient(CIO) {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
}
```

**核心方法**:

| 方法 | 说明 |
|------|------|
| `fetchPresets(url)` | 从URL获取预设列表 |
| `fetchAndSave(context, url, forceUpdate)` | 获取并保存到本地，支持版本号比较 |

**更新逻辑**:
1. 从远程获取JSON数据
2. 验证JSON格式和必填字段（name、author）
3. 比较build版本号，如果相同且非强制更新则跳过
4. 保存到 `files/sub_<hash>.json`
5. 更新订阅状态（预设数量、更新时间）
6. 清除JsonUtil缓存

### 4.5 工具层

#### JsonUtil — JSON解析工具

**文件**: [JsonUtil.kt](file:///workspace/app/src/main/java/com/silas/omaster/util/JsonUtil.kt)

**职责**: 从assets和订阅文件加载预设数据。

**加载流程**:
1. 检查内存缓存
2. 检测旧版远程文件迁移状态
3. 遍历所有已启用的订阅
   - 如果订阅文件存在，加载本地文件
   - 如果是官方订阅且文件不存在，加载assets内置预设
4. 处理预设：过滤占位符、生成/规范化ID
5. 缓存结果并返回

**ID生成算法**:
```
"富士胶片" -> Normalizer.normalize -> "富士胶片" -> lowercase -> "富士胶片"
-> replace non-alphanumeric -> "" -> fallback to preset index
```
实际流程：移除音调符号 → 转小写 → 替换非字母数字为下划线 → 截断到30字符 → 添加索引后缀

#### PresetI18n — 预设国际化

**文件**: [PresetI18n.kt](file:///workspace/app/src/main/java/com/silas/omaster/util/PresetI18n.kt)

**职责**: 将预设数据中的中文字符串转换为当前语言。

**支持的本地化内容**:
- 滤镜名称（标准、霓虹、复古等18种）
- 柔光强度（无、柔美、梦幻、朦胧）
- 暗角（开/关）
- 预设名称（23+种）
- 白平衡（阴天、日光等）
- 拍摄建议
- `@string/xxx` 资源引用解析

#### Logger — 日志管理器

**文件**: [Logger.kt](file:///workspace/app/src/main/java/com/silas/omaster/util/Logger.kt)

**功能**:
- 同时输出到Logcat和本地文件
- 日志轮转（最大5MB，保留3个备份）
- 启动时自动记录版本、设备信息

**日志路径**: `files/logs/app.log`

#### UpdateChecker — 更新检查

**文件**: [UpdateChecker.kt](file:///workspace/app/src/main/java/com/silas/omaster/util/UpdateChecker.kt)

**功能**:
- 支持GitHub和Gitee双渠道检查更新
- 使用DownloadManager下载APK
- 下载完成后通过FileProvider安装

**API端点**:
- GitHub: `api.github.com/repos/iCurrer/OMaster/releases/latest`
- Gitee: `gitee.com/api/v5/repos/qiublog/OMaster/releases/latest`

---

## 5. 核心类与函数

### 5.1 关键类清单

| 类名 | 包路径 | 职责 | 重要性 |
|------|--------|------|--------|
| `MasterPreset` | model | 预设数据模型，包含所有摄影参数 | ⭐⭐⭐⭐⭐ |
| `BrandTheme` | ui.theme | 9种品牌主题配色枚举 | ⭐⭐⭐⭐⭐ |
| `PresetRepository` | data.repository | 统一数据访问入口 | ⭐⭐⭐⭐⭐ |
| `ConfigCenter` | data.config | 所有应用配置的统一入口 | ⭐⭐⭐⭐⭐ |
| `SubscriptionConfig` | data.config | 订阅配置管理子模块 | ⭐⭐⭐⭐ |
| `FavoriteManager` | data.local | 收藏状态管理 | ⭐⭐⭐⭐ |
| `CustomPresetManager` | data.local | 自定义预设CRUD | ⭐⭐⭐⭐ |
| `JsonUtil` | util | JSON解析与预设加载 | ⭐⭐⭐⭐ |
| `PresetI18n` | util | 国际化本地化 | ⭐⭐⭐⭐ |
| `FloatingWindowService` | ui.service | 系统级悬浮窗服务 | ⭐⭐⭐⭐ |
| `FloatingWindowController` | ui.service | 悬浮窗预设切换控制 | ⭐⭐⭐⭐ |
| `PresetRemoteManager` | network | 远程预设获取与保存 | ⭐⭐⭐ |
| `UpdateChecker` | util | 应用内更新检查与安装 | ⭐⭐⭐ |
| `Logger` | util | 日志管理器 | ⭐⭐⭐ |
| `MainActivity` | 根包 | 应用入口、导航管理 | ⭐⭐⭐⭐⭐ |
| `OMasterApplication` | 根包 | 全局初始化 | ⭐⭐⭐⭐ |
| `HomeViewModel` | ui.home | 首页状态管理 | ⭐⭐⭐⭐ |

### 5.2 关键函数详解

#### MasterPreset.getDisplaySections()

**文件**: [MasterPreset.kt#L226](file:///workspace/app/src/main/java/com/silas/omaster/model/MasterPreset.kt#L226-L350)

**功能**: 动态生成预设参数的展示分组，兼容新旧两种数据格式。

**逻辑**:
1. 如果 `sections` 字段存在，使用JSON定义的sections并通过 `PresetI18n` 本地化
2. 否则，根据旧版硬编码字段动态生成sections：
   - Pro模式参数组（ISO、快门、曝光补偿、色温、色调）
   - 调色参数组（滤镜、柔光、影调、饱和度、冷暖、青品、锐度、暗角）

#### PresetRepository.getAllPresets()

**文件**: [PresetRepository.kt#L127](file:///workspace/app/src/main/java/com/silas/omaster/data/repository/PresetRepository.kt#L127-L138)

**数据合并流程**:
```
_defaultPresets (内置预设Flow)
       ↓
   combine ← customPresetsFlow (自定义预设Flow)
       ↓
   combine ← favoritesFlow (收藏ID集合Flow)
       ↓
   map → 标记每个预设的 isFavorite 状态
       ↓
   sortedByDescending → 新预设(isNew=true)置顶
       ↓
Flow<List<MasterPreset>>
```

#### HomeViewModel.refresh()

**文件**: [HomeViewModel.kt](file:///workspace/app/src/main/java/com/silas/omaster/ui/home/HomeViewModel.kt)

**功能**: 触发所有启用订阅的云更新。

**流程**:
1. 遍历所有已启用的订阅
2. 调用 `PresetRemoteManager.fetchAndSave()` 获取并保存
3. 统计成功/无需更新/失败数量
4. 调用 `repository.reloadDefaultPresets()` 重新加载
5. 重新收集数据流
6. 返回 `RefreshResult` 结果对象

---

## 6. 数据流与依赖关系

### 6.1 数据流向

```
预设数据来源
    │
    ├── assets/presets.json ──────────────────────────────────┐
    ├── 远程订阅文件 (files/sub_<hash>.json)                   │
    │       ↑                                                   │
    │       │ PresetRemoteManager.fetchAndSave()               │
    │       │                                                   │
    ├── 用户自定义预设 ─────────────────────────────────────┐   │
    │       ↑                                               │   │
    │       │ CustomPresetManager.add/update                 │   │
    │                                                       │   │
    ▼                                                       ▼   ▼
┌─────────────────────────────────────────────────────────────────┐
│                        JsonUtil                                  │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ 1. 检查内存缓存                                           │   │
│  │ 2. 遍历已启用订阅 → 加载本地文件 / assets                 │   │
│  │ 3. 处理预设（过滤、ID规范化）                             │   │
│  │ 4. 缓存结果                                               │   │
│  └─────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                     PresetRepository                             │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ getAllPresets() = combine(内置 + 自定义 + 收藏)           │   │
│  │ → 标记收藏状态 → 排序 → Flow<List<MasterPreset>>         │   │
│  └─────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                              │
                    ┌─────────┴─────────┐
                    ▼                   ▼
┌────────────────────────┐  ┌────────────────────────┐
│    HomeViewModel        │  │  DetailViewModel        │
│  collect Flow → State  │  │  collect Flow → State  │
└────────────────────────┘  └────────────────────────┘
                    │                   │
                    ▼                   ▼
┌────────────────────────┐  ┌────────────────────────┐
│     HomeScreen          │  │    DetailScreen         │
│  LazyVerticalStaggered  │  │  参数展示 + 样片轮播    │
│  Grid (2列瀑布流)       │  │                         │
└────────────────────────┘  └────────────────────────┘
```

### 6.2 模块依赖关系

```
MainActivity
├── OMasterApplication (全局初始化)
│   ├── Logger (日志系统)
│   ├── HapticSettings (震动设置)
│   └── UMeng (友盟统计，需用户同意)
│
├── ConfigCenter (配置中心)
│   ├── SubscriptionConfig (订阅子模块)
│   └── ConfigMigration (数据迁移)
│
├── PresetRepository (数据仓库)
│   ├── FavoriteManager (收藏管理)
│   ├── CustomPresetManager (自定义预设)
│   └── JsonUtil (JSON解析)
│       └── PresetRemoteManager (远程获取)
│           └── Ktor HttpClient
│
├── FloatingWindowController (悬浮窗控制)
│   └── FloatingWindowService (系统悬浮窗)
│
├── NavHost (导航)
│   ├── HomeScreen → HomeViewModel
│   ├── DetailScreen
│   ├── UniversalCreatePresetScreen → UniversalCreatePresetViewModel
│   ├── DiscoverScreen
│   ├── SettingsScreen
│   ├── ProfileScreen
│   ├── SubscriptionScreen
│   ├── ColorWalkScreen
│   └── PhotoFrameScreen
│
├── OMasterTheme (主题)
│   └── BrandTheme (9种品牌配色)
│
└── UpdateChecker (应用更新)
    └── DownloadCompleteReceiver (下载完成广播)
```

### 6.3 存储结构

```
/data/data/com.silas.omaster/
├── shared_prefs/
│   ├── omaster_prefs.xml                  # 收藏、隐私同意、震动设置
│   ├── omaster_custom_presets.xml         # 自定义预设(JSON序列化)
│   ├── omaster_config_center.xml          # 应用配置（主题、语言等）
│   ├── omaster_config_subscriptions.xml   # 订阅列表(JSON序列化)
│   └── json_util_prefs.xml               # 迁移状态标记
│
├── files/
│   ├── sub_<hash>.json                    # 下载的订阅预设文件
│   ├── presets/                           # 自定义预设的图片文件
│   │   ├── cover_<uuid>.jpg
│   │   └── gallery_<uuid>.jpg
│   └── logs/
│       ├── app.log                        # 当前日志
│       ├── app.log.1                      # 备份日志
│       └── ...
│
├── cache/
│   └── coil_cache/                        # Coil图片缓存
│
└── assets/
    ├── presets.json                       # 内置预设数据
    └── iconfont.ttf                       # 图标字体
```

---

## 7. 项目运行方式

### 7.1 开发环境要求

| 工具 | 版本要求 |
|------|----------|
| Android Studio | 最新稳定版 |
| JDK | 11 |
| Gradle | 8.0+ |
| Kotlin | 2.3.10 |
| Android SDK | API 33-36 |

### 7.2 构建命令

```bash
# 清理项目
./gradlew clean

# 构建Debug版本
./gradlew assembleDebug

# 构建Release版本（启用混淆和资源压缩）
./gradlew assembleRelease

# 按ABI拆分构建
./gradlew assembleRelease
# 输出: app-armeabi-v7a-release.apk, app-arm64-v8a-release.apk, app-x86-release.apk, app-x86_64-release.apk, app-universal-release.apk

# 运行测试
./gradlew test

# 安装到设备
./gradlew installDebug
```

### 7.3 运行配置

1. **打开项目**: 使用 Android Studio 打开项目根目录
2. **同步Gradle**: 点击 "Sync Now" 同步依赖
3. **选择设备**: 连接 Android 13+ 设备或启动模拟器
4. **运行**: 点击运行按钮或按 `Shift+F10`

### 7.4 权限要求

**AndroidManifest.xml** 声明的权限:

| 权限 | 用途 |
|------|------|
| `INTERNET` | 网络请求（云更新、版本检查） |
| `ACCESS_NETWORK_STATE` | 网络状态检测 |
| `ACCESS_WIFI_STATE` | WiFi状态检测 |
| `READ_PHONE_STATE` | 友盟统计设备信息 |
| `SYSTEM_ALERT_WINDOW` | 悬浮窗显示 |
| `REQUEST_INSTALL_PACKAGES` | 应用内安装更新 |
| `DOWNLOAD_WITHOUT_NOTIFICATION` | 静默下载 |

**组件声明**:

| 组件 | 说明 |
|------|------|
| `FloatingWindowService` | 悬浮窗服务，`exported=false` |
| `MainActivity` | 主入口Activity，`exported=true` |
| `FileProvider` | APK安装文件共享 |
| `DownloadCompleteReceiver` | 下载完成广播接收器 |

### 7.5 系统要求

- Android 13 (API 33) 及以上
- 系统允许浮窗显示在相机应用上方

---

## 8. 构建配置

### 8.1 build.gradle.kts (App级别)

```kotlin
android {
    namespace = "com.silas.omaster"
    compileSdk { version = release(36) }

    defaultConfig {
        applicationId = "com.silas.omaster"
        minSdk = 33       // Android 13
        targetSdk = 36    // Android 14
        versionCode = 12
        versionName = "1.5.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true        // 启用代码混淆
            isShrinkResources = true      // 启用资源压缩
            proguardFiles(...)
        }
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            isUniversalApk = true         // 同时生成通用APK
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}
```

### 8.2 ProGuard 规则

[proguard-rules.pro](file:///workspace/app/proguard-rules.pro) 包含以下关键规则:

- 保留 Compose 相关类
- 保留 Gson 序列化相关类
- 保留 ViewModel
- 保留 Parcelable/Serializable
- 保留友盟 SDK
- 保留 Coil 图片加载
- 保留 Kotlin Serialization
- 保留数据模型 (`com.silas.omaster.model.**`)
- 保留资源ID（Realme预设反射获取资源需要）
- 忽略 Ktor 调试和 SLF4J 警告

### 8.3 版本 Catalog (libs.versions.toml)

依赖版本集中管理，包含:
- Android Gradle Plugin 9.1.0
- Kotlin 2.3.10
- Compose BOM 2026.03.01
- Ktor 3.4.2
- Coil 2.7.0
- Gson 2.13.2
- Navigation Compose 2.9.7
- Kotlinx Serialization 1.10.0

### 8.4 Maven 仓库配置

settings.gradle.kts 配置了阿里云镜像作为优先源:

```
pluginManagement:
  1. maven.aliyun.com/repository/gradle-plugin
  2. maven.aliyun.com/repository/google
  3. maven.aliyun.com/repository/public
  4. google()
  5. mavenCentral()
  6. gradlePluginPortal()

dependencyResolutionManagement:
  1. maven.aliyun.com/repository/public
  2. maven.aliyun.com/repository/google
  3. google()
  4. mavenCentral()
  5. repo1.maven.org/maven2/
```

---

## 9. 开发规范

### 9.1 代码规范

- **命名**: 使用Kotlin命名规范
  - 类名: 大驼峰 (PascalCase) — `MasterPreset`, `ConfigCenter`
  - 函数/变量: 小驼峰 (camelCase) — `loadPresets()`, `floatingWindowOpacity`
  - 常量: 大写下划线 — `KEY_THEME_ID`, `DEFAULT_PRESET_URL`
- **注释**: 关键类和函数使用KDoc注释，包含功能说明、参数说明、返回值说明
- **架构**: 遵循MVVM架构，UI层不直接访问数据层
- **异步**: 使用Kotlin Coroutines和Flow处理异步操作
- **空安全**: 合理使用 `?.`、`?:`、`let`、`also` 等Kotlin特性
- **单例**: 使用 `@Volatile` + 双重检查锁模式

### 9.2 数据管理原则

1. **用户数据与应用数据分离**:
   - 内置预设随App更新覆盖（assets）
   - 用户数据（收藏、自定义预设）App更新时保留（SharedPreferences）
2. **配置集中管理**: 通过 `ConfigCenter` 统一访问，支持Flow响应式监听
3. **缓存策略**: JsonUtil提供内存缓存，`invalidateCache()` 用于数据更新后清除

### 9.3 提交规范

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

### 9.4 版本规范

```
主版本号.次版本号.修订号[-预发布标识]

示例:
- 1.5.0         # 正式版
- 1.0.0-beta1   # 测试版
- 1.1.0         # 功能更新
- 1.1.1         # 修复更新
```

### 9.5 预设贡献规范

> 云端预设已迁移至独立仓库 [OMaster-Community](https://github.com/fengyec2/OMaster-Community)

贡献流程:
1. Fork 社区仓库
2. 新建分支（如 `preset`）
3. 在 `presets.json` 中添加预设数据
4. 在主分支修改README
5. 提交PR合并到 `fengyec2/OMaster-Community:main`

### 9.6 预设JSON格式

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
            { "label": "参数名", "value": "参数值", "span": 1 }
          ]
        }
      ],
      "tags": ["Auto"]
    }
  ]
}
```

---

## 附录

### A. SharedPreferences 文件清单

| 文件名 | 存储内容 | 管理模块 |
|--------|---------|---------|
| `omaster_prefs` | 收藏ID集合、隐私同意状态 | FavoriteManager, OMasterApplication |
| `omaster_custom_presets` | 自定义预设JSON | CustomPresetManager |
| `omaster_config_center` | 主题、语言、深色模式、悬浮窗设置等 | ConfigCenter |
| `omaster_config_subscriptions` | 订阅列表JSON | SubscriptionConfig |
| `json_util_prefs` | 数据迁移状态标记 | JsonUtil |

### B. 权限处理流程

```
冷启动
  │
  ├── Logger.init()
  ├── ConfigCenter.getInstance() → 加载所有配置
  ├── HapticSettings.enabled = config.isVibrationEnabled
  ├── UMConfigure.preInit() (不采集数据)
  │
  └── 检查用户是否同意隐私政策
        │
        ├── 未同意 → 显示欢迎对话框
        │             ├── 同意 → setUserAgreed(true) → initUMeng()
        │             └── 拒绝 → finish() 退出应用
        │
        └── 已同意 → 检查统计开关 → 如开启则 initUMeng()
```

### C. 相关链接

- **项目主页**: https://github.com/iCurrer/OMaster
- **社区仓库** (贡献预设): https://github.com/fengyec2/OMaster-Community
- **开源协议**: CC BY-NC-SA 4.0
- **下载地址**:
  - GitHub Releases
  - 蒲公英: https://www.pgyer.com/omaster-android
  - 蓝奏云: https://wwbwy.lanzouu.com/b016klqmib

---

*文档版本: 1.1.0*  
*最后更新: 2026-05-08*  
*基于 PR 合并后的最新代码生成*
