package ru.gildor.gradle.firebase.testlab.internal

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface Artifacts {
    var junit: Boolean
    var logcat: Boolean
    var video: Boolean
    var instrumentation: Boolean

    fun getArtifactPaths(): List<String>
}

@Suppress("unused")
class ArtifactsImpl : Artifacts {
    override var junit by PathBoolean("test_result_*.xml")
    override var logcat by PathBoolean("logcat")
    override var video by PathBoolean("video.mp4")
    override var instrumentation by PathBoolean("instrumentation.results")

    override fun getArtifactPaths() = paths.toList()

    private val paths = mutableListOf<String>()

    init {
        //Values by default
        junit = true
    }

    private class PathBoolean(
            private var path: String
    ) : ReadWriteProperty<ArtifactsImpl, Boolean> {
        private var value: Boolean = false

        override fun getValue(thisRef: ArtifactsImpl, property: KProperty<*>) = value

        override fun setValue(thisRef: ArtifactsImpl, property: KProperty<*>, value: Boolean) {
            this.value = value
            updatePaths(thisRef, value)
        }

        private fun updatePaths(thisRef: ArtifactsImpl, value: Boolean) {
            if (value) {
                thisRef.paths += path
            } else {
                thisRef.paths -= path
            }
        }
    }
}
