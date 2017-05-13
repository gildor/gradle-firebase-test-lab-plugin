package ru.gildor.gradle.firebase.testutils

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder

open class GradleBaseTest {
    @get:Rule
    val testProjectDir = TemporaryFolder()

    protected fun createFile(file: String) = testProjectDir.newFile(file)!!

    protected fun createFolder(vararg file: String) = testProjectDir.newFolder(*file)!!

    protected fun createFile(file: String, content: String) = createFile(file).apply {
        writeText(content)
    }

    fun createBuildFile(content: String) = createBuildFile("build.gradle", content)

    fun createBuildFile(file: String = "build.gradle", content: String): GradleRunner = GradleRunner
            .create()
            .apply {
                val buildFile = createFile(file, content)
                withProjectDir(buildFile.parentFile)
                withPluginClasspath()
            }
}
