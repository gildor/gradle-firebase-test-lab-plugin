package ru.gildor.gradle.firebase.testlab

import com.android.build.gradle.api.TestVariant
import groovy.lang.Closure
import org.gradle.api.Project
import java.io.File

open class FirebaseTestLabPluginExtension(private val project: Project) {

    var gcloudPath: String = ""
    var bucketName: String = ""
    var ignoreFailures: Boolean = false
    var enableVariantLessTasks = false
    val matrices = project.container(Matrix::class.java)!!
    val artifacts = Artifacts()

    @Suppress("unused")
    fun matrices(closure: Closure<Matrix>) {
        matrices.configure(closure)
    }

    @Suppress("unused")
    fun copyArtifact(closure: Closure<Artifacts>) {
        project.configure(artifacts, closure)
    }
}

@Suppress("unused")
class Artifacts {
    @get:ArtifactPath("test_result_*.xml")
    var junit = true
    @get:ArtifactPath("logcat")
    var logcat = false
    @get:ArtifactPath("video.mp4")
    var video = false
    @get:ArtifactPath("instrumentation.results")
    var instrumentation = false
}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY_GETTER)
internal annotation class ArtifactPath(val pathWildcard: String)

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
    override val apk: File = variant.outputs.first().outputFile
    override val testApk: File = variant.testedVariant.outputs.first().outputFile
}
