pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // 将此模式改为 'PREFER_PROJECT'，允许项目中的仓库配置覆盖设置
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        // 这里可以保留，但主要仓库在 allprojects 中定义
        google()
        mavenCentral()
    }
}

rootProject.name = "GeoSurveyToolbox"
include(":app")
