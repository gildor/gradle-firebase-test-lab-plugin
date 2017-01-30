import com.gradle.publish.PluginBundleExtension
import org.gradle.script.lang.kotlin.*

group = "ru.gildor.gradle.firebase.testlab"
version = "0.1.1"

buildscript {

    repositories {
        jcenter()
        gradleScriptKotlin()
        maven { setUrl("https://plugins.gradle.org/m2/") }
    }
    dependencies {
        //noinspection DifferentKotlinGradleVersion
        classpath(kotlinModule("gradle-plugin"))
        classpath("com.gradle.publish:plugin-publish-plugin:0.9.7")
    }
}

apply {
    plugin("kotlin")
    plugin("maven")
    plugin("com.gradle.plugin-publish")
}

repositories {
    jcenter()
}

configure<PluginBundleExtension> {
    website = "https://github.com/gildor/gradle-firebase-test-lab-plugin"
    vcsUrl = "https://github.com/gildor/gradle-firebase-test-lab-plugin.git"
    description = "Gradle plugin for Firebase Test Lab"
    tags = listOf("firebase", "test-lab", "android", "espresso", "robo")
    this.plugins {
        "firebaseTestLabPlugin" {
            id = "ru.gildor.gradle.firebase.testlab"
            displayName = "Gradle Firebase Test Lab plugin"
        }
    }
}

dependencies {
    compile(gradleApi())
    compile(kotlinModule("stdlib", "1.0.6"))
    compile(gradleScriptKotlinApi())
    compileOnly("com.android.tools.build:gradle:2.2.3")
    testCompile("junit:junit:4.11")
}
