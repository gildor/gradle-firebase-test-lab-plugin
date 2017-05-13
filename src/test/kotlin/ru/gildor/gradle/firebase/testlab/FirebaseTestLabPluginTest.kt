package ru.gildor.gradle.firebase.testlab

import org.gradle.testkit.runner.TaskOutcome
import org.hamcrest.CoreMatchers.containsString
import org.junit.Assert.*
import org.junit.Test
import ru.gildor.gradle.firebase.testutils.GradleBaseTest

class FirebaseTestLabPluginTest : GradleBaseTest() {
    val pluginId = "ru.gildor.gradle.firebase.testlab"

    @Test
    fun `empty configuration`() {
        createBuildFile("""
            plugins { id "$pluginId" }
        """).build().apply {
            assertThat(output, containsString("No configured matrices for Firebase Test Lab. Tasks generation skipped"))
        }
    }

    @Test
    fun `all dsl properties groovy`() {
        createBuildFile("""
            ${mockAndroid()}

            firebaseTestLab {
                gcloudPath = "${mockCli()}"
                bucketName = "bucket_name"
                ignoreFailures = true
                enableVariantLessTasks = true
                copyArtifact {
                    junit = false
                    logcat = true
                    video = true
                    instrumentation = true
                }

                $defaultMatrices
            }
        """).withArguments(":tasks", "--stacktrace").build().apply {
            assertEquals(TaskOutcome.SUCCESS, task(":tasks").outcome)
            assertStandaloneTasksExists(output)
            assertAndroidTasksExists(output)
        }
    }

    @Test
    fun `standalone tasks`() {
        createBuildFile("""
            plugins { id "$pluginId" }

            firebaseTestLab {
                gcloudPath = "${mockCli()}"
                bucketName = "testBucket"
                enableVariantLessTasks = true

                $defaultMatrices
            }

        """).withArguments(":tasks").build().apply {
            assertThat(output, containsString("Test lab tasks"))
            assertStandaloneTasksExists(output)
        }
    }

    @Test
    fun `android tasks`() {
        createBuildFile("""
            ${mockAndroid()}

            firebaseTestLab {
                gcloudPath = "${mockCli()}"
                bucketName = "testBucket"

                $defaultMatrices
            }
        """).withArguments(":tasks").build().apply {
            assertThat(output, containsString("Test lab tasks"))
            assertAndroidTasksExists(output)
        }
    }

    @Test
    fun `android tasks with variants`() {
        //TODO: add variants and tests
        createBuildFile("""
            ${mockAndroid()}

            android {
            }

            firebaseTestLab {
                gcloudPath = "${mockCli()}"
                bucketName = "testBucket"

                $defaultMatrices
            }
        """).withPluginClasspath().withArguments(":tasks").build().apply {
            assertThat(output, containsString("Test lab tasks"))
            assertAndroidTasksExists(output)
        }
    }

    private fun mockAndroid(): String {
        createFile("local.properties", "sdk.dir=/Library/android-sdk")

        createFolder("src", "main")

        createFile("src/main/AndroidManifest.xml", """
            <manifest package="com.example.app" />
        """)

        return """
                buildscript {
                  repositories {
                    jcenter()
                  }
                  dependencies {
                    classpath 'com.android.tools.build:gradle:2.3.0'
                  }
                }

                plugins { id "$pluginId" apply false }

                apply plugin: 'com.android.application'
                apply plugin: 'ru.gildor.gradle.firebase.testlab'

                android {
                    compileSdkVersion 25
                    buildToolsVersion "25.0.1"
                }
        """
    }

    private val defaultMatrices = """
        matrices {
            nexus7 {
                androidApiLevels = [19, 21]
                deviceIds = ["flo"]
                locales = ["en"]
                orientations = ["portrait", "landscape"]
                timeoutSec = 120
            }
            nexus5 {
                androidApiLevels = [21]
                deviceIds = ["hammerhead"]
            }
        }
    """

    private fun mockCli(): String {
        val gcloud = createFolder("gcloud", "bin")
        createFile("gcloud/bin/gsutil").setExecutable(true)
        createFile("gcloud/bin/gcloud").setExecutable(true)
        return gcloud.parentFile.absolutePath
    }

    private fun assertStandaloneTasksExists(output: String) {
        assertThat(output, containsString("testNexus5InstrumentationTestLab - "))
        assertThat(output, containsString("testNexus5RoboTestLab - "))
        assertThat(output, containsString("testNexus7InstrumentationTestLab - "))
        assertThat(output, containsString("testNexus7RoboTestLab - "))
    }

    private fun assertAndroidTasksExists(output: String) {
        assertThat(output, containsString("testDebugNexus5InstrumentationTestLab - "))
        assertThat(output, containsString("testDebugNexus5RoboTestLab - "))
        assertThat(output, containsString("testDebugNexus7InstrumentationTestLab - "))
        assertThat(output, containsString("testDebugNexus7RoboTestLab - "))
    }
}