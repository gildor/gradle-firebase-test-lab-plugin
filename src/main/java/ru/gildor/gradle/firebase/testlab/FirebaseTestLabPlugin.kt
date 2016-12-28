@file:Suppress("RemoveCurlyBracesFromTemplate")

package ru.gildor.gradle.firebase.testlab

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.TestVariant
import groovy.lang.Closure
import org.gradle.api.*
import ru.gildor.gradle.firebase.testlab.internal.utils.GcloudCliRunner
import ru.gildor.gradle.firebase.testlab.internal.utils.closureOf
import java.io.File

class FirebaseTestLabPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply("com.android.application")
        val extension = project.extensions.create(
                "firebaseTestLab",
                FirebaseTestLabPluginExtension::class.java
        )
        extension.matrices = project.container(Matrix::class.java)

        project.afterEvaluate {
            createTasks(project)
        }
    }

    private fun createTasks(project: Project) {
        println("ftl: createTasks")
        (project.extensions.findByName("android") as AppExtension).apply {
            testVariants.forEach { variant ->
                //Create task only for testable variants
                createTaskForVariant(project, variant)
            }
        }

    }

    private fun createTaskForVariant(project: Project, variant: TestVariant) {
        println("ftl: createTaskForVariant ${variant.name}")
        val variantName = variant.testedVariant.name.capitalize()
        val testVariantName = variant.name.capitalize()
        val config = project.extensions.findByType(FirebaseTestLabPluginExtension::class.java)
        println(config.matrices.size)
        println(config.matrices)

        project.task("test${variantName}TestLab", closureOf<Task> {
            group = "verification"
            description = "Run instrumentation tests for the ${variantName} build on Firebase Test Lab."
            //Add dependencies on assemble tasks of application and tests
            dependsOn("assemble${variantName}", "assemble${testVariantName}")
            doLast {
                val apks = Apks(
                        variant.testedVariant.outputs.first().outputFile,
                        variant.outputs.first().outputFile
                )
                //TODO: replace with real matrix creation
                val result = GcloudCliRunner(
                        TestType.instrumentation,
                        config.gcloudPath,
                        config.bucketName,
                        config.matrices.getByName("nexus7")!!,
                        apks
                ).start()

                println(result.message)
            }
        })
    }
}

open class FirebaseTestLabPluginExtension {
    open var gcloudPath: String = ""
    open var bucketName: String = ""
    open lateinit var matrices: NamedDomainObjectContainer<Matrix>

    fun matrices(closure: Closure<Matrix>) {
        matrices.configure(closure)
    }
}

open class Matrix(val name: String) {
    open var locales: List<String> = listOf("en")
    open var orientations: List<Orientation> = listOf(Orientation.portrait)
    open var androidApiLevels: List<Int> = emptyList()
    open var deviceIds: List<String> = emptyList()
}

data class Apks(
        val apk: File,
        val testApk: File
) {
    init {
        if (!apk.exists()) throw GradleException("Application APK file not exists: $apk")
        if (!testApk.exists()) throw GradleException("Application test APK file not exists: $testApk")
    }
}

enum class Orientation {
    landscape,
    portrait
}

enum class TestType {
    instrumentation,
    robo
}

val output = """
Have questions, feedback, or issues? Please let us know by using this Google Group:
  https://groups.google.com/forum/#!forum/google-cloud-test-lab-external

Uploading [app-stage-l16-debug.apk] to the Cloud Test Lab...
Uploading [app-stage-l16-debug-androidTest.apk] to the Cloud Test Lab...
Raw results will be stored in your GCS bucket at [https://console.developers.google.com/storage/browser/android_ci/2016-12-23_18:51:12.537353_dgrY/]

Test [matrix-3bh6lkbj2y53f] has been created in the Google Cloud.
Cloud Test Lab will execute your instrumentation test on 1 device(s).

Creating individual test executions...

Creating individual test executions...done.

Test results will be streamed to [https://console.developers.google.com/project/api-project-384961289538/testlab/mobile/histories/bh.548ad3140d048b14/executions/6740022801749329806].
18:51:46 Test is Pending
18:52:13 Starting attempt 1
18:52:13 Test is Running
18:52:26 Installing APK: com.bandlab.bandlab.test
18:53:19 Installing APK: com.bandlab.bandlab.test.androidtest
18:53:32 Running instrumentation test. Package: com.bandlab.bandlab.test.androidtest testrunner: com.bandlab.bandlab.TestRunner options: []
18:54:10 Instrumentation test has finished
18:54:10 Generating video
18:54:23 Retrieving test artifacts
18:54:36 Retrieving any crash results
18:55:28 Retrieving logcat
18:55:54 Done. Test time=41 (secs)
18:56:08 Test is Finished

Instrumentation testing complete.

More details are available at [https://console.developers.google.com/project/api-project-384961289538/testlab/mobile/histories/bh.548ad3140d048b14/executions/6740022801749329806].
"""