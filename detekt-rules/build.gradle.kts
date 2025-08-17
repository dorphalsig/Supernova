import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
}

dependencies {
    compileOnly(libs.detekt.api)
    testImplementation(libs.detekt.test)
    testImplementation(libs.junit5)
}

//kotlin.compilerOptions.jvmTarget.set(JvmTarget.JVM_17)

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
