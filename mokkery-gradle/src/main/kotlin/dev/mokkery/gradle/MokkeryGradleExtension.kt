package dev.mokkery.gradle

import dev.mokkery.MockMode
import dev.mokkery.MokkeryCompilerDefaults
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

open class MokkeryGradleExtension {
    var targetSourceSets: Set<KotlinSourceSet> = emptySet()
    var excludeSourceSets: Set<KotlinSourceSet> = emptySet()
    var defaultMockMode: MockMode = MokkeryCompilerDefaults.mockMode
}
