pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        // 在这里统一声明插件及其版本
        id("com.android.application") version "8.2.0"
        id("org.jetbrains.kotlin.android") version "1.9.20"
        id("org.jetbrains.kotlin.kapt") version "1.9.20"
        id("org.jetbrains.kotlin.plugin.parcelize") version "1.9.20"
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "GeoSurveyToolbox"
include(":app")
