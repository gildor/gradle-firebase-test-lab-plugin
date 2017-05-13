@file:Suppress("RemoveCurlyBracesFromTemplate")

package ru.gildor.gradle.firebase.testlab

import com.android.build.gradle.TestedExtension
import com.android.build.gradle.api.TestVariant
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.Logger
import org.gradle.script.lang.kotlin.closureOf
import ru.gildor.gradle.firebase.testlab.TestType.instrumentation
import ru.gildor.gradle.firebase.testlab.TestType.robo
import ru.gildor.gradle.firebase.testlab.internal.gcloud.GcloudCliResultDownloader
import ru.gildor.gradle.firebase.testlab.internal.gcloud.GcloudCliRunner
import ru.gildor.gradle.firebase.testlab.internal.gcloud.TestResult
import java.io.File

private const val EXTENSION_NAME = "firebaseTestLab"

private val ANDROID_PLUGIN_IDS = listOf("android", "com.android.application", "android-library", "com.android.library")


@Suppress("unused")
class FirebaseTestLabPlugin : Plugin<Project> {
    private val RESULT_PATH = "reports/firebase-test-lab"
    private lateinit var project: Project
    private val logger: Logger get() = project.logger

    override fun apply(project: Project) {
        this.project = project

        project.extensions.create(
                EXTENSION_NAME,
                FirebaseTestLabPluginExtension::class.java,
                project
        )

        project.afterEvaluate {
            createTasks()
        }
    }

    private val config by lazy {
        project.extensions.findByType(FirebaseTestLabPluginExtension::class.java).apply {
            //If we have no configured matrices, just skip verification
            if (matrices.isEmpty()) return@apply
            if (!File(gcloudPath, "bin/gcloud").canExecute()) {
                throw GradleException("gcloud CLI not found on $gcloudPath/bin. Please specify correct path")
            }
            if (!File(gcloudPath, "bin/gsutil").canExecute()) {
                throw GradleException("gsutil CLI not found on $gcloudPath/bin. Please specify correct path")
            }
            if (bucketName.isNullOrBlank()) {
                throw GradleException("Bucket name for Test Lab results not specified")
            }
        } ?: FirebaseTestLabPluginExtension(project)
    }

    private fun createTasks() {
        project.logger.debug("Creating Firebase Test Lab tasks")
        val matrices = config.matrices.toList()
        if (matrices.isEmpty()) {
            project.logger.warn("No configured matrices for Firebase Test Lab. Tasks generation skipped")
            return
        }
        createAndroidTasks(matrices)
        createStandaloneTasks(matrices)

    }

    private fun createStandaloneTasks(matrices: List<Matrix>) {
        //Enable standalone tasks only if user enabled it explicitly
        if (!config.enableVariantLessTasks) return

        val apks = BuildParameterApkSource(project)
        //Create task for each matrix without dependency on build variant
        matrices.forEach { matrix ->
            createTask(instrumentation, matrix, null, apks)
            createTask(robo, matrix, null, apks)
        }
    }

    private fun createAndroidTasks(matrices: List<Matrix>) {
        //Enable Android specific tasks only if project has Android gradle plugin
        if (ANDROID_PLUGIN_IDS.all {project.plugins.findPlugin(it) == null}) return

        project.extensions.findByType(TestedExtension::class.java).apply {
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
    }


    private fun createTask(
            type: TestType,
            matrix: Matrix,
            variant: TestVariant?,
            apks: ApkSource
    ) {
        val variantName = variant?.testedVariant?.name?.toString()?.capitalize() ?: ""
        project.logger.debug("Creating Firebase task for $variantName")
        project.task(
                "test${variantName}${matrix.name.toTaskName()}${type.toTaskName()}TestLab",
                closureOf<Task> {
                    group = "test lab"
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
                            instrumentation -> arrayOf(
                                    "assemble${variantName}",
                                    "assemble${variant.name.capitalize()}"
                            )
                            robo -> arrayOf(
                                    "assemble${variantName}"
                            )
                        })
                    }
                    doLast {
                        val result = runTestLabTest(type, matrix, apks, project.logger)
                        processResult(result, config.ignoreFailures)
                        downloadArtifacts(result)
                    }
                })
    }

    private fun Any?.toTaskName() = if (this == null) "" else toString().toLowerCase().capitalize()

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
                config.artifacts.getArtifactPaths(),
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

