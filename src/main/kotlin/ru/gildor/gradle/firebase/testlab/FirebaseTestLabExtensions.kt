package ru.gildor.gradle.firebase.testlab

import com.android.build.gradle.api.TestVariant
import groovy.lang.Closure
import org.gradle.api.Project
import ru.gildor.gradle.firebase.testlab.internal.Artifacts
import ru.gildor.gradle.firebase.testlab.internal.ArtifactsImpl
import java.io.File

open class FirebaseTestLabPluginExtension(private val project: Project) {

    var gcloudPath: String = ""
    var bucketName: String = ""
    var ignoreFailures: Boolean = false
    var enableVariantLessTasks = false
    val matrices = project.container(Matrix::class.java)!!
    internal val artifacts: Artifacts = ArtifactsImpl()

    @Suppress("unused")
    fun copyArtifact(configure: Artifacts.() -> Unit) {
        configure(artifacts)
    }

    @Suppress("unused")
    fun matrices(closure: Closure<Matrix>) {
        matrices.configure(closure)
    }

    @Suppress("unused")
    fun copyArtifact(closure: Closure<Artifacts>) {
        project.configure(artifacts, closure)
    }
}

class Matrix(val name: String) {
    var locales: List<String> = listOf("en")
    var orientations: List<Orientation> = listOf(Orientation.portrait)
    var androidApiLevels: List<Int> = emptyList()
    var deviceIds: List<String> = emptyList()
    var timeoutSec: Long = 0
}

interface ApkSource {
    val testApk: File
    val apk: File
}

enum class Orientation {
    landscape,
    portrait
}

enum class TestType {
    instrumentation,
    robo
}

class BuildParameterApkSource(private val project: Project) : ApkSource {
    override val testApk: File
        get() = File(project.property("testApk") as String)
    override val apk: File
        get() = File(project.property("apk") as String)

}

internal class VariantApkSource(variant: TestVariant) : ApkSource {
    override val apk: File = variant.testedVariant.outputs.first().outputFile
    override val testApk: File = variant.outputs.first().outputFile
}
