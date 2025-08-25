plugins {
    `kotlin-dsl`          // uses Gradle’s embedded Kotlin; no separate kotlin plugin version needed
    `java-gradle-plugin`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

gradlePlugin {
    plugins {
        create("testGateConvention") {
            id = "com.supernova.testgate.convention"
            implementationClass = "com.supernova.testgate.convention.TestGateConventionPlugin"
        }
    }
}

dependencies {
    implementation(gradleApi())
    implementation(gradleTestKit())
    // We reference these plugin classes in code; keep them compileOnly so consumers don’t get them transitively
    implementation(libs.gradle)                // com.android.tools.build:gradle:<agp version from catalog>
    implementation(libs.detekt.gradle.plugin)  // detekt-gradle-plugin:<detekt version from catalog>


    testImplementation(gradleApi())
    testImplementation(gradleTestKit())
    testImplementation(libs.junit5)
}
