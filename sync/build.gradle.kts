plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.junit5.android)
}

android {
    namespace = "com.supernova.sync"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
}

dependencies {
    implementation(project(":network"))
    implementation(project(":data"))
    implementation(project(":domain"))
    implementation(platform(libs.coroutines.bom))
    implementation(libs.bundles.work)
}
