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

plugins {
    id("com.github.burrunan.s3-build-cache") version "1.9.3"
}


buildCache {
    local {
        isEnabled = true
    }
//    remote<com.github.burrunan.s3cache.AwsS3BuildCache> {
//        isPush = true //System.getenv("CI") != null
//        region = "auto"
//        bucket = "cache"
//        endpoint = "https://aa44b0d9c4503975b23eae50165d0e0f.r2.cloudflarestorage.com"
//        awsAccessKeyId = System.getenv("GRADLE_CACHE_USER") ?: ""
//        awsSecretKey =
//            System.getenv("GRADLE_CACHE_PASSWORD") ?: System.getenv("GRADLE_CACHE_PASS") ?: ""
//        forcePathStyle = true
//    }
}

rootProject.name = "Supernova"
include(":app")
include(":testing-harness")
includeBuild("build-logic")