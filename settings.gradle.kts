pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }

        mavenCentral()
        gradlePluginPortal()
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/dorphalsig/testgate")
            credentials {
                username = "VALUE_UNCHECKED"
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // Corrected Google repository definition
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "Supernova"
include(":app")
include(":domain")
include(":data")
include(":network")
include(":sync")
include(":security")
include(":testing-harness")
include(":testing-instrumented")