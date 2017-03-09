group = "ru.gildor.gradle.firebase.testlab"
version = "0.2.0"
description = "Gradle plugin to run Android instrumentation and robo test on Firebase Test Lab"

plugins {
    `maven-publish`
    `java-gradle-plugin`
    id("nebula.kotlin") version "1.1.0"
    id("com.gradle.plugin-publish") version "0.9.7"
}

repositories {
    jcenter()
}

gradlePlugin {
    plugins.invoke {
        "firebaseTestLab" {
            id = project.name
            implementationClass = "ru.gildor.gradle.firebase.testlab.FirebaseTestLabPlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/gildor/gradle-firebase-test-lab-plugin"
    vcsUrl = "$website.git"
    description = project.description
    tags = listOf("firebase", "test-lab", "android", "espresso", "robo")

    plugins.invoke {
        "firebaseTestLab" {
            id = project.group as String
            displayName = "Gradle Firebase Test Lab plugin"
        }
    }
}

dependencies {
    compile(gradleScriptKotlinApi())
    compileOnly("com.android.tools.build:gradle:2.3.0")
    testCompile("junit:junit:4.12")
}
