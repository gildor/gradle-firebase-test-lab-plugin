@file:Suppress("RemoveCurlyBracesFromTemplate")

package ru.gildor.gradle.firebase.testlab

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.TestVariant
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.Logger
import ru.gildor.gradle.firebase.testlab.TestType.instrumentation
import ru.gildor.gradle.firebase.testlab.TestType.robo
import ru.gildor.gradle.firebase.testlab.internal.gcloud.GcloudCliResultDownloader
import ru.gildor.gradle.firebase.testlab.internal.gcloud.GcloudCliRunner
import ru.gildor.gradle.firebase.testlab.internal.gcloud.TestResult
import ru.gildor.gradle.firebase.testlab.internal.getArtifactPaths
import ru.gildor.gradle.firebase.testlab.internal.utils.closureOf
import java.io.File

class FirebaseTestLabPlugin : Plugin<Project> {
    private val RESULT_PATH = "reports/firebase-test-lab"
    private lateinit var project: Project
    private lateinit var config: FirebaseTestLabPluginExtension
    private val logger: Logger get() = project.logger

    override fun apply(project: Project) {
        this.project = project

        project.extensions.create(
                "firebaseTestLab",
                FirebaseTestLabPluginExtension::class.java,
                project
        )

        project.afterEvaluate {
            initConfig()
            createTasks()
        }
    }

    private fun initConfig() {
        config = project.extensions.findByType(FirebaseTestLabPluginExtension::class.java).apply {
            if (!File(gcloudPath, "gcloud").canExecute()) {
                throw GradleException("gcloud CLI not found. Please specify correct path")
            }
            if (!File(gcloudPath, "gsutil").canExecute()) {
                throw GradleException("gsutil CLI not found. Please specify correct path")
            }
            if (bucketName.isNullOrBlank()) {
                throw GradleException("Bucket name for Test Lab results not specified")
            }
        }
    }

    private fun createTasks() {
        project.logger.debug("Creating Firebase Test Lab tasks")
        val matrices = config.matrices.toList()
        (project.extensions.findByName("android") as AppExtension).apply {
            //Create task only for testable variants
            testVariants.forEach { variant ->
                val apks = VariantApkSource(variant)
                //Create task for each matrix
                matrices.forEach { matrix ->
                    createTask(instrumentation, matrix, variant, apks)
                    createTask(robo, matrix, variant, apks)
                }
            }
        }
        val apks = BuildParameterApkSource(project)
        if (config.enableVariantLessTasks) {
            //Create task for each matrix without dependency on build variant
            matrices.forEach { matrix ->
                createTask(instrumentation, matrix, null, apks)
                createTask(robo, matrix, null, apks)
            }
        }

    }

    private fun createTask(
            type: TestType,
            matrix: Matrix,
            variant: TestVariant?,
            apks: ApkSource
    ) {
        val variantName = variant?.testedVariant?.name?.capitalize() ?: ""
        project.logger.debug("Creating Firebase task for $variantName")
        project.task("test${variantName}${matrix.name.capitalize()}${type.toString().capitalize()}TestLab", closureOf<Task> {
            group = "verification"
            description = "Run ${type} tests " +
                    (if (variant == null) "" else "for the ${variantName} build ") +
                    "in Firebase Test Lab."
            if (variant == null) {
                description += "\nTo run test for your matrix without build project" +
                        " you must specify paths to apk and test apk using parameters -Papk and -PtestApk"
            }
            //Add dependencies on assemble tasks of application and tests
            //But only for "variant" builds,
            if (variant != null) {
                dependsOn(*when (type) {
                    instrumentation -> arrayOf("assemble${variantName}", "assemble${variant.name.capitalize()}")
                    robo -> arrayOf("assemble${variantName}")
                })
            }
            doLast {
                val result = runTestLabTest(type, matrix, apks, project.logger)
                processResult(result, config.ignoreFailures)
                downloadArtifacts(result)
            }
        })
    }

    private fun processResult(result: TestResult, ignoreFailures: Boolean) {
        if (result.isSuccessful) {
            logger.lifecycle(result.message)
        } else {
            if (ignoreFailures) {
                logger.error(result.message)
            } else {
                throw GradleException(result.message)
            }
        }
    }

    private fun downloadArtifacts(result: TestResult) {
        logger.lifecycle("Artifact downloading started")
        GcloudCliResultDownloader(
                getArtifactPaths(config),
                File(project.buildDir, RESULT_PATH),
                File(config.gcloudPath),
                config.bucketName,
                project.logger
        ).downloadResult(result)
    }

    private fun runTestLabTest(
            testType: TestType,
            matrix: Matrix,
            apks: ApkSource,
            logger: Logger
    ): TestResult {
        return GcloudCliRunner(
                testType,
                logger,
                File(config.gcloudPath),
                config.bucketName,
                matrix,
                apks
        ).start()
    }
}

