plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("com.supernova.testgate.convention") apply false
}
subprojects{
    apply(plugin = "com.supernova.testgate.convention")
}
