plugins {
    kotlin("jvm")
    id("com.supernova.testgate")
}

dependencies {
    // JSON parsing (for JsonFixtureLoader)
    implementation(libs.moshi)
    
    // Core testing
    testImplementation(libs.jupiter.api)
    testRuntimeOnly(libs.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotlin.test)
}

tasks.test {
    useJUnitPlatform()
}