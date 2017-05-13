package ru.gildor.gradle.firebase.testlab

import org.junit.Assert.*
import org.junit.Test
import org.hamcrest.CoreMatchers.`is` as isEquals

class MatrixTest {
    @Test
    fun `default matrix config`() {
        Matrix("defaultMatrix").apply {
            assertThat(listOf("en"), isEquals(locales))
            assertThat(listOf(Orientation.portrait), isEquals(orientations))
            assertThat(listOf(Orientation.portrait), isEquals(orientations))
            assertThat(emptyList(), isEquals(androidApiLevels))
            assertThat(emptyList(), isEquals(deviceIds))
            assertEquals(0, timeoutSec)
        }
    }

    @Test
    fun `custom matrix config`() {
        val matrix = Matrix("defaultMatrix").apply {
            locales = listOf("en", "ru")
            orientations = listOf(Orientation.landscape)
            androidApiLevels = listOf(20, 21, 22)
            deviceIds = listOf("device1", "device2")
            timeoutSec = 42
        }

        matrix.apply {
            assertThat(listOf("en", "ru"), isEquals(locales))
            assertThat(listOf(Orientation.landscape), isEquals(orientations))
            assertThat(listOf(20, 21, 22), isEquals(androidApiLevels))
            assertThat(listOf("device1", "device2"), isEquals(deviceIds))
            assertEquals(42, timeoutSec)
        }
    }
}