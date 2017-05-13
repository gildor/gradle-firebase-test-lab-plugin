
import org.gradle.script.lang.kotlin.*

group = "ru.gildor.gradle.firebase.testlab"
version = "0.2.0"
description = "Gradle plugin to run Android instrumentation and robo test on Firebase Test Lab"

plugins {
    `maven-publish`
    `java-gradle-plugin`
    id("org.jetbrains.kotlin.jvm") version "1.1.1"
    id("com.gradle.plugin-publish") version "0.9.7"
}


repositories {
    jcenter()
}

gradlePlugin {
    plugins.invoke {
        "firebaseTestLab" {
            id = project.group as String
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
    compile(kotlinModule("stdlib"))
    compile(gradleScriptKotlinApi())
    compileOnly("com.android.tools.build:gradle:2.3.0")
    testRuntime("com.android.tools.build:gradle:2.3.0")
    testCompile("junit:junit:4.12")
}
