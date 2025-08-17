package com.supernova.testgate.convention

import com.android.build.api.dsl.CommonExtension
import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.testing.jacoco.tasks.JacocoReport

/**
 * TestGateConventionPlugin
 * - Applies TestGate + quality tooling
 * - Produces per-variant Jacoco XML for Android unit tests (avoids aggregation)
 * - Piggybacks JVM/KMP jacocoTestReport
 * - Exposes properties for TestGate:
 *   - isAndroid: Boolean
 *   - currentTestVariant: String (last seen variant, e.g., "Debug")
 *   - executedTestTasks: String (comma-separated list of test task names that actually ran)
 */
class TestGateConventionPlugin : Plugin<Project> {

    private val androidPlugins = listOf("com.android.application", "com.android.library")
    private val kotlinPlugins = listOf("org.jetbrains.kotlin.jvm", "org.jetbrains.kotlin.multiplatform")

    override fun apply(target: Project) = with(target) {
        // Exposed flags for consumers (e.g., TestGate analyzers)
        extensions.extraProperties["isAndroid"] = false
        extensions.extraProperties["currentTestVariant"] = ""
        extensions.extraProperties["executedTestTasks"] = ""

        applyPlugins()
        configureDetekt()
        configureAndroidLint()
        configureJacoco()
        configureJUnitWiring()
        captureExecutedTestTasks()
    }

    // --- Apply baseline plugins ---------------------------------------------------------------
    private fun Project.applyPlugins() {
        pluginManager.apply("com.supernova.testgate")
        (androidPlugins + kotlinPlugins).forEach { pid ->
            pluginManager.withPlugin(pid) {
                pluginManager.apply("io.gitlab.arturbosch.detekt")
                pluginManager.apply("jacoco")
                if (pid in androidPlugins) extensions.extraProperties["isAndroid"] = true
            }
        }
    }

    // --- Detekt -------------------------------------------------------------------------------
    private fun Project.configureDetekt() {
        val detektConfig = rootProject.file("detekt-config.yml")
        tasks.withType(Detekt::class.java).configureEach {
            config.setFrom(detektConfig)
            reports { xml.required.set(true); html.required.set(false); txt.required.set(false); sarif.required.set(false) }
            ignoreFailures = true
        }
    }

    // --- Android Lint -------------------------------------------------------------------------
    private fun Project.configureAndroidLint() {
        androidPlugins.forEach { pid ->
            pluginManager.withPlugin(pid) {
                (extensions.findByName("android") as? CommonExtension<*, *, *, *, *, *>)?.lint {
                    lintConfig = rootProject.file("lint-config.xml")
                    abortOnError = false
                    warningsAsErrors = false
                    xmlReport = true
                }
            }
        }
    }

    // --- Jacoco (JVM/KMP + Android) -----------------------------------------------------------
    private fun Project.configureJacoco() {
        // JVM / KMP: ensure standard jacocoTestReport writes XML
        kotlinPlugins.forEach { pid ->
            pluginManager.withPlugin(pid) {
                tasks.matching { it.name == "jacocoTestReport" }.configureEach {
                    (this as JacocoReport).configureReports()
                }
            }
        }
        // Android: per-variant reports, one XML per unit-test variant
        configureJacocoForAndroid()
    }

    private fun Project.configureJacocoForAndroid() {
        androidPlugins.forEach { pid ->
            pluginManager.withPlugin(pid) {
                // Turn on coverage for debug (extend as needed)
                (extensions.findByName("android") as? CommonExtension<*, *, *, *, *, *>)?.let { android ->
                    android.buildTypes.getByName("debug") { enableUnitTestCoverage = true }
                }
                // For every Android unit-test task (test<Variant>UnitTest), register a matching JacocoReport
                tasks.matching { it.name.startsWith("test") && it.name.endsWith("UnitTest") }
                    .configureEach {
                        val testTaskName = name
                        val variant = testTaskName.removePrefix("test").removeSuffix("UnitTest") // e.g., Debug, Release
                        val variantLower = variant.replaceFirstChar { it.lowercase() }
                        val reportTaskName = "jacoco${variant}UnitTestReport"

                        // Surface the most recent variant (useful for consumers that only care about one)
                        extensions.extraProperties["currentTestVariant"] = variant

                        val reportTask = tasks.register(reportTaskName, JacocoReport::class.java) { 
                            dependsOn(this)
                            configureReports()

                            // Exec/EC files for this variant
                            executionData.setFrom(
                                fileTree(layout.buildDirectory) {
                                    include(
                                        "jacoco/${testTaskName}*.exec",
                                        "jacoco/${testTaskName}*.ec",
                                        "outputs/unit_test_code_coverage/${variantLower}/${testTaskName}.exec",
                                        "outputs/unit_test_code_coverage/${variantLower}/${testTaskName}.ec"
                                    )
                                }
                            )

                            // Class dirs for this variant (Java + Kotlin)
                            classDirectories.setFrom(
                                files(
                                    layout.buildDirectory.dir("intermediates/javac/${variantLower}/classes"),
                                    layout.buildDirectory.dir("tmp/kotlin-classes/${variant}"),
                                    layout.buildDirectory.dir("tmp/kotlin-classes/${variantLower}")
                                )
                            )

                            // Source roots
                            sourceDirectories.setFrom(files("src/main/java", "src/main/kotlin"))
                        }

                        // Run per-variant report after the test task
                        finalizedBy(reportTask)
                    }
            }
        }
    }

    // --- JUnit wiring (name-based) ------------------------------------------------------------
    private fun Project.configureJUnitWiring() {
        // JVM: finalize `test` -> `jacocoTestReport`
        pluginManager.withPlugin("org.jetbrains.kotlin.jvm") { wireFinalizer("test", "jacocoTestReport") }
        // KMP: finalize `jvmTest` -> `jacocoTestReport`
        pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") { wireFinalizer("jvmTest", "jacocoTestReport") }
        // Android handled in configureJacocoForAndroid() per variant
    }

    // --- Capture executed test tasks (for exact filtering in TestGate) ------------------------
    private fun Project.captureExecutedTestTasks() {
        // At execution time, record the exact test task names that run in this project
        gradle.taskGraph.whenReady {
            val executed = allTasks
                .filter { it.project == this }
                .map { it.name }
                .filter { name ->
                    name == "test" || name == "jvmTest" ||
                            (name.startsWith("test") && name.endsWith("UnitTest"))
                }
            if (executed.isNotEmpty()) {
                extensions.extraProperties["executedTestTasks"] = executed.joinToString(",")
            }
        }
    }

    // --- Helpers -----------------------------------------------------------------------------
    private fun JacocoReport.configureReports() {
        reports { xml.required.set(true); html.required.set(false); csv.required.set(false) }
    }

    /** Name-based: finalize [fromName] by [toName] when present. */
    private fun Project.wireFinalizer(fromName: String, toName: String) {
        tasks.matching { it.name == fromName }.configureEach { finalizedBy(tasks.named(toName)) }
    }
}
