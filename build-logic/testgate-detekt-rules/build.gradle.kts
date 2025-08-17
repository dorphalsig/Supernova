
plugins { kotlin("jvm") version "2.0.21" }

group = "com.supernova.testgate"

//java { toolchain { languageVersion.set(JavaLanguageVersion.of(17)) } }
repositories {
    google()
    mavenCentral()
}

dependencies {
    // Keep in lockstep with the applied Detekt Gradle plugin version
    compileOnly(libs.detekt.api)

    testImplementation(kotlin("test"))
    testImplementation(libs.detekt.test)
}

tasks.test { useJUnitPlatform() }
