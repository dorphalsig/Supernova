plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    // Moshi for JSON
    implementation("com.squareup.moshi:moshi:1.15.2")
    // XML parsing and Java Compiler API are on the JDK
}