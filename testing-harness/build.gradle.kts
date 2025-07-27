plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("plugin.serialization") version "2.2.0"
    id("com.supernova.testgate")
}

// Allow JsonFixtureLoaderTest to bypass structure checks since it only tests
// utility behavior without extending a base class.
extra["allowBadStructureTestClasses"] =
    "com.supernova.testing.JsonFixtureLoaderTest"

android {
    namespace = "com.supernova.testing"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
        targetSdk = 35
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }


    testOptions {
        targetSdk = 35
    }

}

dependencies {
    // Kotlin & Coroutines
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.test)

    // Room (for in-memory DB testing)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.testing)

    // Retrofit & OkHttp
    implementation(libs.retrofit)
    implementation(libs.converter.moshi)
    implementation(libs.okhttp)
    implementation(libs.mockwebserver)

    // Moshi
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)

    // Kotlinx Serialization
    implementation(libs.serialization)

    // JUnit 5 (Jupiter)
    implementation(libs.junit.jupiter.api)
    implementation(libs.junit.jupiter.engine)

    // Kotlin Test (assertions)
    implementation(libs.kotlin.test)

    //tests
    testImplementation(libs.junit.jupiter) // already covered in implementation, but retained here if split is needed
    testImplementation(libs.mockk) // Used in TestEntityFactoryTest.kt
}
