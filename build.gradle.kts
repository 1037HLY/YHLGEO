// 根目录构建脚本，所有插件已在settings.gradle.kts中管理
tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
