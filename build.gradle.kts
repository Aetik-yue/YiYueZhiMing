plugins {
    id("com.android.application") version "8.9.1" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.25" apply false
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
}

tasks.register<org.gradle.api.tasks.Copy>("exportYiYueDebugApk") {
    dependsOn(":app:assembleDebug")
    val releaseVersion = providers.gradleProperty("releaseVersion").orElse("1.0.4")
    from(layout.projectDirectory.file("app/build/outputs/apk/debug/app-debug.apk"))
    into(layout.projectDirectory.dir("version/${releaseVersion.get()}"))
    rename { "以越之名.apk" }
}
