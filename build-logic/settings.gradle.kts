rootProject.name = "build-logic"

pluginManagement {
  repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
  }
}

dependencyResolutionManagement {
  repositories { google(); mavenCentral() }
  versionCatalogs {
    create("conventionLibs") {
      from(files("../gradle/libs.versions.toml"))
    }
  }
}