pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        // 明确声明Android和Kotlin插件的版本
        id("com.android.application") version "8.2.0"
        id("org.jetbrains.kotlin.android") version "1.9.20"
        id("kotlin-kapt") version "1.9.20"
        id("kotlin-parcelize") version "1.9.20"
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
