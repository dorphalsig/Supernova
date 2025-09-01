plugins {
    id("org.jetbrains.kotlin.jvm") version libs.versions.kotlin
}

dependencies {
    implementation(platform(libs.coroutines.bom))
    implementation(platform(libs.okhttp.bom))
    implementation(libs.bundles.network)
}
