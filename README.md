# Gradle plugin for Firebase Test Lab

[![License](https://img.shields.io/badge/License-Apache%202.0-yellowgreen.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![CircleCI](https://img.shields.io/circleci/project/github/gildor/gradle-firebase-test-lab-plugin/master.svg)](https://circleci.com/gh/gildor/gradle-firebase-test-lab-plugin)

This is experimental Gradle plugin for [Firebase Test Lab](https://firebase.google.com/docs/test-lab/)

## Prerequirements:
- Installed and inited [gcloud CLI](https://cloud.google.com/sdk/gcloud/)
- Configured [Firebase project](https://console.firebase.google.com/) with corresponding [billing plan](https://firebase.google.com/pricing/)

## Configuration

```groovy
//Add dependency to your build script
buildscript {
    dependencies {
        classpath "ru.gildor.gradle.firebase.testlab:firebase-test-lab:$TEST_LAB_PLUGIN_VERSION"
    }
}

//Apply plugin in your target Android projet
apply plugin: 'firebase-test-lab'

//Configure plugin
firebaseTestLab {
    // [REQUIRED] Path to Google Cloud SDK CLI tools - https://cloud.google.com/sdk/gcloud/
    gcloudPath = "/Library/google-cloud-sdk/"
    // [REQUIRED] Google Cloud Storage bucket name to keep test results
    bucketName = "android_ci"
    // `false` by default
    ignoreFailures = false
    // Plugin will create tasks not only for each your build variant, 
    // but also without dependency on current project app build, 
    // this allow you to run tests with configured matrices for any pair of apk and test apk
    // You must pass path to apk and testApk as gradle build parameters -Papk and -PtestApk. false by default 
    enableVariantLessTasks = false
    // Configuration that allows to download test result artifacts to build dir
    copyArtifact {
        // Test results in junit XML result format. true by default
        junit = true
        // File with Android logs. false by default
        logcat = false
        // Video of test. false by default
        video = false
        // Test results in test lab format (plain text log file)
        instrumentation = false
    }
    // Configuration of your matrices. Plugin creates 2 tasks for each matrix (for instrumentation and robo tests)
    matrices {
        // Name of matrix (test configuration)
        // How to choose configuration:
        // https://firebase.google.com/docs/test-lab/command-line#choosing_test_configurations
        nexus7 {
            // [REQUIRED] API level of devices
            androidApiLevels = [19, 21]
            // [REQUIRED] Names of devices.
            deviceIds = ["flo"]
            // Locale. "en" by default
            locales = ["en"]
            // Orientation of device. Can be `portrait` or `landscape`. Portrait by default
            orientations = ["portrait", "landscape"]
            // Maximum test length. No timeout by default
            timeoutSec = 0
        }
        // Example of minimal matrix configuration for 1 device (Nexus 5 with API Level 21)
        nexus5 {
            androidApiLevels = [21]
            deviceIds = ["hammerhead"]
        }
    }
}
```

Example of configuration for [Gradle Script Kotlin](https://github.com/gradle/gradle-script-kotlin) (for version 0.7)
```kotlin
import ru.gildor.gradle.firebase.testlab.*
import ru.gildor.gradle.firebase.testlab.Orientation.*

apply {
    plugin<FirebaseTestLabPlugin>()
}

configure<FirebaseTestLabPlugin> {
    gcloudPath = "/Library/google-cloud-sdk/"
    bucketName = "android_ci"
    ignoreFailures = true
    copyArtifact {
        junit = true
        logcat = true
    }
    matrices {
        "nexus7" {
            androidApiLevels = listOf(19, 21)
            deviceIds = listOf("flo")
            locales = listOf("en")
            orientations = listOf(portrait, landscape)
        }
    }
}
```

## Usage with Android project and Android Gradle Plugin

//TODO:

## Standalone usage

//TODO:

## Run tests
To run tests you should use one of plugin tasks
```
./gradlew test<BuildVariant><MatrixName><TestType>TestLab
```
For example:
```
./gradlew testDebugNexus5InstrumentationTestLab
```
Will be started instrumentation test for debug build (no flavor) for matrix with name `nexus5`

## TODO:
- Sample app
- Tests
- Maybe some additional settings from gcloud CLI
- Find way to avoid gcloud CLI and use pure Java API to upload files (need investigation) and download copyArtifact (definitely possible)
- Task to print possible matrix settings from gcloud
