package com.supernova.testgate

import org.gradle.api.Plugin
import org.gradle.api.Project

class TestGatePlugin : Plugin<Project> {
    companion object {
        private var registered = false
    }

    override fun apply(project: Project) {
        if (!registered) {
            registered = true
            val collector = ErrorCollectorListener()
            project.gradle.addListener(collector)
            project.gradle.buildFinished {
                ReportCoordinator.handleBuildFinished(project.rootProject, collector.allResults)
            }
        }
        // no per-project registration needed beyond this
    }
}
