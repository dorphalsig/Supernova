import org.gradle.caching.http.HttpBuildCache

// Read credentials (project / user gradle.properties or -PcacheUser=...)
// Fallback to environment variables, then literals for quick testing.
val cacheUser: String = providers.gradleProperty("cacheUser").orNull
    ?: System.getenv("GRADLE_CACHE_USER")
    ?: "gradlecache"        // temporary fallback; remove in production
val cachePass: String = providers.gradleProperty("cachePass").orNull
    ?: System.getenv("GRADLE_CACHE_PASS")
    ?: "CHANGEME"           // temporary fallback; remove in production

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
        isEnabled = false
    }
    remote<HttpBuildCache> {
        url = uri("  https://build-cache-node.onrender.com/cache/")    // replace
        credentials {
            username = cacheUser
            password = cachePass
        }
    }
}

rootProject.name = "Supernova"
include(":app")
