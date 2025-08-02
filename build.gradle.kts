plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.testgate) apply false
}

subprojects {
    apply(plugin = "jacoco")
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "com.supernova.testgate")
//    afterEvaluate {
//        extensions.findByType<com.android.build.gradle.BaseExtension>()?.apply {
//            buildTypes {
//                getByName("debug") {
//                    isTestCoverageEnabled = true
//                }
//            }
//        }
//    }
}