// 根目录构建脚本 - 所有插件和依赖已在settings.gradle.kts和app/build.gradle.kts中管理
tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
