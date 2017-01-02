# Gradle plugin for Firebase Test Lab

This is experimental Gradle plugin for [Firebase Test Lab](https://firebase.google.com/docs/test-lab/)

WARNING! It's just draft of future plugin, not production or even beta ready 

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
    gcloudPath = "/Library/google-cloud-sdk/bin/"
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

## Prerequirements:
- Installed and inited [gcloud CLI](https://cloud.google.com/sdk/gcloud/)
- Configured [Firebase project](https://console.firebase.google.com/) with corresponding [billing plan](https://firebase.google.com/pricing/)

## Run tests

```
./gradlew testFreeDebugTestLab
```

## TODO:
- Sample app
- Tests
- Maybe some additional settings from gcloud CLI
- Find way to avoid gcloud CLI and use pure Java API to upload files (need investigation) and download copyArtifact (definitely possible)
- Task to print possible matrix settings from gcloud
