plugins {
    id("com.android.library")
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.supernova.testing"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
        targetSdk = 36
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    api(libs.androidx.room.runtime)
    api(libs.androidx.room.ktx)
    api(libs.kotlinx.coroutines.test)
    api(libs.androidx.test.core)
    api(libs.sqlite.jdbc)
    api(libs.jupiter.api)
    ksp(libs.androidx.room.compiler)

    testRuntimeOnly(libs.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.mockk)
    testImplementation(libs.androidx.core.testing)
    kspTest(libs.androidx.room.compiler)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
