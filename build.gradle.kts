// Top-level build file
plugins {
    // 这里不需要再添加 classpath，由 settings.gradle.kts 管理
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
