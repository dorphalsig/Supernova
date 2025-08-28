plugins {
    id("org.jetbrains.kotlin.jvm") version libs.versions.kotlin
}

dependencies {
    implementation(platform(libs.junit.bom))
    implementation(libs.bundles.testing.jvm)
}
