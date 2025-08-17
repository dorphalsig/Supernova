rootProject.name = "build-logic"
include(":testgate-conventions", ":testgate-detekt-rules")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}