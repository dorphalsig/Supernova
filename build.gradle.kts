plugins {
    id("com.supernova.testgate.convention") apply false
}

subprojects {
    if (!gradle.startParameter.projectProperties.containsKey("skipConventions")) {
        apply(plugin = "com.supernova.testgate.convention")
    }
}
