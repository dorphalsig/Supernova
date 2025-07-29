// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.ktlint) apply false
}

subprojects {
    apply(plugin = "jacoco")
    plugins.withId("io.gitlab.arturbosch.detekt") {}
    plugins.withId("org.jlleitschuh.gradle.ktlint") {}
}

tasks.register<JacocoReport>("jacocoMergedReport") {
    val execFiles = fileTree(rootDir) { include("**/build/jacoco/test.exec") }
    executionData.setFrom(execFiles)
    sourceDirectories.setFrom(files(subprojects.map { it.file("src/main/kotlin") }))
    classDirectories.setFrom(files(subprojects.map { it.file("build/tmp/kotlin-classes") }))
    dependsOn(subprojects.map { it.tasks.withType<Test>() })
}