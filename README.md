# 以越之名

[![GitHub stars](https://img.shields.io/github/stars/Aetik-yue/YiYueZhiMing?style=social)](https://github.com/Aetik-yue/YiYueZhiMing/stargazers)
[![License: MIT](https://img.shields.io/badge/License-MIT-pink.svg)](LICENSE)
[![Latest release](https://img.shields.io/github/v/release/Aetik-yue/YiYueZhiMing?color=ff69b4)](https://github.com/Aetik-yue/YiYueZhiMing/releases)

以越之名是一款本地优先的情侣记忆 APP，用来收藏两个人的点滴、照片、重要日期和音乐瞬间。应用采用粉色、柔和、圆角的视觉风格，所有记录、导入照片和音乐副本都保存在本机。

## 功能

- 百宝箱：集中进入【点滴】和【日期】功能。
- 点滴记录：保存日期、心情、文字、照片、音乐和分类。
- 相册：查看记忆照片，支持从本机相册导入图片并全屏放大查看。
- 日期提醒：管理纪念日、生日、初见等重要日期。
- 音乐播放器：内置沉浸式播放器，支持播放、上一首、下一首、Shuffle、Repeat 和进度拖动。
- 设置：夜间模式、本地私密说明、密码保护入口和关于信息。

## 技术栈

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- Room
- Hilt
- WorkManager
- Coil

## 构建

项目是标准 Android Gradle 工程，根目录为 `YiYueZhiMing`。

```powershell
$env:JAVA_HOME="$env:USERPROFILE\.jdks\temurin-24"
& "$env:USERPROFILE\.gradle\wrapper\dists\gradle-8.12-bin\cetblhg4pflnnks72fxwobvgv\gradle-8.12\bin\gradle.bat" :app:assembleDebug
```

生成的默认 Debug APK 位于：

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 版本下载

历史 APK 归档位于 `version/` 目录，每个版本包含：

- `以越之名.apk`
- `更新日志.md`

推荐从 [GitHub Releases](https://github.com/Aetik-yue/YiYueZhiMing/releases) 下载最新 APK。

## 发布新版本

只有在需要发布新的 APK 时运行发布脚本。普通代码修改不会自动推送到 GitHub。

```powershell
.\scripts\release-apk.ps1 -Version 1.0.4 -Notes "设置页布局优化；相册预览修复；音乐控制栏居中。"
```

脚本会构建 APK、写入 `version/<版本号>/`、提交 git、推送 GitHub，并创建 GitHub Release。

## 开源协议

本项目基于 [MIT License](LICENSE) 开源。
