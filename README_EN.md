# OMaster - Master Mode Color Tuning Library

<p align="center">
  <a href="README.md">🇨🇳 中文</a> | 
  <a href="README_EN.md">🇺🇸 English</a>
</p>

<p align="center">
  <img src="app/src/main/ic_launcher-playstore.png" width="120" alt="OMaster Logo"/>
</p>

<p align="center">
  <b>Photography color tuning parameter management tool for various mobile phone brands</b>
</p>

<p align="center">
  <a href="https://github.com/iCurrer/OMaster/releases">
    <img src="https://img.shields.io/github/v/release/iCurrer/OMaster" alt="Version"/>
  </a>
  <a href="https://creativecommons.org/licenses/by-nc-sa/4.0/deed.zh">
    <img src="https://img.shields.io/badge/License-CC%20BY--NC--SA%204.0-orange.svg?style=flat-square" alt="License"/>
  </a>
  <img src="https://img.shields.io/badge/Platform-Android%2013+-brightgreen.svg?style=flat-square" alt="Platform"/>
  <img src="https://img.shields.io/badge/Tech-Jetpack%20Compose-4285F4.svg?style=flat-square" alt="Tech"/>
  <img src="https://img.shields.io/github/actions/workflow/status/iCurrer/OMaster/beta-release.yml" alt="Build"/>
</p>

<p align="center">
  <a href="https://github.com/iCurrer/OMaster/releases">
    <b>⬇️ Download Latest Version</b>
  </a>
</p>

---

## 📸 Still worried about photography parameters?

Every time you want to take a satisfactory photo, you have to search for parameters in the vast amount of information on the Internet like **looking for a needle in a haystack**, which is time-consuming and doesn't always guarantee you'll find the right one 😫

Now, **OMaster** provides you with a clean and fresh platform where all data is clear at a glance, allowing you to easily say goodbye to parameter anxiety ✨

---

## ✨ Core Features

### 🎨 Rich Preset Library
- **23+ Professional Presets** - Covers film, vintage, fresh, black and white, food, and other styles
- **New Preset Sticky Mark** - Newly added presets display a NEW label and are pinned to the top

### ☁️ Cloud Configuration Updates
- Support obtaining the latest configuration from the cloud
- Support custom update sources

### ⭐ Collection Management
- One-click collection of favorite presets
- Quick access to frequently used parameters
- Local storage, no internet required

### 🛠️ Cross-platform Support
- Support creating custom presets
- Support professional cameras of major mainstream platforms
- Support remote subscription updates

### 🔲 Floating Window Mode
- Floating display of parameters during photography
- Support swiping left and right to switch presets
- Translucent design does not block the viewfinder

### 📱 Simple and Elegant Interface
- Pure black background + color schemes of major photography brands
- Smooth animation transitions
- Waterfall card layout

---

## 🎬 Feature Preview

| Home Browse | Preset Details | Floating Window |
|---------|---------|--------|
| Waterfall display of all presets | View full parameters and samples | Reference at any time during photography |
| Support category filtering | Image carousel display effect | Can be collapsed into a floating ball |

---

## 📥 Download and Install

### Method 1: GitHub Releases
Go to the [Releases page](https://github.com/iCurrer/OMaster/releases) to download the latest version APK

### Method 2: China Mirrors
- Pgyer: [https://www.pgyer.com/omaster-android](https://www.pgyer.com/omaster-android)
- LanzouCloud: [https://wwbwy.lanzouu.com/b016klqmib](https://wwbwy.lanzouu.com/b016klqmib)

### System Requirements
- Android 13 (API 33) and above
- Your phone system allows floating windows to be displayed on top of the camera application

---

## 🛠️ Tech Stack

| Tech | Usage |
|------|------|
| **Kotlin** | Main development language |
| **Jetpack Compose** | Modern UI framework |
| **Material Design 3** | Design language |
| **Coil** | Image loading |
| **Gson** | JSON parsing |
| **Kotlin Serialization** | Type-safe navigation |

---

## 📋 Parameter Description

Master mode parameters supported by OMaster include but are not limited to:

| Parameter Category | Specific Parameters |
|---------|---------|
| **Basic Parameters** | Filter, Soft Light, Tone, Saturation, Warmth/Coolness, Tint (Magenta/Green), Sharpness, Vignette |
| **Professional Parameters** | ISO, Shutter Speed, Exposure Compensation, Color Temperature, Tint |

---

## 📝 Changelog

See [Changelog](CHANGELOG.md)

---

## ❓ FAQ

### Floating window cannot be opened?

Some ColorOS / OxygenOS systems may identify this application as an "application from unknown sources", thereby restricting floating window permission authorization.

**Solution:**

1. Open **Settings** → **Apps** → **App Management**
2. Find **OMaster**, click **Permission Management**
3. Click the **⋮** icon in the upper right corner and select **Remove all authorization restrictions**
4. Return to the application and re-enable floating window permission

> [!WARNING]
> 
> After removing the restriction, please ensure that only "Floating Window" permission is granted. Other sensitive permissions can be granted as needed.

### Floating window not displaying on top of the camera application?

Unfortunately, some customized Android systems may limit applications from displaying floating windows on top of the camera application.

**Solution:**

1. Try a new phone
2. Throw away the old phone
3. No other solutions at the moment

---

## 🔒 Privacy Policy

- All data is stored locally
- Floating window permission is only used to display the parameter window
- Statistics function is enabled only after user consent

---

## 🤝 Contribution Guide

Issues and Pull Requests are welcome!

### Submit New Preset

> [!IMPORTANT]
> Cloud presets have been migrated to an independent repository for maintenance
> 
> If you are only updating cloud presets, do not submit a Pull Request directly to the OMaster main repository

If you want to contribute new color presets:

1. Go to the [OMaster-Community](https://github.com/fengyec2/OMaster-Community) community repository
2. Fork the community repository
3. Create a new branch (e.g., `preset` )
4. Add preset data to `presets.json` in the new branch (e.g., `preset` )
5. Modify README in the main branch (i.e., `main` branch)
6. Submit a Pull Request to merge your new branch (e.g., `preset` ) into `fengyec2/OMaster-Community:main`

---

## 📄 License

This project is open-sourced under [CC BY-NC-SA 4.0](https://creativecommons.org/licenses/by-nc-sa/4.0/deed.zh)

- **Attribution** (BY) - Copyright notice must be retained when used
- **Non-Commercial** (NC) - Prohibit commercial use
- **ShareAlike** (SA) - Must use the same license after modification

---

## 🙏 Acknowledgments

- **Project Maintenance:**

| | |
|---|---|
| <a href="https://github.com/iCurrer"><img src="https://avatars.githubusercontent.com/u/61453275?v=4" width="80px;" alt="Silas"/><br /><sub><b>Silas</b></sub></a><br /><a href="https://github.com/iCurrer/OMaster/commits?author=iCurrer" title="Code">💻</a> <a href="#design-Silas" title="Design">🎨</a> <a href="https://github.com/iCurrer/OMaster/commits?author=iCurrer" title="Documentation">📖</a> <a href="#ideas-Silas" title="Ideas, Planning, & Feedback">🤔</a> <a href="#maintenance-Silas" title="Maintenance">🚧</a> | <a href="https://github.com/fengyec2"><img src="https://avatars.githubusercontent.com/u/85821538?v=4" width="80px;" alt="Luminary"/><br /><sub><b>Luminary</b></sub></a><br /><a href="https://github.com/iCurrer/OMaster/commits?author=fengyec2" title="Code">💻</a> <a href="https://github.com/iCurrer/OMaster/commits?author=fengyec2" title="Documentation">📖</a> <a href="#ideas-Luminary" title="Ideas">💡</a>|

- **Material Providers:**
  - [@OPPO影像](https://xhslink.com/m/8c2gJYGlCTR)
  - [@蘭州白鴿](https://xhslink.com/m/4h5lx4Lg37n)
  - [@派瑞特凯](https://xhslink.com/m/AkrgUI0kgg1)
  - [@ONESTEP™](https://xhslink.com/m/4LZ8zRdNCSv)
  - [@盒子叔](https://xhslink.com/m/4mje9mimNXJ)
  - [@Aurora](https://xhslink.com/m/2Ebow4iyVOE)
  - **[@屋顶橙子味](https://v.douyin.com/YkVXPX9kZgY/)** ⭐ New

---

## 📞 Contact Us

- Submit [GitHub Issue](https://github.com/iCurrer/OMaster/issues)
- Send email to: iboy66lee@qq.com

---

<p align="center">
  <b>Made with ❤️ by Silas</b>
</p>

<p align="center">
  <sub>Purely localized operation, data stored locally</sub>
</p>