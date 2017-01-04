package ru.gildor.gradle.firebase.testlab.internal.gcloud

import org.gradle.api.GradleException
import org.gradle.api.logging.Logger
import ru.gildor.gradle.firebase.testlab.internal.utils.command
import ru.gildor.gradle.firebase.testlab.internal.utils.startCommand
import java.io.File

interface TestLabResultDownloader {
    fun downloadResult(result: TestResult)
}

class GcloudCliResultDownloader(
        private val artifacts: List<String>,
        private val destinationDir: File,
        private val gcloudPath: File?,
        private val bucket: String,
        private val logger: Logger?
) : TestLabResultDownloader {
    override fun downloadResult(result: TestResult) {
        if (artifacts.isEmpty()) {
            logger?.lifecycle("Artifact downloading not configured")
            return
        }
        //Read result
        getResultDirs(result).forEach { resultDir ->
            logger?.debug("Downloading artifacts from $resultDir")
            val matrixName = resultDir.split("/").last()
            artifacts.forEach { resource ->
                val destination = "$destinationDir/$matrixName"
                val destFile = File(destination)
                prepareDestination(destFile)
                downloadResource("$resultDir$resource", destination)
            }
        }
        logger?.lifecycle("gcloud: Artifacts downloaded")
    }

    private fun prepareDestination(destination: File) {
        logger?.debug("gcloud: Preparing destination dir $destination")
        if (destination.exists()) {
            logger?.debug("gcloud: Destination $destination is exists, delete recursively")
            if (!destination.isDirectory) {
                throw GradleException("Destination path $destination is not directory")
            }
            destination.deleteRecursively()
        }
        destination.mkdirs()
        if (!destination.exists()) {
            throw GradleException("Cannot create destination dir $destination")
        }
    }

    private fun downloadResource(source: String, destination: String): Boolean {
        return "${command("gsutil", gcloudPath)} -m cp $source $destination"
                .startCommand()
                .apply {
                    inputStream.bufferedReader().forEachLine { logger?.lifecycle(it) }
                    errorStream.bufferedReader().forEachLine { logger?.lifecycle(it) }
                }
                .waitFor() == 0
    }

    private fun getResultDirs(result: TestResult) =
            "${command("gsutil", gcloudPath)} ls gs://$bucket/${result.resultDir}"
                    .startCommand()
                    .apply {
                        errorStream.bufferedReader().forEachLine {
                            logger?.error(it)
                        }
                    }
                    .inputStream
                    .bufferedReader()
                    .readLines()
                    .filter { it.endsWith("/") }

}
