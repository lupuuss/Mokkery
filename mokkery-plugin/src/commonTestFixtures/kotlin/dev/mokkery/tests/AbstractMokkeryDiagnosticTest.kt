package dev.mokkery.tests

import org.jetbrains.kotlin.compiler.plugin.devkit.runners.DevKitTest
import org.jetbrains.kotlin.config.JvmTarget.JVM_1_8
import org.jetbrains.kotlin.test.directives.DiagnosticsDirectives.RENDER_DIAGNOSTICS_FULL_TEXT
import org.jetbrains.kotlin.test.directives.JvmEnvironmentConfigurationDirectives.FULL_JDK
import org.jetbrains.kotlin.test.directives.JvmEnvironmentConfigurationDirectives.JVM_TARGET
import org.jetbrains.kotlin.test.runners.AbstractFirLightTreeDiagnosticsTest

open class AbstractMokkeryDiagnosticTest : DevKitTest(object : AbstractFirLightTreeDiagnosticsTest() {}, {
    useConfigurators(::MokkeryConfigurator)
    useDirectives(MokkeryDirectives)
    defaultDirectives {
        JVM_TARGET.with(JVM_1_8)
        +FULL_JDK
        +RENDER_DIAGNOSTICS_FULL_TEXT
    }
})
