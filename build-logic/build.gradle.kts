plugins {
  `kotlin-dsl`
  `java-gradle-plugin`
}

repositories {
  gradlePluginPortal()
  google()
  mavenCentral()
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
  // Gradle APIs
  implementation(gradleApi())
  implementation(gradleTestKit())

  // Compile against AGP & Detekt types without leaking them to consumers:
  compileOnly(conventionLibs.gradle)                 // com.android.tools.build:gradle
  compileOnly(conventionLibs.detekt.gradle.plugin)   // io.gitlab.arturbosch.detekt:detekt-gradle-plugin

  // Tests: JUnit 5 via BOM + artifacts
  testImplementation(gradleApi())
  testImplementation(gradleTestKit())
  testImplementation(platform(conventionLibs.junit.bom))
  testImplementation(conventionLibs.junit5.api)
  testImplementation(conventionLibs.junit5.params)
  testRuntimeOnly(conventionLibs.junit5.engine)
}