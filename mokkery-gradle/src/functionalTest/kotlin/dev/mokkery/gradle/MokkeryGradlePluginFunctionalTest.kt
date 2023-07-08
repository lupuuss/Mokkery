package dev.mokkery.gradle

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import java.io.File
import dev.mokkery.BuildConfig.MOKKERY_VERSION
import dev.mokkery.BuildConfig.MOKKERY_KOTLIN_VERSION

class MokkeryGradlePluginFunctionalTest {

    private val testProjectDir = File("../test-mokkery")

    @Test
    fun `test all unit tests build and passes`() {
        GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments(
                "-PisTest=true",
                "-PmokkeryVersion=${MOKKERY_VERSION}",
                "-PkotlinVersion=${MOKKERY_KOTLIN_VERSION}",
                "clean",
                "kotlinUpgradeYarnLock",
                "allTests"
            ).withPluginClasspath()
            .forwardStdOutput(System.out.writer())
            .build()
    }
}
