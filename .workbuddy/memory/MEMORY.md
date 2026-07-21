# 以越之名 (YiYueZhiMing) — 项目长期记忆

## 项目概况
- 本地优先的情侣记忆 Android APP，粉色/圆角/kawaii 视觉风格，所有数据存本机。
- 包名 `com.example.yiyuezhiming`，applicationId 同名。versionName 1.1.0 (versionCode 110)。
- Gradle 8.12，AGP + Kotlin，compileSdk 36 / minSdk 26 / targetSdk 36，JVM 17。

## 技术栈
Kotlin + Jetpack Compose + Material 3 + Navigation Compose + Room + Hilt + WorkManager + Coil + Media3(ExoPlayer/session) + OkHttp。

## 多模块架构 (settings.gradle.kts)
- `:app` — 入口(MainActivity/YiYueApplication)、AppNavGraph、splash。
- `:core:ui` — 设计系统：theme(Color/Type/Shape/Theme)、components(Kawaii* 系列)、animation、model(数据模型)、navigation/Routes.kt。**所有模块共享**。
- `:core:data` — Room(YiYueDatabase, 各 Entity/Dao)、Repository、DI(AppModule)、worker(Reminder/BookImport)、player(PlaybackService)、reader(分页引擎/Epub)、notification。
- feature 模块：memories, album, memo, reminders, fortune, novel, music, settings, toolbox。每个 feature 依赖 core:ui + core:data。

## 前端模式 (MVVM)
- Screen(@Composable, hiltViewModel()) → ViewModel(@HiltViewModel, StateFlow<XxxUiState>) → Repository → Room DAO(Flow)。
- UiState data class 含 isLoading/error/数据。ViewModel init 里 observe repository flow + seedIfEmpty(MockData)。
- 导航：sealed class Route(path)，AppNavGraph 用 NavHost，起始 Splash→Toolbox(百宝箱)。底部栏 KawaiiBottomBar 在主要页面显示。
- 设计系统色：PrimaryPink/AccentHotPink(#FF69B4)/BackgroundPink 等，深色模式 Dark* 系列。darkMode 目前在 MainActivity 用 rememberSaveable 管理(未持久化)。

## 待完善方向线索
- darkMode 未持久化(仅内存 state)；MockData 为种子数据；fallbackToDestructiveMigration(无迁移)。
- 小说导入链：`enqueueImport` 仍在主线程同步拷贝文件、拷贝失败静默 return（2026-07-20 已修复 WorkManager/Hilt 接线 + EPUB 容错解码，见当日日志）。
