package com.supernova.testgate

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.gradle.BuildAdapter
import org.gradle.BuildResult
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.w3c.dom.Element
import java.io.File
import java.time.Instant
import javax.xml.parsers.DocumentBuilderFactory

class TestGatePlugin : Plugin<Project> {

    private lateinit var project: Project
    private val moshi = Moshi.Builder().build()
    private val adapter = moshi.adapter<MutableMap<String, Any>>(
        Types.newParameterizedType(MutableMap::class.java, String::class.java, Any::class.java)
    )
    private lateinit var resultsFile: java.io.File

    // Utility functions for glob matching
    private fun matchesGlob(className: String, patterns: Set<String>): Boolean {
        return patterns.any { pattern ->
            val regex = Regex(
                "^" + pattern
                    .replace(".", "\\.")
                    .replace("**", ".*")
                    .replace("*", "[^.]*") + "$"
            )
            regex.matches(className)
        }
    }


    // JSON helpers
    private fun readResults(): MutableMap<String, Any> {
        val text = resultsFile.takeIf { it.exists() }?.readText()
        return if (text != null) {
            adapter.fromJson(text) ?: mutableMapOf("timestamp" to Instant.now().toString())
        } else {
            mutableMapOf("timestamp" to Instant.now().toString())
        }
    }

    private fun writeResults(m: MutableMap<String, Any>) {
        resultsFile.parentFile.mkdirs()
        resultsFile.writeText(adapter.toJson(m))
    }

    // Error processing utilities
    private fun extractErrorType(error: String): String = when {
        error.contains("Unresolved reference") -> "UNRESOLVED_REFERENCE"
        error.contains("Type mismatch") -> "TYPE_MISMATCH"
        error.contains("Expecting") -> "SYNTAX_ERROR"
        error.contains("Unresolved import") -> "MISSING_IMPORT"
        error.contains("compilation failed", ignoreCase = true) -> "COMPILATION_ERROR"
        else -> "UNKNOWN_ERROR"
    }

    private fun extractFileName(error: String): String? {
        return Regex("([A-Za-z0-9_]+\\.kt)").find(error)?.groupValues?.get(1)
    }

    private fun extractLineNumber(error: String): Int? {
        return Regex(":(\\d+):").find(error)?.groupValues?.get(1)?.toIntOrNull()
    }

    private fun cleanErrorMessage(error: String): String {
        return error.replace(Regex("file:///[^\\s]+"), "")
            .replace(Regex(":\\d+:\\d+"), "")
            .trim()
    }

    private fun getErrorSuggestion(error: String): String = when {
        error.contains("Unresolved reference") -> "Check spelling, imports, or if the referenced item exists"
        error.contains("Type mismatch") -> "Verify expected vs actual types match"
        error.contains("Expecting ')'") -> "Add missing closing parenthesis"
        error.contains("Expecting '}'") -> "Add missing closing brace"
        else -> "Review compilation error and fix syntax or references"
    }

    private fun classifyFailure(failure: String): String = when {
        failure.contains("AssertionError") -> "ASSERTION_ERROR"
        failure.contains("TimeoutException") -> "TIMEOUT"
        failure.contains("NullPointerException") -> "NULL_POINTER"
        failure.contains("IllegalStateException") -> "ILLEGAL_STATE"
        else -> "RUNTIME_EXCEPTION"
    }

    private fun extractAssertionDetails(failure: String): Map<String, String?> {
        val expectedMatch = Regex("expected:<([^>]+)>").find(failure)
        val actualMatch = Regex("but was:<([^>]+)>").find(failure)
        return mapOf(
            "expected" to expectedMatch?.groupValues?.get(1),
            "actual" to actualMatch?.groupValues?.get(1)
        )
    }

    private fun suggestTestFix(failure: String): String = when {
        failure.contains("AssertionError") -> "Review assertion logic and expected values"
        failure.contains("TimeoutException") -> "Increase timeout or optimize test performance"
        failure.contains("NullPointerException") -> "Add null checks or proper test setup"
        else -> "Review test logic and exception cause"
    }

    private fun extractClassName(error: String): String? {
        return Regex("([A-Za-z0-9_]+(?:\\.[A-Za-z0-9_]+)*)#").find(error)?.groupValues?.get(1)
    }

    private fun extractMethodName(error: String): String? {
        return Regex("#([A-Za-z0-9_]+)").find(error)?.groupValues?.get(1)
    }

    private fun extractFailureMessage(failure: String): String {
        return failure.split("\n").firstOrNull()?.trim() ?: failure.trim()
    }

    private fun extractFile(error: String): String? {
        return Regex("([^\\s]+\\.kt):").find(error)?.groupValues?.get(1)
    }

    private fun extractImport(error: String): String? {
        return Regex("import\\s+([^\\s]+)").find(error)?.groupValues?.get(1)
    }

    private fun uploadToPastebin(content: String): String {
        return try {
            val url = java.net.URL("https://paste.rs")
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "text/plain")

            connection.outputStream.use { it.write(content.toByteArray()) }

            val responseCode = connection.responseCode

            return if (responseCode in 200..299) {
                connection.inputStream
                    .bufferedReader(Charsets.UTF_8)
                    .use { it.readText().trim() }
            } else {
                "Pastebin upload failed (HTTP $responseCode)"
            }

        } catch (e: Exception) {
            "Pastebin upload failed: ${e.message}"
        }
    }

    // Helper for recording task results
    private fun recordTaskResult(
        taskKey: String,
        startTime: Long,
        errors: List<String> = emptyList(),
        violations: List<Map<String, Any>> = emptyList(),
        additionalData: Map<String, Any> = emptyMap()
    ) {
        val success = errors.isEmpty() && violations.isEmpty()
        val exitCode = if (success) 0 else 1
        val duration = System.currentTimeMillis() - startTime

        val results = readResults().also {
            it[taskKey] = mapOf(
                "success" to success,
                "exitCode" to exitCode,
                "durationMs" to duration,
                "errors" to errors,
                "violations" to violations
            ) + additionalData
        }
        writeResults(results)
    }

    // Check functions with integrated result recording
    private fun checkBannedImports(
        allowBannedImportClasses: Set<String>,
        bannedImportPatterns: List<Regex>
    ): Long {
        val startTime = System.currentTimeMillis()
        val errors = mutableListOf<String>()
        val violations = mutableListOf<Map<String, Any>>()

        fun extractFullyQualifiedClassName(text: String, file: File): String {
            val pkg = Regex("""^\s*package\s+([\w.]+)""", RegexOption.MULTILINE)
                .find(text)?.groupValues?.get(1)
            val cls = Regex("""\bclass\s+(\w+)""")
                .find(text)?.groupValues?.get(1)
            return if (pkg != null && cls != null) "$pkg.$cls"
            else file.relativeTo(project.projectDir).path.removeSuffix(".kt").replace('/', '.')
        }

        project.fileTree("src").matching { include("**/*.kt", "**/*.java") }.forEach { f ->
            val text = f.readText()
            val fqcn = extractFullyQualifiedClassName(text, f)

            if (!matchesGlob(fqcn, allowBannedImportClasses)) {
                f.readLines().forEach { line ->
                    if (line.trim().startsWith("import ") && bannedImportPatterns.any {
                            it.containsMatchIn(line)
                        }) {
                        errors += "$fqcn: $line"
                        violations.add(
                            mapOf(
                                "file" to fqcn,
                                "bannedImport" to (extractImport(line) ?: line.trim()),
                                "rule" to "BANNED_IMPORT",
                                "severity" to "HIGH",
                                "remediation" to "Remove this import or add class to allowBannedImportClasses property"
                            )
                        )
                    }
                }
            }
        }

        recordTaskResult(
            "checkBanned",
            startTime,
            errors,
            violations,
            mapOf(
                "allowList" to allowBannedImportClasses.toList(),
                "bannedPatterns" to bannedImportPatterns.map { it.pattern }
            )
        )
        return startTime
    }

    private fun checkCoroutineTests(allowCoroutineTestClasses: Set<String>): Long {
        val startTime = System.currentTimeMillis()
        val errors = mutableListOf<String>()
        val violations = mutableListOf<Map<String, Any>>()

        fun extractFullyQualifiedClassName(text: String, file: File): String {
            val pkg = Regex("""^\s*package\s+([\w.]+)""", RegexOption.MULTILINE)
                .find(text)?.groupValues?.get(1)
            val cls = Regex("""\bclass\s+(\w+)""")
                .find(text)?.groupValues?.get(1)
            return if (pkg != null && cls != null) "$pkg.$cls"
            else file.relativeTo(project.projectDir).path.removeSuffix(".kt").replace('/', '.')
        }

        fun scanTests(dir: String) {
            project.fileTree(dir).matching { include("**/*Test.kt") }.forEach { f ->
                val text = f.readText()
                val fqcn = extractFullyQualifiedClassName(text, f)

                if (matchesGlob(fqcn, allowCoroutineTestClasses)) return@forEach

                Regex("@Test\\s*\\n\\s*suspend fun\\s+(\\w+)").findAll(text).forEach { m ->
                    val fn = m.groupValues[1]
                    if (!text.contains("runTest(")) {
                        errors += "$fqcn#$fn"
                        violations.add(
                            mapOf(
                                "testClass" to fqcn,
                                "testMethod" to fn,
                                "issue" to "MISSING_RUNTEST",
                                "remediation" to "Wrap suspend test function body with runTest { ... }"
                            )
                        )
                    }
                }

                if (Regex("\\brunBlockingTest\\(").containsMatchIn(text)) {
                    errors += "$fqcn#runBlockingTest usage"
                    violations.add(
                        mapOf(
                            "testClass" to fqcn,
                            "testMethod" to "runBlockingTest usage",
                            "issue" to "DEPRECATED_RUNBLOCKINGTEST",
                            "remediation" to "Replace runBlockingTest with runTest"
                        )
                    )
                }
            }
        }

        scanTests("src/test")
        scanTests("src/androidTest")

        recordTaskResult(
            "checkCoroutines",
            startTime,
            errors,
            violations,
            mapOf("allowList" to allowCoroutineTestClasses.toList())
        )
        return startTime
    }


    private fun checkFailureCount(maxFails: Int): Long {
        val startTime = System.currentTimeMillis()
        var total = 0
        project.fileTree("build/test-results").matching { include("**/*.xml") }
            .forEach { f ->
                val txt = f.readText()
                total += Regex("failures=\"(\\d+)\"").findAll(txt)
                    .sumOf { it.groupValues[1].toInt() }
                total += Regex("errors=\"(\\d+)\"").findAll(txt)
                    .sumOf { it.groupValues[1].toInt() }
            }

        val errors =
            if (total > maxFails) listOf("Failed tests: $total, max allowed: $maxFails") else emptyList()
        recordTaskResult(
            "checkFails",
            startTime,
            errors,
            emptyList(),
            mapOf(
                "failedCount" to total,
                "maxFails" to maxFails
            )
        )
        return startTime
    }

    private fun checkIgnoredTests(allowSkippedTests: Set<String>): Long {
        val startTime = System.currentTimeMillis()
        val errors = mutableListOf<String>()
        val violations = mutableListOf<Map<String, Any>>()
        val parser = DocumentBuilderFactory.newInstance().newDocumentBuilder()

        project.fileTree("build/test-results").matching { include("**/*.xml") }
            .forEach { f ->
                val doc = parser.parse(f)
                val cases = doc.getElementsByTagName("testcase")
                for (i in 0 until cases.length) {
                    val tc = cases.item(i) as Element
                    if (tc.getElementsByTagName("skipped").length > 0) {
                        val className = tc.getAttribute("classname")
                        val testName = tc.getAttribute("name")
                        val key = "$className#$testName"
                        if (testName !in allowSkippedTests) {
                            errors += key
                            violations.add(
                                mapOf(
                                    "testClass" to className,
                                    "testMethod" to testName,
                                    "issue" to "UNAPPROVED_SKIP",
                                    "remediation" to "Either fix the test or add '$testName' to allowSkippedTests property"
                                )
                            )
                        }
                    }
                }
            }

        recordTaskResult(
            "checkIgnored",
            startTime,
            errors,
            violations,
            mapOf("allowList" to allowSkippedTests.toList())
        )
        return startTime
    }

    private fun recordTestResults(): Long {
        val startTime = System.currentTimeMillis()
        val errorsList = mutableListOf<String>()
        val failures = mutableListOf<Map<String, Any>>()
        val parser = DocumentBuilderFactory.newInstance().newDocumentBuilder()

        project.fileTree("build/test-results").matching { include("**/*.xml") }
            .forEach { f ->
                val doc = parser.parse(f)
                val cases = doc.getElementsByTagName("testcase")
                for (i in 0 until cases.length) {
                    val tc = cases.item(i) as Element
                    val className = tc.getAttribute("classname")
                    val testName = tc.getAttribute("name")

                    for (j in 0 until tc.getElementsByTagName("failure").length) {
                        val node = tc.getElementsByTagName("failure").item(j) as Element
                        val message = node.getAttribute("message")
                        val stack = node.textContent.trim()
                        val fullError = "$className#$testName: $message\n$stack"
                        errorsList += fullError
                        failures.add(
                            mapOf(
                                "testClass" to className,
                                "testMethod" to testName,
                                "failureType" to classifyFailure(fullError),
                                "message" to extractFailureMessage(fullError),
                                "stackTrace" to stack,
                                "expectedVsActual" to extractAssertionDetails(fullError),
                                "remediation" to suggestTestFix(fullError)
                            )
                        )
                    }

                    for (j in 0 until tc.getElementsByTagName("error").length) {
                        val node = tc.getElementsByTagName("error").item(j) as Element
                        val message = node.getAttribute("message")
                        val stack = node.textContent.trim()
                        val fullError = "$className#$testName: $message\n$stack"
                        errorsList += fullError
                        failures.add(
                            mapOf(
                                "testClass" to className,
                                "testMethod" to testName,
                                "failureType" to "ERROR",
                                "message" to extractFailureMessage(fullError),
                                "stackTrace" to stack,
                                "expectedVsActual" to emptyMap<String, String>(),
                                "remediation" to suggestTestFix(fullError)
                            )
                        )
                    }
                }
            }

        val affectedClasses = failures.map { it["testClass"] as String }.distinct()
        val failureTypes = failures.map { it["failureType"] as String }.distinct()

        recordTaskResult(
            "testResults",
            startTime,
            errorsList,
            emptyList(),
            mapOf(
                "summary" to mapOf(
                    "totalFailures" to failures.size,
                    "failureTypes" to failureTypes,
                    "affectedClasses" to affectedClasses
                ),
                "failures" to failures
            )
        )
        return startTime
    }

    private fun recordCompileErrors(): Long {
        val startTime = System.currentTimeMillis()
        val compileErrors = mutableListOf<String>()
        val structuredErrors = mutableListOf<Map<String, Any>>()
        val compileTasksOutcomes = mutableListOf<String>()

        // Check if any compile tasks failed by looking at their state
        project.tasks.matching { it.name.contains("compile", ignoreCase = true) }
            .forEach { task ->
                val state = task.state
                if (state.failure != null) {
                    val errorMsg = "${task.name}: ${state.failure?.message ?: "Compilation failed"}"
                    compileErrors.add(errorMsg)
                    compileTasksOutcomes.add("${task.name}: FAILED")

                    structuredErrors.add(
                        mapOf(
                            "type" to extractErrorType(errorMsg),
                            "task" to task.name,
                            "message" to cleanErrorMessage(errorMsg),
                            "suggestion" to getErrorSuggestion(errorMsg)
                        )
                    )
                } else if (state.executed) {
                    compileTasksOutcomes.add("${task.name}: SUCCESS")
                }
            }

        // Also check build/tmp for kotlinc error outputs
        project.fileTree("build/tmp").matching {
            include("**/compile*/**/*.log", "**/kotlin-daemon*.log")
        }.forEach { logFile ->
            if (logFile.exists() && logFile.length() > 0) {
                val content = logFile.readText()
                if (content.contains("error:", ignoreCase = true) ||
                    content.contains("compilation failed", ignoreCase = true)
                ) {
                    val errorMsg = "${logFile.name}: Found errors in compilation log"
                    compileErrors.add(errorMsg)
                    structuredErrors.add(
                        mapOf(
                            "type" to "COMPILATION_LOG_ERROR",
                            "file" to logFile.name,
                            "message" to "Compilation errors found in log file",
                            "suggestion" to "Check log file for detailed error information"
                        )
                    )
                }
            }
        }

        val errorTypes = structuredErrors.map { it["type"] as String }.distinct()
        val affectedFiles = structuredErrors.mapNotNull { it["file"] as? String }.distinct()

        recordTaskResult(
            "compileErrors",
            startTime,
            compileErrors,
            emptyList(),
            mapOf(
                "errorSummary" to mapOf(
                    "totalErrors" to compileErrors.size,
                    "errorTypes" to errorTypes,
                    "affectedFiles" to affectedFiles
                ),
                "errors" to structuredErrors,
                "rawErrors" to compileErrors,
                "compileTasksStatus" to compileTasksOutcomes
            )
        )
        return startTime
    }

    fun extractFullyQualifiedClassName(text: String, file: File): String {
        val pkg = Regex("""^\s*package\s+([\w.]+)""").find(text)?.groupValues?.get(1)
        val cls = Regex("""class\s+(\w+)""").find(text)?.groupValues?.get(1)
        return if (pkg != null && cls != null) "$pkg.$cls"
        else file.relativeTo(project.projectDir).path.removeSuffix(".kt").replace('/', '.')
    }


    private fun checkTestStructure(allowBadStructureTestClasses: Set<String>): Long {
        val startTime = System.currentTimeMillis()
        val allowedBases = listOf(
            "BaseRoomTest",
            "BaseSyncTest",
            "TestEntityFactory",
            "UiStateTestHelpers"
        )
        val errors = mutableListOf<String>()
        val violations = mutableListOf<Map<String, Any>>()

        fun extractFullyQualifiedClassName(text: String, file: File): String {
            val pkg = Regex("""^\s*package\s+([\w.]+)""", RegexOption.MULTILINE)
                .find(text)?.groupValues?.get(1)
            val cls = Regex("""\bclass\s+(\w+)""")
                .find(text)?.groupValues?.get(1)
            return if (pkg != null && cls != null) "$pkg.$cls"
            else file.relativeTo(project.projectDir).path.removeSuffix(".kt").replace('/', '.')
        }

        fun checkDir(dir: String) {
            project.fileTree(dir).matching { include("**/*.kt", "**/*.java") }
                .forEach { f ->
                    val text = f.readText()
                    val fqcn = extractFullyQualifiedClassName(text, f)

                    val classOk = allowedBases.any { base ->
                        Regex("""class\s+\w+\s*(?::|extends)\s*$base""").containsMatchIn(text)
                    }
                    val fixtureOk = Regex("""runTest\s*\(""").containsMatchIn(text) &&
                            Regex("""load[A-Za-z]*Fixture\s*\(""").containsMatchIn(text)

                    if (!classOk && !fixtureOk && !matchesGlob(
                            fqcn,
                            allowBadStructureTestClasses
                        )
                    ) {
                        val filePath = f.relativeTo(project.projectDir).path
                        errors += filePath
                        violations.add(
                            mapOf(
                                "file" to filePath,
                                "issue" to "INVALID_TEST_STRUCTURE",
                                "remediation" to "Test must either extend an allowed base class (${allowedBases.joinToString()}) or use runTest with fixture loading",
                                "allowList" to allowBadStructureTestClasses.toList()
                            )
                        )
                    }
                }
        }

        checkDir("src/test")
        checkDir("src/androidTest")

        recordTaskResult(
            "checkTestStructure",
            startTime,
            errors,
            violations,
            mapOf("allowedBases" to allowedBases)
        )
        return startTime
    }

    // Configuration extraction
    private val maxFails: Int
        get() = project.findProperty("maxFails")?.toString()?.toIntOrNull() ?: 0
    private val allowSkippedTests: Set<String>
        get() =
            (project.findProperty("allowSkippedTests") as? String)
                ?.split(',')?.map(String::trim)?.filter(String::isNotEmpty)?.toSet() ?: emptySet()
    private val allowBannedImportClasses: Set<String>
        get() =
            (project.findProperty("allowBannedImportClasses") as? String)
                ?.split(',')?.map(String::trim)?.filter(String::isNotEmpty)?.toSet() ?: emptySet()
    private val bannedImportPatterns: List<Regex>
        get() =
            (project.findProperty("bannedImportPatterns") as? String)
                ?.split(',')?.map(String::trim)?.filter(String::isNotEmpty)
                ?.map { it.toRegex(RegexOption.IGNORE_CASE) } ?: emptyList()
    private val allowCoroutineTestClasses: Set<String>
        get() =
            (project.findProperty("allowCoroutineTestClasses") as? String)
                ?.split(',')?.map(String::trim)?.filter(String::isNotEmpty)?.toSet() ?: emptySet()
    private val allowBadStructureTestClasses: Set<String>
        get() =
            (project.findProperty("allowBadStructureTestClasses") as? String)
                ?.split(',')?.map(String::trim)?.filter(String::isNotEmpty)?.toSet() ?: emptySet()

    // BuildListener logic extracted
    private fun registerBuildListener() {
        project.gradle.addBuildListener(object : BuildAdapter() {
            override fun buildFinished(result: BuildResult) {
                val compileErrors = mutableListOf<String>()
                if (result.failure != null) {
                    var cause: Throwable? = result.failure
                    while (cause != null) {
                        if (cause.message?.contains(
                                "Compilation failed",
                                ignoreCase = true
                            ) == true ||
                            cause.message?.contains("compilation error", ignoreCase = true) == true
                        ) {
                            compileErrors.add("Build failed with compilation error: ${cause.message}")
                        }
                        cause = cause.cause
                    }
                }
                if (compileErrors.isNotEmpty()) {
                    val results = readResults().also {
                        val existing =
                            it["compileErrors"] as? MutableMap<String, Any> ?: mutableMapOf()
                        existing["buildFailureErrors"] = compileErrors
                        existing["success"] = false
                        it["compileErrors"] = existing
                    }
                    writeResults(results)
                }
            }
        })
    }

    // Task registration helpers
    private fun registerTasks() {
        project.tasks.register("checkBanned") {
            group = "verification"
            description = "Ensure banned imports only appear in whitelisted classes."
            doLast { checkBannedImports(allowBannedImportClasses, bannedImportPatterns) }
        }
        project.tasks.register("checkCoroutines") {
            group = "verification"
            description = "Ensure suspend-fun tests use runTest and forbid runBlockingTest."
            doLast { checkCoroutineTests(allowCoroutineTestClasses) }
        }
        project.tasks.register("checkFails") {
            group = "verification"
            description = "Fail if test failures/errors exceed maxFails property."
            doLast { checkFailureCount(maxFails) }
        }
        project.tasks.register("checkIgnored") {
            group = "verification"
            description = "Error on new skipped tests not in the allowSkippedTests property."
            doLast { checkIgnoredTests(allowSkippedTests) }
        }
        project.tasks.register("recordTestResults") {
            group = "verification"
            description = "Record detailed test-case failures and stacktraces to JSON"
            doLast { recordTestResults() }
        }
        project.tasks.register("recordCompileErrors") {
            group = "verification"
            description = "Record compile errors to JSON"
            doLast { recordCompileErrors() }
        }
        project.tasks.register("checkTestStructure") {
            group = "verification"
            description =
                "Ensure every test file extends allowed base or uses runTest + fixture loading."
            doLast { checkTestStructure(allowBadStructureTestClasses) }
        }
        project.tasks.register("qaGate") {
            group = "verification"
            description = "Quality gate - fails if any checks failed"
            dependsOn(
                "checkBanned",
                "checkCoroutines",
                "checkFails",
                "checkIgnored",
                "recordTestResults",
                "checkTestStructure",
                "recordCompileErrors"
            )
            doLast { qaGateCheck() }
        }
    }

    // Aggregator logic extracted
    private fun qaGateCheck() {
        val results = readResults()
        val failures = mutableListOf<String>()
        val summary = mutableMapOf<String, Any>()
        listOf(
            "checkBanned",
            "checkCoroutines",
            "checkFails",
            "checkIgnored",
            "testResults",
            "checkTestStructure",
            "compileErrors"
        ).forEach { taskKey ->
            val taskResult = results[taskKey] as? Map<*, *>
            if (taskResult?.get("success") == false) {
                val errors = when (val errorsValue = taskResult["errors"]) {
                    is List<*> -> errorsValue
                    is Map<*, *> -> listOf(errorsValue)
                    else -> emptyList<Any>()
                }
                val violations = (taskResult["violations"] as? List<*>) ?: emptyList<Any>()
                val count = maxOf(errors.size, violations.size)
                failures.add("$taskKey: $count issues")
                summary[taskKey] = mapOf(
                    "failed" to true,
                    "issueCount" to count
                )
            } else {
                summary[taskKey] = mapOf("failed" to false, "issueCount" to 0)
            }
        }
        val finalResults = results.also {
            it["qaGateSummary"] = mapOf(
                "overallSuccess" to failures.isEmpty(),
                "failedChecks" to failures.size,
                "totalChecks" to 7,
                "checksSummary" to summary,
                "timestamp" to Instant.now().toString()
            )
        }
        writeResults(finalResults)
        if (failures.isNotEmpty()) {
            val jsonContent = resultsFile.readText()
            val pastebinUrl = uploadToPastebin(jsonContent)
            val errorMessage = """
                Quality gate failed. Issues found in:
                ${failures.joinToString("\n")}
                See ${resultsFile.absolutePath} for details.
                Online report: $pastebinUrl
            """.trimIndent()
            throw GradleException(errorMessage)
        }
        println("✅ All quality checks passed!")
    }

    // Build lifecycle hooks
    private fun registerLifecycleHooks() {
        project.tasks.matching { it.name.contains("compile", ignoreCase = true) }.configureEach {
            finalizedBy("qaGate")
        }
        project.tasks.withType(Test::class.java).configureEach {
            finalizedBy("qaGate")
        }
    }

    override fun apply(project: Project) {
        this.project = project
        resultsFile = project.layout.buildDirectory.file("reports/check-results.json").get().asFile
        registerBuildListener()
        registerTasks()
        registerLifecycleHooks()
    }

}
