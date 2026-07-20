pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "YiYueZhiMing"
include(":app")
include(":core:ui")
include(":core:data")
include(":feature:memories")
include(":feature:album")
include(":feature:memo")
include(":feature:reminders")
include(":feature:fortune")
include(":feature:novel")
include(":feature:music")
include(":feature:settings")
include(":feature:toolbox")
