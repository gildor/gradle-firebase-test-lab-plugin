package ru.gildor.gradle.firebase.testlab.internal

import ru.gildor.gradle.firebase.testlab.ArtifactPath
import ru.gildor.gradle.firebase.testlab.FirebaseTestLabPluginExtension

fun getArtifactPaths(config: FirebaseTestLabPluginExtension) = config.artifacts.javaClass
        .declaredMethods
        .filter { it.isAnnotationPresent(ArtifactPath::class.java) }
        .map {
            if (it.invoke(config.artifacts) == true) {
                it.getAnnotation(ArtifactPath::class.java).pathWildcard
            } else {
                null
            }
        }
        .filterNotNull()
