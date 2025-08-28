plugins {
    id("org.jetbrains.kotlin.jvm") version libs.versions.kotlin
}

dependencies {
    implementation(platform(libs.coroutines.bom))
    implementation(libs.coroutines.core)
}
