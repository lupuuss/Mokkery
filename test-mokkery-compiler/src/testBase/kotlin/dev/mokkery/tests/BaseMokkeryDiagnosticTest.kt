package dev.mokkery.tests

import dev.mokkery.mockable.tests.config.MokkeryMockableConfigurator
import org.jetbrains.kotlin.config.JvmTarget.JVM_1_8
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.directives.DiagnosticsDirectives.RENDER_DIAGNOSTICS_FULL_TEXT
import org.jetbrains.kotlin.test.directives.JvmEnvironmentConfigurationDirectives.FULL_JDK
import org.jetbrains.kotlin.test.directives.JvmEnvironmentConfigurationDirectives.JVM_TARGET
import org.jetbrains.kotlin.test.runners.AbstractFirLightTreeDiagnosticsTest
import org.jetbrains.kotlin.test.services.KotlinStandardLibrariesPathProvider

open class BaseMokkeryDiagnosticTest(
    private val pathProvider: KotlinStandardLibrariesPathProvider
) : AbstractFirLightTreeDiagnosticsTest() {

    override fun createKotlinStandardLibrariesPathProvider(): KotlinStandardLibrariesPathProvider = pathProvider

    override fun configure(builder: TestConfigurationBuilder) {
        super.configure(builder)
        with(builder) {
            useConfigurators(
                ::MokkeryConfigurator,
                ::MokkeryMockableConfigurator,
            )
            useCustomRuntimeClasspathProviders(
                MokkeryConfigurator::PathProvider,
                MokkeryMockableConfigurator::PathProvider,
            )
            useDirectives(MokkeryDirectives)
            useDirectives(MokkeryMockableDirectives)
            defaultDirectives {
                JVM_TARGET.with(JVM_1_8)
                +FULL_JDK
                +RENDER_DIAGNOSTICS_FULL_TEXT
            }
        }
    }
}
