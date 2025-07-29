package com.supernova.testgate

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertTrue

class CoverageCheckTest {
    @Test
    fun `build fails when coverage low`() {
        val projectDir = File("build/functionalTest").apply { mkdirs() }
        File(projectDir, "settings.gradle.kts").writeText("")
        File(projectDir, "build.gradle.kts").writeText(
            """
            plugins {
                id("org.jetbrains.kotlin.jvm") version \"2.2.0\"
                id("jacoco")
                id("com.supernova.testgate")
            }

            repositories { mavenCentral() }

            tasks.withType<Test> { useJUnitPlatform() }
            """.trimIndent()
        )
        File(projectDir, "src/main/kotlin/Foo.kt").apply {
            parentFile.mkdirs()
            writeText("class Foo { fun bar(): Int = 1 }")
        }
        File(projectDir, "src/test/kotlin/FooTest.kt").apply {
            parentFile.mkdirs()
            writeText(
                """
                import kotlin.test.*
                class FooTest { @Test fun ok() { Foo().bar(); assertTrue(true) } }
                """.trimIndent()
            )
        }

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("test", "jacocoTestReport", "checkCoverage", "--stacktrace")
            .withPluginClasspath()
            .buildAndFail()

        assertTrue(result.output.contains("Coverage"))
        assertTrue(result.task(":checkCoverage")?.outcome == TaskOutcome.FAILED)
    }
}
