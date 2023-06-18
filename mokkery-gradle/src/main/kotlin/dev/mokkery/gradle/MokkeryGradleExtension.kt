package dev.mokkery.gradle

import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

open class MokkeryGradleExtension {
    var targetSourceSets: Set<KotlinSourceSet> = emptySet()
    var excludeSourceSets: Set<KotlinSourceSet> = emptySet()
}
