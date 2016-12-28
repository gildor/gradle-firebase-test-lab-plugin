# Gradle plugin for Firebase Test Lab

This is experimental Gradle plugin for [Firebase Test Lab](https://firebase.google.com/docs/test-lab/)

WARNING! It's just draft of future plugin, not production or even beta ready 

## Configuration

```groovy
apply plugin: 'firebase-test-lab'

firebaseTestLab {
    gcloudPath = "/Path/to/google-cloud-sdk/bin/"
    bucketName = "glcoud_storage_bucket_name"
    matrices {
        nexus7 {
            androidApiLevels = [21]
            deviceIds = ["flo"]
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
- Generate task for each matrix
- Settings validation
- Timeout configuration
- Maybe some additional settings from gcloud CLI
- Support of robo tests (2 ways: set of tasks for roboto test, or maybe more clean just to set test type to matrix)
- Find way to avoid gcloud CLI and use pure Java API to upload files (need investigation) and download results (definitely possible)
- Sample app
- Download test artifacts: logcat, screenshot, videos (config DSL for result)
- Tests
- Task to print possible matrix settings from gcloud
- Improve logging (use gradle logger instead `println()`)
