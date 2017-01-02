package ru.gildor.gradle.firebase.testlab.internal.gcloud

import org.gradle.api.logging.Logger
import ru.gildor.gradle.firebase.testlab.ApkSource
import ru.gildor.gradle.firebase.testlab.Matrix
import ru.gildor.gradle.firebase.testlab.TestType
import ru.gildor.gradle.firebase.testlab.internal.utils.asCommand
import ru.gildor.gradle.firebase.testlab.internal.utils.command
import ru.gildor.gradle.firebase.testlab.internal.utils.joinArgs
import java.io.File

interface  TestLabRunner {
    fun start(): TestResult
}

class GcloudCliRunner(
        type: TestType,
        val logger: Logger,
        gcloudPath: File?,
        val bucket: String,
        matrix: Matrix,
        apks: ApkSource
) : TestLabRunner {

    //TODO: check --result-history-name attribute
    private val processBuilder = ProcessBuilder("""
        ${command("gcloud", gcloudPath)}
                beta test android run
                --format json
                --results-bucket $bucket
                --locales ${matrix.locales.joinArgs()},
                --os-version-ids ${matrix.androidApiLevels.joinArgs()}
                --orientations ${matrix.orientations.joinArgs()}
                --device-ids ${matrix.deviceIds.joinArgs()}
                --app ${apks.apk}
                ${if (type == TestType.instrumentation) "--test ${apks.testApk}" else "" }
                ${if (matrix.timeoutSec > 0) "--timeoutSec ${matrix.timeoutSec}s" else ""}
                --type $type
    """.asCommand())

    override fun start(): TestResult {
        val process = processBuilder.start()
        var resultDir: String? = null
        process.errorStream.bufferedReader().forEachLine {
            logger.lifecycle(it)
            if (it.contains(bucket)) {
                resultDir = "$bucket\\/(.*)\\/".toRegex().find(it)?.groups?.get(0)?.value
                if (resultDir == null) {
                    logger.error("Cannot parse output to get result dir name. Result artifacts will not be downloaded")
                } else {
                    logger.debug("Target result dir name is $resultDir")
                }
            }
        }
        process.inputStream.bufferedReader().forEachLine { logger.lifecycle(it) }

        val code = process.waitFor()

        return TestResult(
                isSuccessful = code == RESULT_SUCCESSFUL,
                resultDir = resultDir,
                message = resultMessages[code] ?: ""
        )
    }
}

data class TestResult(
        val isSuccessful: Boolean,
        val resultDir: String?,
        val message: String
)

const val RESULT_SUCCESSFUL = 0

val resultMessages = mapOf(
        0 to "All test executions passed.",
        1 to "A general failure occurred. Possible causes include: a filename that does not exist, or an HTTP/network error.",
        2 to "Testing exited because unknown commands or arguments were provided.",
        10 to "One or more test cases (tested classes or class methods) within a test execution did not pass.",
        15 to "Firebase Test Lab for Android could not determine if the test matrix passed or failed because of an unexpected error.",
        18 to "The test environment for this test execution is not supported because of incompatible test dimensions. This error might occur if the selected Android API level is not supported by the selected device type.",
        19 to "The test matrix was canceled by the user.",
        20 to "A test infrastructure error occurred."
)

