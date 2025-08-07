import io.gitlab.arturbosch.detekt.Detekt

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.detekt) apply false
   // alias(libs.plugins.testgate) apply false
}

subprojects {
    apply(plugin = "jacoco")
    apply(plugin = "io.gitlab.arturbosch.detekt")
    //apply(plugin = "com.supernova.testgate")

    tasks.withType<Test> {
        ignoreFailures = true
    }
    plugins.withId("io.gitlab.arturbosch.detekt") {
        configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
            config.setFrom(
                files(
                    rootProject.file("detekt-config.yml")
                )
            )
            ignoreFailures = true
        }
    }

    tasks.withType<Detekt>().configureEach {
        reports {
            xml.required.set(true)
            xml.outputLocation.set(file("build/reports/detekt/detekt.xml"))
        }
    }

}
