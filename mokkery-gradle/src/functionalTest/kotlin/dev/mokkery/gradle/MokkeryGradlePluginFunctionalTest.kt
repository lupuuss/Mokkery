package dev.mokkery.gradle

import dev.mokkery.MokkeryConfig
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import java.io.File

class MokkeryGradlePluginFunctionalTest {

    private val testProjectDir = File("../test-mokkery")

    @Test
    fun `test all unit tests build and passes`() {
        GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments(
                "-PmokkeryVersion=${MokkeryConfig.VERSION}",
                "clean",
                "kotlinUpgradeYarnLock",
                "allTests",
                "--stacktrace"
            )
            .forwardOutput()
            .build()
    }
}
