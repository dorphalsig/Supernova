plugins {
    kotlin("jvm")
    alias(libs.plugins.ksp)
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
    testImplementation(libs.jupiter.api)
    testRuntimeOnly(libs.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotlin.test)
    testImplementation("org.xerial:sqlite-jdbc:3.45.3.0")
}

tasks.test {
    useJUnitPlatform()
}