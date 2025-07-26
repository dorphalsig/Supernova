plugins {
    kotlin("jvm")
    id("com.supernova.testgate")
}

dependencies {
    implementation(libs.moshi)
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
