package ru.gildor.gradle.firebase.testlab.internal

import org.junit.Assert.*
import org.junit.Test

class ArtifactsImplTest {
    @Test
    fun `default configuration`() {
        val paths = ArtifactsImpl().getArtifactPaths()
        assertEquals(1, paths.size)
        assertEquals("test_result_*.xml", paths[0])
    }

    @Test
    fun `no artifacts`() {
        ArtifactsImpl().apply {
            junit = false
            assertTrue(getArtifactPaths().isEmpty())
        }
    }

    @Test
    fun `all artifacts enabled`() {
        ArtifactsImpl().apply {
            logcat = true
            video = true
            instrumentation = true
            getArtifactPaths().apply {
                assertEquals(4, size)
                assertTrue(contains("test_result_*.xml"))
                assertTrue(contains("logcat"))
                assertTrue(contains("video.mp4"))
                assertTrue(contains("instrumentation.results"))
            }
        }
    }
}