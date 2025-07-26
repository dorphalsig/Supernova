plugins {
    kotlin("jvm")
    id("com.supernova.testgate")
}

dependencies {
    // Network testing (for BaseSyncTest)
    implementation(libs.retrofit)
    implementation(libs.converter.moshi)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    implementation(libs.mockwebserver)
    
    // Core testing framework
    api(libs.jupiter.api)
    testRuntimeOnly(libs.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotlin.test)
}

tasks.test {
    useJUnitPlatform()
}