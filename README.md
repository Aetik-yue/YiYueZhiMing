# 以越之名

[![GitHub stars](https://img.shields.io/github/stars/Aetik-yue/YiYueZhiMing?style=social)](https://github.com/Aetik-yue/YiYueZhiMing/stargazers)
[![License: MIT](https://img.shields.io/badge/License-MIT-pink.svg)](LICENSE)
[![Latest release](https://img.shields.io/github/v/release/Aetik-yue/YiYueZhiMing?color=ff69b4)](https://github.com/Aetik-yue/YiYueZhiMing/releases)

以越之名是一款本地优先的情侣记忆 APP，用来收藏两个人的点滴、照片、重要日期和音乐瞬间。应用采用粉色、柔和、圆角的视觉风格，所有记录、导入照片和音乐副本都保存在本机。

## 功能

### 核心功能

- **百宝箱**：集中进入所有功能的导航中心。
- **点滴记录**：保存日期、心情、文字、照片、音乐和分类。
- **相册**：查看记忆照片，支持从本机相册导入图片并全屏放大查看。
- **日期提醒**：管理纪念日、生日、初见等重要日期，每年循环提醒。
- **音乐播放器**：内置沉浸式播放器，支持播放、上一首、下一首、Shuffle、Repeat 和进度拖动。
- **备忘录**：记录待办事项，支持分类、搜索、置顶和完成状态。
- **运势**：每日一签与塔罗牌，确定性随机，每日仅可抽取一次。
- **小说**：私人阅读小书架，支持本地导入 TXT/EPUB 和在线抓取。
- **设置**：夜间模式、相爱天数计数器、密码保护和关于信息。

### 音乐播放器

- 扫描设备本地音乐（自动过滤铃声）
- 沉浸式黑胶唱片界面，实时旋转动画
- 播放控制：播放/暂停、上/下首、进度拖动、随机播放、单曲循环
- 播放列表管理，支持收藏歌曲
- 后台播放与通知栏控制
- 记忆播放：自动保存上次播放的歌曲与进度

### 小说阅读器

- 本地导入 TXT / EPUB 文件
- 在线抓取小说（支持 HTTP/HTTPS）
- 智能章节分割（支持 第X章、Chapter N、编号、卷X 等格式）
- 分页阅读，支持字体大小、行距、边距调节
- 阅读主题：白天 / 夜间 / 护眼
- 阅读进度自动保存与恢复
- 书签与目录导航

### 安全

- 密码保护：4-6 位数字 PIN，保存在本地 SharedPreferences
- 所有数据仅存本机，无后端账号、无数据上传
- 照片和音乐导入时复制到应用私有目录

## 技术栈

| 类别 | 技术 |
|---|---|
| 语言 | Kotlin |
| UI | Jetpack Compose + Material 3 |
| 导航 | Navigation Compose |
| 数据库 | Room (SQLite) |
| 依赖注入 | Hilt |
| 后台任务 | WorkManager |
| 图片加载 | Coil |
| 媒体播放 | Media3 (ExoPlayer) |
| 网络 | OkHttp |
| 异步 | Kotlin Coroutines + Flow |

## 项目结构

```
以越之名/
├── app/                          # 应用入口（Application + Activity + NavGraph）
├── core/
│   ├── ui/                       # 共享 UI 层（主题、组件、动画、模型、路由）
│   └── data/                     # 核心数据层（Room、Repository、Worker、Service）
├── feature/
│   ├── memories/                 # 点滴记录（首页 + 添加记忆）
│   ├── album/                    # 相册
│   ├── memo/                     # 备忘录
│   ├── reminders/                # 日期提醒
│   ├── fortune/                  # 运势（签运 + 塔罗）
│   ├── music/                    # 音乐播放器
│   ├── novel/                    # 小说（书架 + 阅读器）
│   ├── settings/                 # 设置
│   └── toolbox/                  # 百宝箱（导航中心）
├── scripts/                      # 发布脚本
└── version/                      # APK 归档
```

### 模块依赖关系

```
:app → :feature:* → :core:ui + :core:data
```

所有 feature 模块共享 `core:ui`（主题/组件/动画）和 `core:data`（数据库/Repository），`app` 模块负责组装导航。

## 数据库

使用 Room (SQLite)，数据库名 `yi_yue.db`，当前版本 8，包含 11 张表：

| 表 | 用途 |
|---|---|
| `memories` | 点滴记录 |
| `reminders` | 日期提醒 |
| `memos` / `memo_categories` | 备忘录及其分类 |
| `album_photos` / `album_categories` | 相册照片及其分类 |
| `fortune_records` | 运势抽签记录 |
| `books` / `chapters` / `bookmarks` / `reading_stats` | 书籍、章节、书签、阅读统计 |

## 构建

项目是标准 Android Gradle 工程。

**环境要求：**
- JDK 17+
- Android SDK 36 (compileSdk/targetSdk)
- minSdk 26 (Android 8.0+)

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
.\scripts\release-apk.ps1 -Version 1.1.4 -Notes "Bug 修复与稳定性提升"
```

脚本会构建 APK、写入 `version/<版本号>/`、提交 git、推送 GitHub，并创建 GitHub Release。

## 已知问题与修复记录

### 1.1.4 (2026-07-22)

**严重修复：**
- 修复运势抽签按钮在首次使用后永久失效的问题
- 修复年度提醒只触发一次的问题（现在每年循环提醒）
- 修复 HTTP 明文流量在 Android 9+ 设备上被拦截的问题

**功能修复：**
- 修复 MediaPlayer 音频播放资源泄漏
- 修复小说阅读器段落间距在所有设置下均为 0 的问题
- 修复添加记忆时日期字段无法手动输入的问题
- 修复阅读进度在修改字号后计算错误的问题
- 修复 EPUB 解析器的 zip bomb 内存溢出风险
- 修复在线小说抓取无总体超时的问题
- 修复运势抽签 TOCTOU 竞争条件

**优化：**
- 修复书籍删除后缓存未释放的问题
- 为 OkHttpClient 添加总体调用超时
- 修复设置页密码保护实际写入本地存储
- 修复相爱天数计数器跨午夜后过期的问题
- 移除 fallbackToDestructiveMigration，避免未来升级数据丢失风险

## 开源协议

本项目基于 [MIT License](LICENSE) 开源。
