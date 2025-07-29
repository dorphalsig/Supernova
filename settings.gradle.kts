import org.gradle.caching.http.HttpBuildCache

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
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}


buildCache {
    local {
        isEnabled = true
    }
    remote<HttpBuildCache> {
        url = uri("https://aa44b0d9c4503975b23eae50165d0e0f.r2.cloudflarestorage.com/cache")
        isPush = System.getenv("CI") != null
        credentials {
            username = System.getenv("GRADLE_CACHE_USER") ?: ""
            password = System.getenv("GRADLE_CACHE_PASSWORD") ?: System.getenv("GRADLE_CACHE_PASS") ?: ""
        }
    }
}

rootProject.name = "Supernova"
include(":app")
include(":testing-harness")
include(":data")
