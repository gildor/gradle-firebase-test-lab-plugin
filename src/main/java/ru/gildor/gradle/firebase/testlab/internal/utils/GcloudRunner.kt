package ru.gildor.gradle.firebase.testlab.internal.utils

import ru.gildor.gradle.firebase.testlab.Apks
import ru.gildor.gradle.firebase.testlab.Matrix
import ru.gildor.gradle.firebase.testlab.TestType
import java.io.InputStream

interface  TestLabRunner {
    fun start(): FirebaseTestLabResult
}

class GcloudCliRunner(
        val type: TestType,
        val gcloudPath: String?,
        val bucket: String,
        val matrix: Matrix,
        val apks: Apks
) : TestLabRunner {

    private val processBuilder: ProcessBuilder

    init {
        val command = """
            ${gcloudPath}gcloud beta test android run
                    --format json
                    --results-bucket ${bucket}
                    --locales ${matrix.locales.joinArgs()},
                    --os-version-ids ${matrix.androidApiLevels.joinArgs()}
                    --orientations ${matrix.orientations.joinArgs()}
                    --device-ids ${matrix.deviceIds.joinArgs()}
                    --app ${apks.apk}
                    --test ${apks.testApk}
                    --type $type
        """.split(" ", "\n").filterNot(String::isNullOrBlank)
        //TODO: check --results-history-name attribute

        processBuilder = ProcessBuilder(command)
    }

    override fun start(): FirebaseTestLabResult {
        val process = processBuilder.start()
        var resultDir: String? = null
        process.errorStream.readSync {
            println(it)
            if (it.contains(bucket)) {
                resultDir = "$bucket\\/(.*)\\/".toRegex().find(it)!!.groups[0]!!.value
                //TODO: parse resultDir name
                println("target resultDir $resultDir")
            }
        }
        process.inputStream.readSync(::println)

        val code = process.waitFor()

        println(resultMessages[code])
        println(process.inputStream.bufferedReader().readText())

        return FirebaseTestLabResult(
                isSuccessful = code == RESULT_SUCCESSFUL,
                resultDir = resultDir,
                message = resultMessages.getOrDefault(code, "")
        )
    }
}

data class FirebaseTestLabResult(
        val isSuccessful: Boolean,
        val resultDir: String?,
        val message: String
)

fun InputStream.readSync(onRead: (String) -> Unit): InputStream {
    val reader = bufferedReader()
    do {
        val line: String? = reader.readLine()
        if (line != null) {
            onRead(line)
        }
    } while (line != null)
    return this
}

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


private fun <T> List<T>.joinArgs() = joinToString(",")

val script = """

#!/bin/bash
source '/Users/android-ci/google-cloud-sdk/path.bash.inc'
apk_name="app-stage-l16-debug"
results="results"
apk="$\{apk_name}.apk"
test_apk="$\{apk_name}-androidTest.apk"
build_name=`[ ! -z $\{BUILD_TAG} ] && echo "--results-history-name $\{BUILD_TAG}"`

matrix="--locales en \
--os-version-ids 21 \
--orientations portrait \
--device-ids hammerhead"

cmd="gcloud beta test android run \
--app $\{apk} \
--format json \
--results-history-name $\{BUILD_TAG} \
--results-bucket android_ci"

source_url="gsutil ls gs://android_ci/ | tail -n 1 --"
resCode=0
eval "$\{cmd} $\{matrix} --type instrumentation --test $\{test_apk}" || resCode=1 && true
source=`eval $\{source_url}` \
&& destination="$\{results}" \
&& mkdir -p $\{destination} \
&& gsutil -m cp $\{source}**/**xml $\{source}**/logcat $\{destination}

exit $\{resCode}

"""
