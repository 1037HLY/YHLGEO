// 根目录构建脚本，插件已在settings.gradle.kts中管理
tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
